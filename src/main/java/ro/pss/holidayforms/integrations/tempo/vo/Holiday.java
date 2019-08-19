package ro.pss.holidayforms.integrations.tempo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Holiday {
	@JsonProperty("name")
	private String name;
	@JsonProperty("description")
	private String description;
}
