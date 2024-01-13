package org.remotej.generator;

import java.lang.reflect.InvocationTargetException;

public final class ProtocolFactory {

    public static synchronized IProtocol getProtocol(String protocolName) throws ProtocolFactoryException {
        String p = "org.remotej.generator.";
        IProtocol protocol;

        p += protocolName.toUpperCase();
        p += "Protocol";

        try {
            Class<IProtocol> v = IProtocol.class;
            return v.getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new ProtocolFactoryException("InstantiationException: Cannot instantiate protocol class: " + p);
        } catch (IllegalAccessException e) {
            throw new ProtocolFactoryException("IllegalAccessException: Cannot instantiate protocol class: " + p);
        } catch (NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
