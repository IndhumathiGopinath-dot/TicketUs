package com.ticketsystem.e2e.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration from src/test/resources/config.properties and exposes
 * typed accessors. System properties (-Dkey=value on the mvn command) take
 * precedence over the file, allowing per-environment overrides without
 * editing the file.
 */
public final class ConfigReader {

    private static final Properties PROPS = load();

    private ConfigReader() {}  // utility class

    private static Properties load() {
        Properties p = new Properties();
        try (InputStream in = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on classpath");
            }
            p.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
        return p;
    }

    public static String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;
        String val = PROPS.getProperty(key);
        if (val == null) {
            throw new IllegalArgumentException("Missing config key: " + key);
        }
        return val;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBool(String key) {
        return Boolean.parseBoolean(get(key));
    }

    // Convenience accessors — keeps test code clean of magic strings

    public static String baseUrl()      { return get("base.url"); }
    public static String apiUrl()       { return get("api.url"); }

    public static String itAdminEmail() { return get("admin.it.email"); }
    public static String itAdminPass()  { return get("admin.it.password"); }
    public static String hrAdminEmail() { return get("admin.hr.email"); }
    public static String hrAdminPass()  { return get("admin.hr.password"); }
    public static String employeeEmail(){ return get("employee.email"); }
    public static String employeePass() { return get("employee.password"); }

    public static int explicitWait()    { return getInt("explicit.wait.seconds"); }
    public static int pageLoadTimeout() { return getInt("page.load.timeout.seconds"); }
    public static boolean headless()    { return getBool("headless"); }
}
