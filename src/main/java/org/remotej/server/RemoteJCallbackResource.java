package org.remotej.server;

import org.remotej.generator.Transfer;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class RemoteJCallbackResource extends Resource {
    @SuppressWarnings("unused")

    public RemoteJCallbackResource() {
        super();
    }

    public RemoteJCallbackResource(Context context, Request request,
                                   Response response) {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    public boolean allowPost() {
        return true;
    }

    /**
     * Handle POST requests: create a new item.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void post(Representation entity) {

        Representation rep = new StringRepresentation("OOPS",
            MediaType.TEXT_PLAIN);

        ObjectRepresentation or = null;
        try {
            or = new ObjectRepresentation(entity);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("oops");
            e.printStackTrace();
            return;
        }

        Transfer msg;
        try {
            msg = (Transfer) or.getObject();
        } catch (IOException e) {
            e.printStackTrace();
            getResponse().setEntity(rep);
            return;
        }

        try {
            Object parameters[] = msg.getParameters();
            Class types[] = msg.getParameterTypes();
            Class cls = Class.forName(msg.getClassName());
            Method method = cls.getDeclaredMethod(msg.getMethod(), types);

            Object iCls = cls.getConstructor().newInstance();
            Object output = method.invoke(iCls, parameters);
            msg.setReturnValue(output);
            msg.setParameters(null);
            msg.setParameterTypes(null);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.err.println("Invocation of object failed. Cause: "
                + e.getCause());
        } catch (InstantiationException e) {
            System.err.println("Instantiation of object failed. Cause: "
                + e.getCause());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Representation representation = new ObjectRepresentation(msg);
        getResponse().setEntity(representation);

    }
}
