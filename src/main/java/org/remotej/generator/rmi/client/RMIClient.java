package org.remotej.generator.rmi.client;

public class RMIClient {
    private static java.rmi.registry.Registry currentRegistry = null;
    private String[] hosts = null;
    private static int currentHost = 0;
    private int registryPort;
    private String registryHost;

    public void _RMIClient() {
        String s;
        if (System.getProperty("remotej.servers") != null) {
            s = System.getProperty("remotej.servers");
        } else {
            s = registryHost;
        }
        java.util.StringTokenizer st = new java.util.StringTokenizer(s, ",");
        hosts = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            hosts[i++] = st.nextToken();
        }
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }

    public String getRegistryHost() {
        return registryHost;
    }

    public void setRegistryHost(String registryHost) {
        this.registryHost = registryHost;
    }

    public java.rmi.registry.Registry findAlternateServer() {
        if (hosts == null) {
            throw new RuntimeException("No RMI server hosts have been declared");
        }
        int savedHost = currentHost;
        while (true) {
            System.err.println("Connecting to : " + hosts[currentHost]);
            try {
                java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry(hosts[currentHost], registryPort);
                registry.list(); // to force a connection
                return registry;
            } catch (java.rmi.RemoteException e) {
                System.err.println("Connection to host: " + hosts[currentHost] + " failed.");
            }
            currentHost++;
            if (currentHost + 1 > hosts.length) {
                currentHost = 0;
            }
            if (currentHost == savedHost) {
                throw new RuntimeException("Cannot connect to any remote server(s)");
            }

        }
    }

    public synchronized java.rmi.registry.Registry getRegistry() {
        _RMIClient();
        if (currentRegistry == null) {
            currentRegistry = findAlternateServer();
        }
        return currentRegistry;
    }
}
