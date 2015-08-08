package com.github.zouzhberk.cli;

public class Config {

	public static String getRegisterClassPath() {
		return System
				.getProperty("register.class.file", "register-classes.txt");
	}

}
