package ro.pss.holidayforms;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.util.StringUtils;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.gui.HolidayAppLayout;

@SpringComponent
@UIScope
@Route(value = "", layout = HolidayAppLayout.class)
public class HolidayRequestView extends VerticalLayout {
	final Grid<HolidayRequest> grid;
	final TextField filter;
	private final HolidayRequestRepository requestRepository;
	private final HolidayRequestEditor editor;
	private final Button addNewBtn;
	private final Dialog dialog;
//	@Autowired
//	private DefaultNotificationHolder notifications;

	public HolidayRequestView(HolidayRequestRepository repo, HolidayRequestEditor editor) {
		this.requestRepository = repo;
		this.editor = editor;
		this.grid = new Grid<>(HolidayRequest.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New HolidayRequest", VaadinIcon.PLUS.create());
		this.dialog = new Dialog(editor);
		this.dialog.setWidth("300px");
		this.dialog.setHeight("600px");
		this.dialog.setCloseOnOutsideClick(false);

		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
		add(actions, grid, this.editor);

		grid.setHeight("300px");
		grid.setColumns("id", "requester", "replacer", "dateFrom", "dateTo", "type");
		grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

		filter.setPlaceholder("Filter by last name");

		filter.setValueChangeMode(ValueChangeMode.EAGER);
		filter.addValueChangeListener(e -> listHolidayRequests(e.getValue()));

		grid.asSingleSelect().addValueChangeListener(e -> {
			editor.editHolidayRequest(e.getValue());
			mountEditorInDialog(true);
		});

		addNewBtn.addClickListener(e -> {
//			notifications.addNotification(new DefaultNotification("Test", "This is a test"));
//			editor.editHolidayRequest(new HolidayRequest());
			 editor.editHolidayRequest(new HolidayRequest()); mountEditorInDialog(true);
		});

		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listHolidayRequests(filter.getValue());
			mountEditorInDialog(false);
		});

		listHolidayRequests(null);
	}

	void listHolidayRequests(String filterText) {
		if (StringUtils.isEmpty(filterText)) {
			grid.setItems(requestRepository.findAll());
		}
//		else {
//			grid.setItems(requestRepository.findByLastNameStartsWithIgnoreCase(filterText));
//		}
	}
	void mountEditorInDialog(boolean mount) {
		if(mount && editor.isVisible()) {
			dialog.removeAll();
			dialog.addComponentAsFirst(this.editor);
			dialog.open();
		}else {
			dialog.close();
			dialog.removeAll();
		}
	}
}