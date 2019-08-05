package ro.pss.holidayforms.gui.components.daterange;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.PropertyChangeEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Tag("range-datepicker")
@HtmlImport("bower_components/range-datepicker/range-datepicker.html")
public class DateRangePicker extends AbstractField<DateRangePicker, DateRange> {//PolymerTemplate<DateRangeModel> implements HasValue {
//	private String dateFrom;
//	private String dateTo;

	public DateRangePicker() {
		super(null);
		getElement().setAttribute("force-narrow", "");
		getElement().setAttribute("locale", "ro");
//		getElement().addPropertyChangeListener("dateFrom", this::propertyUpdated);
//		getElement().addPropertyChangeListener("dateTo", this::propertyUpdated);
		setupProperty("dateFrom", "date-from-changed");
		setupProperty("dateTo", "date-to-changed");
//		getElement().addPropertyChangeListener("dateTo", event ->  Notification.show("asd"));
//		getElement().addEventListener("date-from-changed", event -> {
//			getModel();

//			Notification.show("from:" + getModel());
//
//		});
//		getElement().addEventListener("date-to-changed", event -> Notification.show("to:" + getModel().getDateTo()));
	}

	public DateRangePicker(LocalDate startDate, LocalDate endDate) {
		super(null);
		getElement().setAttribute("locale", "ro");
		ZoneId zoneId = ZoneId.systemDefault();
		getElement().setAttribute("month", String.valueOf(startDate.getMonthValue()));
		getElement().setAttribute("year", String.valueOf(startDate.getYear()));
		getElement().setAttribute("min", String.valueOf(Math.toIntExact(startDate.atStartOfDay(zoneId).toEpochSecond())));
		getElement().setAttribute("max", String.valueOf(Math.toIntExact(endDate.atStartOfDay(zoneId).toEpochSecond())));
		setupProperty("dateFrom", "date-from-changed");
		setupProperty("dateTo", "date-to-changed");
	}

	@Override
	protected void setModelValue(DateRange newModelValue, boolean fromClient) {
		super.setModelValue(newModelValue, fromClient);
	}

	private void setupProperty(String name, String event) {
		Element element = getElement();

		element.synchronizeProperty(name, event);
		element.addPropertyChangeListener(name, this::propertyUpdated);
	}

	private void propertyUpdated(PropertyChangeEvent event) {
		Notification.show("" + LocalDate.ofInstant(Instant.ofEpochSecond(1504994400), ZoneId.systemDefault()));
		Element element = getElement();

		int dateFromUi = element.getProperty("dateFrom", -1);
		int dateToUi = element.getProperty("dateTo", -1);

		if (dateFromUi != -1 && dateToUi != -1) {
			LocalDate from = LocalDate.ofInstant(Instant.ofEpochSecond(dateFromUi), ZoneId.systemDefault());
			LocalDate to = LocalDate.ofInstant(Instant.ofEpochSecond(dateToUi), ZoneId.systemDefault());
			DateRange r = new DateRange(from, to);
			setModelValue(r, event.isUserOriginated());
		}
	}

	@Override
	protected void setPresentationValue(DateRange value) {
		Element element = getElement();

		if (value == null) {
			element.removeProperty("dateFrom");
			element.removeProperty("dateTo");
		} else {
			//Math.toIntExact(modelValue.toInstant().getEpochSecond());
			element.setProperty("dateFrom", Math.toIntExact(value.getDateFrom().toEpochDay()));
			element.setProperty("dateTo", Math.toIntExact(value.getDateTo().toEpochDay()));
		}
	}
}
