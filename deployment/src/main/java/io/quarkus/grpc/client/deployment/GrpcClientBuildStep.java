package io.quarkus.grpc.client.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import javax.inject.Singleton;

import org.jboss.jandex.DotName;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.substrate.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.builditem.substrate.RuntimeReinitializedClassBuildItem;
import io.quarkus.grpc.client.runtime.Channel;
import io.quarkus.grpc.client.runtime.ChannelProducer;
import io.quarkus.grpc.client.runtime.GrpcClientTemplate;

public class GrpcClientBuildStep {

    private static final DotName CHANNEL = DotName.createSimple(Channel.class.getName());
    private static final DotName SINGLETON = DotName.createSimple(Singleton.class.getName());

    @BuildStep
    public void build(BuildProducer<FeatureBuildItem> feature,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<BeanDefiningAnnotationBuildItem> beanDefinitions,
            BuildProducer<RuntimeInitializedClassBuildItem> runtimeInit,
            BuildProducer<RuntimeReinitializedClassBuildItem> runtimeReinit,
            BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {

        // Register gRPC feature
        feature.produce(new FeatureBuildItem("grpc-client"));

        // Register additional beans
        additionalBeans.produce(new AdditionalBeanBuildItem(false, ChannelProducer.class));
        beanDefinitions.produce(new BeanDefiningAnnotationBuildItem(CHANNEL, SINGLETON, false));

        // Try to fix "Error: Detected a direct/mapped ByteBuffer in the image heap."
        // runtimeInit.produce(new RuntimeInitializedClassBuildItem("io.netty.channel.DefaultChannelConfig"));
        // runtimeInit.produce(new RuntimeInitializedClassBuildItem("io.netty.channel.socket.DefaultSocketChannelConfig"));
        // runtimeReinit.produce(new RuntimeReinitializedClassBuildItem("io.netty.channel.socket.nio.NioSocketChannel"));

        // Indicates that this extension would like the SSL support to be enabled
        extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem("grpc-client"));
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    public void setShutdownContext(GrpcClientTemplate template, BeanContainerBuildItem beanContainer,
            ShutdownContextBuildItem shutdownContext) {
        template.setShutdownContext(beanContainer.getValue(), shutdownContext);
    }
}
