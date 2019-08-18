package ro.pss.holidayforms.gui.approval;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.repo.ApprovalRequestRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.dialog.HolidayConfirmationDialog;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.gui.notification.Broadcaster;
import ro.pss.holidayforms.gui.notification.NotificationService;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;
import ro.pss.holidayforms.gui.notification.broadcast.UserUITuple;

@SpringComponent
@UIScope
@Route(value = "approvals", layout = HolidayAppLayout.class)
@StyleSheet("responsive-buttons.css")
public class HolidayApprovalView extends HorizontalLayout implements AfterNavigationObserver, Broadcaster.BroadcastListener {
	private final Grid<ApprovalRequest> grid;
	private final ApprovalRequestRepository requestRepository;
	private final VerticalLayout container;
	private HolidayConfirmationDialog holidayConfDialog;
	private final NotificationService notificationService;

	public HolidayApprovalView(ApprovalRequestRepository repo, NotificationService notificationService) {
		this.requestRepository = repo;
		this.notificationService = notificationService;
		this.grid = new Grid<>();
		grid.addColumn(r -> r.getRequest().getRequester()).setHeader(MessageRetriever.get("appViewGridHeaderWho")).setFlexGrow(1);
		grid.addColumn(r -> r.getRequest().getNumberOfDays()).setHeader(MessageRetriever.get("appViewGridHeaderDays")).setFlexGrow(1);
		grid.addColumn(r -> r.getRequest().getSubstitute()).setHeader(MessageRetriever.get("appViewGridHeaderSubstitute")).setFlexGrow(1);
		grid.addColumn(r -> r.getRequest().getType()).setHeader(MessageRetriever.get("gridColType")).setFlexGrow(1);
		grid.addColumn(r -> r.getRequest().getDateFrom()).setHeader(MessageRetriever.get("appViewGridHeaderStart")).setFlexGrow(1);
		grid.addColumn(new ComponentRenderer<>(this::getActionButtons)).setFlexGrow(2);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);

		container = new VerticalLayout();
		container.add(grid);
		container.setWidth("100%");
		container.setMaxWidth("70em");
		container.setHeightFull();

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(container);
		setHeightFull();
		listApprovalRequests(SecurityUtils.getLoggedInUser().getEmail());
	}

	private void listApprovalRequests(String userEmail) {
		grid.setItems(requestRepository.findAllByApproverEmail(userEmail));
	}

	private HorizontalLayout getActionButtons(ApprovalRequest request) {
		Button btnApprove = new Button(MessageRetriever.get("approveTxt"), VaadinIcon.CHECK_CIRCLE.create(), event -> {
			String messageBody = String.format(MessageRetriever.get("msgApproveRequest"),
					request.getRequest().getType(),
					request.getRequest().getRequester().getName());
			holidayConfDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.APPROVAL,
					() -> confirmHolidayApproval(request),
					MessageRetriever.get("confDialogHeaderApprove"),
					messageBody,
					MessageRetriever.get("approveTxt"),
					MessageRetriever.get("backTxt"));
			holidayConfDialog.open();
		});
		btnApprove.addThemeNames("success", "primary");
		btnApprove.addClassName("responsive");

		Button btnDeny = new Button(MessageRetriever.get("denyTxt"), VaadinIcon.CLOSE_CIRCLE.create(), event -> {
			String message = String.format(MessageRetriever.get("msgDenyRequest"), request.getRequest().getType(), request.getRequest().getRequester().getName());
			holidayConfDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.DENIAL, () -> rejectHolidayApproval(request), MessageRetriever.get("confDialogHeaderDeny"), message, MessageRetriever.get("denyTxt"), MessageRetriever.get("backTxt"));
			holidayConfDialog.open();
		});
		btnDeny.addThemeNames("error");
		btnDeny.addClassName("responsive");

		if (request.getStatus() == ApprovalRequest.Status.NEW) {
			return new HorizontalLayout(btnApprove, btnDeny);
		}

		if (request.getStatus() == ApprovalRequest.Status.APPROVED) {
			btnDeny.setEnabled(false);
			btnApprove.setEnabled(false);
			btnApprove.setText(MessageRetriever.get("approvedTxt"));
			return new HorizontalLayout(btnApprove);
		} else {
			btnApprove.setEnabled(false);
			btnDeny.setEnabled(false);
			btnDeny.setText(MessageRetriever.get("deniedTxt"));
			return new HorizontalLayout(btnDeny);
		}
	}

	private void confirmHolidayApproval(ApprovalRequest request) {
		request.approve();
		requestRepository.save(request);
		notificationService.approvalAccepted(request);
		grid.getDataProvider().refreshItem(request);
		ComponentUtil.getData(UI.getCurrent(), HolidayAppLayout.class).decreaseApprovalBadgeCount();
	}

	private void rejectHolidayApproval(ApprovalRequest request) {
		request.deny();
		requestRepository.save(request);
		notificationService.approvalDenied(request);
		grid.getDataProvider().refreshItem(request);
		ComponentUtil.getData(UI.getCurrent(), HolidayAppLayout.class).decreaseApprovalBadgeCount();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		listApprovalRequests(SecurityUtils.getLoggedInUser().getEmail());
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		Broadcaster.register(new UserUITuple(SecurityUtils.getLoggedInUser(), UI.getCurrent()), this);
	}

	@Override
	public void receiveBroadcast(UI ui, BroadcastEvent message) {
		if (BroadcastEvent.Type.APPROVE_ADDED.equals(message.getType())
				|| BroadcastEvent.Type.APPROVE_CHANGED.equals(message.getType())
				|| BroadcastEvent.Type.APPROVE_DELETED.equals(message.getType())) {
			ui.access(() -> this.listApprovalRequests(message.getTargetUserId()));
		}
	}
}