package ro.pss.holidayforms.gui.layout;

import com.github.appreciated.app.layout.behaviour.Behaviour;
import com.github.appreciated.app.layout.builder.AppLayoutBuilder;
import com.github.appreciated.app.layout.component.appbar.AppBarBuilder;
import com.github.appreciated.app.layout.component.menu.left.builder.LeftAppMenuBuilder;
import com.github.appreciated.app.layout.component.menu.left.items.LeftClickableItem;
import com.github.appreciated.app.layout.component.menu.left.items.LeftNavigationItem;
import com.github.appreciated.app.layout.entity.DefaultBadgeHolder;
import com.github.appreciated.app.layout.notification.DefaultNotificationHolder;
import com.github.appreciated.app.layout.notification.NotificationsChangeListener;
import com.github.appreciated.app.layout.notification.component.AppBarNotificationButton;
import com.github.appreciated.app.layout.notification.entitiy.DefaultNotification;
import com.github.appreciated.app.layout.router.AppLayoutRouterLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.notification.Notification;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.approval.HolidayApprovalView;
import ro.pss.holidayforms.gui.clocking.ClockingView;
import ro.pss.holidayforms.gui.dashboard.DashboardView;
import ro.pss.holidayforms.gui.edituser.UsersAdministrationView;
import ro.pss.holidayforms.gui.info.HolidayInformationView;
import ro.pss.holidayforms.gui.notification.Broadcaster;
import ro.pss.holidayforms.gui.notification.NotificationService;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;
import ro.pss.holidayforms.gui.notification.broadcast.UserUITuple;
import ro.pss.holidayforms.gui.planning.HolidayPlanningView;
import ro.pss.holidayforms.gui.preferences.UserPreferencesView;
import ro.pss.holidayforms.gui.request.HolidayRequestView;
import ro.pss.holidayforms.gui.subtitution.SubstitutionRequestView;
import ro.pss.holidayforms.service.UserPreferenceService;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.github.appreciated.app.layout.entity.Section.FOOTER;
import static com.github.appreciated.app.layout.entity.Section.HEADER;

@Push
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class HolidayAppLayout extends AppLayoutRouterLayout implements Broadcaster.BroadcastListener {
	private final DefaultNotificationHolder notifications;
	private final DefaultBadgeHolder substitutionBadge;
	private final DefaultBadgeHolder approvalBadge;
	private final NotificationService notificationService;

	@Autowired
	public HolidayAppLayout(NotificationService notificationService, UserPreferenceService userPreferenceService) {
		this.notificationService = notificationService;
		this.notifications = new DefaultNotificationHolder();
		this.substitutionBadge = new DefaultBadgeHolder();
		this.approvalBadge = new DefaultBadgeHolder();

		UI currentUI = UI.getCurrent();
		ComponentUtil.setData(currentUI, HolidayAppLayout.class, this);
		User user = SecurityUtils.getLoggedInUser();
		UserMenuItem userItem = new UserMenuItem(user.getName(), user.getEmail(), user.getPhoto());
		LeftNavigationItem holidayRequestsMenuEntry = new LeftNavigationItem(MessageRetriever.get("myHolidayRequests"), VaadinIcon.AIRPLANE.create(), HolidayRequestView.class);
		LeftNavigationItem dashboardMenuEntry = new LeftNavigationItem(MessageRetriever.get("menuDashboard"), VaadinIcon.LINE_CHART.create(), DashboardView.class);
		LeftNavigationItem planningMenuEntry = new LeftNavigationItem(MessageRetriever.get("menuPlanning"), VaadinIcon.EDIT.create(), HolidayPlanningView.class);
		LeftNavigationItem approvalMenuEntry = new LeftNavigationItem(MessageRetriever.get("menuApprovals"), VaadinIcon.USER_CHECK.create(), HolidayApprovalView.class);
		LeftNavigationItem clockingMenuEntry = new LeftNavigationItem(MessageRetriever.get("menuClockings"),
																	  VaadinIcon.USER_CLOCK.create(), ClockingView.class);
		LeftNavigationItem infoMenuEntry = new LeftNavigationItem(MessageRetriever.get("menuInfo"), VaadinIcon.QUESTION_CIRCLE_O.create(), HolidayInformationView.class);
		LeftNavigationItem preferencesMenuEntry = new LeftNavigationItem(MessageRetriever.get("menuPreferences"), VaadinIcon.COG.create(), UserPreferencesView.class);

		if (userPreferenceService.findByEmployeeEmail(user.getEmail()).isShowNotifications()) {
			loadUserNotifications();
		}

		VersionMenuItem versionItem = new VersionMenuItem("ver_" + "0.0.7"); // TODO: get version from somewhere
		Broadcaster.register(new UserUITuple(user, currentUI), this);
		LeftClickableItem logoutMenuEntry = new LeftClickableItem(MessageRetriever.get("menuLogout"), VaadinIcon.EXIT.create(), clickEvent -> {
			currentUI.getPage().executeJavaScript("window.location.href='logout'");
			currentUI.getSession().close();
		});
		LeftAppMenuBuilder menuBuilder = LeftAppMenuBuilder.get();
		menuBuilder.addToSection(userItem, HEADER);
		Set<Component> menuEntries = new LinkedHashSet<>();
		menuEntries.add(dashboardMenuEntry);
		menuEntries.add(holidayRequestsMenuEntry);
		setupSubstitutionComponents(user, menuEntries);
		menuEntries.add(planningMenuEntry);

		if (SecurityUtils.loggedInUserHasRole(User.Role.PROJECT_MANGER) || SecurityUtils.loggedInUserHasRole(User.Role.ADMIN)) {
			setupApprovalComponents(user, menuEntries);
		}

		if (SecurityUtils.loggedInUserHasRole(User.Role.HR) || SecurityUtils.loggedInUserHasRole(User.Role.ADMIN)) {
			LeftNavigationItem userAdminEntry = new LeftNavigationItem(MessageRetriever.get("menuEditUser"), VaadinIcon.USERS.create(), UsersAdministrationView.class);
			menuEntries.add(userAdminEntry);
		}
    
    menuEntries.add(clockingMenuEntry);
		menuEntries.add(infoMenuEntry);
		menuEntries.add(preferencesMenuEntry);
		menuEntries.add(logoutMenuEntry);
		for (Component entry : menuEntries) {
			menuBuilder.add(entry);
		}
		menuBuilder.withStickyFooter().addToSection(versionItem, FOOTER);

		init(AppLayoutBuilder
				.get(Behaviour.LEFT_RESPONSIVE)
				.withTitle(MessageRetriever.get("holidays"))
				.withIcon("pss-logo.png")
				.withAppBar(AppBarBuilder
						.get()
						.add(new AppBarNotificationButton(VaadinIcon.BELL, notifications))
						.build())
				.withAppMenu(menuBuilder.build())
				.build());
	}

	private void setupSubstitutionComponents(User user, Set<Component> menuEntries) {
		LeftNavigationItem substitutionMenuEntry = new LeftNavigationItem(MessageRetriever.get("menuSubstitutions"), VaadinIcon.OFFICE.create(), SubstitutionRequestView.class);
		substitutionBadge.bind(substitutionMenuEntry.getBadge());
		substitutionBadge.setCount(notificationService.getSubstitutionRequestsCount(user.getEmail()));
		menuEntries.add(substitutionMenuEntry);
	}

	private void setupApprovalComponents(User user, Set<Component> menuEntries) {
		LeftNavigationItem approvalMenuEntry = new LeftNavigationItem(MessageRetriever.get("menuApprovals"), VaadinIcon.USER_CHECK.create(), HolidayApprovalView.class);
		approvalBadge.bind(approvalMenuEntry.getBadge());
		approvalBadge.setCount(notificationService.getApprovalRequestsCount(user.getEmail()));
		menuEntries.add(approvalMenuEntry);
	}

	private void loadUserNotifications() {
		List<Notification> userNotifications = notificationService.getNotifications(SecurityUtils.getLoggedInUser().getEmail());
		for (Notification notification : userNotifications) {
			String title = MessageRetriever.get("notificationTitle_" + notification.getType());
			String description = String.format(MessageRetriever.get("notificationBody_" + notification.getType()), notification.getUserIdentifier());
			HolidayNotification holidayNotification = new HolidayNotification(title, description, notification);
			holidayNotification.setNotification(notification);
			holidayNotification.setCreationTime(notification.getCreationDateTime());
			if (notification.getStatus().equals(Notification.Status.READ)) {
				holidayNotification.setRead(true);
			}
			notifications.addNotification(holidayNotification);
		}

		notifications.addNotificationsChangeListener(new NotificationsChangeListener<>() {
			@Override
			public void onNotificationRemoved(DefaultNotification notification) {
				notificationService.deleteNotification(((HolidayNotification) notification).getNotification());
			}
		});

		notifications.addClickListener(this::onNotificationClicked);
	}

	@Override
	public void receiveBroadcast(UI ui, BroadcastEvent event) {
		ui.access(() -> {
			BroadcastEvent.Type type = event.getType();
			if (type == BroadcastEvent.Type.WORKLOGS_POSTED) { //TODO: make this cleaner
				com.vaadin.flow.component.notification.Notification.show(MessageRetriever.get("worklogsPosted"), 3000, com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER);
			} else {
				String title = MessageRetriever.get("notificationTitle_" + type);
				String description = String.format(MessageRetriever.get("notificationBody_" + type), event.getUserIdentifier());
				HolidayNotification holidayNotification = new HolidayNotification(title, description, event.getNotification());
				notifications.addNotification(holidayNotification);
				updateBadges(type);
			}
		});
	}

	private void updateBadges(BroadcastEvent.Type type) {
		if (type == BroadcastEvent.Type.SUBSTITUTE_ADDED) {
			substitutionBadge.increase();
		} else if (type == BroadcastEvent.Type.SUBSTITUTE_DELETED || type == BroadcastEvent.Type.SUBSTITUTE_ACCEPTED || type == BroadcastEvent.Type.SUBSTITUTE_DENIED) {
			substitutionBadge.decrease();
		} else if (type == BroadcastEvent.Type.APPROVE_ADDED) {
			approvalBadge.increase();
		} else if (type == BroadcastEvent.Type.APPROVE_DELETED || type == BroadcastEvent.Type.APPROVER_ACCEPTED || type == BroadcastEvent.Type.APPROVER_DENIED) {
			approvalBadge.decrease();
		}
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		ComponentUtil.setData(UI.getCurrent(), HolidayAppLayout.class, this);
	}

	private void onNotificationClicked(DefaultNotification defaultNotification) {
		Notification notification = ((HolidayNotification) defaultNotification).getNotification();
		if (notification.getStatus() == Notification.Status.READ) {
			return;
		}

		notification.setChangedDateTime(LocalDateTime.now());
		notification.setStatus(Notification.Status.READ);
		notificationService.saveNotification(notification);
	}
}