package io.github.graceman.alicemvc.annotation;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisableOperation {

    Operation[] value();

    enum Operation {
        LIST, RETRIEVE, CREATE, UPDATE, PARTIAL_UPDATE, DELETE
    }
}
