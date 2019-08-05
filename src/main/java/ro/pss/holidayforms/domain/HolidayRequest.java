package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.gui.components.daterange.DateRange;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.Month;
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

	@Transient
	@Getter
	private DateRange range;

	@Getter
	@OneToOne(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private SubstitutionRequest substitutionRequest;

	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<ApprovalRequest> approvalRequests = new ArrayList<>();

	@Transient
	private int numberOfDays;

	public void setRange(DateRange range) {
		if (range != null) {
			this.range = range;
			this.dateFrom = range.getDateFrom();
			this.dateTo = range.getDateTo();
		}
	}

	public void addSubstitute(User substitute) {
		if (substitute != null) {
			substitutionRequest = new SubstitutionRequest(substitute, SubstitutionRequest.Status.NEW);
			substitutionRequest.setRequest(this);
		}
	}

	public void addApproval(ApprovalRequest approvalRequest) {
		if (approvalRequest != null) {
			approvalRequests.add(approvalRequest);
			approvalRequest.setRequest(this);
		}
	}

	public int getNumberOfDays() {
		return DateUtils.getWorkingDays(dateFrom, dateTo);
	}

	public User getSubstitute() {
		if (substitutionRequest == null) {
			return null;
		}
		return substitutionRequest.getSubstitute();
	}

	public boolean isCO() {
		return type == HolidayRequest.Type.CO;
	}

	public Month getStartingMonthOfHoliday() {
		return dateFrom.getMonth();
	}

	public enum Type {
		CO, CM, CFP, CPIC
	}
}
