package org.remotej.server;

import jakarta.jms.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.remotej.generator.Transfer;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class JMSServer implements SessionAwareMessageListener {
    private int serverThreads;
    private String initialContextFactory;
    private String destinationClass;
    private String destinationQueueName;
    private JmsTemplate template;
    private String brokerURL;

    private static final RemoteJRegistry remoteJRegistry = new RemoteJRegistry();

    public String getPersist() {
        return persist;
    }

    public void setPersist(String persist) {
        this.persist = persist;
    }

    private String persist;
    private final DefaultMessageListenerContainer container =
        new DefaultMessageListenerContainer();
    public static Log log = LogFactory.getLog("JMSServer");

    /**
     * Creates a new instance of JMSServer
     */

    public void setupContainer() throws NamingException {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
        props.setProperty(Context.PROVIDER_URL, brokerURL);
        props.setProperty("queue.destination", destinationQueueName);

        javax.naming.Context ctx = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
        container.setConcurrentConsumers(getServerThreads());
        container.setConnectionFactory(connectionFactory);
        Destination destination = (Destination) ctx.lookup("destination");
        container.setDestination(destination);
        container.setMessageListener(this);

        setTemplate(new JmsTemplate());
        getTemplate().setDefaultDestination(destination);
        getTemplate().setConnectionFactory(connectionFactory);
    }

    public void start() throws NamingException {
        container.initialize();
        container.start();
        log.info("Server threads : " + serverThreads);
        log.info("ContextFactory : " + initialContextFactory);
        log.info("Destination class : " + destinationClass);
        log.info("Queue name     : " + destinationQueueName);
        log.info("Broker URL     : " + brokerURL);
        log.info("Persist        : " + persist);
        log.info("RemoteJ Container Ready\n");
    }

    public int getServerThreads() {
        return serverThreads;
    }

    public void setServerThreads(int serverThreads) {
        this.serverThreads = serverThreads;
    }

    public JmsTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
        if (System.getProperty("remotej.broker") != null) {
            this.brokerURL = System.getProperty("remotej.broker");
        }
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    public String getDestinationClass() {
        return destinationClass;
    }

    public void setDestinationClass(String destinationClass) {
        this.destinationClass = destinationClass;
    }

    public String getDestinationQueueName() {
        return destinationQueueName;
    }

    public void setDestinationQueueName(String destinationQueueName) {
        this.destinationQueueName = destinationQueueName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(Message message, Session session) throws jakarta.jms.JMSException {
        Transfer msg;
        if (!(message instanceof ObjectMessage)) {
            throw new IllegalArgumentException("Message must be of type ObjectMessage");
        }

        try {
            ObjectMessage o = (ObjectMessage) message;
            msg = (Transfer) o.getObject();
        } catch (JMSException ex) {
            throw new RuntimeException(ex);
        }


        try {
            Object parameters[] = msg.getParameters();
            Class types[] = msg.getParameterTypes();
            Class cls = Class.forName(msg.getClassName());
            Method method = cls.getDeclaredMethod(msg.getMethod(), types);

            Object iCls = remoteJRegistry.get(msg.getClassName());
            if (iCls == null) {
                iCls = cls.getDeclaredConstructor().newInstance();
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if ("true".equals(persist)) {
                template.setDeliveryPersistent(true);
            } else {
                template.setDeliveryPersistent(false);
            }
            template.convertAndSend(message.getJMSReplyTo(), msg, new MessagePostProcessor() {
                public Message postProcessMessage(Message msg) throws JMSException {
                    msg.setJMSCorrelationID(message.getJMSMessageID());
                    log.info("Sending reply to: " + message.getJMSReplyTo() + " " + message.getJMSMessageID());
                    return msg;
                }
            });
        } catch (JmsException ex) {
            ex.printStackTrace();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
        log.debug("Reply sent");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NamingException {
        JMSServer s = new JMSServer();
        s.setServerThreads(10);
        s.setInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        s.setBrokerURL("tcp://localhost:61616");
        s.setDestinationClass("org.apache.activemq.command.ActiveMQQueue");
        s.setDestinationQueueName("REMOTEJ.SEND");
        s.setPersist("false");
        s.setupContainer();
        s.start();
    }

}
