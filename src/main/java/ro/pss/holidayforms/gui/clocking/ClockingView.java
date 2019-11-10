package ro.pss.holidayforms.gui.clocking;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.domain.clocking.EmployeeClockingDay;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.service.ClockingService;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringComponent
@UIScope
@Route(value = "clockings", layout = HolidayAppLayout.class)
public class ClockingView extends HorizontalLayout implements AfterNavigationObserver, HasDynamicTitle {
	private final Grid<EmployeeClockingDay> grid;
	private final H2 heading;
	private final DateTimeFormatter formatter;

	@Autowired
	private ClockingService service;

	public ClockingView() {
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

		VerticalLayout container = new VerticalLayout();
		container.add(heading, grid);
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
			grid.setItems(clockings);
		}
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
