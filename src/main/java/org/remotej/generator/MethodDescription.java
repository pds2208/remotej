package org.remotej.generator;

import org.remotej.ddl.trees.ParameterDeclaration;
import org.remotej.ddl.trees.ReturnValue;

/**
 * Created by IntelliJ IDEA.
 * User: Paul Soule
 * Date: 05-May-2006
 * Time: 16:20:43
 * To change this template use File | Settings | File Templates.
 */
public final class MethodDescription {
    private String name;
    private ParameterDeclaration parameters;
    private ReturnValue returnValue;
    private String className;
    private String recoveryName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getRecoveryName() {
        return recoveryName;
    }

    public void setRecoveryName(String recoveryName) {
        this.recoveryName = recoveryName;
    }

    public ReturnValue getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(ReturnValue returnValue) {
        this.returnValue = returnValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParameterDeclaration getParameters() {
        return parameters;
    }

    public void setParameters(ParameterDeclaration parameters) {
        this.parameters = parameters;
    }


    public boolean equals(Object obj) {
        if ((!(obj instanceof MethodDescription))) {
            return false;
        }
        MethodDescription md = (MethodDescription) obj;
        return md.getClassName().equals(className) &&
            md.returnValue.equals(returnValue);
    }
}
