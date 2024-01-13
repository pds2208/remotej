package org.remotej.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.*;
import org.restlet.data.Protocol;

/**
 * User: soulep Date: Apr 4, 2008 Time: 7:59:04 AM
 */
public class RESTServer extends Application {
   private int port;
   private int serverThreads;
   public static Log log = LogFactory.getLog("RESTServer");

   public int getPort() {
      return port;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public int getServerThreads() {
      return serverThreads;
   }

   public void setServerThreads(int serverThreads) {
      this.serverThreads = serverThreads;
   }

   public RESTServer() {
   }

   /**
    * Creates a root Restlet that will receive all incoming calls.
    */
   @Override
   public synchronized Restlet createRoot() {
      Router router = new Router(getContext());
      router.attachDefault(RemoteJResource.class);
      return router;
   }

   public void startServer() throws Exception {
      Component component = new Component();
      RESTServer server = new RESTServer();
      server.setContext(component.getContext());
      component.getServers().add(Protocol.HTTP, port);

      Context con = component.getServers().getContext();

      con.getParameters().add("defaultThreads", serverThreads + "");

      component.getDefaultHost().attach(server);

      component.start();
      log.info("RemoteJ REST server statred");
      log.info(serverThreads + " threads allocated");
   }

   public static void main(String[] args) {
      try {
         RESTServer server = new RESTServer();
         server.setPort(8182);
         server.setServerThreads(10);
         server.startServer();
      } catch (Exception e) {
         // Something is wrong.
         e.printStackTrace();
         System.exit(1);
      }
   }

}
