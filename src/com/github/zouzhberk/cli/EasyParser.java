package com.github.zouzhberk.cli;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.github.zouzhberk.cli.annotations.ActionMenu;
import com.github.zouzhberk.cli.annotations.ModuleMenu;
import com.github.zouzhberk.cli.annotations.Param;
import com.github.zouzhberk.demo.helloworld.Hello;
import com.github.zouzhberk.demo.helloworld.World;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EasyParser {

	String actionName;

	List<String> moduleNames;

	private String[] actionArgs;

	private List<Class<?>> cmdClasses;

	private String mainModule;

	public EasyParser(String[] args) {
		moduleNames = new ArrayList<>();
		cmdClasses = new ArrayList<>();
		cmdClasses.add(Hello.class);
		cmdClasses.add(World.class);
		parseArgs(args);
	}

	public void setCommandClasses(List<Class<?>> classes) {
		this.cmdClasses = classes;
	}

	private static Option buildHelpOption() {
		return Option.builder().desc("display help info.").longOpt("help")
				.build();
	}

	private int parseOnlyMainModule(String moduleName, String[] actionArgs) {
		if (actionArgs == null || actionArgs.length == 0) {
			printMainMoudleHelp();
			return 0;
		}

		Options options = new Options();
		options.addOption(buildHelpOption());
		options.addOption(Option.builder("v").longOpt("version").hasArg(false)
				.build());

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine commandline = parser.parse(options, actionArgs);
			if (commandline.hasOption('h')) {
				printMainMoudleHelp();
				return 0;
			}

			if (commandline.hasOption('v')) {
				println(mainModule + "Version 1.0");
				return 0;
			}

		} catch (ParseException e) {
			println(e.getMessage());
			printMainMoudleHelp();
			return 2;
		}
		return 0;

	}

	private static Optional<Method> findMenuMethod(String actionName,
			Class<?> moudleClass) {
		return Stream
				.of(moudleClass.getMethods())
				.filter((method) -> {
					return Optional
							.ofNullable(method.getAnnotation(ActionMenu.class))
							.filter((x) -> x.value().equals(actionName))
							.isPresent();
				}).findFirst();
	}

	private void parseArgs(String[] args) {

		List<String> tmpArgs = new ArrayList<>();
		int optionaIndex = args.length;

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				optionaIndex = i;
				break;
			}
			tmpArgs.add(args[i]);

		}

		actionArgs = Arrays.copyOfRange(args, optionaIndex, args.length);
		if (tmpArgs.isEmpty()) {
			println("Must input a module name.");
			System.exit(1);
		}
		List<String> menuArgs = tmpArgs.stream().collect(Collectors.toList());
		mainModule = tmpArgs.remove(0);

		if (tmpArgs.size() < 2) {
			// check global options, like --help, --version
			System.exit(parseOnlyMainModule(mainModule, actionArgs));
		}

		// parse subcommand.
		Collections.reverse(tmpArgs);
		// check if arg is a vailable module.
		actionName = tmpArgs.remove(0);
		moduleNames = tmpArgs;

		Class<?> moduleClass = findCommandClass(moduleNames.get(0))
				.orElse(null);

		if (moduleClass == null) {
			println("No such subcommand found.");
			printMainMoudleHelp();
			System.exit(3);
		}

		Method actionMethod = findMenuMethod(actionName, moduleClass)
				.orElseGet(() -> {
					println("No such action supported.");
					return null;
				});

		if (actionMethod == null || actionName.equalsIgnoreCase("help")) {
			ModuleMenu menu = moduleClass.getAnnotation(ModuleMenu.class);
			println(menu.desc());

			println("Type '" + String.join(" ", mainModule, menu.value())
					+ " <action> --help ' for help on a specific action.");
			println("Avaliable actions:");
			Method[] actionMethods = moduleClass.getDeclaredMethods();
			Stream.of(actionMethods)
					.map((x) -> x.getAnnotation(ActionMenu.class))
					.filter(Objects::nonNull)
					.forEach((x) -> println("\t" + x.value()));
			System.exit(4);
		}

		Options options = new Options();
		List<Option> group = getParameters(actionMethod, actionArgs);

		if (group.isEmpty()) {
			Object obj;
			try {
				obj = moduleClass.newInstance();
				println(actionMethod.invoke(obj));
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(0);
		}
		group.stream().forEach((x) -> options.addOption(x));

		options.addOption(buildHelpOption());

		CommandLineParser parser = new DefaultParser();

		CommandLine line;
		try {

			line = parser.parse(options, actionArgs);

			// System.out.println(line.getArgList());
			if (actionArgs.length == 0 || line.hasOption("help")) {
				printHelper(
						options,
						String.join(" ",
								menuArgs.stream().toArray(String[]::new)));
				System.exit(5);
			}

			println(invokeMethod(moduleClass, actionMethod, group, line));

		} catch (ParseException e) {
			System.out.println(e.getMessage());
			printHelper(options,
					String.join(" ", menuArgs.stream().toArray(String[]::new)));
			System.exit(6);
		}

	}

	public static Gson createGson() {

		return new GsonBuilder().serializeNulls().create();
	}

	private static Gson gson = createGson();

	protected static Object invokeMethod(Class<?> clazz, Method method,
			List<Option> group, CommandLine line) {

		try {
			Object object = clazz.newInstance();
			Object[] args = new Object[group.size()];

			Type[] types = method.getGenericParameterTypes();

			int i = 0;
			for (Option option : group) {

				if (line.hasOption(option.getOpt())) {
					try {
						args[i] = line.getParsedOptionValue(option.getOpt());
					} catch (ParseException e) {
						args[i] = gson.fromJson(
								line.getOptionValue(option.getOpt()), types[i]);
					}
				} else {
					args[i] = null;
				}
				i++;
			}

			return method.invoke(object, args);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static List<Option> getParameters(Method method, String[] rightArgs) {
		Annotation[][] params = method.getParameterAnnotations();

		List<Option> list = new ArrayList<>();
		if (params == null || params.length == 0) {
			return list;
		}

		Class<?>[] paramClasses = method.getParameterTypes();

		for (int i = 0; i < params.length; i++) {
			Annotation[] param = params[i];
			final Class<?> clazz = paramClasses[i];
			list.add(Stream
					.of(param)
					.filter((x) -> x.annotationType().equals(Param.class))
					.findFirst()
					.map((x) -> Param.class.cast(x))
					.map((x) -> Option.builder(x.sname()).longOpt(x.value())
							.required(x.required()).desc(x.desc()).hasArg()
							.type(clazz).build())
					.orElseThrow(
							() -> {
								throw new CliException(
										"All args must annotated by Param annotation class.");
							}));
		}

		return list;
	}

	private Optional<Class<?>> findCommandClass(String modulename) {
		return this.cmdClasses
				.stream()
				.filter((x) -> Optional
						.ofNullable(x.getAnnotation(ModuleMenu.class))
						.filter((menu) -> menu.value().equals(modulename))
						.isPresent()).findFirst();
	}

	public static void println(Object object) {
		System.out.println(object);
	}

	protected void printMainMoudleHelp() {
		println("Type '" + mainModule
				+ " <subcommand> help ' for help on a specific subcommand.");
		println("Avaliable subcommands:");
		this.cmdClasses.stream().map((x) -> x.getAnnotation(ModuleMenu.class))
				.filter(Objects::nonNull)
				.forEach((x) -> println("\t" + x.value()));
	}

	protected void printSubModuleHelp() {

	}

	public static EasyParser from(String[] args) {

		return new EasyParser(args);
	}

	public static void main(String[] args) {

		EasyParser.from(args);
	}

	public static void printHelper(Options options, String text) {
		HelpFormatter formart = new HelpFormatter();
		formart.printHelp(text, options, true);

	}
}
