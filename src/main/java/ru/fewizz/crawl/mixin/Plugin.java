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
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.IntInsnNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class Plugin implements IMixinConfigPlugin, Opcodes {

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
		if(!targetClassName.equals("net.minecraft.entity.EntityPose"))
			return;
		
		targetClass.fields.add(
			new FieldNode(
				ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_ENUM,
				"CRAWLING",
				"Lnet/minecraft/entity/EntityPose;",
				null,
				null
			)
		);
		
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
		varInit.add(new TypeInsnNode(NEW, "net/minecraft/entity/EntityPose"));
		varInit.add(new InsnNode(DUP));
		varInit.add(new LdcInsnNode("CRAWLING"));
		varInit.add(new IntInsnNode(BIPUSH, enums));
		varInit.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/entity/EntityPose", "<init>", "(Ljava/lang/String;I)V", false));
		varInit.add(new FieldInsnNode(PUTSTATIC, "net/minecraft/entity/EntityPose", "CRAWLING", "Lnet/minecraft/entity/EntityPose;"));
		
		init.instructions.set(lastEnumInit.getNext().getNext(), new IntInsnNode(Opcodes.BIPUSH, enums + 1));
		init.instructions.insert(lastEnumInit.getNext(), varInit); // Skip putstatic
		
		AbstractInsnNode putArray = null;
		for(Iterator<AbstractInsnNode> it = init.instructions.iterator(); it.hasNext();) {
			AbstractInsnNode insn = it.next();
			if(insn.getType() == AbstractInsnNode.FIELD_INSN)
				putArray = insn;
		}
		InsnList addElement = new InsnList();
		addElement.add(new InsnNode(DUP)); // Dup array
		addElement.add(new IntInsnNode(BIPUSH, enums)); // index in array
		addElement.add(new FieldInsnNode(GETSTATIC, "net/minecraft/entity/EntityPose", "CRAWLING", "Lnet/minecraft/entity/EntityPose;"));
		addElement.add(new InsnNode(AASTORE));
		
		init.instructions.insert(putArray.getPrevious(), addElement);
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

}
