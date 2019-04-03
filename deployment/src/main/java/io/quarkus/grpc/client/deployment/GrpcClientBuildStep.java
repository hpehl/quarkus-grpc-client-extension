package io.quarkus.grpc.client.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;
import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.grpc.ManagedChannel;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.recording.RecorderContext;
import io.quarkus.deployment.util.HashUtil;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.grpc.client.Channel;
import io.quarkus.grpc.client.runtime.AbstractChannelProducer;
import io.quarkus.grpc.client.runtime.ChannelConfig;
import io.quarkus.grpc.client.runtime.GrpcClientConfig;
import io.quarkus.grpc.client.runtime.GrpcClientTemplate;

public class GrpcClientBuildStep {

    private static final DotName CHANNEL = DotName.createSimple(Channel.class.getName());
    private static final Set<DotName> UNREMOVABLE_BEANS = new HashSet<>(asList(
            DotName.createSimple(AbstractChannelProducer.class.getName())));

    GrpcClientConfig config;

    @BuildStep
    @Record(STATIC_INIT)
    @SuppressWarnings("unchecked")
    public BeanContainerListenerBuildItem build(
            RecorderContext recorder,
            GrpcClientTemplate template,
            BuildProducer<FeatureBuildItem> feature,
            // BuildProducer<RuntimeInitializedClassBuildItem> runtimeInit,
            // BuildProducer<RuntimeReinitializedClassBuildItem> runtimeReinit,
            BuildProducer<ExtensionSslNativeSupportBuildItem> sslNativeSupport,
            BuildProducer<GeneratedBeanBuildItem> generatedBean,
            BuildProducer<BeanContainerListenerBuildItem> beanContainerListener) {

        // Register gRPC feature
        feature.produce(new FeatureBuildItem("grpc-client"));

        // Try to fix "Error: Detected a direct/mapped ByteBuffer in the image heap."
        // runtimeInit.produce(new RuntimeInitializedClassBuildItem("io.netty.channel.DefaultChannelConfig"));
        // runtimeInit.produce(new RuntimeInitializedClassBuildItem("io.netty.channel.socket.DefaultSocketChannelConfig"));
        // runtimeReinit.produce(new RuntimeReinitializedClassBuildItem("io.netty.channel.socket.nio.NioSocketChannel"));

        // Indicates that this extension would like the SSL support to be enabled
        sslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem("grpc-client"));

        // Generate the ChannelProducer bean
        String channelProducerClassName = AbstractChannelProducer.class.getPackage().getName() + ".ChannelProducer";
        createChannelProducerBean(generatedBean, channelProducerClassName);

        // Set configuration on ChannelProducer
        return new BeanContainerListenerBuildItem(template.setConfig(
                (Class<? extends AbstractChannelProducer>) recorder.classProxy(channelProducerClassName), config));
    }

    @BuildStep
    UnremovableBeanBuildItem markBeansAsUnremovable() {
        return new UnremovableBeanBuildItem(beanInfo -> {
            Set<Type> types = beanInfo.getTypes();
            for (Type t : types) {
                if (UNREMOVABLE_BEANS.contains(t.name())) {
                    return true;
                }
            }
            return false;
        });
    }

    /** Create a producer bean managing the lifecycle of the channels. */
    private void createChannelProducerBean(BuildProducer<GeneratedBeanBuildItem> generatedBean,
            String channelProducerClassName) {

        ClassOutput classOutput = (name, data) -> generatedBean.produce(new GeneratedBeanBuildItem(name, data));
        ClassCreator classCreator = ClassCreator.builder().classOutput(classOutput)
                .className(channelProducerClassName)
                .superClass(AbstractChannelProducer.class)
                .build();
        classCreator.addAnnotation(ApplicationScoped.class);

        // create producer methods (e.g. for a named channel "foo"):
        //  @ApplicationScoped
        //  @Produces
        //  @Named("foo")
        //  @Channel("foo")
        //  public ManagedChannel createNamedChannel_0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33() {
        //      return createChannel("foo");
        //  }
        for (Map.Entry<String, ChannelConfig> entry : config.channels.entrySet()) {
            String namedChannel = entry.getKey();

            MethodCreator namedChannelMethodCreator = classCreator.getMethodCreator(
                    "createNamedChannel_" + HashUtil.sha1(namedChannel), ManagedChannel.class);
            namedChannelMethodCreator.addAnnotation(ApplicationScoped.class);
            namedChannelMethodCreator.addAnnotation(Produces.class);
            namedChannelMethodCreator.addAnnotation(AnnotationInstance.create(DotNames.NAMED, null,
                    new AnnotationValue[] { AnnotationValue.createStringValue("value", namedChannel) }));
            namedChannelMethodCreator.addAnnotation(AnnotationInstance.create(CHANNEL, null,
                    new AnnotationValue[] { AnnotationValue.createStringValue("value", namedChannel) }));

            MethodDescriptor createChannelMethod = MethodDescriptor.ofMethod(
                    AbstractChannelProducer.class, "createChannel", ManagedChannel.class, String.class);
            ResultHandle namedChannelHandle = namedChannelMethodCreator.load(namedChannel);
            namedChannelMethodCreator.returnValue(namedChannelMethodCreator.invokeVirtualMethod(createChannelMethod,
                    namedChannelMethodCreator.getThis(), namedChannelHandle));
        }
        classCreator.close();
    }
}
