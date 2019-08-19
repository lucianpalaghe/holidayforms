package ro.pss.holidayforms.integrations.tempo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TempoDay {
	@JsonProperty("date")
	private String date;
	@JsonProperty("type")
	private String type;
	@JsonProperty("holiday")
	private Holiday holiday;
}
