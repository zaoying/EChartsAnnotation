package cn.edu.gdut.zaoying.Option.xAxis.axisLine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OnZeroBoolean {
    boolean value() default true;
}