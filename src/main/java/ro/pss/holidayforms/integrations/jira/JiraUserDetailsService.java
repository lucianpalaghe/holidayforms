package ro.pss.holidayforms.integrations.jira;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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

	@Autowired
	private UserRepository userRepo;

	//	@PostConstruct
	public void loadAllJiraUsers() throws IOException {
//		log.info(String.format("Preparing to load all JIRA users from: %s", jiraUsersApiUrl));
//		RestTemplate restTemplate = new RestTemplate();
//		HttpHeaders httpHeaders = new HttpHeaders();
//		httpHeaders.setBasicAuth(apiUsername, apiKey);
//
//		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(jiraUsersApiUrl)
//				.queryParam("username", "")
//				.queryParam("startAt", 0)
//				.queryParam("maxResults", 1000);
//		String getUrl = builder.toUriString();
//
//		HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
//
//		log.info(String.format("GETing JSON from: %s", getUrl));
//		ResponseEntity<String> response = restTemplate.exchange(getUrl, HttpMethod.GET, httpEntity, String.class);
//		if(response.getStatusCode() != HttpStatus.OK) {
//			log.error(String.format("Response from JIRA is: %d %s", response.getStatusCode(), response.getBody()));
//			return;
//		}
//		log.info(String.format("Response from %s: %s", jiraUsersApiUrl, response));

		File resource = new ClassPathResource("pss_users_jira.json").getFile();
		String employees = new String(Files.readAllBytes(resource.toPath()));

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		List<JiraUserDetails> jiraUserList = objectMapper.readValue(employees, new TypeReference<List<JiraUserDetails>>() {
		});
		jiraUserList = jiraUserList.stream()
				.filter(u -> u.getAccountType().equals("atlassian"))
				.filter(u -> !u.getKey().startsWith("addon"))
				.filter(u -> !u.getKey().startsWith("pss"))
				.collect(toList());

		List<User> userList = jiraUserList.stream()
				.map(User::new)
				.collect(toList());

		userRepo.saveAll(userList);
//		objectMapper.writeValue(new File("output_employees.json"), jiraUserList);
	}
}
