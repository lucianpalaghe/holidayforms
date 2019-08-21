package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.integrations.tempo.ScheduleResponse;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
public class NonWorkingDay {
	@Id
	@GeneratedValue
	@Getter
	private Long id;

	@Getter
	@Setter
	private LocalDate date;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String type;

	public NonWorkingDay(ScheduleResponse.TempoDay tempoDay) {
		this.date = LocalDate.parse(tempoDay.getDate());
		if (tempoDay.getHoliday() != null) {
			this.name = tempoDay.getHoliday().getName();
		}
		this.type = tempoDay.getType();
	}
}
