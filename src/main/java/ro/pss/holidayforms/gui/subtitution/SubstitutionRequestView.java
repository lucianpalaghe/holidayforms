package ro.pss.holidayforms.gui.subtitution;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.domain.repo.SubstitutionRequestRepository;
import ro.pss.holidayforms.gui.HolidayAppLayout;

@SpringComponent
@UIScope
@Route(value = "substitutions", layout = HolidayAppLayout.class)
public class SubstitutionRequestView extends VerticalLayout {
	final Grid<SubstitutionRequest> grid;
	private final SubstitutionRequestRepository requestRepository;

	public SubstitutionRequestView(SubstitutionRequestRepository repo) {
		this.requestRepository = repo;
		this.grid = new Grid<>();

		grid.addColumn(r -> r.getRequest().getRequester()).setHeader("Pe cine");
		grid.addColumn(r -> r.getRequest().getNumberOfDays()).setHeader("Numar de zile");
		grid.addColumn(r -> r.getRequest().getDateFrom()).setHeader("Incepand cu data");
		grid.addColumn(r -> r.getStatus()).setHeader("Status");
		grid.addColumn(new ComponentRenderer<>(this::getActionButtons));
		setHeightFull();
		add(grid);
		listHolidayRequests();
	}

	void listHolidayRequests() {
		grid.setItems(requestRepository.findAllBySubstituteEmail("lucian.palaghe@pss.ro"));
	}

	private HorizontalLayout getActionButtons(SubstitutionRequest substitutionRequest) {
		Button btnApprove = new Button(VaadinIcon.CHECK_CIRCLE.create(), event -> {
			substitutionRequest.approve();
			requestRepository.save(substitutionRequest);
			grid.getDataProvider().refreshItem(substitutionRequest);
		});
		btnApprove.addThemeName("success");

		Button btnDeny = new Button(VaadinIcon.CLOSE_CIRCLE.create(), event -> {
			substitutionRequest.deny();
			requestRepository.save(substitutionRequest);
			grid.getDataProvider().refreshItem(substitutionRequest);
		});
		btnDeny.addThemeName("error");

		if (substitutionRequest.getStatus() == SubstitutionRequest.Status.NEW) {
			return new HorizontalLayout(btnApprove, btnDeny);
		}

		if (substitutionRequest.getStatus() == SubstitutionRequest.Status.APPROVED) {
			btnApprove.setEnabled(false);
			btnDeny.setEnabled(false);
			return new HorizontalLayout(btnApprove);
		} else {
			btnApprove.setEnabled(false);
			btnDeny.setEnabled(false);
			return new HorizontalLayout(btnDeny);
		}
	}
}