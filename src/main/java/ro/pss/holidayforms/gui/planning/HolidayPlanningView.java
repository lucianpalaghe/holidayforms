package ro.pss.holidayforms.gui.planning;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ro.pss.holidayforms.domain.HolidayPlanning;
import ro.pss.holidayforms.domain.HolidayPlanningEntry;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.HolidayPlanningRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.HolidayAppLayout;
import ro.pss.holidayforms.gui.HolidayConfirmationDialog;
import ro.pss.holidayforms.gui.components.daterange.DateRangePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringComponent
@UIScope
@Route(value = "planning", layout = HolidayAppLayout.class)
@StyleSheet("context://mycustom.css")
public class HolidayPlanningView extends HorizontalLayout implements AfterNavigationObserver, BeforeLeaveObserver {
	private final HolidayPlanningRepository repository;
	private final UserRepository userRepository;
	// TODO: remove, only used for testing without security implementation
	private String userId = "lucian.palaghe@pss.ro";

	private final HorizontalLayout container;
	//	private final H2 heading;
	private final DateRangePicker rangePicker;
	private final Grid<HolidayPlanningEntry> grid;
	List<HolidayPlanningEntry> entries = new ArrayList<>();
	private ListDataProvider<HolidayPlanningEntry> provider = new ListDataProvider(entries);

	private HolidayPlanning holidayPlanning;

	public HolidayPlanningView(HolidayPlanningRepository repo, UserRepository userRepo) {
		this.repository = repo;
		this.userRepository = userRepo;
		rangePicker = new DateRangePicker();
		grid = new Grid<>();
		grid.addColumn(HolidayPlanningEntry::getDateFrom).setHeader("Incepand cu data");//.setFlexGrow(2);
		grid.addColumn(HolidayPlanningEntry::getDateTo).setHeader("Pana la data");//.setFlexGrow(2);
		grid.addColumn(HolidayPlanningEntry::getNumberOfDays).setHeader("Nr. de zile");//.setFlexGrow(1);
		grid.addColumn(new ComponentRenderer<>(this::getActionButtons)).setFlexGrow(0);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
		grid.setDataProvider(provider);

		container = new HorizontalLayout();
		container.setWidth("100%");
		container.setMaxWidth("80em");
		// ****************************
//		Div mainDiv = new Div();
//		Div rowDiv = new Div();
//		Div colDiv = new Div();
//		Div colDiv2 = new Div();
//		rowDiv.setClassName("row");
//		colDiv.setClassName("column");
//		colDiv2.setClassName("double-column");

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

//		colDiv.add(rangePicker);
//		colDiv2.add(grid);
//		rowDiv.add(colDiv, colDiv2);
//		mainDiv.add(rowDiv);
//		container.add(mainDiv);


		// *****************************
		container.setHeightFull();
		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);

		HorizontalLayout subContainer = new HorizontalLayout();
		subContainer.setPadding(true);
		VerticalLayout maiAiPixZile = new VerticalLayout(new H3("Mai ai pix zile"), new Hr(), rangePicker);
		maiAiPixZile.setWidth("auto");
		Div d = new Div();

		Button btnSave = new Button("Salveaza", VaadinIcon.LOCK.create(), event -> repo.save(holidayPlanning));
		btnSave.addClassName("butondreapta");
		VerticalLayout salveaza = new VerticalLayout(grid, btnSave);
		subContainer.add(maiAiPixZile, salveaza);
		subContainer.setWidthFull();

		container.add(subContainer);

		rangePicker.addListener(r -> {
			HolidayPlanningEntry planningEntry = new HolidayPlanningEntry(r.getDateFrom(), r.getDateTo());
			holidayPlanning.addPlanningEntry(planningEntry);
//			repository.save(holidayPlanning);
			entries.add(planningEntry);
			grid.getDataProvider().refreshAll();
			rangePicker.setValue(null);
		});

//		setJustifyContentMode(JustifyContentMode.CENTER);
//		setAlignItems(Alignment.CENTER);
		add(container);
//		setHeightFull();

		listHolidayPlanningEntries();
	}

	private HorizontalLayout getActionButtons(HolidayPlanningEntry request) {
		Button btnDeny = new Button(VaadinIcon.CLOSE_CIRCLE.create(), event -> {
			holidayPlanning.removePlanningEntry(request); // TODO: delete doesn't work
			entries.remove(request);
//			repository.save(holidayPlanning);
			grid.getDataProvider().refreshAll();
		});
		btnDeny.addThemeName("error");
		return new HorizontalLayout(btnDeny);
	}

	private void listHolidayPlanningEntries() {
		Optional<HolidayPlanning> planning = repository.findByEmployeeEmail(userId);
		User u = userRepository.findById(userId).get();
		if (planning.isPresent()) {
			holidayPlanning = planning.get();
			grid.setVisible(true);
		} else {
			holidayPlanning = new HolidayPlanning(u, new ArrayList<>());
//			repository.save(holidayPlanning);
			grid.setVisible(true);
		}
		entries.clear();
		entries.addAll(holidayPlanning.getEntries());
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		listHolidayPlanningEntries();
	}

	@Override
	public void beforeLeave(BeforeLeaveEvent event) {
		if (this.hasChanges()) {
			BeforeLeaveEvent.ContinueNavigationAction action = event.postpone();
			HolidayConfirmationDialog holidayConfirmationDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.DENIAL, action::proceed, "Modificari nesalvate", "Exista modificari nesalvate, vrei sa iesi?", "Da", "Inapoi");
			holidayConfirmationDialog.open();
		}
	}

	private boolean hasChanges() {
		// no-op implementation
		return true;
	}
}