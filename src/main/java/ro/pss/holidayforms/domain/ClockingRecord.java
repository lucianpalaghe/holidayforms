package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ClockingRecord {
	@Id
	@GeneratedValue
	Long id;
	@ManyToOne
	User employee;
	LocalDateTime dateTime;
}
