package cn.edu.gdut.zaoying.Option.radiusAxis.axisTick;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InsideBoolean {
    boolean value() default true;
}