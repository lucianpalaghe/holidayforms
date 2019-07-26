package ro.pss.holidayforms;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.util.StringUtils;
import ro.pss.holidayforms.domain.HolidayRequest;

//@Route("/concedii")
@Route
public class HolidayRequestView extends VerticalLayout {
	private final HolidayRequestRepository requestRepository;

	private final HolidayRequestEditor editor;

	final Grid<HolidayRequest> grid;

	final TextField filter;

	private final Button addNewBtn;

	public HolidayRequestView(HolidayRequestRepository repo, HolidayRequestEditor editor) {
		this.requestRepository = repo;
		this.editor = editor;
		this.grid = new Grid<>(HolidayRequest.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New HolidayRequest", VaadinIcon.PLUS.create());

		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
		add(actions, grid, editor);

		grid.setHeight("300px");
		grid.setColumns("id", "requester", "replacer", "dateFrom", "dateTo", "type");
		grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

		filter.setPlaceholder("Filter by last name");

		filter.setValueChangeMode(ValueChangeMode.EAGER);
		filter.addValueChangeListener(e -> listHolidayRequests(e.getValue()));

		grid.asSingleSelect().addValueChangeListener(e -> {
			editor.editHolidayRequest(e.getValue());
		});

		addNewBtn.addClickListener(e -> editor.editHolidayRequest(new HolidayRequest()));

		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listHolidayRequests(filter.getValue());
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
}