package org.remotej.generator.jms.client;

import org.remotej.generator.Transfer;

import javax.jms.JMSException;

public class TestJMSClient {

    private JMSClient jmsClient = new JMSClient();

    public static void main(String[] args) throws Exception {
        TestJMSClient tjms = new TestJMSClient();
        tjms.setupJMS();
        tjms.run();
        System.exit(0);
    }

    private void run() throws JMSException {
        jmsClient.getConnection();
        System.out.println("Address: " + getAddress(" ", " "));
        System.out.println("Address: " + getAddress(" ", " "));
        System.out.println("Address: " + getAddress(" ", " "));
        System.out.println("Address: " + getAddress(" ", " "));
    }

    private void setupJMS() {
        jmsClient.setTimeout(10000);
        jmsClient.setBrokerURL("tcp://localhost:61616");
        jmsClient.setInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        jmsClient.setSendQueue("REMOTEJ.SEND");
    }

    @SuppressWarnings("unchecked")
    public java.lang.String getAddress(java.lang.String a0, java.lang.String a1) throws javax.jms.JMSException {
        setupJMS();
        Transfer t = new Transfer();
        t.setMethod("getAddress");
        t.setClassName("com.paul.Address");
        Object[] o = new Object[2];
        Class[] p = new Class[2];
        o[0] = a0;
        o[1] = a1;
        try {
            p[0] = Class.forName("java.lang.String");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            p[1] = Class.forName("java.lang.String");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        t.setParameters(o);
        t.setParameterTypes(p);
        boolean done = false;
        do {
            try {
                Transfer res = jmsClient.sendMessage(t);
                return (java.lang.String) res.getReturnValue();
            } catch (Exception e) {
                e.printStackTrace();
                jmsClient.findAlternateServer();
            }
        } while (!done);

        return null;
    }


}
