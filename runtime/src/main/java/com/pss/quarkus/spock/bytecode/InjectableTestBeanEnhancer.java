package com.pss.quarkus.spock.bytecode;

import java.lang.reflect.Method;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.pss.quarkus.spock.inject.InjectableTestBean;
import com.pss.quarkus.spock.inject.InjectionOverride;
import com.pss.quarkus.spock.util.CommonUtils;

import io.quarkus.arc.InjectableBean;

import static org.objectweb.asm.Type.*;

/**
 * = Injectable Test Bean Enhancer
 *
 * This class is an ASM visitor that accomplishes the following:
 *
 * - Find any instances of {@link InjectableBean} replace that interface with {@link InjectableTestBean},
 * InjectableTestBean extends InjectableBean and adds {@link InjectableTestBean#createBean(CreationalContext)} method
 * - Add a call to {@link InjectionOverride#putInjectableBean(InjectableTestBean)} add the end of its constructor
 * - Take both the {@link InjectableBean#create(CreationalContext)} and its bridge method and rename them both to
 * 'createBean'
 * - Remake {@link InjectableBean#create(CreationalContext)} and its bridge method to
 * {@link InjectionOverride#getInjectableBean(InjectableTestBean, CreationalContext)} (Using 'this' as the first
 * parameter and passing `CreationalContext` along with it) which will do one of two things:
 * ** Try to find a Mock bean, if it does return the mock
 * ** If the mock is not found call {@link InjectableTestBean#createBean(CreationalContext)} and return the real result
 */
public class InjectableTestBeanEnhancer extends ClassVisitor {


    static class Types {
        private static final Type CREATIONAL_CONTEXT = Type.getType(CreationalContext.class);
        private static final Type OBJECT = Type.getType(Object.class);
    }



    public static final Method INJECTABLE_BEANS;
    public static final Method GET_INJECTION;
    private static final int BRIDGE_CODE = Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE;
    private static final String INJ_BEAN = getInternalName(InjectableBean.class);
    private static final String INJ_TEST_BEAN = getInternalName(InjectableTestBean.class);
    private static final String INJ_OVERRIDE_NAME = getInternalName(InjectionOverride.class);
    private static final Method CREATE;



    private static final Method CREATE_BEAN;
    static {
        try {

            INJECTABLE_BEANS = InjectionOverride.class
                    .getDeclaredMethod("putInjectableBean", InjectableTestBean.class);

            GET_INJECTION = InjectionOverride.class
                    .getDeclaredMethod("getInjectableBean", InjectableTestBean.class, CreationalContext.class);

            CREATE = Contextual.class.getDeclaredMethod("create", CreationalContext.class);

            CREATE_BEAN = InjectableTestBean.class.getDeclaredMethod("createBean", CreationalContext.class);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * If set to true then the class gets enhanced
     * Set by: {@link InjectableTestBeanEnhancer#visit(int, int, String, String, String, String[])} if the class
     * implements {@link InjectableBean}
     */
    private boolean needsEnhancing = false;

    /**
     * JVM Internal internalClassName of the current visited class
     * Set by: {@link InjectableTestBeanEnhancer#visit(int, int, String, String, String, String[])}
     */
    private String internalClassName;

    private String descriptor;
    private String signature;
    private String[] exceptions;

    public InjectableTestBeanEnhancer(ClassVisitor visitor) {
        super(Opcodes.ASM6, visitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.internalClassName = name;
        int index;
        if ((index = CommonUtils.indexOf(interfaces, INJ_BEAN)) > -1) {
            // It found the interface we are looking for, replace it with the new one
            interfaces[index] = INJ_TEST_BEAN;
            needsEnhancing = true;
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (needsEnhancing) {
            if (access == Opcodes.ACC_PUBLIC && "<init>".equals(name)) {
                return new ConstructorVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
            } else if (access == Opcodes.ACC_PUBLIC && "create".equals(name)) {
                this.descriptor = descriptor;
                this.signature = signature;
                this.exceptions = exceptions;
                return super.visitMethod(Opcodes.ACC_PUBLIC, "createBean", descriptor, signature, exceptions);
            } else if (access == BRIDGE_CODE && "create".equals(name)) {
                return new RewriteCallVisitor(super.visitMethod(access, "createBean", descriptor, signature, exceptions));
            }
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        if (needsEnhancing) {

            // Add the new create Method
            MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "create", descriptor, signature, exceptions);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,                         // <Opcode>
                    getInternalName(InjectionOverride.class),     // <Class>
                    GET_INJECTION.getName(),                      // <<Method Name>>
                    getMethodDescriptor(GET_INJECTION),           // <<Params & Return Type>>
                    false);                                       // <<Is Interface>>

            mv.visitTypeInsn(Opcodes.CHECKCAST, getReturnType(descriptor).getInternalName());
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
            String bridgeDescriptor = getMethodDescriptor(Types.OBJECT, Types.CREATIONAL_CONTEXT);

            // Add a Bridge Method
            MethodVisitor br = super.visitMethod(BRIDGE_CODE, "create",
                    bridgeDescriptor, signature, exceptions);
            br.visitCode();
            br.visitVarInsn(Opcodes.ALOAD, 0);
            br.visitVarInsn(Opcodes.ALOAD, 1);

            br.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,  // <Opcode>
                    internalClassName,      // <Class>
                    "create",               // <<Method Name>>
                    getMethodDescriptor(    // <<Params & Return Type>>
                            getReturnType(descriptor), // <<Return Type>>
                            Types.CREATIONAL_CONTEXT), // <<Parameter Type>>
                    false);                 // <<Is Interface>>

            br.visitInsn(Opcodes.ARETURN);
            br.visitMaxs(2, 2);
            br.visitEnd();
        }
        super.visitEnd();
    }

    static class RewriteCallVisitor extends MethodVisitor {

        public RewriteCallVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM6, methodVisitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if ("create".equals(name)) {
                super.visitMethodInsn(opcode, owner, "createBean", descriptor, isInterface);
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
        }
    }

    static class ConstructorVisitor extends MethodVisitor {

        public ConstructorVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM6, methodVisitor);
        }


        /**
         *
         * @param opcode {@link org.objectweb.asm.Opcodes}
         */
        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                super.visitVarInsn(Opcodes.ALOAD, 0);

                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        getInternalName(InjectionOverride.class),
                        INJECTABLE_BEANS.getName(),
                        getMethodDescriptor(INJECTABLE_BEANS),
                        false);
            }
            super.visitInsn(opcode);
        }
    }
}
