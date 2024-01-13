package org.remotej.generator.jms.client;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Copyright(c) Paul Soule 2006.
 * <p/>
 * Date: Feb 17, 2007
 * Time: 2:02:25 PM
 */
public class TestSUBClient {

   private ConnectionFactory connectionFactory;
   private Destination destination;
   private MessageProducer producer;
   private Connection connection;
   private Session session;

   // options
   public String brokerURL;
   public String initialContextFactory;
   public String sendTopic;
   public int receiveTimeout;

   public static void main(String[] args) throws Exception {
      TestSUBClient tjms = new TestSUBClient();
      tjms.setupJMS();
      tjms.run();
      System.exit(0);
   }

   private void run() throws JMSException {
      sendMessage("one");
      sendMessage("two");
      sendMessage("three");
      sendMessage("four");
      sendMessage("five");
   }

   private void sendMessage(String message) throws JMSException {
      ObjectMessage m = session.createObjectMessage();
      m.setObject(message);
      producer.send(m);
   }

   private void setupJMS() {
      Properties props = new Properties();
      props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
      props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
      props.setProperty("topic.destination", "REMOTEJ.SEND");

      try {

         Context ctx = new InitialContext(props);
         connectionFactory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
         connection = connectionFactory.createConnection();

         session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         destination = (Destination) ctx.lookup("destination");
         producer = session.createProducer(destination);
         connection.start();
         return;
      } catch (Exception e) {
         //e.printStackTrace();
         System.err.println("Connection to broker failed.");
      }
   }

}