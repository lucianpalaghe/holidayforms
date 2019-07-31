package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
public class HolidayRequest {
	@Id
	@GeneratedValue
	@Getter
	private Long id;
	@Getter
	@Setter
	private String requester; //User
	@Getter
	@Setter
	private LocalDate dateFrom;
	@Getter
	@Setter
	private LocalDate dateTo;
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private HolidayType type;
	@Getter
	@Setter
	private LocalDate creationDate;
	@Getter
	@Setter
	private String replacer; //User
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<Approval> approvals = new ArrayList<>();

	public void addApproval(Approval approval) {
		approvals.add(approval);
	}
}
