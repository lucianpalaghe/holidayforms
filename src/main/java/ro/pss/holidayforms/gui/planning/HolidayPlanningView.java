package ro.pss.holidayforms.gui.planning;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ro.pss.holidayforms.domain.repo.ApprovalRequestRepository;
import ro.pss.holidayforms.gui.HolidayAppLayout;
import ro.pss.holidayforms.gui.components.daterange.DateRangePicker;

import java.time.LocalDate;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@SpringComponent
@UIScope
@Route(value = "planning", layout = HolidayAppLayout.class)
public class HolidayPlanningView extends HorizontalLayout implements AfterNavigationObserver {
	private final VerticalLayout container;
	private String userId = "lucian.palaghe@pss.ro";
	private final DateRangePicker rangePicker;
	public HolidayPlanningView(ApprovalRequestRepository repo) {
		LocalDate feb = LocalDate.of(2019,2,1);
		rangePicker = new DateRangePicker(LocalDate.of(2019, 1, 1), feb.with(lastDayOfMonth()));
		container = new VerticalLayout();
		container.add(rangePicker);
		container.setWidth("100%");
		container.setMaxWidth("70em");
		container.setHeightFull();
		container.setJustifyContentMode(JustifyContentMode.CENTER);
		container.setAlignItems(Alignment.CENTER);

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(container);
		setHeightFull();

	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
//		listApprovalRequests();
	}
}