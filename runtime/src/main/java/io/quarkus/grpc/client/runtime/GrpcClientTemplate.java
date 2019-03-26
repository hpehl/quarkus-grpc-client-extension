package io.quarkus.grpc.client.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Template;

@Template
public class GrpcClientTemplate {

    public void setShutdownContext(BeanContainer beanContainer, ShutdownContext shutdownContext) {
        // TODO is there an easier way to get the shutdown context into the channel producer?
        ChannelProducer channelProducer = beanContainer.instance(ChannelProducer.class);
        channelProducer.setShutdownContext(shutdownContext);
    }
}
