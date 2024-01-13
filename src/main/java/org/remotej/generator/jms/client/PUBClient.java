package org.remotej.generator.jms.client;

import org.remotej.generator.Transfer;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.StringTokenizer;
import java.util.Properties;
import java.io.Serializable;

/**
 * User: soulep
 * Date: Mar 26, 2008
 * Time: 12:45:50 PM
 */
public class PUBClient {
   private ConnectionFactory connectionFactory;
   private Topic destination;
   private MessageProducer producer;
   private Connection connection;

   private String topic;

   private String[] hosts;
   private static int currentHost = 0;
   private Session session;
   // options
   public String brokerURL;
   public String initialContextFactory;
   public String sendTopic;
   public String subscriberName;
   public String clientID;

   public void sendMessage(Transfer transfer) throws JMSException {
      getConnection();
      ObjectMessage m = session.createObjectMessage();
      Object[]o = transfer.getParameters();
      m.setObject((Serializable)o[0]);
      producer.send(m);
   }

   public PUBClient() {

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
         _PUBClient();
      }
      return hosts[currentHost];
   }

   public void _PUBClient() {
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
      _PUBClient();
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

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = (Topic) ctx.lookup("destination");

            producer = session.createProducer(destination);
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