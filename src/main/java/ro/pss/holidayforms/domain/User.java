package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
public class User {
	@GeneratedValue
	@Getter
	private Long id;

	@Id
	@Getter
	private String email;

	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private Role role;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String department;

	@Getter
	@Setter
	private String photo;

	@Getter
	@Setter
	private int regularVacationDays;

	public static User EMPTY = new User("");

	public User(String name) {
		this.name = name;
	}

	public User(String name, String email, String photo) {
		this.name = name;
		this.email = email;
		this.photo = photo;
	}

	@Override
	public String toString() {
		return name;
	}

	public enum Role {
		USER, HR
	}
}
