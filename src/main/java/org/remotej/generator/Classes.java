package org.remotej.generator;

import javassist.CtClass;

public class Classes {
    private String className;
    private CtClass generatedInterface;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public CtClass getGeneratedInterface() {
        return generatedInterface;
    }

    public void setGeneratedInterface(CtClass generatedInterface) {
        this.generatedInterface = generatedInterface;
    }
}
