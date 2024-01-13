package evaluation.calendar;

import java.util.Vector;

/**
 * User: soulep
 * Date: Mar 27, 2008
 * Time: 8:44:22 PM
 */
public class Calendar {

   private Vector<Appointment> appointments = new Vector<Appointment>();

   public Calendar() {
   }
   
   public Appointment[] getAppointments() {
      Appointment[] app = new Appointment[appointments.size()];
      for (int i = 0; i < app.length; i++) {
         app[i] = (Appointment) appointments.get(i);   
      }
      return app;
   }

   public void addAppointment(Appointment a) {
      System.err.println("Adding appointment");
      System.err.println("Location: " + a.getLocation());
      appointments.add(a);
   }

   public void deleteAppointment(Appointment a) {
      appointments.remove(a);
   }

   public void open() {
      // open storage
   }

   public void close() {
      // close storage   
   }
}
