package com.github.zouzhberk.cli.annotations;

public @interface Param {
	String sname() default "";

	String value();

	String desc() default "";

	boolean required() default false;

}
