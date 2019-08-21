package ro.pss.holidayforms.integrations.tempo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleResponse {
	@JsonProperty("results")
	private List<TempoDay> tempoDays = null;

	@Data
	public static class TempoDay {
		@JsonProperty("date")
		private String date;
		@JsonProperty("type")
		private String type;
		@JsonProperty("holiday")
		private Holiday holiday;
	}

	@Data
	public static class Holiday {
		@JsonProperty("name")
		private String name;
		@JsonProperty("description")
		private String description;
	}
}
