package io.github.graceman.alicemvc.starter;

import io.github.graceman.alicemvc.config.AliceMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot Starter auto-configuration for AliceMVC.
 *
 * @author Graceman
 * @since 1.0.0
 */
@AutoConfiguration
@Import(AliceMvcAutoConfiguration.class)
public class AliceMvcStarterAutoConfiguration {
}
