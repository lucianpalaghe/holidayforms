package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.integrations.jira.JiraUserDetails;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

	@Getter
	@Setter

	private String clockingUid;

	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	private Set<Role> roles = new HashSet<>();

	public enum Role {
		USER, TEAM_LEADER, HR, PROJECT_MANGER, ADMIN
	}

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
		this.roles = new HashSet();
		this.name = jiraDetails.getDisplayName();
		this.department = "IT"; // TODO: find out where to get this from
		this.photo = jiraDetails.getAvatarUrls().getOrDefault("48x48", "");
		this.availableVacationDays = 21; // TODO: find out where to get this from
	}

	@Override
	public String toString() {
		return name;
	}

}
