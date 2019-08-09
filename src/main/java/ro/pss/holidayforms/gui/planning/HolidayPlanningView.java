package ro.pss.holidayforms.gui.planning;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.daterange.DateRangePicker;
import ro.pss.holidayforms.gui.components.dialog.HolidayConfirmationDialog;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@SpringComponent
@UIScope
@Route(value = "planning", layout = HolidayAppLayout.class)
@StyleSheet("responsive-panels-beta.css")
public class HolidayPlanningView extends HorizontalLayout implements AfterNavigationObserver, BeforeLeaveObserver {
	private final HolidayPlanningRepository repository;
	private final UserRepository userRepository;
	private final HorizontalLayout container;
	//	private final H2 heading;
	private final DateRangePicker rangePicker;
	private final Grid<HolidayPlanningEntry> grid;
	// TODO: remove, only used for testing without security implementation
	private String userId = "lucian.palaghe@pss.ro";
	Set<HolidayPlanningEntry> entries = new TreeSet<>();
	private ListDataProvider<HolidayPlanningEntry> provider = new ListDataProvider(entries);

	private HolidayPlanning holidayPlanning;
	private H3 remainingDaysHeader = new H3();

	public HolidayPlanningView(HolidayPlanningRepository repo, UserRepository userRepo) {
		this.repository = repo;
		this.userRepository = userRepo;
		rangePicker = new DateRangePicker();
		grid = new Grid<>();
		grid.addColumn(HolidayPlanningEntry::getDateFrom).setHeader(MessageRetriever.get("gridColFromDate"));
		grid.addColumn(HolidayPlanningEntry::getDateTo).setHeader(MessageRetriever.get("gridColToDate"));
		grid.addColumn(HolidayPlanningEntry::getNumberOfDays).setHeader(MessageRetriever.get("gridColDaysHeader"));
		grid.addColumn(new ComponentRenderer<>(this::getActionButtons)).setFlexGrow(0);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
		ListDataProvider<HolidayPlanningEntry> provider = new ListDataProvider(entries);
		grid.setDataProvider(provider);
		listHolidayPlanningEntries();
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

		refreshRemainingDaysHeader();
		VerticalLayout remainingDays = new VerticalLayout(remainingDaysHeader, new Hr(), rangePicker);
		remainingDays.setWidth("auto");
		Button btnSave = new Button(MessageRetriever.get("btnSaveLbl"), VaadinIcon.LOCK.create(), event -> {
			holidayPlanning.getEntries().clear();
			holidayPlanning.setEntries(entries);
			HolidayPlanning savedPlanning = repo.save(holidayPlanning);
			/*
			 * Unexpected JPA behaviour when trying to save, it only deletes all the entities, without insert command afterwards.
			 * Strangely enough, it returns all the entities as they were saved. So, save once again in order to actually execute the
			 * insert command.
			 */
			//repo.save(savedPlanning);

			Notification.show(MessageRetriever.get("planningSaved"),3000, Notification.Position.TOP_CENTER);
		});
		btnSave.getStyle().set("margin-left", "auto");
		VerticalLayout salveaza = new VerticalLayout(grid, btnSave);
		subContainer.add(remainingDays, salveaza);
		subContainer.setWidthFull();
		container.add(subContainer);

		rangePicker.addListener(r -> {
			HolidayPlanningEntry planningEntry = new HolidayPlanningEntry(r.getDateFrom(), r.getDateTo());
			planningEntry.setPlanning(holidayPlanning);
			HolidayPlanningEntry.EntryValidityStatus status = holidayPlanning.addPlanningEntry(planningEntry);
			if(status.equals(HolidayPlanningEntry.EntryValidityStatus.VALID)) {
				entries.add(planningEntry);
				grid.getDataProvider().refreshAll();
				refreshRemainingDaysHeader();
			}else {
				Notification.show(getMessageFromEntryStatus(status),3000, Notification.Position.TOP_CENTER);
			}
			rangePicker.setValue(null);
		});

//		setJustifyContentMode(JustifyContentMode.CENTER);
//		setAlignItems(Alignment.CENTER);
		add(container);
//		setHeightFull();
	}

	private String getMessageFromEntryStatus(HolidayPlanningEntry.EntryValidityStatus status) {
		String message = "";
		switch(status) {
			case NO_WORKING_DAYS:
				message = MessageRetriever.get("noWorkingDay");
				break;
			case RANGE_CONFLICT:
				message = MessageRetriever.get("rangeConflict");
				break;
			case EXCEEDED_DAYS:
				message = String.format(MessageRetriever.get("exceededDays"), holidayPlanning.getEmployee().getRegularVacationDays());
				break;
			default:
				message = MessageRetriever.get("notImplementedMsg");
		}
		return message;
	}

	private void refreshRemainingDaysHeader() {
		int usedDays = entries.stream().mapToInt(HolidayPlanningEntry::getNumberOfDays).sum();
		int remainingDays = holidayPlanning.getEmployee().getRegularVacationDays()  - usedDays;
		if(remainingDays > 0) {
			remainingDaysHeader.setText(String.format(MessageRetriever.get("remainingDaysHeader"), remainingDays));
		}else {
			remainingDaysHeader.setText(String.format(MessageRetriever.get("remainingDaysHeaderNoDays"), remainingDays));
		}
	}

	private HorizontalLayout getActionButtons(HolidayPlanningEntry request) {
		Button btnDeny = new Button(VaadinIcon.CLOSE_CIRCLE.create(), event -> {
			holidayPlanning.removePlanningEntry(request);
			entries.remove(request);
			grid.getDataProvider().refreshAll();
			refreshRemainingDaysHeader();
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
			holidayPlanning = new HolidayPlanning(u, new TreeSet<>());
			grid.setVisible(true);
		}
		entries.clear();
		entries.addAll(holidayPlanning.getEntries());
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		listHolidayPlanningEntries();
		refreshRemainingDaysHeader();
	}

	@Override
	public void beforeLeave(BeforeLeaveEvent event) {
		if (this.hasChanges()) {
			BeforeLeaveEvent.ContinueNavigationAction action = event.postpone();
			HolidayConfirmationDialog holidayConfirmationDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.DENIAL, action::proceed, MessageRetriever.get("unsavedChanges"), MessageRetriever.get("unsavedChangesMsg"), MessageRetriever.get("answerYes"), MessageRetriever.get("backTxt"));
			holidayConfirmationDialog.open();
		}
	}

	private boolean hasChanges() {
		Optional<HolidayPlanning> original = repository.findByEmployeeEmail(userId);
		if(original.isPresent()) {
			HolidayPlanning holidayPlanning = original.get();
			if (!(holidayPlanning.getEntries().containsAll(entries) && holidayPlanning.getEntries().size() == entries.size())) {
				return true;
			}
		} else if (entries.size() > 0) {
			return true;
		}
		return false;
	}
}