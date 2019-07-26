package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atmosphere.config.service.Get;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HolidayRequest {
	@Id
	@GeneratedValue
	@Getter
	private Long id;

	private String requester; //User

	private LocalDate dateFrom;

	private LocalDate dateTo;

	@Enumerated(EnumType.STRING)
	private HolidayType type;

	private LocalDate creationDate;

	private String replacer; //User

	@OneToMany
	private List<Approval> approvals;
}
