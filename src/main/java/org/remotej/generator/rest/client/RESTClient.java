package org.remotej.generator.rest.client;

import org.remotej.generator.Transfer;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;

import java.io.IOException;
import java.util.StringTokenizer;

public class RESTClient {
    private String[] hosts;
    private static int currentHost = 0;
    private int exceptionDepth = 0;
    private Client client = null;
    private String serviceName;
    private Reference serverUri;

    // options
    public int timeout = 10000;
    public String serversURL;
    public String initialContextFactory;
    public String sendQueue;

    public int getExceptionDepth() {
        return exceptionDepth;
    }

    public void setExceptionDepth(int exceptionDepth) {
        this.exceptionDepth = exceptionDepth;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCurrentHost() {
        if (hosts == null) {
            _RESTClient();
        }
        return hosts[currentHost];
    }

    public void _RESTClient() {
        String s;
        if (System.getProperty("remotej.servers") != null) {
            s = System.getProperty("remotej.servers");
        } else {
            s = serversURL;
        }
        StringTokenizer st = new StringTokenizer(s, ",");
        hosts = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            hosts[i] = st.nextToken();
            i++;
        }
    }

    public void setHosts(String[] hosts) {
        String s = "";
        for (int i = 0; i < hosts.length; i++) {
            s += hosts[i];
            if (i != hosts.length - 1) {
                s += ",";
            }
        }
        System.setProperty("remotej.servers", s);
    }

    public String getServersURL() {
        return serversURL;
    }

    public void setServersURL(String serversURL) {
        this.serversURL = serversURL;
    }

    public synchronized Reference getConnection() throws Exception {
        _RESTClient();
        if (serverUri == null) {
            findAlternateServer();
        }
        return serverUri;
    }

    public void findAlternateServer() throws Exception {

        if (hosts == null) {
            throw new RuntimeException("No REST hosts have been declared");
        }

        int savedHost = currentHost;

        while (true) {
            client = new Client(Protocol.HTTPS);
            serverUri = new Reference(hosts[currentHost] + "/" + serviceName);

            // send a connection request
            Transfer transfer = new Transfer();
            transfer.setMethod("REMOTEJ_CONNECTION");

            Representation rep = new ObjectRepresentation(transfer);

            Response response = client.post(serverUri, rep);

            if (response.getStatus().isSuccess()) {
                return;
            }

            serverUri = null;

            currentHost++;
            if (currentHost + 1 > hosts.length) {
                currentHost = 0;
            }
            if (currentHost == savedHost) { // give up
                exceptionDepth++;
                throw new Exception("Cannot connect to any remote server(s)");
            }
        }
    }

    public Transfer sendMessage(final Transfer transfer) throws Exception {
        getConnection();

        Representation rep = new ObjectRepresentation(transfer);

        Response response = client.post(serverUri, rep);

        if (!response.getStatus().isSuccess()) {
            throw new Exception("REST server returned a status of " + response.getStatus().getCode());
        }
        if (!response.isEntityAvailable()) {
            throw new Exception("REST server did not send a response");
        }

        try {
            ObjectRepresentation or;
            try {
                or = new ObjectRepresentation(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw e;
            }
            Transfer t = (Transfer) or.getObject();
            return t;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
