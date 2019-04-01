# Quarkus gRPC Client Extension

Extension to call [gRPC](https://grpc.io/) services in your [Quarkus](https://quarkus.io) application. 

```java
@ApplicationScoped
public class RouteGuideClient {

    private RouteGuideBlockingStub blockingStub;
    
    @Inject
    public RouteGuideClient(@Channel("route") ManagedChannel channel) {
        blockingStub = RouteGuideGrpc.newBlockingStub(channel);    
    }

    public Feature getFeature(int lat, int lon) {
        Point request = Point.newBuilder().setLatitude(lat).setLongitude(lon).build();
        return blockingStub.getFeature(request);
    }
}
```

You can inject named [channels](https://grpc.io/docs/guides/concepts.html#channels) into your beans. Each channel has a name and is configured in your `application.properties`:

```properties
io.quarkus.grpc.client.route.host=localhost
io.quarkus.grpc.client.route.port=5050
``` 

The prperty names are made up of `io.quarkus.grpc.client` plus the name of the channel. Right now you can configure host and port. Channels are created with `usePlaintext()`, but TLS and support for additional settings (like timeouts) is in the works. Injected channels are created as singletons and are automatically shutdown when Quarkus is shutdown. 

## Getting Started

The gRPC client extension is not available in Maven Central. For now you have to clone the repository and install the extension in your local maven repository. Then follow these steps to write and deploy a simple hello world gRPC service:

### Setup Project

Create a new project using the Quarkus [archetype](https://quarkus.io/guides/getting-started-guide#bootstrapping-the-project):

```bash
mvn io.quarkus:quarkus-maven-plugin:0.11.0:create \
    -DprojectGroupId=io.grpc.helloworld \
    -DprojectArtifactId=helloworld
``` 

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-grpc-client</artifactId>
    <version>${quarkus.grpc.client.version}</version>
    <scope>provided</scope>
</dependency>
```

### Setup gRPC

To setup the gRPC code generation, add the following settings to your `pom.xml`:

```xml
<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.5.0.Final</version>
        </extension>
    </extensions>
    <plugins>
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.5.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.5.1-1:exe:${os.detected.classifier}
                </protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.18.0:exe:${os.detected.classifier}
                </pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    <plugins>
</build>
```

### Define gRPC Client

You need a running gRPC service you can talk to. See the [Quarkus gRPC extension](https://github.com/hpehl/quarkus-grpc-extension) on how to get your gRPC services up and running. Then in your client code, inject a managed channel and create a blocking and/or asynchronous stub:
 
```java
@ApplicationScoped
public class RouteGuideClient {

    private RouteGuideStub asyncStub;
    private RouteGuideBlockingStub blockingStub;
    
    @Inject
    public RouteGuideClient(@Channel("route") ManagedChannel channel) {
        asyncStub = RouteGuideGrpc.newStub(channel);
        blockingStub = RouteGuideGrpc.newBlockingStub(channel);    
    }

    public Feature getFeature(int lat, int lon) {
        Point request = Point.newBuilder().setLatitude(lat).setLongitude(lon).build();
        return blockingStub.getFeature(request);
    }
}
```

Finally specify the host and port in your `application.properties`:

```properties
io.quarkus.grpc.client.route.host=localhost
io.quarkus.grpc.client.route.port=5050
``` 

## Quickstart

If you want to see a more complex example, checkout the [gRPC quickstart](https://github.com/hpehl/quarkus-grpc-quickstart). It uses both the [gRPC](https://github.com/hpehl/quarkus-grpc-extension) and the [gRPC client](https://github.com/hpehl/quarkus-grpc-client-extension) extension to implement the [route guide example](https://github.com/grpc/grpc-java/tree/v1.18.0/examples#grpc-examples) provided by [gRPC Java](https://github.com/grpc/grpc-java). 

## What's Missing

- TLS
- Better devmode support
- More configuration options

See also https://github.com/quarkusio/quarkus/issues/820

Have fun!
