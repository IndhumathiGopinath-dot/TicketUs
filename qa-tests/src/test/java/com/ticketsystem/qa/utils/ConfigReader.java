package com.ticketsystem.qa.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final Properties P = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) throw new RuntimeException("config.properties not on classpath");
            P.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        String v = System.getProperty(key, P.getProperty(key));
        if (v == null) throw new RuntimeException("Missing config key: " + key);
        return v;
    }
    public static String get(String key, String dflt) { return System.getProperty(key, P.getProperty(key, dflt)); }
    public static int getInt(String key) { return Integer.parseInt(get(key)); }
    public static boolean getBoolean(String key) { return Boolean.parseBoolean(get(key)); }
}
