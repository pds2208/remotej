package org.remotej.server;

import org.restlet.*;
import org.restlet.data.Protocol;

import java.util.Hashtable;

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
