package io.quarkus.grpc.client.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.jboss.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractChannelProducer {

    private static final Logger log = Logger.getLogger("io.quarkus.grpc.client");

    private GrpcClientConfig config;
    private List<ManagedChannel> channels = new ArrayList<>();

    public ManagedChannel createChannel(String name) {
        ChannelConfig channelConfig = config.channels.get(name);

        // TODO Read and *verify* additional properties like target, timeouts, ...
        String host = channelConfig.host;
        int port = channelConfig.port;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        log.infof("Created gRPC channel %s:%d for service %s", host, port, name);
        channels.add(channel);
        return channel;
    }

    @PreDestroy
    public void shutdown() {
        for (ManagedChannel channel : channels) {
            channel.shutdown();
        }
    }

    public void setConfig(GrpcClientConfig config) {
        this.config = config;
    }
}
