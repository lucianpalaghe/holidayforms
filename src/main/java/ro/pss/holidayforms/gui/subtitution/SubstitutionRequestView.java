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

@SpringComponent
@UIScope
@Route(value = "substitutions", layout = HolidayAppLayout.class)
public class SubstitutionRequestView extends HorizontalLayout implements AfterNavigationObserver {
	final Grid<SubstitutionRequest> grid;
	private final SubstitutionRequestRepository requestRepository;
	private final VerticalLayout container;

	public SubstitutionRequestView(SubstitutionRequestRepository repo) {
		this.requestRepository = repo;
		this.grid = new Grid<>();
		grid.addColumn(r -> r.getRequest().getRequester()).setHeader("Pe cine").setFlexGrow(2);
		grid.addColumn(r -> r.getRequest().getNumberOfDays()).setHeader("Numar de zile").setFlexGrow(1);
		grid.addColumn(r -> r.getRequest().getDateFrom()).setHeader("Incepand cu data").setFlexGrow(1);
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
		Button btnApprove = new Button("Aproba", VaadinIcon.CHECK_CIRCLE.create(), event -> {
			request.approve();
			requestRepository.save(request);
			grid.getDataProvider().refreshItem(request);
		});
		btnApprove.addThemeName("success");
		btnApprove.addThemeName("primary");

		Button btnDeny = new Button("Respinge", VaadinIcon.CLOSE_CIRCLE.create(), event -> {
			request.deny();
			requestRepository.save(request);
			grid.getDataProvider().refreshItem(request);
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
			btnApprove.setText("Aprobat");
			return new HorizontalLayout(btnApprove);
		} else {
			btnApprove.setEnabled(false);
			btnDeny.setEnabled(false);
			btnDeny.setText("Respins");
			return new HorizontalLayout(btnDeny);
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		listSubstitutionRequests();
	}
}