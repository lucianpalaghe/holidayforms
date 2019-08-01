package ro.pss.holidayforms.domain;

import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Entity
public class ApprovalRequest {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private User approver;

	@ManyToOne
	@Setter
	private HolidayRequest request;

	@Enumerated(EnumType.STRING)
	private Status status;

	public ApprovalRequest(User approver, Status status) {
		this.approver = approver;
		this.status = status;
	}

	public enum Status {
		NEW, APPROVED, DENIED, POSTPONED
	}
}