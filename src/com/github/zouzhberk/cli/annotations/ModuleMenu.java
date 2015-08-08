package com.github.zouzhberk.cli.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface ModuleMenu {
	String value();

	String desc() default "";

	Class<?> parent() default ModuleMenu.class;
}
