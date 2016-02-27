package cn.edu.gdut.zaoying.Option.dataZoom.slider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataBackgroundColorHex {
    int value() default 0;
}