import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConnection {

    private static Connection connection = null;

    // Loaded once from .env
    private static final String HOST;
    private static final String PORT;
    private static final String DB_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String BASE_URL;

    static {
        Map<String, String> env = loadEnv();
        HOST     = env.getOrDefault("DB_HOST", "localhost");
        PORT     = env.getOrDefault("DB_PORT", "3306");
        DB_NAME  = env.getOrDefault("DB_NAME", "medicine_store1");
        USER     = env.getOrDefault("DB_USER", "root");
        PASSWORD = env.getOrDefault("DB_PASSWORD", "");
        BASE_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/";
    }

    private static Map<String, String> loadEnv() {
        Map<String, String> map = new HashMap<>();
        // Look for .env in the working directory (project root)
        try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key   = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim();
                    map.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: .env file not found, using defaults. (" + e.getMessage() + ")");
        }
        return map;
    }

    public Connection getConnection() {
        if (connection == null) {
            try {
                Connection bootstrap = DriverManager.getConnection(
                    BASE_URL + "?useSSL=false&allowPublicKeyRetrieval=true", USER, PASSWORD);
                initDatabase(bootstrap);
                bootstrap.close();

                connection = DriverManager.getConnection(
                    BASE_URL + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true", USER, PASSWORD);
                System.out.println("Database connection established successfully.");
            } catch (SQLException e) {
                System.err.println("Failed to establish database connection: " + e.getMessage());
            }
        }
        return connection;
    }

    private void initDatabase(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            st.executeUpdate("USE " + DB_NAME);

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS login (" +
                "  username      VARCHAR(50)  PRIMARY KEY," +
                "  password_hash VARCHAR(255) NOT NULL" +
                ")"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Doctor (" +
                "  doctor_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  name      VARCHAR(100) NOT NULL" +
                ")"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Patient (" +
                "  patient_id  INT AUTO_INCREMENT PRIMARY KEY," +
                "  name        VARCHAR(100) NOT NULL," +
                "  age         INT          NOT NULL," +
                "  gender      CHAR(1)      NOT NULL," +
                "  phone       VARCHAR(10)  UNIQUE NOT NULL," +
                "  blood_group VARCHAR(5)   NOT NULL," +
                "  address     VARCHAR(255)" +
                ")"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Medicine (" +
                "  medicine_id    INT AUTO_INCREMENT PRIMARY KEY," +
                "  name           VARCHAR(100)   UNIQUE NOT NULL," +
                "  stock_quantity INT            NOT NULL DEFAULT 0," +
                "  expiry_date    DATE           NOT NULL," +
                "  cost_per_unit  DECIMAL(10,2)  NOT NULL" +
                ")"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Prescription (" +
                "  prescription_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  doctor_id       INT       NOT NULL," +
                "  patient_id      INT       NOT NULL," +
                "  medication      TEXT      NOT NULL," +
                "  date_created    TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (doctor_id)  REFERENCES Doctor(doctor_id)," +
                "  FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)" +
                ")"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Bill (" +
                "  bill_id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  prescription_id INT           NOT NULL," +
                "  patient_id      INT           NOT NULL," +
                "  total_amount    DECIMAL(10,2) NOT NULL," +
                "  date_issued     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (prescription_id) REFERENCES Prescription(prescription_id)," +
                "  FOREIGN KEY (patient_id)      REFERENCES Patient(patient_id)" +
                ")"
            );
            st.executeUpdate("INSERT IGNORE INTO login (username, password_hash) VALUES ('admin', 'admin')");
            st.executeUpdate("INSERT IGNORE INTO Doctor (doctor_id, name) VALUES (1, 'Dr. Sharma')");
            System.out.println("Database and tables ready.");
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close database connection: " + e.getMessage());
            }
        }
    }
}
