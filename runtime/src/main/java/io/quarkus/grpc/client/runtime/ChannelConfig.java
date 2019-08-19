package io.quarkus.grpc.client.runtime;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
@SuppressWarnings("WeakerAccess")
public class ChannelConfig {

    /** The host */
    @ConfigItem(defaultValue = "localhost")
    public String host;

    /** The port */
    @ConfigItem(defaultValue = "5050")
    public int port;
}
