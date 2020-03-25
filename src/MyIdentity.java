
import java.util.Properties;

public class MainClass {

    public static void setIdentity(Properties prop) {
      prop.setProperty("database", "csci3901");
      prop.setProperty("user", "singh4");  // Replace with your CSID for bluenose
      prop.setProperty("password", "B00853930"); // Replace with your Banner ID (which is your mysql password)
    }
}
