package com.github.zouzhberk.cli;

import java.lang.reflect.Method;
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
import com.github.zouzhberk.demo.helloworld.Hello;
import com.github.zouzhberk.demo.helloworld.World;

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

	private static Option buildHelpOption(String moduleName) {
		return Option.builder("h").desc("display help info.").longOpt("help")
				.build();
	}

	private int parseOnlyMainModule(String moduleName, String[] actionArgs) {
		if (actionArgs == null || actionArgs.length == 0) {
			printMainMoudleHelp();
			return 0;
		}

		Options options = new Options();
		options.addOption(buildHelpOption(mainModule));
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

		Collections.reverse(tmpArgs);
		// check if arg is a vailable module.
		actionName = tmpArgs.remove(0);
		moduleNames = tmpArgs;
		findCommandClass(moduleNames.get(0)).ifPresent((clazz) -> {
			findMenuMethod(actionName, clazz);
		});

		if (!findCommandClass(moduleNames.get(0)).isPresent()) {

		}

		actionArgs = Arrays.copyOfRange(args, optionaIndex, args.length);
		System.out.println("actionName = " + actionName);
		System.out.println("modulenames = " + moduleNames);
		System.out.println("action args = " + Arrays.deepToString(actionArgs));
		System.out.println(menuArgs);
		if (moduleNames.isEmpty()) {
			printMainMoudleHelp();
		}

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

		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		// OptionGroup group = new OptionGroup();
		options.addOption(Option.builder("t").longOpt("text")
				.desc("say a text.").hasArg().required().build());

		options.addOption(Option.builder("h").longOpt("help")
				.desc("Dislpay help messages.").hasArg(false).build());
		// options.addOptionGroup(group);
		CommandLine line;
		try {
			line = parser.parse(options, args);

			// System.out.println(line.getArgList());
			if (line.getArgList().isEmpty() || line.hasOption("help")) {
				printHelper(options, "helloworld");
			}

		} catch (ParseException e) {
			System.out.println(e.getMessage());
			printHelper(options, "helloworld");
		}
	}

	public static void printHelper(Options options, String text) {
		HelpFormatter formart = new HelpFormatter();
		formart.printHelp(text, options, true);

	}
}
