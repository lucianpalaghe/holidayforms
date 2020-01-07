package ro.pss.holidayforms.gui.request;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.gatanaso.MultiselectComboBox;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.daterange.DateRange;
import ro.pss.holidayforms.gui.components.daterange.DateRangePicker;
import ro.pss.holidayforms.service.HolidayRequestService;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.*;

@SpringComponent
@UIScope
public class HolidayRequestEditor extends VerticalLayout implements KeyNotifier {
	private final HolidayRequestService requestsService;
	private final DateRangePicker dateRange = new DateRangePicker();
	private final ComboBox<HolidayRequest.Type> type = new ComboBox<>();
	private final TextArea comments = new TextArea();
	private final DatePicker creationDate = new DatePicker();
	private final Button btnDelete = new Button(MessageRetriever.get("btnDeleteLbl"), VaadinIcon.TRASH.create());
	private final Binder<HolidayRequest> binder = new Binder<>(HolidayRequest.class);
	private final MultiselectComboBox<User> substitutes = new MultiselectComboBox<>();
	private HolidayRequest holidayRequest;
	private ChangeHandler changeHandler;

	@Autowired
	public HolidayRequestEditor(HolidayRequestService requestsService) {
		this.requestsService = requestsService;

		DatePicker.DatePickerI18n dp18n = new DatePicker.DatePickerI18n();
		dp18n.setCalendar(MessageRetriever.get("calendarName"));
		dp18n.setFirstDayOfWeek(0);
		dp18n.setCancel(MessageRetriever.get("cancelName"));
		dp18n.setClear(MessageRetriever.get("clearName"));
		dp18n.setToday(MessageRetriever.get("todayName"));
		dp18n.setWeek(MessageRetriever.get("weekName"));
		dp18n.setWeekdays(Arrays.asList(MessageRetriever.get("daysNamesLong").split(",")));
		dp18n.setWeekdaysShort(Arrays.asList(MessageRetriever.get("daysNamesShort").split(",")));
		dp18n.setMonthNames(Arrays.asList(MessageRetriever.get("monthsNamesLong").split(",")));
//		creationDate.setLocale(MessageRetriever.getLocale());
		creationDate.setLocale(UI.getCurrent().getLocale());
		creationDate.setI18n(dp18n);
		creationDate.setPlaceholder(MessageRetriever.get("creationDate"));
		creationDate.setWidthFull();
		creationDate.setLocale(new Locale("ro", "RO"));

		dateRange.setForceNarrow(true);

		type.setItems(HolidayRequest.Type.values());
		type.setItemLabelGenerator(i -> MessageRetriever.get("holidayType_" + i.toString()));
		type.setPlaceholder(MessageRetriever.get("holidayType"));
		type.setWidthFull();

		comments.setPlaceholder(MessageRetriever.get("holidayCommentsPlaceholder"));
		comments.setWidthFull();

		substitutes.setDataProvider(new ListDataProvider<>(requestsService.getAvailableSubstitutes()));
		substitutes.setPlaceholder(MessageRetriever.get("substituteName"));
		substitutes.setWidthFull();
		addValidations();

		binder.bindInstanceFields(this);

		Button btnSave = new Button(MessageRetriever.get("btnSaveLbl"), VaadinIcon.CHECK.create());
		btnSave.getElement().getThemeList().add("primary");
		btnDelete.getElement().getThemeList().add("error");
		btnSave.addClickListener(e -> save());
		btnDelete.addClickListener(e -> delete());
		Button btnCancel = new Button(MessageRetriever.get("btnCancelLbl"));
		btnCancel.addClickListener(e -> cancelEdit());

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		HorizontalLayout actions = new HorizontalLayout(btnSave, btnCancel, btnDelete);
		add(substitutes, dateRange, type, comments, creationDate, actions);
		addKeyPressListener(Key.ENTER, e -> save());
		setSpacing(true);
		setVisible(false);
	}

	private void addValidations() {
		binder.forField(substitutes).asRequired(MessageRetriever.get("validationSubstitute"))
				.bind(HolidayRequest::getSubstitutes, HolidayRequest::setSubstitutes);

		Binder.Binding<HolidayRequest, DateRange> holidayRequestDateRangeBinding = binder.forField(dateRange).asRequired(MessageRetriever.get("validationHolidayPeriod"))
				.withValidator(DateRange::hasWorkingDays, MessageRetriever.get("validationHolidayPeriodNoWorkingDays"))
				.withValidator(hasEnoughHolidayDays(), MessageRetriever.get("validationHolidayPeriodNotEnoughDaysLeft"))
				.withValidator(isPeriodNotOverlapping(), MessageRetriever.get("validationHolidayPeriodOverlapping"))
				.bind(HolidayRequest::getRange, HolidayRequest::setRange);

		type.addValueChangeListener(event -> holidayRequestDateRangeBinding.validate());

		binder.forField(type).asRequired(MessageRetriever.get("validationHolidayType"))
				.bind(HolidayRequest::getType, HolidayRequest::setType);

		binder.forField(comments).bind(HolidayRequest::getComments, HolidayRequest::setComments);

		binder.forField(creationDate).asRequired(MessageRetriever.get("validationDate"))
				.bind(HolidayRequest::getCreationDate, HolidayRequest::setCreationDate);
	}

	private SerializablePredicate<? super DateRange> isPeriodNotOverlapping() {
		return range -> {
			List<HolidayRequest> allByRequesterEmail = requestsService.getHolidayRequests(SecurityUtils.getLoggedInUser().getEmail());
			allByRequesterEmail.removeIf(r -> r.getId().equals(holidayRequest.getId()));
			List<HolidayRequest> notOverlapping = allByRequesterEmail.stream()
					.filter(r -> !range.isOverlapping(r.getDateFrom(), r.getDateTo()))
					.collect(toList());
			return allByRequesterEmail.size() == notOverlapping.size();
		};
	}

	private SerializablePredicate<DateRange> hasEnoughHolidayDays() {
		return range -> {
			List<HolidayRequest> allByRequesterEmail = requestsService.getHolidayRequests(SecurityUtils.getLoggedInUser().getEmail());
			User user = SecurityUtils.getLoggedInUser();
			int sumDaysTaken = allByRequesterEmail.stream()
					.filter(HolidayRequest::isCO)
					.mapToInt(HolidayRequest::getNumberOfDays)
					.sum();
			int days = user.getAvailableVacationDays() - sumDaysTaken;
			if (type.getValue() != null && type.getValue().equals(HolidayRequest.Type.CO)) {
				return days - range.getNumberOfDays() >= 0;
			}
			return true;
		};
	}

	private void delete() {
		requestsService.removeRequest(holidayRequest);
		changeHandler.onChange();
	}

	private void save() {
		if (binder.validate().isOk()) {
			requestsService.saveRequest(holidayRequest);
			changeHandler.onChange();
		}
	}

	final void editHolidayRequest(HolidayRequest request) {
		if (request == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = request.getId() != null;
		holidayRequest = request;
		btnDelete.setVisible(persisted);
		binder.setBean(holidayRequest);
		setVisible(true);
	}

	private void cancelEdit() {
		changeHandler.onChange();
	}

	void setChangeHandler(ChangeHandler handler) {
		changeHandler = handler;
	}

	public interface ChangeHandler {
		void onChange();
	}
}