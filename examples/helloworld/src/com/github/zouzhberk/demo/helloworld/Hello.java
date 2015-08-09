package com.github.zouzhberk.demo.helloworld;

import com.github.zouzhberk.cli.annotations.ActionMenu;
import com.github.zouzhberk.cli.annotations.ModuleMenu;
import com.github.zouzhberk.cli.annotations.Param;

@ModuleMenu(value = "hello", desc = "The Hello Module.")
public class Hello {

	@ActionMenu(value = "say", desc = "say hello")
	public Object say(
			@Param(value = "text", sname = "t", desc = "The words.", required = true) String text,
			@Param(value = "username", sname = "u", desc = "user name.") String username) {
		return "say" + text;
	}

	@ActionMenu(value = "version", desc = "Display version info.")
	public Object version() {
		return "1.0";
	}

	public Object help() {

		// Arrays.stream(getClass().getMethods()).
		return "";
	}
}
