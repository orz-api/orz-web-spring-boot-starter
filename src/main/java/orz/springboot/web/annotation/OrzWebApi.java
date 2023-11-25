package orz.springboot.web.annotation;

import org.springframework.web.bind.annotation.RestController;
import orz.springboot.base.annotation.OrzFullyQualifier;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@OrzFullyQualifier
public @interface OrzWebApi {
    String domain();

    String resource() default "";

    String action();

    int variant() default 0;

    boolean idempotent() default false;
}
