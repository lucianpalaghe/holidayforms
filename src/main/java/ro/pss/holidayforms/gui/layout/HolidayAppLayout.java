package ro.pss.holidayforms.gui.layout;

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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.notification.Notification;
import ro.pss.holidayforms.domain.repo.ApprovalRequestRepository;
import ro.pss.holidayforms.domain.repo.NotificationRepository;
import ro.pss.holidayforms.domain.repo.SubstitutionRequestRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.approval.HolidayApprovalView;
import ro.pss.holidayforms.gui.broadcast.BroadcastEvent;
import ro.pss.holidayforms.gui.broadcast.Broadcaster;
import ro.pss.holidayforms.gui.broadcast.UserUITuple;
import ro.pss.holidayforms.gui.dashboard.DashboardView;
import ro.pss.holidayforms.gui.planning.HolidayPlanningView;
import ro.pss.holidayforms.gui.request.HolidayRequestView;
import ro.pss.holidayforms.gui.subtitution.SubstitutionRequestView;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.github.appreciated.app.layout.entity.Section.FOOTER;
import static com.github.appreciated.app.layout.entity.Section.HEADER;

@Push
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class HolidayAppLayout extends AppLayoutRouterLayout implements Broadcaster.BroadcastListener {
	private final DefaultNotificationHolder notifications;
	private final DefaultBadgeHolder substitutionBadge;
	private final DefaultBadgeHolder approvalBadge;
	private final String approverEmail = "Luminita.Petre";
	private NotificationRepository notificationRepository;

	public HolidayAppLayout(ApprovalRequestRepository approvalRepository, SubstitutionRequestRepository substitutionRepository,
	NotificationRepository notificationRepository)  {
		this.notificationRepository = notificationRepository;
		this.notifications = new DefaultNotificationHolder();
		this.substitutionBadge = new DefaultBadgeHolder();
		this.approvalBadge = new DefaultBadgeHolder();
		User user = SecurityUtils.getLoggedInUser();
		UserMenuItem userItem = new UserMenuItem(user.getName(), user.getEmail(), user.getPhoto());
		LeftNavigationItem holidayRequestsMenuEntry = new LeftNavigationItem(MessageRetriever.get("myHolidayRequests"), VaadinIcon.AIRPLANE.create(), HolidayRequestView.class);
		LeftNavigationItem dashboardMenuEntry = new LeftNavigationItem(MessageRetriever.get("dashboard"), VaadinIcon.LINE_CHART.create(), DashboardView.class);
		LeftNavigationItem substitutionMenuEntry = new LeftNavigationItem(MessageRetriever.get("asReplacer"), VaadinIcon.OFFICE.create(), SubstitutionRequestView.class);
		LeftNavigationItem planningMenuEntry = new LeftNavigationItem(MessageRetriever.get("planningTxt"), VaadinIcon.EDIT.create(), HolidayPlanningView.class);
		LeftNavigationItem approvalMenuEntry = new LeftNavigationItem(MessageRetriever.get("toApprove"), VaadinIcon.USER_CHECK.create(), HolidayApprovalView.class);
		substitutionBadge.bind(substitutionMenuEntry.getBadge());
		approvalBadge.bind(approvalMenuEntry.getBadge());
		approvalBadge.setCount(approvalRepository.findAllByApproverEmailAndStatus(user.getEmail(), ApprovalRequest.Status.NEW).size());
		substitutionBadge.setCount(substitutionRepository.findAllBySubstituteEmailAndStatus(user.getEmail(), SubstitutionRequest.Status.NEW).size());
		VersionMenuItem versionItem = new VersionMenuItem("ver_" + "0.0.4"); // TODO: get version from somewhere
		Broadcaster.register(new UserUITuple(user, UI.getCurrent()), this);
		LeftClickableItem preferencesMenuEntry = new LeftClickableItem(MessageRetriever.get("preferencesTxt"), VaadinIcon.COG.create(),
				clickEvent -> {/*this.notifications.addNotification(new DefaultNotification("Whoops", MessageRetriever.get("notImplementedMsg"))*/});

		LeftClickableItem languageMenuEntry = new LeftClickableItem(MessageRetriever.get("changeLanguage"), VaadinIcon.FLAG.create(),
				clickEvent -> {
					MessageRetriever.switchLocale();
					UI.getCurrent().getPage().reload();
				}

		);

		List<Notification> unreadNotifications = getUserNotifications(SecurityUtils.getLoggedInUser().getEmail(), Notification.Status.NEW);
		for(Notification notification : unreadNotifications) {
			String typeString = MessageRetriever.get("notification_" + notification.getType());
			//DefaultNotification defaultNotification = new DefaultNotification(typeString, notification.getMessage());
			HolidayNotification holidayNotification = new HolidayNotification(typeString, notification.getMessage(), notification);
			 holidayNotification.setNotification(notification);
			//defaultNotification.setCreationTime(LocalDateTime.ofInstant(notification.getCreationDateTime(), ZoneOffset.ofTotalSeconds(0)));
			//notifications.addNotification(defaultNotification);
			notifications.addNotification(holidayNotification);
		}
		notifications.addClickListener(defaultNotification -> {
			HolidayNotification holidayNotification = (HolidayNotification) defaultNotification;
			//Notification notification = notificationRepository.findByCreationDateTimeAndTargetUserEmail(defaultNotification.getCreationTime().toInstant(ZoneOffset.ofTotalSeconds(0)), SecurityUtils.getLoggedInUser().getEmail());
			Notification notification = notificationRepository.findById(holidayNotification.getNotification().getId()).get();
			notification.setChangedDateTime(LocalDateTime.now().toInstant(ZoneOffset.ofTotalSeconds(0)));
			notification.setStatus(Notification.Status.READ);
			notificationRepository.save(notification);
			defaultNotification.setRead(true);
			notifications.removeNotification(defaultNotification);
		});

		init(AppLayoutBuilder
				.get(Behaviour.LEFT_RESPONSIVE)
				.withTitle(MessageRetriever.get("holidays"))
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
						.add(approvalMenuEntry)
						.add(planningMenuEntry)
						.add(languageMenuEntry)
						.add(preferencesMenuEntry)
						.withStickyFooter()
						.addToSection(versionItem, FOOTER)
						.build())
				.build());

	}

    @Override
    public void receiveBroadcast(UI ui, BroadcastEvent message) {
       ui.access(() -> {
            String typeString = MessageRetriever.get("notification_" + message.getType());
           // DefaultNotification defaultNotification = new DefaultNotification(typeString, message.getMessage());
           // defaultNotification.setCreationTime(LocalDateTime.ofInstant(message.getNotification().getCreationDateTime(), ZoneOffset.ofTotalSeconds(0)));
            //notifications.addNotification(defaultNotification);
		   HolidayNotification holidayNotification = new HolidayNotification(typeString, message.getMessage(), message.getNotification());
		  // holidayNotification.setCreationTime(LocalDateTime.ofInstant(message.getNotification().getCreationDateTime(), ZoneOffset.ofTotalSeconds(0)));
		   notifications.addNotification(holidayNotification);

            BroadcastEvent.Type type = message.getType();
            switch (type) {
				case SUBSTITUTE_ADDED:
					substitutionBadge.increase();
                    break;
				case SUBSTITUTE_DELETED:
					substitutionBadge.decrease();
					break;
				case APPROVE_ADDED:
                    approvalBadge.increase();
                    break;
				case APPROVE_DELETED:
					approvalBadge.decrease();
					break;
				case SUBSTITUTE_CHANGED:
				case APPROVE_CHANGED:
					break;
                default:
                    throw new IllegalArgumentException("Unknown BroadcastMessageType:" + type);
            }
        });
    }

    private List<Notification> getUserNotifications(String targetUserEmail, Notification.Status status) {
		List<Notification> result = notificationRepository.findAllByTargetUserEmailAndStatus(targetUserEmail, status);
		return result;
	}

//	@Override
//	public Registration addDetachListener(ComponentEventListener<DetachEvent> listener) {
//		return (Registration) () -> Broadcaster.unregister(SecurityUtils.getLoggedInUser().getEmail(), UI.getCurrent().getUIId());
//	}

}

