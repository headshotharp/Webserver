package de.headshotharp.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class StaticResourceConfiguration extends WebMvcConfigurerAdapter
{
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		super.addResourceHandlers(registry);
		registry.addResourceHandler("/external/**").addResourceLocations("file:" + Config.PATH_EXTERNAL);
		registry.addResourceHandler("/skins/**").addResourceLocations("file:" + Config.PATH_SKINS);
	}
}