package io.quarkus.grpc.client.runtime;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

import static io.quarkus.runtime.annotations.ConfigPhase.RUN_TIME;

/** gRPC client configuration. */
@ConfigRoot(phase = RUN_TIME)
public class GrpcClientConfig {

    /** Channel configuration */
    @ConfigItem(name = ConfigItem.PARENT)
    public Map<String, ChannelConfig> channels;
}
