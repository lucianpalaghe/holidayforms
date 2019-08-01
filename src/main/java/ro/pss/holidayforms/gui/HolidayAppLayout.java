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
import com.github.appreciated.app.layout.router.AppLayoutRouterLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import ro.pss.holidayforms.gui.approval.HolidayApprovalView;
import ro.pss.holidayforms.gui.dashboard.DashboardView;
import ro.pss.holidayforms.gui.planning.HolidayPlanningView;
import ro.pss.holidayforms.gui.request.HolidayRequestView;
import ro.pss.holidayforms.gui.subtitution.SubstitutionRequestView;

import static com.github.appreciated.app.layout.entity.Section.FOOTER;
import static com.github.appreciated.app.layout.entity.Section.HEADER;

@Push
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class HolidayAppLayout extends AppLayoutRouterLayout {
	private DefaultNotificationHolder notifications;
	private DefaultBadgeHolder badge = new DefaultBadgeHolder();

	public HolidayAppLayout(DefaultNotificationHolder notifications) {
		this.notifications = notifications;
		this.notifications.addClickListener(notification -> System.out.println(notification.getTitle()));
//		this.notifications.addNotification(new DefaultNotification("title", "description"));

		LeftNavigationItem menuEntry = new LeftNavigationItem("De aprobat", VaadinIcon.USER_CHECK.create(), HolidayApprovalView.class);
		badge.bind(menuEntry.getBadge());
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
						.addToSection(new LeftHeaderItemExt("User Johnson", null, "cat.jpg"), HEADER)
						.add(new LeftNavigationItem("Dashboard", VaadinIcon.LINE_CHART.create(), DashboardView.class))
						.add(new LeftNavigationItem("Cererile mele", VaadinIcon.AIRPLANE.create(), HolidayRequestView.class))
						.add(new LeftNavigationItem("Ca inlocuitor", VaadinIcon.OFFICE.create(), SubstitutionRequestView.class))
						.add(new LeftNavigationItem("Planificare", VaadinIcon.EDIT.create(), HolidayPlanningView.class))
						.add(menuEntry)
						.addToSection(new LeftClickableItem("Preferinte",
								VaadinIcon.COG.create(),
								clickEvent -> {//Notification.show("onClick ...");
									badge.increase();
								}
						), FOOTER)
						.build())
				.build());
	}
}

