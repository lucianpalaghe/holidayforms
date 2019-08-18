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
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import ro.pss.holidayforms.config.security.CustomUserPrincipal;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.daterange.DateRange;
import ro.pss.holidayforms.gui.components.daterange.DateRangePicker;
import ro.pss.holidayforms.gui.notification.NotificationService;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.*;

@SpringComponent
@UIScope
public class HolidayRequestEditor extends VerticalLayout implements KeyNotifier {
	private final HolidayRequestRepository holidayRepo;
	private final UserRepository userRepo;

	private final ComboBox<User> replacer = new ComboBox<>(MessageRetriever.get("replacerName"));
	private final DateRangePicker dateRange = new DateRangePicker();
	private final ComboBox<HolidayRequest.Type> type = new ComboBox<>(MessageRetriever.get("holidayType"));
	private final DatePicker creationDate = new DatePicker(MessageRetriever.get("creationDate"));
	private final Button btnSave = new Button(MessageRetriever.get("btnSaveLbl"), VaadinIcon.CHECK.create());
	private final Button btnCancel = new Button(MessageRetriever.get("btnCancelLbl"));
	private final Button btnDelete = new Button(MessageRetriever.get("btnDeleteLbl"), VaadinIcon.TRASH.create());
	private final HorizontalLayout actions = new HorizontalLayout(btnSave, btnCancel, btnDelete);
	private final Binder<HolidayRequest> binder = new Binder<>(HolidayRequest.class);
	private final List<String> approverIds = Arrays.asList("lucian.palaghe", "claudia.gican", "luminita.petre");
	private HolidayRequest holidayRequest;
	private ChangeHandler changeHandler;
	private final NotificationService notificationService;

	@Autowired
	public HolidayRequestEditor(HolidayRequestRepository holidayRepository, UserRepository userRepository, NotificationService notificationService) {
		this.holidayRepo = holidayRepository;
		this.userRepo = userRepository;
		this.notificationService = notificationService;
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
		type.setItemLabelGenerator(i -> MessageRetriever.get("holidayType_" + i.toString()));
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
		User user = ((CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

		binder.forField(replacer).asRequired(MessageRetriever.get("validationReplacer"))
				.bind(HolidayRequest::getSubstitute, HolidayRequest::addSubstitute);

		List<HolidayRequest> allByRequesterEmail = holidayRepo.findAllByRequesterEmail(user.getEmail());

		Binder.Binding<HolidayRequest, DateRange> holidayRequestDateRangeBinding = binder.forField(dateRange).asRequired(MessageRetriever.get("validationHolidayPeriod"))
				.withValidator(DateRange::hasWorkingDays, MessageRetriever.get("validationHolidayPeriodNoWorkingDays"))
				.withValidator(hasEnoughHolidayDays(allByRequesterEmail), MessageRetriever.get("validationHolidayPeriodNotEnoughDaysLeft"))
				.withValidator(isPeriodNotOverlapping(allByRequesterEmail), MessageRetriever.get("validationHolidayPeriodOverlapping"))
				.bind(HolidayRequest::getRange, HolidayRequest::setRange);

		type.addValueChangeListener(event -> holidayRequestDateRangeBinding.validate());

		binder.forField(type).asRequired(MessageRetriever.get("validationHolidayType"))
				.bind(HolidayRequest::getType, HolidayRequest::setType);

		binder.forField(creationDate).asRequired(MessageRetriever.get("validationDate"))
				.bind(HolidayRequest::getCreationDate, HolidayRequest::setCreationDate);
	}

	private SerializablePredicate<? super DateRange> isPeriodNotOverlapping(List<HolidayRequest> requests) {
		return range -> {
			List<HolidayRequest> notOverlapping = requests.stream()
					.filter(e -> !range.isOverlapping(e.getDateFrom(), e.getDateTo()))
					.collect(toList());
			return requests.size() == notOverlapping.size();
		};
	}

	private SerializablePredicate<DateRange> hasEnoughHolidayDays(List<HolidayRequest> requests) {
		return range -> {
			User user = ((CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
			int sumDaysTaken = requests.stream()
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
		holidayRepo.delete(holidayRequest);
		notificationService.requestDeleted(holidayRequest);
		changeHandler.onChange();
	}

	private void save() {
		if (binder.validate().isOk()) {
			User requester = ((CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
			holidayRequest.setRequester(requester);

			if (holidayRequest.getApprovalRequests().size() > 0) { // if this request is edited, remove all previous approvals
				holidayRequest.getApprovalRequests().clear();
			}

			List<ApprovalRequest> approvalRequests = approverIds.stream().map(a -> { // TODO: refactor
				User approver = userRepo.getOne(a);
				return new ApprovalRequest(approver, ApprovalRequest.Status.NEW);
			}).collect(toList());
			approvalRequests.forEach(a -> holidayRequest.addApproval(a));

			changeHandler.onChange();
			boolean isNewRequest = holidayRequest.getId() == null;
			if (isNewRequest) {
				notificationService.requestCreated(holidayRequest);
			} else {
                notificationService.requestEdited(holidayRequest);
			}

			holidayRepo.save(holidayRequest);
		}
	}

	final void editHolidayRequest(HolidayRequest c) {
		if (c == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = c.getId() != null;
		if (persisted) {
			holidayRequest = holidayRepo.findById(c.getId()).orElseThrow();
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