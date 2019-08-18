package ro.pss.holidayforms.gui.subtitution;

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
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.SubstitutionRequestRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.dialog.HolidayConfirmationDialog;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.gui.notification.Broadcaster;
import ro.pss.holidayforms.gui.notification.NotificationService;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;
import ro.pss.holidayforms.gui.notification.broadcast.UserUITuple;

@SpringComponent
@UIScope
@Route(value = "substitutions", layout = HolidayAppLayout.class)
@StyleSheet("responsive-buttons.css")
public class SubstitutionRequestView extends HorizontalLayout implements AfterNavigationObserver, Broadcaster.BroadcastListener {
	private final Grid<SubstitutionRequest> grid;
	private final SubstitutionRequestRepository requestRepository;
	private final VerticalLayout container;
	private HolidayConfirmationDialog holidayConfDialog;

	@Autowired
	private NotificationService notificationService;

	public SubstitutionRequestView(SubstitutionRequestRepository repo) {
		this.requestRepository = repo;
		this.grid = new Grid<>();
		grid.addColumn(r -> r.getRequest().getRequester()).setHeader(MessageRetriever.get("appViewGridHeaderWho")).setFlexGrow(1);
		grid.addColumn(r -> r.getRequest().getNumberOfDays()).setHeader(MessageRetriever.get("appViewGridHeaderDays")).setFlexGrow(1);
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

		User user = SecurityUtils.getLoggedInUser();
		listSubstitutionRequests(user.getEmail());
	}

	private void listSubstitutionRequests(String userEmail) {
		grid.setItems(requestRepository.findAllBySubstituteEmail(userEmail));
	}

	private HorizontalLayout getActionButtons(SubstitutionRequest request) {
		Button btnApprove = new Button(MessageRetriever.get("substituteTxt"), VaadinIcon.CHECK_CIRCLE.create(), event -> {
			String message = String.format(MessageRetriever.get("msgApproveRequestSubst"), request.getRequest().getRequester().getName(), request.getRequest().getType());
			holidayConfDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.APPROVAL, () -> confirmHolidaySubstitution(request), MessageRetriever.get("confDialogHeaderApproveSubst"), message, MessageRetriever.get("approveTxt"), MessageRetriever.get("backTxt"));
			holidayConfDialog.open();
		});
		btnApprove.addThemeName("success");
		btnApprove.addThemeName("primary");
		btnApprove.addClassName("responsive");

		Button btnDeny = new Button(MessageRetriever.get("denyTxt"), VaadinIcon.CLOSE_CIRCLE.create(), event -> {
			String message = String.format(MessageRetriever.get("msgDenyRequestSubst"), request.getRequest().getRequester().getName(), request.getRequest().getType());
			holidayConfDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.DENIAL, () -> rejectHolidaySubstitution(request), MessageRetriever.get("confDialogHeaderDenySubst"), message, MessageRetriever.get("denyTxt"), MessageRetriever.get("backTxt"));
			holidayConfDialog.open();
		});
		btnDeny.addThemeName("error");
		btnDeny.addClassName("responsive");

		if (request.getStatus() == SubstitutionRequest.Status.NEW) {
			return new HorizontalLayout(btnApprove, btnDeny);
		}

		if (request.getStatus() == SubstitutionRequest.Status.APPROVED) {
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

	private void rejectHolidaySubstitution(SubstitutionRequest request) {
		request.deny();
		requestRepository.save(request);
		notificationService.substitutionDenied(request);
		grid.getDataProvider().refreshItem(request);
		ComponentUtil.getData(UI.getCurrent(), HolidayAppLayout.class).decreaseSubstitutionBadgeCount();
	}

	private void confirmHolidaySubstitution(SubstitutionRequest request) {
		request.approve();
		requestRepository.save(request);
		notificationService.substitutionAccepted(request);
		grid.getDataProvider().refreshItem(request);
		ComponentUtil.getData(UI.getCurrent(), HolidayAppLayout.class).decreaseSubstitutionBadgeCount();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		listSubstitutionRequests(SecurityUtils.getLoggedInUser().getEmail());

	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		Broadcaster.register(new UserUITuple(SecurityUtils.getLoggedInUser(), UI.getCurrent()), this);
	}

	@Override
	public void receiveBroadcast(UI ui, BroadcastEvent message) {
		if (BroadcastEvent.Type.SUBSTITUTE_ADDED.equals(message.getType())
				|| BroadcastEvent.Type.SUBSTITUTE_CHANGED.equals(message.getType())
				|| BroadcastEvent.Type.SUBSTITUTE_DELETED.equals(message.getType())) {
			ui.access(() -> this.listSubstitutionRequests(message.getTargetUserId()));
		}
	}
}