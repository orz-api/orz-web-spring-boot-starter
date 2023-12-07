package orz.springboot.web.annotation;

import java.lang.annotation.*;

@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrzWebScope {
    String name() default "";
}
