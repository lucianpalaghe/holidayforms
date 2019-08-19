package ro.pss.holidayforms.integrations.tempo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleResponse {
	@JsonProperty("results")
	private List<TempoDay> tempoDays = null;
}
