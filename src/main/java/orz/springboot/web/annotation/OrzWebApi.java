package orz.springboot.web.annotation;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
public @interface OrzWebApi {
    String domain();

    String resource() default "";

    String action();

    int variant();

    boolean query() default false;

    String description() default "";
}
