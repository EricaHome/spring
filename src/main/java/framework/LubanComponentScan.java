package framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Erica
 * @date 2020/11/5 23:47
 * @description TODO
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LubanComponentScan {

    // @ComponentScan("service")中的service
    String value() default "";
}
