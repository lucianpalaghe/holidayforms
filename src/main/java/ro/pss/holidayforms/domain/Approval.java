package ro.pss.holidayforms.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Approval {
	@Id
	@GeneratedValue
	private Long id;

	private String approver; //User

	private ApprovalStatus status;
}
