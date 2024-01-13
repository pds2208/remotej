package org.remotej.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.remotej.generator.Transfer;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * User: soulep
 * Date: Apr 4, 2008
 * Time: 11:53:39 AM
 */
public class RemoteJResource extends Resource {
   public static Log log = LogFactory.getLog("RemoteJResource");
   private static RemoteJRegistry remoteJRegistry = new RemoteJRegistry();

   public RemoteJResource() {
      super();
   }

   public RemoteJResource(Context context, Request request, Response response) {
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

      Request request = super.getRequest();
      String message = "Resource URI  : " + request.getResourceRef()
         + '\n' + "Root URI      : " + request.getRootRef()
         + '\n' + "Routed part   : "
         + request.getResourceRef().getBaseRef() + '\n'
         + "Remaining part: "
         + request.getResourceRef().getRemainingPart();
      log.info(message);

      Representation rep = new StringRepresentation("OOPS", MediaType.TEXT_PLAIN);

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

      // check if its a connection request
      if (msg.getMethod().equals("REMOTEJ_CONNECTION")) {
         Representation representation = new ObjectRepresentation(msg);
         getResponse().setEntity(representation);
         return;
      }

      try {
         Object parameters[] = msg.getParameters();
         Class types[] = msg.getParameterTypes();
         Class cls = Class.forName(msg.getClassName());
         Method method = cls.getDeclaredMethod(msg.getMethod(), types);

         Object iCls;

         // check the cache
         iCls = remoteJRegistry.get(msg.getClassName());
         if (iCls == null) {
           iCls = cls.getConstructor().newInstance();
            remoteJRegistry.put(msg.getClassName(), iCls);
         }

         Object output = method.invoke(iCls, parameters);

         msg.setReturnValue(output);
         msg.setParameters(null);
         msg.setParameterTypes(null);

      } catch (IllegalAccessException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      } catch (NoSuchMethodException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      } catch (InvocationTargetException e) {
         System.err.println("Invocation of object failed. Cause: " + e.getCause());
         //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      } catch (InstantiationException e) {
         System.err.println("Instantiation of object failed. Cause: " + e.getCause());
         //e.printStackTrace();
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      }

      Representation representation = new ObjectRepresentation(msg);
      getResponse().setEntity(representation);

   }
   
}
