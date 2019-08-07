package ro.pss.holidayforms.gui.request;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.daterange.DateRangePicker;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
public class HolidayRequestEditor extends VerticalLayout implements KeyNotifier {
	private final HolidayRequestRepository holidayRepo;
	private final UserRepository userRepo;

	private ComboBox<User> replacer = new ComboBox<>(MessageRetriever.get("replacerName"));
	private DateRangePicker dateRange = new DateRangePicker();
	private ComboBox<HolidayRequest.Type> type = new ComboBox<>(MessageRetriever.get("holidayType"));
	private DatePicker creationDate = new DatePicker(MessageRetriever.get("creationDate"));
	private Button btnSave = new Button(MessageRetriever.get("btnSaveLbl"), VaadinIcon.CHECK.create());
	private Button btnCancel = new Button(MessageRetriever.get("btnCancelLbl"));
	private Button btnDelete = new Button(MessageRetriever.get("btnDeleteLbl"), VaadinIcon.TRASH.create());
	private HorizontalLayout actions = new HorizontalLayout(btnSave, btnCancel, btnDelete);
	private Binder<HolidayRequest> binder = new Binder<>(HolidayRequest.class);
	private HolidayRequest holidayRequest;
	private ChangeHandler changeHandler;

	// TODO: remove, only used for testing without security implementation
	private String userId = "lucian.palaghe@pss.ro";
	private List<String> approverIds = Arrays.asList("luminita.petre@pss.ro", "claudia.gican@pss.ro");

	@Autowired
	public HolidayRequestEditor(HolidayRequestRepository holidayRepository, UserRepository userRepository) {
		this.holidayRepo = holidayRepository;
		this.userRepo = userRepository;
		creationDate.setLocale(MessageRetriever.getLocale());
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
		creationDate.setI18n(dp18n);

		dateRange.setForceNarrow(true);
		type.setItems(HolidayRequest.Type.values());
//		type.setItemLabelGenerator(i -> example.getDescription());
		replacer.setItems(userRepo.findAll());
		replacer.setWidthFull();
		type.setWidthFull();
		creationDate.setWidthFull();
		creationDate.setLocale(new Locale("ro", "RO"));

		binder.bindInstanceFields(this);

		btnSave.getElement().getThemeList().add("primary");
		btnDelete.getElement().getThemeList().add("error");
		btnSave.addClickListener(e -> save());
		btnDelete.addClickListener(e -> delete());
		btnCancel.addClickListener(e -> cancelEdit());

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(replacer, dateRange, type, creationDate, actions);
		addKeyPressListener(Key.ENTER, e -> save());
		addValidations();
		setSpacing(true);
		setVisible(false);
	}

	private void addValidations() {
		binder.forField(replacer).asRequired(MessageRetriever.get("validationReplacer"))
				.bind(HolidayRequest::getSubstitute, HolidayRequest::addSubstitute);

		binder.forField(dateRange).asRequired(MessageRetriever.get("validationHolidayPeriod"))
				.bind(HolidayRequest::getRange, HolidayRequest::setRange);
//				.bind(HolidayRequest::getDateFrom, HolidayRequest::setDateFrom);
//
//		Binder.BindingBuilder<HolidayRequest, LocalDate> returnBindingBuilder = binder
//				.forField(dateTo)
//				.asRequired("Pana cand vrei sa pleci in concediu?")
//				.withValidator(r -> r != null && !r.isBefore(dateFrom.getValue()), "Nu poti sa pleci inainte sa te intorci!");
//		Binder.Binding<HolidayRequest, LocalDate> returnBinder = returnBindingBuilder
//				.bind(HolidayRequest::getDateTo, HolidayRequest::setDateTo);

		binder.forField(type).asRequired(MessageRetriever.get("validationHolidayType"))
				.bind(HolidayRequest::getType, HolidayRequest::setType);

		binder.forField(creationDate).asRequired(MessageRetriever.get("validationDate"))
				.bind(HolidayRequest::getCreationDate, HolidayRequest::setCreationDate);

//		dateTo.addValueChangeListener(event -> returnBinder.validate());
	}

	private void delete() {
		holidayRepo.delete(holidayRequest);
		changeHandler.onChange();
	}

	//	void save(@UserPrincipal User loggedInUser) {
	private void save() {
		if (binder.validate().isOk()) {
			User requester = userRepo.getOne(userId);

			List<ApprovalRequest> approvalRequests = approverIds.stream().map(a -> { // TODO: refactor
				User approver = userRepo.getOne(a);
				return new ApprovalRequest(approver, ApprovalRequest.Status.NEW);
			}).collect(Collectors.toList());
			approvalRequests.forEach(a -> holidayRequest.addApproval(a));

			holidayRequest.setRequester(requester);
			holidayRepo.save(holidayRequest);
			changeHandler.onChange();
		}
	}

	final void editHolidayRequest(HolidayRequest c) {
		if (c == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = c.getId() != null;
		if (persisted) {
			holidayRequest = holidayRepo.findById(c.getId()).get();
		} else {
			holidayRequest = c;
		}

		btnDelete.setVisible(persisted);
		binder.setBean(holidayRequest);
		setVisible(true);
	}

	private void cancelEdit() {
		changeHandler.onChange();
	}

	void setChangeHandler(ChangeHandler h) {
		changeHandler = h;
	}

	public interface ChangeHandler {
		void onChange();
	}
}