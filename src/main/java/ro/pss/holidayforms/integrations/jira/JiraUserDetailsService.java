package ro.pss.holidayforms.integrations.jira;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ro.pss.holidayforms.domain.Role;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.domain.repo.UserRoleRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class JiraUserDetailsService {
	@Value("${integrations.jira.apiKey}")
	private String apiKey;
	@Value("${integrations.jira.apiUsername}")
	private String apiUsername;
	@Value("${integrations.jira.apiUrl}")
	private String jiraUsersApiUrl;
	@Value("${skipIntegrationInit}")
	private boolean skipIntegrationInit;
	@Value("${usersWithHrRole}")
	private String usersWithHrRole;
	@Value("${usersWithAdminRole}")
	private String usersWithAdminRole;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private UserRoleRepository userRoleRepository;

	@PostConstruct
	public void populateUserRoles() {
		Set<Role> rolesToSave = new HashSet<>();
		for(Role.RoleName role: Role.RoleName.values()) {
			if(!userRoleRepository.findByName(role).isPresent()) {
				rolesToSave.add(new Role(role));
			}
		}
		userRoleRepository.saveAll(rolesToSave);
	}

	@PostConstruct
	public void loadAllJiraUsers() throws IOException {
		if (skipIntegrationInit) {
			return;
		}

		log.info(String.format("Preparing to load all JIRA users from: %s", jiraUsersApiUrl));
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBasicAuth(apiUsername, apiKey);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(jiraUsersApiUrl)
				.queryParam("username", "")
				.queryParam("startAt", 0)
				.queryParam("maxResults", 1000);
		String getUrl = builder.toUriString();

		HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

		log.info(String.format("GETting JSON from: %s", getUrl));
		ResponseEntity<String> response = restTemplate.exchange(getUrl, HttpMethod.GET, httpEntity, String.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.error(String.format("Response from JIRA is: %s %s", response.getStatusCode(), response.getBody()));
			return;
		}
		log.info(String.format("Response from %s: %s", jiraUsersApiUrl, response));

//		File resource = new ClassPathResource("pss_users_jira.json").getFile();
//		String jiraDetails = new String(Files.readAllBytes(resource.toPath()));
		populateUsersWithJiraDetails(response.getBody());
	}

	private void populateUsersWithJiraDetails(String jsonDetails) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		List<JiraUserDetails> jiraUserList = objectMapper.readValue(jsonDetails, new TypeReference<List<JiraUserDetails>>() {
		});
		jiraUserList = jiraUserList.stream()
				.filter(u -> u.getAccountType().equals("atlassian"))
				.filter(u -> !u.getKey().startsWith("addon"))
				.filter(u -> !u.getKey().startsWith("pss"))
				.collect(toList());
		Role defaultRole = userRoleRepository.findByName(Role.RoleName.USER).get();
		List<User> userList = jiraUserList.stream()
				.map(User::new)
				.collect(toList());
		userList.forEach(u -> u.getRoles().add(defaultRole));
		for(String user:usersWithHrRole.split(",")) {
			userList.stream().filter(u -> u.getEmail().equalsIgnoreCase(user)).findFirst().get().getRoles().add(userRoleRepository.findByName(Role.RoleName.HR).get());
		}
		for(String user:usersWithAdminRole.split(",")) {
			userList.stream().filter(u -> u.getEmail().equalsIgnoreCase(user)).findFirst().get().getRoles().add(userRoleRepository.findByName(Role.RoleName.ADMIN).get());
		}
		userRepo.saveAll(userList);
	}
}
