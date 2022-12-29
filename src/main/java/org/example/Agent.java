package org.example;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Agent implements ClassFileTransformer {

  private boolean hasStackMapFrames(byte[] classfileBuffer) {
    ClassReader reader = new ClassReader(classfileBuffer);
    final boolean[] hasFrames = {false};
    ClassVisitor cv = new ClassVisitor(Opcodes.ASM9) {
      @Override
      public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
          String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM9) {
          @Override
          public void visitFrame(int type, int numLocal, Object[] local, int numStack,
              Object[] stack) {
            hasFrames[0] = true;
          }
        };
      }
    };
    reader.accept(cv, 0);
    return hasFrames[0];
  }

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) {
    if (!className.equals("java/lang/ProcessBuilder")) {
      return null;
    }
    System.err.printf("%s java.lang.ProcessBuilder%n", classBeingRedefined != null ? "Retransforming" : "Transforming");
    System.err.println("Has stack map frames: " + hasStackMapFrames(classfileBuffer));
    return null;
  }

  @Override
  public byte[] transform(Module module, ClassLoader loader, String className,
      Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
    return transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
  }

  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new Agent(), true);
    try {
      inst.retransformClasses(ProcessBuilder.class);
    } catch (UnmodifiableClassException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
  }
}
