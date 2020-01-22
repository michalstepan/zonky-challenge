package cz.michalstepan.zonky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class ZonkyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZonkyApplication.class, args);
	}


}
