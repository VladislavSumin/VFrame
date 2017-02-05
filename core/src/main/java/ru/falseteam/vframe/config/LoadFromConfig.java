package ru.falseteam.vframe.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate parameter to load from config file
 * To load you must call {@link ConfigLoader}
 * Support types: String, Integer, Boolean.
 *
 * @author Sumin Vladislav
 * @version 1.2
 * @see ConfigLoader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LoadFromConfig {
    String filename() default "main";

    // If use default value key name automatically load from field name.
    String key() default "";

    String defaultValue();
}