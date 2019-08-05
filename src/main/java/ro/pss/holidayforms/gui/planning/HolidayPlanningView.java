package ro.pss.holidayforms.gui.planning;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ro.pss.holidayforms.domain.HolidayPlanning;
import ro.pss.holidayforms.domain.HolidayPlanningEntry;
import ro.pss.holidayforms.domain.repo.HolidayPlanningRepository;
import ro.pss.holidayforms.gui.HolidayAppLayout;
import ro.pss.holidayforms.gui.components.daterange.DateRangePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringComponent
@UIScope
@Route(value = "planning", layout = HolidayAppLayout.class)
@StyleSheet("context://mycustom.css")
public class HolidayPlanningView extends Div implements AfterNavigationObserver {
	private final HolidayPlanningRepository repository;

	private final Div container;
	//	private final H2 heading;
	private String userId = "lucian.palaghe@pss.ro";
	private final DateRangePicker rangePicker;
	private final Grid<HolidayPlanningEntry> grid;
	List<HolidayPlanningEntry> entries = new ArrayList<>();
	private ListDataProvider<HolidayPlanningEntry> provider = new ListDataProvider(entries);

	public HolidayPlanningView(HolidayPlanningRepository repo) {
		this.repository = repo;
		rangePicker = new DateRangePicker();
		grid = new Grid<>();
		grid.addColumn(HolidayPlanningEntry::getDateFrom).setHeader("Incepand cu data");//.setFlexGrow(2);
		grid.addColumn(HolidayPlanningEntry::getDateTo).setHeader("Pana la data");//.setFlexGrow(2);
		grid.addColumn(HolidayPlanningEntry::getNumberOfDays).setHeader("Nr. de zile");//.setFlexGrow(1);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
		grid.setDataProvider(provider);

		container = new Div();
		container.setWidth("100%");
		container.setMaxWidth("80em");
		// ****************************
		Div mainDiv = new Div();
		Div rowDiv = new Div();
		Div colDiv = new Div();
		Div colDiv2 = new Div();
		rowDiv.setClassName("row");
		colDiv.setClassName("column");
		colDiv2.setClassName("double-column");

//		FlexLayout.cre
//		rowDiv.getStyle().set("display","flex");
//		rowDiv.getStyle().set("flex-direction","row");
//		rowDiv.getStyle().set("flex-wrap","wrap");
//		rowDiv.getStyle().set("width","100%");
//
//		colDiv.getStyle().set("display","flex");
//		colDiv.getStyle().set("flex-direction","column");
//		colDiv.getStyle().set("flex-basis","100%");
//		colDiv.getStyle().set("flex","1");
//
//		colDiv2.getStyle().set("display","flex");
//		colDiv2.getStyle().set("flex-direction","column");
//		colDiv2.getStyle().set("flex-basis","100%");
//		colDiv2.getStyle().set("flex","1");

		colDiv.add(rangePicker);
		colDiv2.add(grid);
		rowDiv.add(colDiv, colDiv2);
		mainDiv.add(rowDiv);
		container.add(mainDiv);


		// *****************************
//		container.setHeightFull();
//		setJustifyContentMode(JustifyContentMode.CENTER);
//		setAlignItems(Alignment.CENTER);

//		HorizontalLayout subContainer = new HorizontalLayout();
//		subContainer.setPadding(false);
//		subContainer.add(rangePicker, grid);
//		subContainer.setWidthFull();
//
//		container.add(subContainer);

		rangePicker.addListener(r -> {
			entries.add(new HolidayPlanningEntry(r.getDateFrom(), r.getDateTo()));
			grid.getDataProvider().refreshAll();
			rangePicker.setValue(null);
		});

//		setJustifyContentMode(JustifyContentMode.CENTER);
//		setAlignItems(Alignment.CENTER);
		add(container);
//		setHeightFull();

		listHolidayPlanningEntries();
	}

	private void listHolidayPlanningEntries() {
		Optional<HolidayPlanning> planning = repository.findByEmployeeEmail(userId);
		if (planning.isPresent()) {
			grid.setVisible(true);
			grid.setItems(planning.get().getEntries());
		} else {
//			grid.setVisible(false);
			grid.setVisible(true);
//			container.add(new H2("Nu exista planificare de concediu"));
		}
	}


	@Override
	public void afterNavigation(AfterNavigationEvent event) {
//		listApprovalRequests();
	}
}