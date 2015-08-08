package com.github.zouzhberk.demo.helloworld;

import com.github.zouzhberk.cli.annotations.ActionMenu;
import com.github.zouzhberk.cli.annotations.ModuleMenu;
import com.github.zouzhberk.cli.annotations.Param;

@ModuleMenu(value = "world", desc = "the world moudle")
public class World {

	@ActionMenu(value = "create", desc = "create a new world.")
	public Object create(
			@Param(value = "name", sname = "n", desc = "The world name. default as random string.") String worldName) {
		return "Good " + worldName;
	}
}
