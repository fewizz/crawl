package ru.fewizz.crawl.mixin;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class Plugin implements IMixinConfigPlugin {
	private static final Path CONFIG_PATH = Path.of("./config/crawl.properties");
	private static final Logger LOGGER = LogManager.getLogger("CrawlMixinPlugin");

	final private Properties props = new Properties();

	@Override
	public void onLoad(String mixinPackage) {
		props.setProperty("replace-crawl-animation", Boolean.toString(true));

		if (!Files.exists(CONFIG_PATH)) {
			try {
				props.store(Files.newOutputStream(CONFIG_PATH), null);
			} catch (IOException e) {
				LOGGER.warn("Couldn't save config", e);
			}
			return;
		}

		try {
			props.load(Files.newInputStream(CONFIG_PATH));
		} catch (IOException e) {
			LOGGER.warn("Couldn't load config", e);
		}
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		boolean animationClass =
			mixinClassName.equals("ru.fewizz.crawl.mixin.client.HumanoidRenderStateMixin") ||
			mixinClassName.equals("ru.fewizz.crawl.mixin.client.HumanoidModelMixin") ||
			mixinClassName.equals("ru.fewizz.crawl.mixin.client.PlayerRendererMixin");

		boolean replaceAnimation =
			Boolean.parseBoolean(props.getProperty("replace-crawl-animation"));

		return !animationClass || replaceAnimation;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (mixinClassName.equals("ru.fewizz.crawl.mixin.PoseMixin")) {
			String internalName = targetClassName.replace(".", "/");
			String desc = "L"+internalName+";";

			MethodNode arrayInitMethod = targetClass.methods.stream()
				.filter(m -> !m.name.equals("values") && m.desc.equals("()["+desc))
				.findFirst().get();

			MethodNode classInitMethod = targetClass.methods.stream()
				.filter(m -> m.name.equals("<clinit>"))
				.findFirst().get();

			TypeInsnNode aNewArrayInsn = (TypeInsnNode) StreamSupport.stream(arrayInitMethod.instructions.spliterator(), false)
				.filter(insn->insn.getOpcode() == ANEWARRAY)
				.findFirst().get();

			IntInsnNode sizeArgInsn = (IntInsnNode) aNewArrayInsn.getPrevious();
			int newEntryIndex = sizeArgInsn.operand;
			sizeArgInsn.operand += 1; // increase array size

			targetClass.fields.add(new FieldNode(ACC_PUBLIC | ACC_FINAL | ACC_STATIC | ACC_ENUM, "CRAWLING", desc, null, null));

			InsnList createNewEntry = new InsnList();
			createNewEntry.add(new TypeInsnNode(NEW, internalName));
			createNewEntry.add(new InsnNode(DUP));
			createNewEntry.add(new LdcInsnNode("CRAWLING"));
			createNewEntry.add(new IntInsnNode(BIPUSH, newEntryIndex));
			createNewEntry.add(new IntInsnNode(BIPUSH, newEntryIndex));
			createNewEntry.add(new MethodInsnNode(INVOKESPECIAL, internalName, "<init>", "(Ljava/lang/String;II)V"));
			createNewEntry.add(new FieldInsnNode(PUTSTATIC, internalName, "CRAWLING", desc));
			classInitMethod.instructions.insertBefore(
				StreamSupport.stream(classInitMethod.instructions.spliterator(), false)
					.filter(insn -> insn.getOpcode() == NEW)
					.findFirst().get(),
				createNewEntry
			);

			InsnList addNewEntry = new InsnList();
			addNewEntry.add(new InsnNode(DUP)); // dup array
			addNewEntry.add(new IntInsnNode(BIPUSH, newEntryIndex));
			addNewEntry.add(new FieldInsnNode(GETSTATIC, internalName, "CRAWLING", desc));
			addNewEntry.add(new InsnNode(AASTORE));
			arrayInitMethod.instructions.insert(aNewArrayInsn, addNewEntry);
		}
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

}
