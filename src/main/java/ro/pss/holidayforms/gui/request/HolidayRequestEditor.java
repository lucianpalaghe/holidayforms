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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
public class HolidayRequestEditor extends VerticalLayout implements KeyNotifier {
	private final HolidayRequestRepository holidayRepo;
	private final UserRepository userRepo;

	ComboBox<User> replacer = new ComboBox<>("Inlocuitor");
	DatePicker dateFrom = new DatePicker("De la");
	DatePicker dateTo = new DatePicker("Pana la");
	ComboBox<HolidayRequest.Type> type = new ComboBox<>("Tipul de concediu");
	DatePicker creationDate = new DatePicker("Data crearii");

	Button btnSave = new Button("Save", VaadinIcon.CHECK.create());
	Button btnCancel = new Button("Cancel");
	Button btnDelete = new Button("Delete", VaadinIcon.TRASH.create());
	HorizontalLayout actions = new HorizontalLayout(btnSave, btnCancel, btnDelete);
	Binder<HolidayRequest> binder = new Binder<>(HolidayRequest.class);
	private HolidayRequest holidayRequest;
	private ChangeHandler changeHandler;

	// TODO: remove, only used for testing without security implementation
	private String userId = "lucian.palaghe@pss.ro";
	private List<String> approverIds = Arrays.asList("luminita.petre@pss.ro");

	@Autowired
	public HolidayRequestEditor(HolidayRequestRepository holidayRepository, UserRepository userRepository) {
		this.holidayRepo = holidayRepository;
		this.userRepo = userRepository;

		type.setItems(HolidayRequest.Type.values());
//		type.setItemLabelGenerator(i -> example.getDescription());
		replacer.setItems(userRepo.findAll());

		add(replacer, dateFrom, dateTo, type, creationDate, actions);

		addValidations();
		binder.bindInstanceFields(this);


		setSpacing(true);

		btnSave.getElement().getThemeList().add("primary");
		btnDelete.getElement().getThemeList().add("error");

		addKeyPressListener(Key.ENTER, e -> save());

		btnSave.addClickListener(e -> save());
		btnDelete.addClickListener(e -> delete());
		btnCancel.addClickListener(e -> cancelEdit());
		setVisible(false);
	}

	private void addValidations() {
		binder.forField(replacer).asRequired("Cine te inlocuieste?")
				.bind(HolidayRequest::getSubstitute, HolidayRequest::addSubstitute);

		binder.forField(dateFrom).asRequired("Cand vrei sa pleci in concediu?")
				.bind(HolidayRequest::getDateFrom, HolidayRequest::setDateFrom);

		Binder.BindingBuilder<HolidayRequest, LocalDate> returnBindingBuilder = binder
				.forField(dateTo)
				.asRequired("Pana cand vrei sa pleci in concediu?")
				.withValidator(r -> r != null && !r.isBefore(dateFrom.getValue()), "Nu poti sa pleci inainte sa te intorci!");
		Binder.Binding<HolidayRequest, LocalDate> returnBinder = returnBindingBuilder
				.bind(HolidayRequest::getDateTo, HolidayRequest::setDateTo);

		binder.forField(type).asRequired("Trebuie selectat tipul de concediu!")
				.bind(HolidayRequest::getType, HolidayRequest::setType);

		binder.forField(creationDate).asRequired("Data cererii trebuie selectata!")
				.bind(HolidayRequest::getCreationDate, HolidayRequest::setCreationDate);

		dateTo.addValueChangeListener(event -> returnBinder.validate());
	}

	void delete() {
		holidayRepo.delete(holidayRequest);
		changeHandler.onChange();
	}

	//	void save(@UserPrincipal User loggedInUser) {
	void save() {
		if (binder.validate().isOk()) {
			User requester = userRepo.getOne(userId);

			List<ApprovalRequest> approvalRequests = approverIds.stream().map(a -> { // TODO: refactor
				User approver = userRepo.getOne(a);
				return new ApprovalRequest(approver, ApprovalRequest.Status.NEW);
			}).collect(Collectors.toList());
			approvalRequests.stream().forEach(a -> holidayRequest.addApproval(a));

			holidayRequest.setRequester(requester);
			holidayRepo.save(holidayRequest);
			changeHandler.onChange();
		}
	}

	public final void editHolidayRequest(HolidayRequest c) {
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

	void cancelEdit() {
		changeHandler.onChange();
	}

	public void setChangeHandler(ChangeHandler h) {
		changeHandler = h;
	}

	public interface ChangeHandler {
		void onChange();
	}
}