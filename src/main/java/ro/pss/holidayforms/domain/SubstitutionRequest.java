package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Entity
public class SubstitutionRequest {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@Getter
	private User substitute;

	@ManyToOne
	@Setter
	@Getter
	private HolidayRequest request;

	@Enumerated(EnumType.STRING)
	@Getter
	private Status status;

	public SubstitutionRequest(User substitute, Status status, HolidayRequest request) {
		this.substitute = substitute;
		this.status = status;
		this.request = request;
	}

	public void approve() {
		status = Status.APPROVED;
	}

	public void deny() {
		status = Status.DENIED;
	}

	public boolean isApproved() {
		return status == Status.APPROVED;
	}

	public boolean isNew() {
		return status == Status.NEW;
	}

	public enum Status {
		NEW, APPROVED, DENIED
	}
}