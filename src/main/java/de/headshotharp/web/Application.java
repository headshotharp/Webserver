package de.headshotharp.web;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static void main(String[] args) {
		File properties = new File("application.properties");
		if (!properties.exists()) {
			URL url = Application.class.getResource("/default.properties");
			try {
				FileUtils.copyURLToFile(url, properties);
				logger.info("Saved properties file to " + properties.getAbsolutePath());
			} catch (IOException e) {
				logger.error("Error while saving " + properties.getAbsolutePath(), e);
			}
			return;
		}
		SpringApplication.run(Application.class);
	}
}
