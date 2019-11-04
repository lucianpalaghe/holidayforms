package ro.pss.holidayforms.gui.clocking;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
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
import ro.pss.holidayforms.domain.clocking.ClockingDay;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.service.ClockingService;

import java.util.List;

@SpringComponent
@UIScope
@Route(value = "clockings", layout = HolidayAppLayout.class)
public class ClockingView extends HorizontalLayout implements AfterNavigationObserver, HasDynamicTitle {
	private final Grid<ClockingDay> grid;
	private final H2 heading;

	@Autowired
	private ClockingService service;

	public ClockingView() {
		this.grid = new Grid<>();
		grid.addColumn(r -> r.getEmployee().getName()).setHeader(MessageRetriever.get("clockingViewGridHeaderName"));
		grid.addColumn(ClockingDay::getClockingDate).setHeader(MessageRetriever.get("clockingViewGridHeaderDate"));
		grid.addColumn(ClockingDay::getClockInTime).setHeader(MessageRetriever.get("clockingViewGridHeaderClockInTime"));
		grid.addColumn(ClockingDay::getClockOutTime).setHeader(MessageRetriever.get("clockingViewGridHeaderClockOutTime"));

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
		List<ClockingDay> clockings = service.getClockingDays();
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

	private ComponentRenderer<HorizontalLayout, ClockingDay> getClockingRecordsDetails() {
		return new ComponentRenderer<>(clockingDay -> {
			HorizontalLayout detailsContainer = new HorizontalLayout();
			detailsContainer.setWidthFull();

			String clockingRecordMessage = MessageRetriever.get("clockingRecordDetail");
			clockingDay
					.getRecords()
					.forEach(clockingRecord ->
							detailsContainer.add(String.format(clockingRecordMessage, clockingRecord.getDateTime())));
			return detailsContainer;
		});
	}

	@Override
	public String getPageTitle() {
		return MessageRetriever.get("titleClockings");
	}

}
