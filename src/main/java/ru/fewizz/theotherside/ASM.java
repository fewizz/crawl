package ru.fewizz.theotherside;

import java.util.function.Function;

import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.util.Textifier;
import org.spongepowered.asm.lib.util.TraceMethodVisitor;

public class ASM {
	public static MethodNode findMethod(ClassNode clazz, String name) {
		return clazz.methods.stream().filter(m -> m.name.equals(name)).findFirst().get();
	}
	
	public enum Action {
		FOUND_RETURN,
		FOUND_CONTINUE,
		CONTINUE;
	}

	public static<R extends AbstractInsnNode> R indexOf(Function<AbstractInsnNode, ASM.Action> cond, InsnList insns) {
		return indexOf(cond, insns, 0);
	}

	public static<R extends AbstractInsnNode> R indexOf(Function<AbstractInsnNode, ASM.Action> cond, InsnList insns, AbstractInsnNode from) {
		return indexOf(cond, insns, insns.indexOf(from));
	}
	
	@SuppressWarnings("unchecked")
	public static<R extends AbstractInsnNode> R indexOf(Function<AbstractInsnNode, ASM.Action> cond, InsnList insns, int from) {
		int result = -1;
		for(int i = from; i < insns.size(); i ++) {
			switch(cond.apply(insns.get(i))) {
			case FOUND_RETURN:
				return (R) insns.get(i);
			case FOUND_CONTINUE:
				result = i;
			default:
			}
		}
		if(result == -1)
			throw new RuntimeException("Fault condition");
		return (R) insns.get(result);
	}
	
	public static<R extends AbstractInsnNode> R indexOfMethodInvoke(String name, InsnList insns) {
		return indexOf(a -> {
			if(a instanceof MethodInsnNode && ((MethodInsnNode)a).name.equals(name))
				return Action.FOUND_RETURN;
			return Action.CONTINUE;
		}, insns);
	}
	
	public static LabelNode labelOf(int insn, InsnList il) {
		for(int i = insn; i > 0; i--) {
			AbstractInsnNode ai = il.get(i);
			if(ai instanceof LabelNode)
				return (LabelNode) ai;
		}
		throw new RuntimeException("Can't find label");
	}
	
	public static LabelNode labelOf(AbstractInsnNode insn, InsnList il) {
		return labelOf(il.indexOf(insn), il);
	}
	
	public static void printInfo(MethodNode mn) {
		Textifier text = new Textifier();
		TraceMethodVisitor tmv = new TraceMethodVisitor(text);
		mn.accept(tmv);
		for(Object o : text.text)
			System.out.print(o);
	}
}