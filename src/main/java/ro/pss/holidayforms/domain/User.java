package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.integrations.jira.JiraUserDetails;

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
	private String jiraAccountId;

	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private Role role;

	@Getter
	private String name;

	@Getter
	@Setter
	private String department;

	@Getter
	@Setter
	private String photo;

	@Getter
	@Setter
	private int availableVacationDays;

	public User(String name) {
		this.name = name;
	}

	public User(String name, String email, String jiraAccountId) {
		this.name = name;
		this.email = email;
		this.jiraAccountId = jiraAccountId;
	}

	public User(JiraUserDetails jiraDetails) {
		this.email = jiraDetails.getName().toLowerCase(); // TODO: replace with email after everyone sets their email addresses in JIRA
		this.jiraAccountId = jiraDetails.getAccountId();
		this.role = Role.USER;
		this.name = jiraDetails.getDisplayName();
		this.department = "IT"; // TODO: find out where to get this from
		this.photo = jiraDetails.getAvatarUrls().getOrDefault("48x48", "");
		this.availableVacationDays = 21; // TODO: find out where to get this from
	}

	@Override
	public String toString() {
		return name;
	}

	public enum Role {
		USER, HR, TEAM_LEADER, MANAGER
	}
}
