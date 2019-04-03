package io.quarkus.grpc.client.runtime;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class ChannelConfig {

    /** The host */
    @ConfigItem
    public String host;

    /** The port */
    @ConfigItem
    public int port;
}
