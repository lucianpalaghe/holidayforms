package ro.pss.holidayforms.domain;

import lombok.Getter;
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
    @Getter
	private User approver;

	@ManyToOne
	@Setter
	@Getter
	private HolidayRequest request;

	@Enumerated(EnumType.STRING)
	@Getter
	private Status status;

	public ApprovalRequest(User approver, Status status) {
		this.approver = approver;
		this.status = status;
	}

	public enum Status {
		NEW, APPROVED, DENIED, POSTPONED
	}

	public void approve() {
		status = ApprovalRequest.Status.APPROVED;
	}

	public void deny() {
		status = ApprovalRequest.Status.DENIED;
	}

	public boolean isApproved() {
		return status == ApprovalRequest.Status.APPROVED;
	}
}