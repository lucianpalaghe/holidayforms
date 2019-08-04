package ro.pss.holidayforms.gui.components.daterange;

import com.vaadin.flow.templatemodel.ModelEncoder;

import java.time.Instant;
import java.util.Date;

public class DateToIntegerEncoder implements ModelEncoder<Date, Integer> {

	@Override
	public Integer encode(Date modelValue) {
		return Math.toIntExact(modelValue.toInstant().getEpochSecond());

	}

	@Override
	public Date decode(Integer presentationValue) {
		return Date.from(Instant.ofEpochSecond(presentationValue));
	}

}
