package orz.springboot.web.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(OrzWebErrors.class)
public @interface OrzWebError {
    /**
     * 错误代码
     */
    String code();

    /**
     * 错误原因
     */
    String reason();

    /**
     * 发送给客户端的通知
     */
    String notice() default "";

    /**
     * 是否发出警报
     */
    boolean alarm() default false;

    /**
     * 是否记录日志
     */
    boolean logging() default false;

    /**
     * 错误描述
     */
    String description() default "";
}
