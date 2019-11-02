package ro.pss.holidayforms.integrations.tempo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.NonWorkingDay;
import ro.pss.holidayforms.domain.repo.NonWorkingDayRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.*;

@Service
@Slf4j
public class TempoService {
	@Value("${integrations.tempo.apiKey}")
	private String apiKey;
	@Value("${integrations.tempo.apiUrl}")
	private String tempoApiUrl;
	@Value("${skipIntegrationInit}")
	private boolean skipIntegrationInit;
	@Autowired
	private NonWorkingDayRepository nonWorkingDayRepo;

	@PostConstruct
	private void loadNonWorkingDays() throws IOException {
		if (skipIntegrationInit) {
			return;
		}

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.setBearerAuth(apiKey);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tempoApiUrl)
				.path("user-schedule")
				.queryParam("from", LocalDate.now().with(firstDayOfYear()))
				.queryParam("to", LocalDate.now().with(lastDayOfYear()));
		String getUrl = builder.toUriString();

		HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

		log.info(String.format("Loading Tempo non-working days from: %s", getUrl));
		ResponseEntity<String> response = restTemplate.exchange(getUrl, HttpMethod.GET, httpEntity, String.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.error(String.format("Response from Tempo is: %s %s", response.getStatusCode(), response.getBody()));
			return;
		}
		log.info(String.format("Response from %s: %s", getUrl, response));

		populateNonWorkingDays(response.getBody());
	}

	private void populateNonWorkingDays(String responseJson) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		ScheduleResponse response = objectMapper.readValue(responseJson, new TypeReference<ScheduleResponse>() {
		});
		List<ScheduleResponse.TempoDay> tempoDays = response.getTempoDays().stream()
				.filter(r -> !r.getType().equals("WORKING_DAY"))
				.filter(r -> !r.getType().equals("NON_WORKING_DAY")) // keep only HOLIDAY and HOLIDAY_AND_NON_WORKING_DAY
				.collect(toList());

		List<NonWorkingDay> nonWorkingDayList = tempoDays.stream()
				.map(NonWorkingDay::new)
				.collect(toList());

		nonWorkingDayRepo.saveAll(nonWorkingDayList);
	}

	public List<NonWorkingDay> getNonWorkingDays() {
		return nonWorkingDayRepo.findAll();
	}

	public void postHolidayWorklog(HolidayRequest request) {
		log.info(String.format("Preparing Tempo worklog for: %s", request));
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.setBearerAuth(apiKey);

		String worklogsUrl = tempoApiUrl + "worklogs";
		for (int i = 0; i < request.getNumberOfDays(); i++) {
			String worklogJson = buildWorklogJson(request, request.getDateFrom().plusDays(i));

			log.info(String.format("POSTing JSON to: %s \n data: %s", worklogsUrl, worklogJson));
			HttpEntity<String> httpEntity = new HttpEntity<>(worklogJson, httpHeaders);
			String response = restTemplate.postForObject(worklogsUrl, httpEntity, String.class);
			log.info(String.format("Response from %s: %s", worklogsUrl, response));
		}
	}

	private String buildWorklogJson(HolidayRequest request, LocalDate worklogDate) {
		JSONObject holidayWorklog = new JSONObject();
		holidayWorklog.put("issueKey", "HOLIDAY-2");
		int EIGHT_OURS = 28800;
		holidayWorklog.put("timeSpentSeconds", EIGHT_OURS);
		holidayWorklog.put("startDate", worklogDate);
		holidayWorklog.put("startTime", "09:00:00");
		holidayWorklog.put("description", request.getType() + "\n" + request.getComments());
		holidayWorklog.put("authorAccountId", request.getRequester().getJiraAccountId());
		JSONArray array = new JSONArray();
		JSONObject account = new JSONObject();
		account.put("key", "_Account_");
		account.put("value", "PSSINTHOL");
		JSONObject workType = new JSONObject();
		workType.put("key", "_Worktype_");
		workType.put("value", "7");
		array.put(account);
		array.put(workType);

		holidayWorklog.put("attributes", array);
		return holidayWorklog.toString();
	}
}