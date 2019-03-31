package com.pss.quarkus.spock.bytecode;

import org.objectweb.asm.*;

import static java.lang.reflect.Modifier.PUBLIC;

public class ReplacingEnhancer extends ClassVisitor {


    public ReplacingEnhancer(int api) {
        super(api);
    }

    public ReplacingEnhancer(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if(access == Opcodes.ACC_PUBLIC && "create".equals(name)){
            
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

}
