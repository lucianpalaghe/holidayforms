package ro.pss.holidayforms.integrations.jira;

import lombok.Data;

import java.util.Map;

@Data
public class JiraUserDetails {
	private String self;
	private String key;
	private String accountId;
	private String accountType;
	private String name;
	private String emailAddress;
	private String displayName;
	private boolean active;
	private String timeZone;
	private String locale;
	private Map<String, String> avatarUrls;
}
