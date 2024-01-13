package org.remotej.server;

import java.util.Hashtable;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.Protocol;

/**
 * Callback server for the REST protocol. Used by the <b>ref</b>  keyword
 * to refer to an object reference.
 *  
 * @author Paul Soule
 *
 */
public class RestCallbackServer extends Application {
   private int port;
   private Router router;
   private final Hashtable<String, Object> objects = new Hashtable<String, Object>();

   public int getPort() {
      return port;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public void addObject(String name, Object obj) {
      objects.put(name, obj);
   }

   public RestCallbackServer(int port) {
      this.port = port;
   }

   /**
    * Creates a root Restlet that will receive all incoming calls.
    */
   @Override
   public synchronized Restlet createRoot() {
      router = new Router(getContext());
      router.attachDefault(RemoteJCallbackResource.class);
      return router;
   }
   
   public Route getRouter() {
      return router.getDefaultRoute();
   }

   public void startServer() throws Exception {
      Component component = new Component();
      RESTServer server = new RESTServer();
      server.setContext(component.getContext());
      component.getServers().add(Protocol.HTTP, port);

      Context con = component.getServers().getContext();

      con.getParameters().add("defaultThreads", "1");

      component.getDefaultHost().attach(server);

      component.start();
   }

   public static void main(String[] args) {
      try {
         RestCallbackServer server = new RestCallbackServer(8182);
         server.startServer();
      } catch (Exception e) {
         // Something is wrong.
         e.printStackTrace();
         System.exit(1);
      }
   }

}
