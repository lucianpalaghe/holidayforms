package ro.pss.holidayforms.domain;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Approval {
	@Id
	@GeneratedValue
	private Long id;

	private String approver; //User

	private ApprovalStatus status;
}
