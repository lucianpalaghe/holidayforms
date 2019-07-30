package ro.pss.holidayforms.domain;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Entity
public class Approval {
	@Id
	@GeneratedValue
	private Long id;

	private String approver; //User

	@Enumerated(EnumType.STRING)
	private ApprovalStatus status;

	public Approval(String approver, ApprovalStatus status) {
		this.approver = approver;
		this.status = status;
	}
}
