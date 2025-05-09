package com.example.tictactoe.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * This class is responsible for loading configuration properties from a file.
 * It provides methods to load the properties from a default file or a specified file.
 * The loaded properties can be used throughout the application for configuration purposes.
 *
 */

public class ConfigLoader {
    private static final String CONFIG_FILE = "config.properties";

    public static Properties loadConfig() {
        return loadConfig(CONFIG_FILE);
    }

    public static Properties loadConfig(String filename) {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream(filename)) {
            properties.load(input);
            System.out.println("loaded config file successfully from " + filename);
        } catch (FileNotFoundException e) {
            System.err.println("config file not found: " + filename);
        } catch (IOException e) {
            System.err.println("config file could not be read: " + filename);
        }
        return properties;
    }

}
