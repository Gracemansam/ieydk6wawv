package io.github.graceman.alicemvc.config;

import io.github.graceman.alicemvc.dto.DefaultResponseFactory;
import io.github.graceman.alicemvc.dto.ResponseFactory;
import io.github.graceman.alicemvc.exception.AliceExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Auto-configuration for AliceMVC.
 *
 * <p>Registers the {@link DefaultResponseFactory} and
 * {@link AliceExceptionHandler} as conditional beans. Both back off
 * automatically if the developer defines their own.</p>
 *
 * <p><b>ResponseFactory:</b> Define your own {@code @Component} implementing
 * {@link ResponseFactory} and the default backs off.</p>
 *
 * <p><b>Exception Handler:</b> Disable via property or replace:</p>
 * <pre>
 * # application.properties
 * alicemvc.exception-handler.enabled=false
 * </pre>
 *
 * @author Graceman
 * @since 1.0.0
 */
@Configuration
@ComponentScan(
        basePackages = "io.github.graceman.alicemvc",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = AliceExceptionHandler.class
        )
)
public class AliceMvcAutoConfiguration {

    /**
     * Default ResponseFactory — wraps responses in ApiResponse envelope.
     * Backs off if the developer defines their own ResponseFactory bean.
     *
     * @return the default response factory
     */
    @Bean
    @ConditionalOnMissingBean(ResponseFactory.class)
    public ResponseFactory aliceResponseFactory() {
        return new DefaultResponseFactory();
    }

    /**
     * Default exception handler — catches all AliceMVC exceptions.
     *
     * <p>Backs off if:</p>
     * <ul>
     *   <li>The developer defines their own AliceExceptionHandler bean</li>
     *   <li>The property {@code alicemvc.exception-handler.enabled} is set to {@code false}</li>
     * </ul>
     *
     * @param responseFactory the response factory (auto-injected)
     * @return the exception handler
     */
    @Bean
    @ConditionalOnMissingBean(AliceExceptionHandler.class)
    @ConditionalOnProperty(
            name = "alicemvc.exception-handler.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public AliceExceptionHandler aliceExceptionHandler(ResponseFactory responseFactory) {
        return new AliceExceptionHandler(responseFactory);
    }
}
