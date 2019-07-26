package ro.pss.holidayforms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ro.pss.holidayforms.domain.HolidayRequest;


@SpringBootApplication
public class HolidayformsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HolidayformsApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(HolidayRequestRepository repository) {
		return (args) -> {
//			repository.save(new Employee("Bill", "Gates"));
//			repository.save(new Employee("Mark", "Zuckerberg"));
//			repository.save(new Employee("Sundar", "Pichai"));
//			repository.save(new Employee("Jeff", "Bezos"));
		};
	}

}
