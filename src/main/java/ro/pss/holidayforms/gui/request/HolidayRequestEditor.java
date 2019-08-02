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

	Button save = new Button("Save", VaadinIcon.CHECK.create());
	Button cancel = new Button("Cancel");
	Button delete = new Button("Delete", VaadinIcon.TRASH.create());
	HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);
	Binder<HolidayRequest> binder = new Binder<>(HolidayRequest.class);
	private HolidayRequest holidayRequest;
	private ChangeHandler changeHandler;

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

		save.getElement().getThemeList().add("primary");
		delete.getElement().getThemeList().add("error");

		addKeyPressListener(Key.ENTER, e -> save());

		save.addClickListener(e -> save());
		delete.addClickListener(e -> delete());
		cancel.addClickListener(e -> cancelEdit());
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
//			User requester = userRepo.getOne("lucian.palaghe@pss.ro");
			User requester = userRepo.getOne("luminita.petre@pss.ro");
			User approver = userRepo.getOne("luminita.petre@pss.ro");
			holidayRequest.setRequester(requester);
			holidayRequest.addApproval(new ApprovalRequest(approver, ApprovalRequest.Status.NEW));
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

		cancel.setVisible(persisted);
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