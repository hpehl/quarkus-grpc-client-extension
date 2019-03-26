package io.quarkus.grpc.client.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.quarkus.runtime.ShutdownContext;

@ApplicationScoped
public class ChannelProducer {

    private static final Logger log = Logger.getLogger("io.quarkus.grpc.client");

    @Inject
    Config config;

    private final Map<String, ManagedChannel> channels;
    private ShutdownContext shutdownContext;

    public ChannelProducer() {
        channels = new HashMap<>();
    }

    void setShutdownContext(ShutdownContext shutdownContext) {
        this.shutdownContext = shutdownContext;
    }

    @Produces
    public ManagedChannel getChannel(InjectionPoint injectionPoint) {
        Channel annotation = injectionPoint.getAnnotated().getAnnotation(Channel.class);
        if (annotation != null) {
            return channels.computeIfAbsent(annotation.value(), this::createChannel);
        }
        return null;
    }

    private ManagedChannel createChannel(String name) {
        // TODO Read and *verify* additional properties like target, timeouts, ...
        String host = config.getValue(fqProperty(name, "host"), String.class);
        int port = config.getValue(fqProperty(name, "port"), Integer.class);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        log.fine("Created channel for gRPC service " + name);
        if (shutdownContext != null) {
            shutdownContext.addShutdownTask(() -> {
                channel.shutdown();
                log.info("Shutdown channel for gRPC service " + name);
            });
        }
        return channel;
    }

    private String fqProperty(String grpcService, String property) {
        return "io.quarkus.grpc.client." + grpcService + "." + property;
    }
}
