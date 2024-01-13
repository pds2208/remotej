package evaluation.calendar;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

/**
 * User: soulep
 * Date: Mar 27, 2008
 * Time: 8:45:25 PM
 */
@SuppressWarnings("serial")
public class Appointment implements Serializable {
   private Date time;
   private String location;
   private Vector<String> people = new Vector<String>();
   
   public Date getTime() {
      return time;
   }

   public void setTime(Date time) {
      this.time = time;
   }

   public String getLocation() {
      System.err.println("Returning location");
      return location;
   }

   public void setLocation(String location) {
      this.location = location;
   }

   public String[] getPeople() {
      return (String[]) people.toArray();
   }

   public void addPerson(String person) {
      people.add(person);
   }

   public void deletePerson(String p) {
      people.remove(p);
   }
}
