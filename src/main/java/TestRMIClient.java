import com.paul.Address;
import com.paul.UKAddress;

public class TestRMIClient {

    private TestRMIClient() {
    }

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {
            UKAddress uk = new UKAddress();
            int[] i = new int[2];
            i[0] = 25;
            i[1] = 26;
            double[] d = uk.printVersion(i);
            System.out.println("Version: " + d[0]);
            System.out.println("Version: " + d[1]);
            Address a = new Address();
            System.out.println("Address: " + a.getAddress(" ", " "));
            //RMIClient c = new RMIClient();
            //String response = c.getAddress("paul", "fred");
            //System.out.println("response: "  + response);
            //c.printVersion();
            //c.dummy(new Harry());
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            //e.printStackTrace();
        }
        System.exit(1);
    }
}
