package ro.pss.holidayforms.domain.clocking;

import lombok.Getter;
import lombok.Setter;
import ro.pss.holidayforms.domain.User;

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
