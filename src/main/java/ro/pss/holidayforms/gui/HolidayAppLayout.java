package ro.pss.holidayforms.gui;

import com.github.appreciated.app.layout.behaviour.Behaviour;
import com.github.appreciated.app.layout.builder.AppLayoutBuilder;
import com.github.appreciated.app.layout.component.appbar.AppBarBuilder;
import com.github.appreciated.app.layout.component.menu.left.builder.LeftAppMenuBuilder;
import com.github.appreciated.app.layout.component.menu.left.items.LeftClickableItem;
import com.github.appreciated.app.layout.component.menu.left.items.LeftNavigationItem;
import com.github.appreciated.app.layout.entity.DefaultBadgeHolder;
import com.github.appreciated.app.layout.notification.DefaultNotificationHolder;
import com.github.appreciated.app.layout.notification.component.AppBarNotificationButton;
import com.github.appreciated.app.layout.notification.entitiy.DefaultNotification;
import com.github.appreciated.app.layout.router.AppLayoutRouterLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import ro.pss.holidayforms.gui.approval.HolidayApprovalView;
import ro.pss.holidayforms.gui.dashboard.DashboardView;
import ro.pss.holidayforms.gui.request.HolidayRequestView;
import ro.pss.holidayforms.gui.subtitution.SubstitutionRequestView;

import static com.github.appreciated.app.layout.entity.Section.FOOTER;
import static com.github.appreciated.app.layout.entity.Section.HEADER;

@Push
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class HolidayAppLayout extends AppLayoutRouterLayout {
	private DefaultNotificationHolder notifications = new DefaultNotificationHolder();
	private DefaultBadgeHolder substitutionBadge = new DefaultBadgeHolder();
	private DefaultBadgeHolder approvalBadge = new DefaultBadgeHolder();

	public HolidayAppLayout(DefaultNotificationHolder notifications) {
		this.notifications.addClickListener(notification -> System.out.println(notification.getTitle()));

		LeftHeaderItemExt userItem = new LeftHeaderItemExt("User Johnson", null, "cat.jpg");
		LeftNavigationItem holidayRequestsMenuEntry = new LeftNavigationItem("Cererile mele", VaadinIcon.AIRPLANE.create(), HolidayRequestView.class);
		LeftNavigationItem dashboardMenuEntry = new LeftNavigationItem("Dashboard", VaadinIcon.LINE_CHART.create(), DashboardView.class);
		LeftNavigationItem substitutionMenuEntry = new LeftNavigationItem("Ca inlocuitor", VaadinIcon.OFFICE.create(), SubstitutionRequestView.class);
		LeftNavigationItem planningMenuEntry = new LeftNavigationItem("Planificare", VaadinIcon.EDIT.create(), SubstitutionRequestView.class);
		LeftNavigationItem approvalMenuEntry = new LeftNavigationItem("De aprobat", VaadinIcon.USER_CHECK.create(), HolidayApprovalView.class);
		substitutionBadge.bind(substitutionMenuEntry.getBadge());
		approvalBadge.bind(approvalMenuEntry.getBadge());

		LeftClickableItem preferencesMenuEntry = new LeftClickableItem("Preferinte", VaadinIcon.COG.create(),
				clickEvent -> this.notifications.addNotification(new DefaultNotification("Whoops", "Not implemented yet!"))
		);

		init(AppLayoutBuilder
				.get(Behaviour.LEFT_RESPONSIVE)
				.withTitle("Holidays")
				.withIcon("pss-logo.png")
				.withAppBar(AppBarBuilder
						.get()
						.add(new AppBarNotificationButton(VaadinIcon.BELL, notifications))
						.build())
				.withAppMenu(LeftAppMenuBuilder
						.get()
						.addToSection(userItem, HEADER)
						.add(dashboardMenuEntry)
						.add(holidayRequestsMenuEntry)
						.add(substitutionMenuEntry)
						.add(planningMenuEntry)
						.add(approvalMenuEntry)
						.addToSection(preferencesMenuEntry, FOOTER)
						.build())
				.build());
	}
}

