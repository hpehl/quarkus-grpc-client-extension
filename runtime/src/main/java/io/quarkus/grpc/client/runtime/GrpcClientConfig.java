package io.quarkus.grpc.client.runtime;

import static io.quarkus.runtime.annotations.ConfigPhase.BUILD_AND_RUN_TIME_FIXED;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

/** gRPC client configuration. */
@ConfigRoot(phase = BUILD_AND_RUN_TIME_FIXED)
public class GrpcClientConfig {

    /** Channel configuration */
    @ConfigItem(name = ConfigItem.PARENT)
    public Map<String, ChannelConfig> channels;
}
