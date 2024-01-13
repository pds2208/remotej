package evaluation.bank;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * User: soulep
 * Date: Mar 28, 2008
 * Time: 4:31:42 PM
 */
public class BankJMS implements MessageListener {

    private double balance;

   public BankJMS() {
   }

   public void debit(double amount) {
      balance -= amount;
   }

   public void credit(double amount) {
      balance += amount;
   }

   public void create() {
      balance = 0;
   }

   public void open() {
      // open balance file
   }

   public void close() {
      // close balance file
   }

   public double getBalance() {
      return balance;
   }

   public static void main(String[] args) throws NamingException, JMSException {
        BankJMS d = new BankJMS();
        d.initialize();
    }

   private void initialize() throws JMSException, NamingException {
      Properties props = new Properties();
      props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
      props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
      props.setProperty("queue.destination", "TEST");
      Context ctx = new InitialContext(props);
      QueueConnectionFactory connectionFactory =
         (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      QueueConnection c = connectionFactory.createQueueConnection();
       Queue destination = (Queue) ctx.lookup("destination");
       Session session = c.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer requestConsumer = session.createConsumer(destination);
      requestConsumer.setMessageListener(this);
      c.start();
   }

   public void onMessage(Message message) {
      // Need to decide what the message means and what method to call
   }
}
