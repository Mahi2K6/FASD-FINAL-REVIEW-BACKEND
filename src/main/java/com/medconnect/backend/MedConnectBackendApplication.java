package com.medconnect.backend;

import com.medconnect.backend.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class MedConnectBackendApplication {

	public static void main(String[] args) {
		org.springframework.context.ApplicationContext ctx = SpringApplication.run(MedConnectBackendApplication.class, args);
		String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
		String activeProfile = activeProfiles.length > 0 ? java.util.Arrays.toString(activeProfiles) : "default";
		String port = ctx.getEnvironment().getProperty("server.port");
		System.out.println("MedConnect Backend Running");
		System.out.println("Environment: " + activeProfile);
		System.out.println("Port: " + port);
	}

}
