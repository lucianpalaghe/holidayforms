package ro.pss.holidayforms.gui.subtitution;

import com.vaadin.flow.component.button.Button;
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
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.domain.repo.SubstitutionRequestRepository;
import ro.pss.holidayforms.gui.HolidayAppLayout;
import ro.pss.holidayforms.gui.HolidayConfirmationDialog;
import ro.pss.holidayforms.gui.MessageRetriever;

@SpringComponent
@UIScope
@Route(value = "substitutions", layout = HolidayAppLayout.class)
public class SubstitutionRequestView extends HorizontalLayout implements AfterNavigationObserver {
	final Grid<SubstitutionRequest> grid;
	private final SubstitutionRequestRepository requestRepository;
	private final VerticalLayout container;
	private HolidayConfirmationDialog holidayConfDialog;

	public SubstitutionRequestView(SubstitutionRequestRepository repo) {
		this.requestRepository = repo;
		this.grid = new Grid<>();
		grid.addColumn(r -> r.getRequest().getRequester()).setHeader(MessageRetriever.get("appViewGridHeaderWho")).setFlexGrow(2);
		grid.addColumn(r -> r.getRequest().getNumberOfDays()).setHeader(MessageRetriever.get("appViewGridHeaderDays")).setFlexGrow(1);
		grid.addColumn(r -> r.getRequest().getDateFrom()).setHeader(MessageRetriever.get("appViewGridHeaderStart")).setFlexGrow(1);
		grid.addColumn(new ComponentRenderer<>(this::getActionButtons)).setFlexGrow(4);
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

		listSubstitutionRequests();
	}

	void listSubstitutionRequests() {
		grid.setItems(requestRepository.findAllBySubstituteEmail("lucian.palaghe@pss.ro"));
	}

	private HorizontalLayout getActionButtons(SubstitutionRequest request) {
		Button btnApprove = new Button(MessageRetriever.get("approveTxt"), VaadinIcon.CHECK_CIRCLE.create(), event -> {
			String message = String.format(MessageRetriever.get("msgApproveRequestSubst"), request.getRequest().getRequester().getName(), request.getRequest().getType());
			holidayConfDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.APPROVAL, () -> confirmHolidaySubstitution(request), MessageRetriever.get("confDialogHeaderApproveSubst"), message, MessageRetriever.get("approveTxt"), MessageRetriever.get("backTxt"));
			holidayConfDialog.open();
		});
		btnApprove.addThemeName("success");
		btnApprove.addThemeName("primary");

		Button btnDeny = new Button(MessageRetriever.get("denyTxt"), VaadinIcon.CLOSE_CIRCLE.create(), event -> {
			String message = String.format(MessageRetriever.get("msgDenyRequestSubst"), request.getRequest().getRequester().getName(), request.getRequest().getType());
			holidayConfDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.DENIAL, () -> rejectHolidaySubstitution(request), MessageRetriever.get("confDialogHeaderDenySubst"), message, MessageRetriever.get("denyTxt"), MessageRetriever.get("backTxt"));
			holidayConfDialog.open();
		});
		btnDeny.addThemeName("error");

		if (request.getStatus() == SubstitutionRequest.Status.NEW) {
			HorizontalLayout horizontalLayout = new HorizontalLayout(btnApprove, btnDeny);
			horizontalLayout.setMinWidth("10em");
			return horizontalLayout;
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
		grid.getDataProvider().refreshItem(request);
	}

	private void confirmHolidaySubstitution(SubstitutionRequest request) {
		request.approve();
		requestRepository.save(request);
		grid.getDataProvider().refreshItem(request);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		listSubstitutionRequests();
	}
}