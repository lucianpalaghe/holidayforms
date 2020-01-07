package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
public class UserPreferences {
	@GeneratedValue
	@Getter
	@Id
	private Long id;

	@Getter
	@Setter
	@OneToOne
	private User employee;

	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private Locale locale;

	@Getter
	@Setter
	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	private Set<EmailOption> emailOptions = new HashSet<>();

	@Getter
	@Setter
	private boolean showNotifications;

	public UserPreferences(User employee, Locale localeOption, Set<EmailOption> emailOptions, boolean showNotifications) {
		this.employee = employee;
		this.locale = localeOption;
		this.emailOptions = emailOptions;
		this.showNotifications = showNotifications;
	}

	public UserPreferences(Locale localeOption, Set<EmailOption> emailOptions, boolean showNotifications) {
		this.locale = localeOption;
		this.emailOptions = emailOptions;
		this.showNotifications = showNotifications;
	}

	public static UserPreferences defaultPreferences() {
		return new UserPreferences(Locale.ro, new HashSet<>(), true);
	}

	public enum Locale {
		ro(new java.util.Locale("ro")), en(java.util.Locale.ENGLISH);
		public java.util.Locale locale;

		Locale(java.util.Locale l) {
			this.locale = l;
		}
	}

	public enum EmailOption {
		ON_NEW_REQUEST, ON_REPLACER_ACTION, ON_APPROVER_ACTION, ON_REQUEST_CHANGED
	}
}
