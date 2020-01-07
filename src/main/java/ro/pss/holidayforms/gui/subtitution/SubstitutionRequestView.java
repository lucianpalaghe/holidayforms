package ro.pss.holidayforms.gui.subtitution;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Emphasis;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.dialog.HolidayConfirmationDialog;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.gui.notification.Broadcaster;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;
import ro.pss.holidayforms.gui.notification.broadcast.UserUITuple;
import ro.pss.holidayforms.service.HolidaySubstitutionService;

import java.util.List;

@Slf4j
@SpringComponent
@UIScope
@Route(value = "substitutions", layout = HolidayAppLayout.class)
@StyleSheet("responsive-buttons.css")
public class SubstitutionRequestView extends HorizontalLayout implements AfterNavigationObserver, Broadcaster.BroadcastListener, HasDynamicTitle {
	private final Grid<SubstitutionRequest> grid;
	private final H2 heading;
	@Autowired
	private HolidaySubstitutionService service;
	private HolidayConfirmationDialog holidayConfDialog;

	public SubstitutionRequestView() {
		this.grid = new Grid<>();
		grid.addColumn(r -> r.getRequest().getRequester()).setHeader(MessageRetriever.get("appViewGridHeaderWho")).setFlexGrow(1);
		grid.addColumn(r -> r.getRequest().getNumberOfDays()).setHeader(MessageRetriever.get("appViewGridHeaderDays")).setFlexGrow(1);
		grid.addColumn(r -> r.getRequest().getDateFrom()).setHeader(MessageRetriever.get("appViewGridHeaderStart")).setFlexGrow(1);
		grid.addColumn(new ComponentRenderer<>(this::getActionButtons)).setFlexGrow(2);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
		grid.setItemDetailsRenderer(getRequestInfoRenderer());
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

	private void listSubstitutionRequests(String userEmail) {
		List<SubstitutionRequest> substitutionRequests = service.getSubstitutionRequests(userEmail);
		if (substitutionRequests.isEmpty()) {
			grid.setVisible(false);
			heading.setText(MessageRetriever.get("noSubstitutionRequests"));
			heading.setVisible(true);
		} else {
			heading.setVisible(false);
			grid.setVisible(true);
			grid.setItems(substitutionRequests);
		}
	}

	private HorizontalLayout getActionButtons(SubstitutionRequest request) {
		Button btnApprove = new Button(MessageRetriever.get("substituteTxt"), VaadinIcon.CHECK_CIRCLE.create(), event -> {
			String message = String.format(MessageRetriever.get("msgApproveRequestSubst"), request.getRequest().getRequester().getName(), request.getRequest().getType());
			holidayConfDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.APPROVAL, () -> confirmHolidaySubstitution(request), MessageRetriever.get("confDialogHeaderApproveSubst"), message, MessageRetriever.get("approveTxt"), MessageRetriever.get("backTxt"));
			holidayConfDialog.open();
		});
		btnApprove.addThemeNames("success", "primary");
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

	private ComponentRenderer<HorizontalLayout, SubstitutionRequest> getRequestInfoRenderer() {
		return new ComponentRenderer<>(substitutionRequest -> {
			HorizontalLayout detailsContainer = new HorizontalLayout();
			detailsContainer.setWidthFull();

			String comments = substitutionRequest.getRequest().getComments();
			if (comments.isEmpty()) {
				comments = MessageRetriever.get("msgNoAdditionalComments");
				Emphasis em = new Emphasis();
				em.setText(comments);
				detailsContainer.add(em);
				return detailsContainer;
			}
			TextArea areaComments = new TextArea();
			areaComments.setValue(comments);
			areaComments.setReadOnly(true);
			areaComments.setWidthFull();
			detailsContainer.add(areaComments);
			return detailsContainer;
		});
	}

	private void rejectHolidaySubstitution(SubstitutionRequest request) {
		service.denyRequest(request);
		grid.getDataProvider().refreshItem(request);
	}

	private void confirmHolidaySubstitution(SubstitutionRequest request) {
		service.approveRequest(request);
		grid.getDataProvider().refreshItem(request);
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
			ui.access(() -> {
				if (holidayConfDialog != null) {
					holidayConfDialog.close();
				}
				log.warn("Refreshing grid");
				this.listSubstitutionRequests(message.getTargetUserId());
			});
		}
	}

	@Override
	public String getPageTitle() {
		return MessageRetriever.get("titleSubstitutions");
	}
}