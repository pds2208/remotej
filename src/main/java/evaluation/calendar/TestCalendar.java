package evaluation.calendar;

/**
 * User: soulep
 * Date: Mar 27, 2008
 * Time: 9:15:39 PM
 */
public class TestCalendar {

   public static void main(String[] args) {
      TestCalendar c = new TestCalendar();
      c.doIt();
      System.exit(0);
   }

   private void doIt() {
      Appointment a = new Appointment();
      a.addPerson("paul");
      a.setLocation("home");
      Calendar c = new Calendar();
      c.addAppointment(a);
      Appointment[] all = c.getAppointments();
      System.out.println("Received " + all.length + " appointments.");
      for (int i = 0; i < all.length; i++) {
         System.out.println("location: " + all[i].getLocation());
      }
   }
}
