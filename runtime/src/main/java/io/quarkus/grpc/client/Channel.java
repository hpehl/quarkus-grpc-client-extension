package io.quarkus.grpc.client;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@SuppressWarnings("unused")
@Target({ METHOD, FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Qualifier
public @interface Channel {

    String value();
}
