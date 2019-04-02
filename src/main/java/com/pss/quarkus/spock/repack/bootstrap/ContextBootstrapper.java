package com.pss.quarkus.spock.repack.bootstrap;

import com.pss.quarkus.spock.bytecode.InjectableTestBeanEnhancer;
import com.pss.quarkus.spock.inject.InjectionOverride;
import io.quarkus.deployment.ClassOutput;
import io.quarkus.deployment.QuarkusClassWriter;
import io.quarkus.deployment.util.IoUtil;
import io.quarkus.runner.RuntimeRunner;
import io.quarkus.runner.TransformerTarget;
import io.quarkus.runtime.LaunchMode;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiFunction;

import static io.quarkus.test.common.PathTestHelper.getAppClassLocation;
import static io.quarkus.test.common.PathTestHelper.getTestClassesLocation;

public class ContextBootstrapper {

    private final RuntimeRunner runner;

    private final Closeable shutdownTask;

    private ContextBootstrapper(RuntimeRunner runner, Closeable shutdownTask) {
        this.runner = runner;
        this.shutdownTask = shutdownTask;
    }

    public void run() {
        runner.run();
    }


    public void shutdown() {
        try {
            shutdownTask.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ContextBootstrapper from(Class<?> clazz) {

        final LinkedBlockingDeque<Runnable> shutdownTasks = new LinkedBlockingDeque<>();

        final Closeable shutdownTask;



        Path appClassLocation = getAppClassLocation(clazz);
        Path testClassLocation = getTestClassesLocation(clazz);
        RuntimeRunner runtimeRunner = RuntimeRunner.builder()
                .setLaunchMode(LaunchMode.TEST)
                .setClassLoader(ContextBootstrapper.class.getClassLoader())
                .setTarget(appClassLocation)
                .setClassOutput(new ClassOutput() {

                    @Override
                    public void writeClass(boolean applicationClass, String className, byte[] data) throws IOException {
                        Path location = testClassLocation.resolve(className.replace('.', '/') + ".class");
                        Files.createDirectories(location.getParent());
                        try (FileOutputStream out = new FileOutputStream(location.toFile())) {

                            ClassReader classReader = new ClassReader(new ByteArrayInputStream(data));
                            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                            InjectableTestBeanEnhancer enhancer = new InjectableTestBeanEnhancer(writer);
                            classReader.accept(enhancer, 0);
                            out.write(writer.toByteArray());

                        }
                        // This is commented out because I need to inspect the output bytecode
                       // shutdownTasks.add(new DeleteRunnable(location));
                    }

                    @Override
                    public void writeResource(String name, byte[] data) throws IOException {
                        Path location = testClassLocation.resolve(name);
                        Files.createDirectories(location.getParent());
                        try (FileOutputStream out = new FileOutputStream(location.toFile())) {
                            out.write(data);
                        }
                        shutdownTasks.add(new DeleteRunnable(location));
                    }
                })
                .setTransformerTarget(new TransformerTarget() {
                    @Override
                    public void setTransformers(Map<String, List<BiFunction<String, ClassVisitor, ClassVisitor>>> functions) {
                        ClassLoader main = Thread.currentThread().getContextClassLoader();

                        //we need to use a temp class loader, or the old resource location will be cached
                        ClassLoader temp = new ClassLoader() {
                            @Override
                            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                                // First, check if the class has already been loaded
                                Class<?> c = findLoadedClass(name);
                                if (c == null) {
                                    c = findClass(name);
                                }
                                if (resolve) {
                                    resolveClass(c);
                                }
                                return c;
                            }

                            @Override
                            public URL getResource(String name) {
                                return main.getResource(name);
                            }

                            @Override
                            public Enumeration<URL> getResources(String name) throws IOException {
                                return main.getResources(name);
                            }
                        };
                        for (Map.Entry<String, List<BiFunction<String, ClassVisitor, ClassVisitor>>> e : functions.entrySet()) {
                            String resourceName = e.getKey().replace('.', '/') + ".class";
                            try (InputStream stream = temp.getResourceAsStream(resourceName)) {
                                if (stream == null) {
                                    System.err.println("Failed to transform " + e.getKey());
                                    continue;
                                }
                                byte[] data = IoUtil.readBytes(stream);

                                ClassReader cr = new ClassReader(data);
                                ClassWriter cw = new QuarkusClassWriter(cr,
                                        ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {

                                    @Override
                                    protected ClassLoader getClassLoader() {
                                        return temp;
                                    }
                                };
                                ClassLoader old = Thread.currentThread().getContextClassLoader();
                                Thread.currentThread().setContextClassLoader(temp);
                                try {
                                    ClassVisitor visitor = cw;
                                    for (BiFunction<String, ClassVisitor, ClassVisitor> i : e.getValue()) {
                                        visitor = i.apply(e.getKey(), visitor);
                                    }
                                    cr.accept(visitor, 0);
                                } finally {
                                    Thread.currentThread().setContextClassLoader(old);
                                }

                                Path location = testClassLocation.resolve(resourceName);
                                Files.createDirectories(location.getParent());
                                try (FileOutputStream out = new FileOutputStream(location.toFile())) {
                                    out.write(cw.toByteArray());
                                }
                                shutdownTasks.add(new DeleteRunnable(location));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                })
                .build();


        shutdownTask = new Closeable() {
            @Override
            public void close() throws IOException {
                runtimeRunner.close();
                while (!shutdownTasks.isEmpty()) {
                    shutdownTasks.pop().run();
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    shutdownTask.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "Quarkus Test Cleanup Shutdown task"));

        return new ContextBootstrapper(runtimeRunner, shutdownTask);
    }

    static class DeleteRunnable implements Runnable {
        final Path path;

        DeleteRunnable(Path path) {
            this.path = path;
        }

        @Override
        public void run() {
            try {
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
