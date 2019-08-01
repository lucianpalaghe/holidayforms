package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.gui.utils.DateUtils;

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
	@ManyToOne
	private User requester;

	@Getter
	@Setter
	private LocalDate dateFrom;

	@Getter
	@Setter
	private LocalDate dateTo;

	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private Type type;

	@Getter
	@Setter
	private LocalDate creationDate;

	@Getter
	@Setter
	@ManyToOne
	private User replacer;

	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<ApprovalRequest> approvalRequests = new ArrayList<>();

	@Transient
	private int numberOfDays;

	public void addApproval(ApprovalRequest approvalRequest) {
		if (approvalRequest != null) {
			approvalRequests.add(approvalRequest);
			approvalRequest.setRequest(this);
		}
	}

	public long getNumberOfDays() {
		return DateUtils.getWorkingDays(dateFrom, dateTo);
	}

	public enum Type {
		CO, CM, CFP, CPIC
	}
}
