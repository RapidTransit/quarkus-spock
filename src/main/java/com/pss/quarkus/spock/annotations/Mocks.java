package com.pss.quarkus.spock.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * = Mock your CDI Beans (or Stub or Spy)
 *
 * Add this annotation on a method in your Spock Specification
 *
 * ```groovy
 * @QuarkusSpec
 * class MySpec extends Specification {
 *
 *    @Inject
 *    MySimpleBeanService service
 *
 *    @Inject
 *    SimpleBean1 simple1
 *
 *    def "Make Sure My Stuff Mocks"(){
 *        setup:
 *        simple1.get() >> "OK"
 *        expect:
 *        service.callSimple1sMethod() == "OK"
 *    }
 *
 *
 *
 *    @Mocks
 *    SimpleBean1 mock(){
 *        return Mock(SimpleBean1)
 *    }
 *
 *    @Mocks
 *    SimpleBean2 mock(){
 *        return Stub(SimpleBean2)
 *    }
 * ```
 *
 *  WARNING: {@link Mocks#implmentation()} Is not working, this will probably take some more byte code manipulation
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mocks {

    /**
     *
     * If you are using an interface to mock, what is the implementation class,
     *
     */
    Class implmentation() default void.class;
}
