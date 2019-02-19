package ru.fewizz.crawl.mixin.client;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
abstract class MixinMouseHack {
	private static boolean disableCursorDisabling = Boolean.parseBoolean(System.getProperty("Crawl.disableCursorDisabling", "false"));
	
	@ModifyConstant(method="lockCursor", constant = @Constant(intValue = GLFW.GLFW_CURSOR_DISABLED))
	public int onCursorParameter(int old) {
		return disableCursorDisabling ? GLFW.GLFW_CURSOR_NORMAL : old;
	}
}