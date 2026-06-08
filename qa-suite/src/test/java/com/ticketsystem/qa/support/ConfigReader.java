package com.ticketsystem.qa.support;

import java.io.InputStream;
import java.util.Properties;

/**
 * Loads config.properties from the classpath once and exposes typed getters.
 * System properties (-Dkey=value on the Maven command line) override file values.
 */
public final class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new RuntimeException("config.properties not found on classpath");
            }
            PROPS.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private ConfigReader() {}

    public static String get(String key) {
        // System property takes precedence so we can override at the CLI
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.isBlank()) return sysVal;
        String val = PROPS.getProperty(key);
        if (val == null) {
            throw new RuntimeException("Missing config key: " + key);
        }
        return val;
    }

    public static String uiBaseUrl()      { return get("ui.baseUrl"); }
    public static String apiBaseUrl()     { return get("api.baseUrl"); }
    public static String adminEmail()     { return get("admin.email"); }
    public static String adminPassword()  { return get("admin.password"); }
    public static String hrAdminEmail()   { return get("hr.admin.email"); }
    public static String hrAdminPass()    { return get("hr.admin.password"); }
    public static String employeeEmail()  { return get("employee.email"); }
    public static String employeePass()   { return get("employee.password"); }
    public static String browser()        { return get("browser"); }
    public static boolean headless()      { return Boolean.parseBoolean(get("headless")); }
    public static int implicitWait()      { return Integer.parseInt(get("implicit.wait")); }
    public static int explicitWait()      { return Integer.parseInt(get("explicit.wait")); }
}
