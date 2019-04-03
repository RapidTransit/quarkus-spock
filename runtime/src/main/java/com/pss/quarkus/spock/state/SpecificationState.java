package com.pss.quarkus.spock.state;

public class SpecificationState {

    private static Object specification;

    public static Object getSpecification() {
        return specification;
    }

    public static void setSpecification(Object specification) {
        SpecificationState.specification = specification;
    }
}
