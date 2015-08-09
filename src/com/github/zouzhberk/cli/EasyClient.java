package com.github.zouzhberk.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import com.github.zouzhberk.cli.annotations.ModuleMenu;

public class EasyClient {

	public static void main(String[] args) throws CliException {
		if (args == null || args.length == 0) {
			return;
		}

		Arrays.stream(args);
		// String command = args[0];
		Stream<Class<?>> menus = registerMenuClasses();
		menus.map((x) -> x.getAnnotation(ModuleMenu.class)).filter((x) -> true);

	}

	private static <V> V tryGet(Callable<V> callable, V exceptionValue) {
		try {
			return callable.call();
		} catch (Exception e) {
			return exceptionValue;
		}
	}

	private static Stream<Class<?>> registerMenuClasses() throws CliException {
		String path = Config.getRegisterClassPath();
		try {
			return Files.lines(Paths.get(path)).distinct()
					.map((x) -> tryGet(() -> Class.forName(x), Object.class))
					.filter((x) -> !Object.class.equals(x))
					.filter((x) -> x.getAnnotation(ModuleMenu.class) != null);
		} catch (IOException e) {
			throw new CliException("read file [" + path + "]occur error.", e);
		}

	}
}
