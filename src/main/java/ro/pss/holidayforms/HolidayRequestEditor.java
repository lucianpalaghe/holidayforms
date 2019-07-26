package ro.pss.holidayforms;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.HolidayType;

@SpringComponent
@UIScope
public class HolidayRequestEditor extends VerticalLayout implements KeyNotifier {

	private final HolidayRequestRepository repository;

	private HolidayRequest holidayRequest;

	TextField requester = new TextField("Cine cere");
	TextField replacer = new TextField("Cine inlocuieste");

	DatePicker dateFrom = new DatePicker("De la");
	DatePicker dateTo = new DatePicker("Pana la");

	ComboBox<HolidayType> type = new ComboBox<>();

	DatePicker creationDate = new DatePicker("Data crearii");

	Button save = new Button("Save", VaadinIcon.CHECK.create());
	Button cancel = new Button("Cancel");
	Button delete = new Button("Delete", VaadinIcon.TRASH.create());
	HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

	Binder<HolidayRequest> binder = new Binder<>(HolidayRequest.class);
	private ChangeHandler changeHandler;

	@Autowired
	public HolidayRequestEditor(HolidayRequestRepository repository) {
		this.repository = repository;

		add(requester, replacer, dateFrom, dateTo, type, creationDate, actions);

		binder.bindInstanceFields(this);

		setSpacing(true);

		save.getElement().getThemeList().add("primary");
		delete.getElement().getThemeList().add("error");

		addKeyPressListener(Key.ENTER, e -> save());

		save.addClickListener(e -> save());
		delete.addClickListener(e -> delete());
		cancel.addClickListener(e -> editHolidayRequest(holidayRequest));
		setVisible(false);
	}

	void delete() {
		repository.delete(holidayRequest);
		changeHandler.onChange();
	}

	void save() {
		repository.save(holidayRequest);
		changeHandler.onChange();
	}

	public interface ChangeHandler {
		void onChange();
	}

	public final void editHolidayRequest(HolidayRequest c) {
		if (c == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = c.getId() != null;
		if (persisted) {
			holidayRequest = repository.findById(c.getId()).get();
		} else {
			holidayRequest = c;
		}

		cancel.setVisible(persisted);
		binder.setBean(holidayRequest);
		setVisible(true);
		requester.focus();
	}

	public void setChangeHandler(ChangeHandler h) {
		changeHandler = h;
	}
}