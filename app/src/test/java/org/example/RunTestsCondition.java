import java.io.InputStream;
import java.util.Properties;

public class RunTestsCondition {

    public static boolean isTestsEnabled() {
        try (InputStream input = RunTestsCondition.class.getClassLoader().getResourceAsStream("test-config.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                return Boolean.parseBoolean(prop.getProperty("runTestsForMainFunction", "false"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

