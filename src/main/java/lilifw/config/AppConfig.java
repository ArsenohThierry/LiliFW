package lilifw.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    public static final String UPLOAD_PATH;
    public static final String DB_URL;
    public static final String DB_USER;
    public static final String DB_PASSWORD;
    public static final String DB_DRIVER;
    static {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("lilifw.properties")) {
            if (in != null)
                props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Erreur chargement lilifw.properties", e);
            //TODO : ne pas faire une erreur mais logger : Pas de fichhier de configuration
        }
        UPLOAD_PATH = props.getProperty("upload_path", "/uploads");
        DB_URL = props.getProperty("db_url", "");
        DB_USER = props.getProperty("db_user", "");
        DB_PASSWORD = props.getProperty("db_password", "");
        DB_DRIVER = props.getProperty("db_driver", "");
    }
}
