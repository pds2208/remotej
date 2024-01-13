package org.remotej.generator.jms.client;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import java.util.StringTokenizer;

public class SUBClient {
    private ConnectionFactory connectionFactory;
    private Topic destination;
    private MessageConsumer consumer;
    private Connection connection;

    private String topic;

    private String[] hosts;
    private static int currentHost = 0;
    // options
    public String brokerURL;
    public String initialContextFactory;
    public String sendTopic;
    public int receiveTimeout;
    public boolean durable;
    public String subscriberName;
    public String clientID;
    public MessageListener listener;

    public MessageListener getListener() {
        return listener;
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public Object getReceivedMessage() throws JMSException {
        getConnection();
        Object message = consumer.receive(receiveTimeout);

        // a timeout
        if (message == null) {
            return null;
        }

        if (!(message instanceof ObjectMessage)) {
            throw new IllegalArgumentException("Message must be of type ObjectMessage");
        }

        try {
            ObjectMessage o = (ObjectMessage) message;
            return o.getObject();
        } catch (JMSException ex) {
            throw new RuntimeException(ex);
        }
    }


    public int getTimeout() {
        return receiveTimeout;
    }

    public void setTimeout(int receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public SUBClient() {

    }

    public int getExceptionDepth() {
        return exceptionDepth;
    }

    public void setExceptionDepth(int exceptionDepth) {
        this.exceptionDepth = exceptionDepth;
    }

    private int exceptionDepth = 0;

    public String getCurrentHost() {
        if (hosts == null) {
            _SUBClient();
        }
        return hosts[currentHost];
    }

    public boolean getDurable() {
        return durable;
    }

    public void setPersist(boolean durable) {
        this.durable = durable;
    }


    public void _SUBClient() {
        String s;
        if (System.getProperty("remotej.servers") != null) {
            s = System.getProperty("remotej.servers");
        } else {
            s = brokerURL;
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

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public synchronized Connection getConnection() throws JMSException {
        _SUBClient();
        if (connection == null) {
            findAlternateServer();
        }


        return connection;
    }

    public void findAlternateServer() throws JMSException {

        if (hosts == null) {
            throw new RuntimeException("No JMS broker hosts have been declared");
        }

        int savedHost = currentHost;

        while (true) {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            props.setProperty(Context.PROVIDER_URL, hosts[currentHost]);
            props.setProperty("topic.destination", topic);

            try {

                Context ctx = new InitialContext(props);
                connectionFactory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
                connection = connectionFactory.createConnection();

                if (durable) {
                    connection.setClientID(clientID);
                }

                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                destination = (Topic) ctx.lookup("destination");

                if (durable) {
                    consumer = session.createDurableSubscriber(destination, subscriberName);
                } else {
                    consumer = session.createConsumer(destination);
                }
                consumer.setMessageListener(listener);
                connection.start();
                return;
            } catch (Exception e) {
                //e.printStackTrace();
                System.err.println("Connection to broker: " + hosts[currentHost] + " failed.");
            }

            currentHost++;
            if (currentHost + 1 > hosts.length) {
                currentHost = 0;
            }
            if (currentHost == savedHost) { // give up
                exceptionDepth++;
                throw new JMSException("Cannot connect to any remote brokers");
            }
        }

    }

}
