package com.pss.quarkus.spock;

import org.spockframework.compiler.model.Spec;
import spock.lang.Specification;

import javax.inject.Inject;

public class JavaSpockSpec  {
    @Inject
    String value;
}
