package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
public class User {
	@GeneratedValue
	@Getter
	private Long id;

	@Id
	@Getter
	private String email;

	@Getter
	@Setter
	private String name;
}
