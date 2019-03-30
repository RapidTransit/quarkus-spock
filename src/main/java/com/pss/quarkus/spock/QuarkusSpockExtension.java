/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pss.quarkus.spock;

import io.quarkus.deployment.ClassOutput;
import io.quarkus.deployment.QuarkusClassWriter;
import io.quarkus.deployment.util.IoUtil;
import io.quarkus.runner.RuntimeRunner;
import io.quarkus.runner.TransformerTarget;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.test.common.NativeImageLauncher;
import io.quarkus.test.common.RestAssuredURLManager;
import io.quarkus.test.common.TestInjectionManager;
import io.quarkus.test.common.TestResourceManager;
import io.quarkus.test.common.http.TestHTTPResourceManager;
import org.jboss.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiFunction;

import static io.quarkus.test.common.PathTestHelper.getAppClassLocation;
import static io.quarkus.test.common.PathTestHelper.getTestClassesLocation;

/**
 * Copy Pasta'd
 *
 *
 * todo: Add a switch to leave written classes, sometimes we want to see what happened from the processor output
 */
public class QuarkusSpockExtension extends AbstractAnnotationDrivenExtension<QuarkusSpec> {


    private static final Logger LOGGER = Logger.getLogger(QuarkusSpockExtension.class.getName());

    @Override
    public void visitSpecAnnotation(QuarkusSpec annotation, SpecInfo spec) {
        SpecInitializer extension = new SpecInitializer();

       // TestResourceManager testResourceManager = new TestResourceManager(spec.getReflection());

        LOGGER.debug("Starting Test Resources");
       // testResourceManager.start();

        String logLocation = Optional.of(annotation.logLocation())
                .filter(log-> !"".equals(log))
                .orElse(determineLogLocation());

        System.setProperty("quarkus.log.file.path", logLocation);

        spec.addSetupSpecInterceptor(new IMethodInterceptor() {
            @Override
            public void intercept(IMethodInvocation invocation) throws Throwable {
                if(annotation.substrate()){
                    NativeImageLauncher launcher = new NativeImageLauncher(spec.getReflection());
                    launcher.start();

                    // Launcher implements closeable :)
                    extension.shutdownTask = launcher;
                } else {
                    extension.doJavaStart(spec.getReflection());
                }
                invocation.proceed();
            }
        });
        spec.addInitializerInterceptor(new IMethodInterceptor() {
            @Override
            public void intercept(IMethodInvocation invocation) throws Throwable {

                TestHTTPResourceManager.inject(invocation.getInstance());
                TestInjectionManager.inject(invocation.getInstance());
                RestAssuredURLManager.setURL();
                invocation.proceed();
            }
        });
        spec.addCleanupInterceptor(new IMethodInterceptor() {
            @Override
            public void intercept(IMethodInvocation invocation) throws Throwable {
                RestAssuredURLManager.clearURL();
            }
        });

        spec.addCleanupSpecInterceptor(new IMethodInterceptor() {
            @Override
            public void intercept(IMethodInvocation invocation) throws Throwable {
                Optional.ofNullable(extension.shutdownTask).ifPresent(x-> {
                    try {
                        x.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
               // testResourceManager.stop();
            }
        });
    }


    private String determineLogLocation(){
        Path path = Paths.get(System.getProperty("user.dir"));
        boolean target = path.resolve("target").toFile().exists();
        boolean build = path.resolve("build").toFile().exists();
        if(target && build){
            LOGGER.warn("Both a /build directory and a /target directory were detected, logging to /target");
            return "target/quarkus.log";
        } else if(build){
            return "build/quarkus.log";
        } else {
            return "target/quarkus.log";
        }
    }

    static class SpecInitializer {

        final LinkedBlockingDeque<Runnable> shutdownTasks = new LinkedBlockingDeque<>();

        Closeable shutdownTask;

        void doJavaStart(Class clazz) {



            Path appClassLocation = getAppClassLocation(clazz);
            Path testClassLocation = getTestClassesLocation(clazz);
            RuntimeRunner runtimeRunner = RuntimeRunner.builder()
                    .setLaunchMode(LaunchMode.TEST)
                    .setClassLoader(getClass().getClassLoader())
                    .setTarget(appClassLocation)
                    .setClassOutput(new ClassOutput() {
                        @Override
                        public void writeClass(boolean applicationClass, String className, byte[] data) throws IOException {
                            Path location = testClassLocation.resolve(className.replace('.', '/') + ".class");
                            Files.createDirectories(location.getParent());
                            try (FileOutputStream out = new FileOutputStream(location.toFile())) {
                                out.write(data);
                            }
                            //shutdownTasks.add(new DeleteRunnable(location));
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
            runtimeRunner.run();

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

        }
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
