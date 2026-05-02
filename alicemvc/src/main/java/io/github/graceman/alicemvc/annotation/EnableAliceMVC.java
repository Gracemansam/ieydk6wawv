package io.github.graceman.alicemvc.annotation;

import io.github.graceman.alicemvc.config.AliceMvcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Activates AliceMVC auto-configuration.
 * Place on your {@code @SpringBootApplication} class.
 *
 * @author Graceman — In loving memory of Grandma Alice
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AliceMvcAutoConfiguration.class)
public @interface EnableAliceMVC {
}
