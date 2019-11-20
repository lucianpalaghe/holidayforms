package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ro.pss.holidayforms.gui.components.daterange.DateRange;
import ro.pss.holidayforms.gui.components.daterange.utils.DateUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.*;

import static java.util.Comparator.comparing;

@Entity
@NoArgsConstructor
@ToString
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
	private String comments;

	@Getter
	@Setter
	private LocalDate creationDate;

	@Transient
	private DateRange range;

	@Getter
	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<SubstitutionRequest> substitutionRequests = new TreeSet<>(comparing(s -> s.getSubstitute().getName()));

	@Getter
	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ApprovalRequest> approvalRequests = new TreeSet<>(comparing(s -> s.getApprover().getName()));

	public DateRange getRange() {
		if (dateFrom != null && dateTo != null) {
			return new DateRange(dateFrom, dateTo);
		}
		return null;
	}

	public void setRange(DateRange range) {
		if (range != null) {
			this.range = range;
			this.dateFrom = range.getDateFrom();
			this.dateTo = range.getDateTo();
		}
	}

	public void addSubstitute(User substitute) {
		if (substitute != null) {
			SubstitutionRequest s = new SubstitutionRequest(substitute, SubstitutionRequest.Status.NEW, this);
			substitutionRequests.add(s);
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

	public boolean isStillEditable() {
		return substitutionRequests.stream().allMatch(SubstitutionRequest::isNew);
	}

	public boolean isCO() {
		return type == HolidayRequest.Type.CO;
	}

	public Month getStartingMonthOfHoliday() {
		return dateFrom.getMonth();
	}

	public Set<User> getSubstitutes() {
		return substitutionRequests.stream().map(s -> s.getSubstitute()).collect(Collectors.toCollection(() -> new TreeSet<>(comparing(User::getName))));
	}

	public void setSubstitutes(Set<User> newSubstitutes) {
		if (substitutionRequests.size() == 0) {
			substitutionRequests.addAll(newSubstitutes.stream()
					.map(user -> new SubstitutionRequest(user, SubstitutionRequest.Status.NEW, this))
					.collect(Collectors.toSet()));
		} else {
			List<User> existingSubstitutes = substitutionRequests.stream().map(SubstitutionRequest::getSubstitute).collect(Collectors.toList());
			for (User user : newSubstitutes) {
				if (!existingSubstitutes.contains(user)) {
					substitutionRequests.add(new SubstitutionRequest(user, SubstitutionRequest.Status.NEW, this));
				}
			}
			for (User user : existingSubstitutes) {
				if (!newSubstitutes.contains(user)) {
					substitutionRequests.removeIf(r -> r.getSubstitute().getEmail().equalsIgnoreCase(user.getEmail()));
				}
			}
		}
	}

	public enum Type {
		CO, CM, CFP, CIC, CP, R
	}
}
