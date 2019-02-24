package ru.fewizz.crawl.mixin;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.IntInsnNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class Plugin implements IMixinConfigPlugin {

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return Arrays.asList(new String[] {"MixinEntityPose"});
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if(!targetClassName.equals("EntityPose"))
			return;
		MethodNode init =
			StreamSupport.stream(targetClass.methods.spliterator(), false).filter(mn -> mn.name.equals("<clinit>")).findFirst().get();
		
		int enums = 0;
		MethodInsnNode lastEnumInit = null;
		
		for(Iterator<AbstractInsnNode> it = init.instructions.iterator(); it.hasNext();) {
			AbstractInsnNode insn = it.next();
			if(insn.getType() == AbstractInsnNode.METHOD_INSN && ((MethodInsnNode)insn).name.equals("<init>")) {
				enums++;
				lastEnumInit = (MethodInsnNode) insn;
			}
		}
		
		InsnList varInit = new InsnList();
		varInit.add(new TypeInsnNode(Opcodes.NEW, targetClass.signature));
		varInit.add(new InsnNode(Opcodes.DUP));
		varInit.add(new LdcInsnNode("CRAWLING"));
		varInit.add(new IntInsnNode(Opcodes.BIPUSH, enums));
		varInit.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, targetClassName, "<init>", "(Ljava/lang/String;I)V", false));
		varInit.add(new FieldInsnNode(Opcodes.PUTSTATIC, "ru/fewizz/crawl/CrawlMod$Shared", "CRAWLING", "Lnet/minecraft/entity/EntityPose;"));
		
		init.instructions.set(lastEnumInit.getNext().getNext().getNext(), new IntInsnNode(Opcodes.BIPUSH, enums + 1));
		init.instructions.insert(lastEnumInit.getNext()); // Skip putstatic
		
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

}
