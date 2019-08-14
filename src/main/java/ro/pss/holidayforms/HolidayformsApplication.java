package ro.pss.holidayforms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class HolidayformsApplication {
	public static void main(String[] args) {
		SpringApplication.run(HolidayformsApplication.class, args);
	}
}
