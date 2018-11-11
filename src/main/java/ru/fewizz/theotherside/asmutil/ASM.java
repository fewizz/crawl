package ru.fewizz.theotherside.asmutil;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.RETURN;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class ASM {
    public static  class ExtendedClassVisitor extends ClassVisitor {
        public ExtendedClassVisitor() {
            super(ASM6);
        }

        public ExtendedClassVisitor(ClassVisitor classVisitor) {
            super(ASM6, classVisitor);
        }

        public ExtendedClassVisitor transform(String name, Consumer<ExtendedMethodVisitor> imv) {
            return transform(name, null, imv);
        }
        public ExtendedClassVisitor transform(String name, String desc, Consumer<ExtendedMethodVisitor> imv) {
            ExtendedClassVisitor ecv = new ExtendedClassVisitor() {
                @Override
                public MethodVisitor visitMethod(int access, String name0, String descriptor, String signature, String[] exceptions) {
                    MethodVisitor spr = super.visitMethod(access, name0, descriptor, signature, exceptions);
                    if(name0.equals(name) && (desc == null ? true : descriptor.equals(desc))) {
                        ExtendedMethodVisitor emv = new ExtendedMethodVisitor(spr);
                        imv.accept(emv);
                        return emv;
                    }
                    return spr;
                }
            };

            ecv.next(cv);
            next(ecv);
            return ecv;
        }

        public void next(ClassVisitor cv) {
            this.cv = cv;
        }

        public byte[] toByteArray() {
            return cv != null ? ((ExtendedClassVisitor)cv).toByteArray() : toByteArray();
        }
    }

    public static class ExtendedMethodVisitor extends MethodVisitor {
        public ExtendedMethodVisitor() {
            super(ASM6);
        }

        public ExtendedMethodVisitor(MethodVisitor methodVisitor) {
            super(ASM6, methodVisitor);
        }

        public void next(MethodVisitor mv) {
            this.mv = mv;
        }

        public void visitMethodInsn(int opcode, Method method) {
            visitMethodInsn(opcode, method.getDeclaringClass().getName().replace(".", "/"), method.getName(), Type.getMethodDescriptor(method), method.getDeclaringClass().isInterface());
        }

        @FunctionalInterface
        public interface IOperation {
            void letsdo() throws Exception;
        }

        public ExtendedMethodVisitor beforeReturn(IOperation operator) {
            ExtendedMethodVisitor nmv = new ExtendedMethodVisitor() {
                @Override
                public void visitInsn(int opcode) {
                    if(opcode == RETURN) {
                        try {
                            operator.letsdo();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    super.visitInsn(opcode);
                }
            };
            nmv.next(this.mv);
            next(nmv);
            return nmv;
        }
    }

    public static byte[] transform(byte[] clazz, Consumer<ExtendedClassVisitor> icv) {
        ClassReader cr = new ClassReader(clazz);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ExtendedClassVisitor ecv = new ExtendedClassVisitor(cw);
        icv.accept(ecv);
        cr.accept(ecv, 0);
        return cw.toByteArray();
    }
}
