package ro.pss.holidayforms.integrations.tempo;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ro.pss.holidayforms.domain.HolidayRequest;

import java.time.LocalDate;

@Service
@Slf4j
public class TempoService {
	@Value("${integrations.tempo.apiKey}")
	private String apiKey;

	@Value("${integrations.tempo.apiUrl}")
	private String tempoWorklogsApiUrl;

	int EIGHT_OURS = 28800;

	public void postHolidayWorklog(HolidayRequest request) {
		log.info(String.format("Preparing Tempo worklog for: %s", request));
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.setBearerAuth(apiKey);

		for (int i = 0; i < request.getNumberOfDays(); i++) {
			String worklogJson = buildWorklogJson(request, request.getDateFrom().plusDays(i));

			log.info(String.format("POSTing JSON to: %s \n data: %s", tempoWorklogsApiUrl, worklogJson));
			HttpEntity<String> httpEntity = new HttpEntity<>(worklogJson, httpHeaders);
			String response = restTemplate.postForObject(tempoWorklogsApiUrl, httpEntity, String.class);
			log.info(String.format("Response from %s: %s", tempoWorklogsApiUrl, response));
		}
	}

	private String buildWorklogJson(HolidayRequest request, LocalDate worklogDate) {
		JSONObject holidayWorklog = new JSONObject();
		holidayWorklog.put("issueKey", "HOLIDAY-2");
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