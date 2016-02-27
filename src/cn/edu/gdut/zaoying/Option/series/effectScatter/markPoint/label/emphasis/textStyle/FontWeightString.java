package cn.edu.gdut.zaoying.Option.series.effectScatter.markPoint.label.emphasis.textStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FontWeightString {
    String value() default "";
}