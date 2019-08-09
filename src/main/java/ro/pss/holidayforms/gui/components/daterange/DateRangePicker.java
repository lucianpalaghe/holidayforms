package ro.pss.holidayforms.gui.components.daterange;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.PropertyChangeEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Tag("range-datepicker")
@HtmlImport("bower_components/range-datepicker/range-datepicker.html")
@StyleSheet("rangepicker-validation.css")
public class DateRangePicker extends AbstractField<DateRangePicker, DateRange> implements HasValidation {
	private List<DateRangeSelectedLisetner> listeners = new ArrayList<>();

	private ZoneId zoneId = ZoneId.systemDefault();

	public DateRangePicker() {
		super(null);
//		getElement().setAttribute("force-narrow", "");
		getElement().setAttribute("locale", "ro");
		getElement().addPropertyChangeListener("dateFrom", "date-from-changed", this::propertyUpdated);
		getElement().addPropertyChangeListener("dateTo", "date-to-changed", this::propertyUpdated);
	}

	public void setForceNarrow(boolean forceNarrow) {
		if (forceNarrow) {
			getElement().setAttribute("force-narrow", "");
		} else {
			getElement().removeAttribute("force-narrow");
		}
	}
//
//	public DateRangePicker(LocalDate startDate, LocalDate endDate) {
//		super(null);
//		getElement().setAttribute("locale", "ro");
//		getElement().setAttribute("month", String.valueOf(startDate.getMonthValue()));
//		getElement().setAttribute("year", String.valueOf(startDate.getYear()));
//		getElement().setAttribute("min", String.valueOf(Math.toIntExact(startDate.atStartOfDay(zoneId).toEpochSecond())));
//		getElement().setAttribute("max", String.valueOf(Math.toIntExact(endDate.atStartOfDay(zoneId).toEpochSecond())));
//		getElement().addPropertyChangeListener("dateFrom", "date-from-changed", this::propertyUpdated);
//		getElement().addPropertyChangeListener("dateTo", "date-to-changed", this::propertyUpdated);
//	}

	private void propertyUpdated(PropertyChangeEvent event) {
		Element element = getElement();

		int dateFromUi = element.getProperty("dateFrom", -1);
		int dateToUi = element.getProperty("dateTo", -1);

		if (dateFromUi != -1 && dateToUi != -1) {
			LocalDate from = LocalDate.ofInstant(Instant.ofEpochSecond(dateFromUi), ZoneId.systemDefault());
			LocalDate to = LocalDate.ofInstant(Instant.ofEpochSecond(dateToUi), ZoneId.systemDefault());
			DateRange r = new DateRange(from, to);
			setModelValue(r, event.isUserOriginated());
			for (DateRangeSelectedLisetner l : listeners) {
				l.rangeSelected(r);
			}
		}
	}

	public void addListener(DateRangeSelectedLisetner listener) {
		listeners.add(listener);
	}

	@Override
	protected void setModelValue(DateRange newModelValue, boolean fromClient) {
		super.setModelValue(newModelValue, fromClient);
	}

	@Override
	public void setValue(DateRange value) {
		if (value != null && !value.isObjectValid()) {
			super.setValue(null);
			setPresentationValue(null);
			return;
		}
		super.setValue(value);
		setPresentationValue(value);
	}

	@Override
	protected void setPresentationValue(DateRange value) {
		Element element = getElement();

		if (value == null) {
			element.removeProperty("dateFrom");
			element.removeProperty("dateTo");

		} else if (value.getDateFrom() != null && value.getDateTo() != null) {
			element.setProperty("dateFrom", String.valueOf(Math.toIntExact(value.getDateFrom().atStartOfDay(zoneId).toEpochSecond())));
			element.setProperty("dateTo", String.valueOf(Math.toIntExact(value.getDateTo().atStartOfDay(zoneId).toEpochSecond())));
		}
	}

	@Override
	public void setErrorMessage(String errorMessage) {
		if (errorMessage != null) {
			getElement().setAttribute("errorMessage", errorMessage);
		}
	}

	@Override
	public String getErrorMessage() {
		return getElement().getAttribute("errorMessage");
	}

	@Override
	public void setInvalid(boolean invalid) {
		if (invalid) {
			getElement().setAttribute("invalid", "");
		} else {
			getElement().removeAttribute("invalid");
		}
	}

	@Override
	public boolean isInvalid() {
		return isInvalidBoolean();
	}

	@Synchronize(property = "invalid", value = "invalid-changed")
	private boolean isInvalidBoolean() {
		return getElement().getProperty("invalid", false);
	}
}
