package io.quarkus.grpc.client.runtime;

import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class GrpcClientRecorder {

    public BeanContainerListener setConfig(Class<? extends AbstractChannelProducer> channelProducerClass,
            GrpcClientConfig config) {
        return beanContainer -> {
            AbstractChannelProducer producer = beanContainer.instance(channelProducerClass);
            producer.setConfig(config);
        };
    }
}
