package com.pss.quarkus.spock;

import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.ClientProxy;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Not to sure about Spock's threading model yet, this was going to be a place holder for a thread local
 */
public class SpecificationState {


    private static final MethodHandle MH;
    private static final MethodHandle INIT;
    /**
     * Do not leak Package Private Status
     * This is so we can run multiple Arc Containers, it is package private so we don't want to interfere with package
     * splitting, multi threaded test execution will also need multiple compilation and enhancement locations
     */
    static {
        try {
            Class clazz = Class.forName("io.quarkus.arc.ArcContainerImpl");
            Constructor declaredConstructor = clazz.getDeclaredConstructor();
            Method  method = clazz.getDeclaredMethod("init");
            declaredConstructor.setAccessible(true);
            method.setAccessible(true);
            MH = MethodHandles.lookup()
                    .unreflectConstructor(declaredConstructor)
                    .asType(MethodType.methodType(ArcContainer.class));
            INIT = MethodHandles.lookup()
                    .unreflect(method);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    /**
     * There is some extra threads created so we might need an {@link InheritableThreadLocal} I don't know yet
     */
    private static final ThreadLocal<SpecificationState> SPECIFICATION_STATES = new InheritableThreadLocal<>();

    private final AtomicReference<ArcContainer> instance = new AtomicReference<>();

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private Object specification;
    private final SpecInfo info;
    private final Class clazz;

    public SpecificationState(SpecInfo info, Class clazz) {
        this.info = info;
        this.clazz = clazz;
    }

    public SpecInfo getInfo() {
        return info;
    }

    public Class getClazz() {
        return clazz;
    }

    public Object getSpecification() {
        return specification;
    }

    public void setSpecification(Object specification) {
        this.specification = specification;
    }

    public static Object unwrap(Object object){
        if(object instanceof ClientProxy){
            return ((ClientProxy) object).getContextualInstance();
        }
        return object;
    }


    public ArcContainer getContainer(){
        if(initialized.compareAndSet(false, true)){
            ArcContainer arcContainer = create();
            instance.set(arcContainer);
            init(arcContainer);
        }
        return instance.get();
    }

    public static void init(ArcContainer container){
        try {
        INIT.bindTo(container)
                .invokeExact();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    public static ArcContainer create(){
        try {
            return (ArcContainer) MH.invokeExact();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }


    public static SpecificationState getInstance(){
        return SPECIFICATION_STATES.get();
    }

    public static void remove(){
        SPECIFICATION_STATES.remove();
    }

    public static void set(SpecInfo info){
        SPECIFICATION_STATES.set(new SpecificationState(info, info.getReflection()));
    }

}
