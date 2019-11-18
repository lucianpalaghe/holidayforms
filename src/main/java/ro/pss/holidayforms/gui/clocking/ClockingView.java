package ro.pss.holidayforms.gui.clocking;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.clocking.EmployeeClockingDay;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.service.ClockingService;
import ro.pss.holidayforms.service.HolidayRequestService;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

@SpringComponent
@UIScope
@Route(value = "clockings", layout = HolidayAppLayout.class)
public class ClockingView extends HorizontalLayout implements AfterNavigationObserver, HasDynamicTitle {
	private final Grid<EmployeeClockingDay> grid;
	private final H2 heading;
	private final DateTimeFormatter formatter;
	private final ComboBox<User> filterEmployee = new ComboBox<>();
	private final DatePicker filterDate = new DatePicker();
	private final Button btnRefresh = new Button(VaadinIcon.REFRESH.create());

	@Autowired
	private ClockingService service;
	private HolidayRequestService reqsService;

	public ClockingView(HolidayRequestService rService) {
		formatter = DateTimeFormatter.ofPattern("HH:mm");
		grid = new Grid<>();
		grid.addColumn(r -> r.getEmployee().getName()).setHeader(MessageRetriever.get("clockingViewGridHeaderName"));
		grid.addColumn(EmployeeClockingDay::getClockingDate).setHeader(MessageRetriever.get("clockingViewGridHeaderDate"));
		grid.addColumn(e -> {
			if (e.getClockInTime().isPresent()) {
				return e.getClockInTime().get().format(formatter).toString();
			} else {
				return "-";
			}
		}).setHeader(MessageRetriever.get("clockingViewGridHeaderClockInTime"));
		grid.addColumn(e -> {
			if (e.getClockOutTime().isPresent()) {
				return e.getClockOutTime().get().format(formatter).toString();
			} else {
				return "-";
			}
		}).setHeader(MessageRetriever.get("clockingViewGridHeaderClockOutTime"));
		grid.addColumn(e -> {
			if (e.getDuration().isPresent()) {
				Duration duration = e.getDuration().get();
				return duration.toHoursPart() + ":" + duration.toMinutesPart();
			} else {
				return "-";
			}
		}).setHeader(MessageRetriever.get("clockingViewGridHeaderDuration"));

		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
		grid.setItemDetailsRenderer(getClockingRecordsDetails());

		heading = new H2();
		heading.setVisible(false);
		this.reqsService = rService;
		filterEmployee.setDataProvider(new ListDataProvider<>(reqsService.getAvailableSubstitutes()));
		filterEmployee.setWidthFull();
		filterEmployee.addValueChangeListener(e -> listClockingDays());
		filterDate.addValueChangeListener(e -> listClockingDays());
		btnRefresh.addClickListener(e -> listClockingDays());

		HorizontalLayout searchControls = new HorizontalLayout(filterEmployee, filterDate, btnRefresh);
		VerticalLayout container = new VerticalLayout();
		container.add(searchControls, heading, grid);
		container.setWidth("100%");
		container.setMaxWidth("70em");
		container.setHeightFull();

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(container);
		setHeightFull();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		listClockingDays();
	}

	private void listClockingDays() {
		List<EmployeeClockingDay> clockings = service.getClockingDays();
		if (clockings.isEmpty()) {
			grid.setVisible(false);
			heading.setText(MessageRetriever.get("noClockings"));
			heading.setVisible(true);
		} else {
			heading.setVisible(false);
			grid.setVisible(true);
//			grid.setItems(clockings);
			filterClockingDays();
		}
	}

	private void filterClockingDays() {
		Stream<EmployeeClockingDay> stream = service.getClockingDays().stream();
		if (filterEmployee.getValue() != null) {
			stream = stream.filter(e -> e.getEmployee().getEmail().equals(filterEmployee.getValue().getEmail()));
		}
		if (filterDate.getValue() != null) {
			stream = stream.filter(e -> e.getClockingDate().equals(filterDate.getValue()));
		}
		grid.setItems(stream.collect(toList()));
	}

	private ComponentRenderer<HorizontalLayout, EmployeeClockingDay> getClockingRecordsDetails() {
		return new ComponentRenderer<>(employeeClockingDay -> {
			HorizontalLayout detailsContainer = new HorizontalLayout();
			detailsContainer.setWidthFull();
			UnorderedList ul = new UnorderedList();
			detailsContainer.add(ul);
			String clockingRecordMessage = MessageRetriever.get("clockingRecordDetail");
			employeeClockingDay.getRecords().forEach(clockingRecord -> ul.add(new ListItem(String.format(clockingRecordMessage,
					clockingRecord.getDateTime().toLocalTime().format(formatter)))));
			return detailsContainer;
		});
	}

	@Override
	public String getPageTitle() {
		return MessageRetriever.get("titleClockings");
	}

}
