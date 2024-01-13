package org.remotej.generator.jms.client;

import org.remotej.generator.Transfer;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 8:57:42 AM on Jun 15, 2006
 * <p/>
 * This class is a skeleton that will be extended at compile time.
 */
public class JMSClient {
   private static QueueConnectionFactory connectionFactory = null;
   public QueueConnection connection;
   private QueueSession session;
   private String[] hosts;
   private static int currentHost = 0;
   private QueueSender senderQueue;
   private Queue defaultDestination;
   private Queue replyDestination;
   // options
   public int timeout = 10000;
   public String brokerURL;
   public String initialContextFactory;
   public String sendQueue;

   public int getExceptionDepth() {
      return exceptionDepth;
   }

   public void setExceptionDepth(int exceptionDepth) {
      this.exceptionDepth = exceptionDepth;
   }

   private int exceptionDepth = 0;

   public String getCurrentHost() {
      if (hosts == null) {
         _JMSClient();
      }
      return hosts[currentHost];
   }

   public String getReceiveQueue() {
      return receiveQueue;
   }

   public void setReceiveQueue(String receiveQueue) {
      this.receiveQueue = receiveQueue;
   }

   public String receiveQueue;

   public String getPersist() {
      return persist;
   }

   public void setPersist(String persist) {
      this.persist = persist;
   }

   public String persist;

   public void _JMSClient() {
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

   public int getTimeout() {
      return timeout;
   }

   public void setTimeout(int timeout) {
      this.timeout = timeout;
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

   public String getSendQueue() {
      return sendQueue;
   }

   public void setSendQueue(String sendQueue) {
      this.sendQueue = sendQueue;
   }

   public synchronized Connection getConnection() throws JMSException {
      _JMSClient();
      if (connection == null) {
         findAlternateServer();
         session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         if ("temporary".equals(receiveQueue)) {
            replyDestination = session.createTemporaryQueue();
         } else {
            replyDestination = session.createQueue(receiveQueue);
         }
         senderQueue = session.createSender(defaultDestination);
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
         props.setProperty("queue.destination", sendQueue);

         try {
            Context ctx = new InitialContext(props);
            connectionFactory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
            QueueConnection c = connectionFactory.createQueueConnection();
            c.start();
            defaultDestination = (Queue) ctx.lookup("destination");
            connection = c;
            return;
         } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("Connection to host: " + hosts[currentHost] + " failed.");
         }

         currentHost++;
         if (currentHost + 1 > hosts.length) {
            currentHost = 0;
         }
         if (currentHost == savedHost) { // give up
            exceptionDepth++;
            throw new JMSException("Cannot connect to any remote server(s)");
         }
      }

   }

   public Transfer sendMessage(final Transfer o) throws JMSException {
      getConnection();
      ObjectMessage msg = session.createObjectMessage();
      msg.setObject(o);
      msg.setJMSReplyTo(replyDestination);
      if ("true".equals(persist)) {
         msg.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
      } else {
         msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
      }
      senderQueue.send(msg);

      String selector = "JMSCorrelationID='" + msg.getJMSMessageID() + "'";
      QueueReceiver replyQueue = session.createReceiver(replyDestination, selector);

      msg = (ObjectMessage) replyQueue.receive(timeout);
      if (msg == null) {
         throw new JMSException("no response received in " + timeout + " milliseconds.");
      }
      return (Transfer) msg.getObject();
   }

}
