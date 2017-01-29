package de.headshotharp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import de.headshotharp.web.util.ServerOnlineChecker;

@SpringBootApplication
public class Application
{
	private static ConfigurableApplicationContext context;

	public static void main(String[] args)
	{
		start();
	}

	public static void start()
	{
		context = SpringApplication.run(Application.class);
		ServerOnlineChecker.startChecker();
	}

	public static void stop()
	{
		context.stop();
	}
}
