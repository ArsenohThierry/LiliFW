package lilifw.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    public static final String UPLOAD_PATH;

    static {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("lilifw.properties")) {
            if (in != null)
                props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Erreur chargement lilifw.properties", e);
        }
        UPLOAD_PATH = props.getProperty("upload_path", "/uploads");
    }
}
