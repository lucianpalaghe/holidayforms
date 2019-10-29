package ro.pss.holidayforms.config.security.jira;

import lombok.Getter;

import java.util.Map;

public class JiraOAuth2UserInfo {
	@Getter
	protected Map<String, Object> attributes;

	public JiraOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public String getId() {
		return (String) attributes.get("sub");
	}

	public String getName() {
		return (String) attributes.get("name");
	}

	public String getEmail() {
		return (String) attributes.get("email");
	}

	public String getUniqueName() {
		return (String) attributes.get("unique_name");
	}

	public String getImageUrl() {
		return (String) attributes.get("picture");
	}
}
