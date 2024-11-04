package com.clarkrpc.config;

/**
 * ClassName: ConfigReader
 * Package: com.clarkrpc.config
 */
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfigReader {
    private Properties properties = new Properties();
    public ConfigReader(String filePath) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (input == null) {
                log.error("Sorry ,unable to find[{}]",filePath);
                return;
            }
            // Load the properties file
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getZkAddress() {
        return properties.getProperty("zookeeper.address");
    }
}
