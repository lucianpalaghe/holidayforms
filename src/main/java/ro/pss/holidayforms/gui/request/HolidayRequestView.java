package ro.pss.holidayforms.gui.request;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.gui.HolidayAppLayout;

import java.io.IOException;
import java.util.List;

import static ro.pss.holidayforms.pdf.PDFGenerator.fillHolidayRequest;

@SpringComponent
@UIScope
@Route(value = "requests", layout = HolidayAppLayout.class)
@StyleSheet("context://myprogress.css")
public class HolidayRequestView extends HorizontalLayout implements AfterNavigationObserver {
	private final Grid<HolidayRequest> grid;
	private final HolidayRequestRepository requestRepository;
	private final HolidayRequestEditor editor;
	private final Dialog dialog;
	private final VerticalLayout container;
	private final H2 heading;
	private String userId = "lucian.palaghe@pss.ro";

	public HolidayRequestView(HolidayRequestRepository repo, HolidayRequestEditor editor) {
		this.requestRepository = repo;
		this.editor = editor;
		this.editor.setChangeHandler(() -> {
			this.editor.setVisible(false);
			listHolidayRequests();
			mountEditorInDialog(false);
		});

		grid = new Grid<>();
		grid.addColumn(HolidayRequest::getNumberOfDays).setHeader("Nr. de zile").setWidth("min-content");//.setFlexGrow(1);
		grid.addColumn(HolidayRequest::getDateFrom).setHeader("Incepand cu data");//.setFlexGrow(2);
		grid.addColumn(HolidayRequest::getSubstitute).setHeader("Inlocuitor");//.setFlexGrow(5);
		grid.addColumn(new ComponentRenderer<>(this::getActionButtons));//.setFlexGrow(0);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
		grid.setItemDetailsRenderer(getRequestStatusRenderer());

		Button btnAdd = new Button("Adauga cerere", VaadinIcon.PLUS.create());
		btnAdd.addClickListener(e -> {
			this.editor.editHolidayRequest(new HolidayRequest());
			mountEditorInDialog(true);
		});

		HorizontalLayout actions = new HorizontalLayout(btnAdd);
		dialog = new Dialog(editor);
		dialog.setCloseOnOutsideClick(false);

		heading = new H2();
		heading.setVisible(false);

		container = new VerticalLayout();
		container.add(actions, heading, grid, this.editor);
		container.setWidth("100%");
		container.setMaxWidth("70em");
		container.setHeightFull();

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(container);
		setHeightFull();

		listHolidayRequests();
	}

	private ComponentRenderer<HorizontalLayout, HolidayRequest> getRequestStatusRenderer() {
		return new ComponentRenderer<>(holidayRequest -> {
			ListItem initialStep = new ListItem("Creat");
			initialStep.addClassName("active");

			ListItem substituteStep = new ListItem("Inlocuitor");
			if (holidayRequest.getSubstitutionRequest().getStatus() == SubstitutionRequest.Status.APPROVED) {
				substituteStep.addClassName("active");
			} else if (holidayRequest.getSubstitutionRequest().getStatus() == SubstitutionRequest.Status.DENIED) {
				substituteStep.addClassName("denied");
			}

			ListItem teamLeaderStep = new ListItem("Team leader");
			if (holidayRequest.getApprovalRequests().get(0).getStatus() == ApprovalRequest.Status.APPROVED) {
				teamLeaderStep.addClassName("active");
			} else if (holidayRequest.getApprovalRequests().get(0).getStatus() == ApprovalRequest.Status.DENIED) {
				teamLeaderStep.addClassName("denied");
			}

			ListItem projectManagerStep = new ListItem("Project manager");
			if (holidayRequest.getApprovalRequests().get(1).getStatus() == ApprovalRequest.Status.APPROVED) {
				projectManagerStep.addClassName("active");
			} else if (holidayRequest.getApprovalRequests().get(1).getStatus() == ApprovalRequest.Status.DENIED) {
				projectManagerStep.addClassName("denied");
			}

			UnorderedList stepList = new UnorderedList(initialStep, substituteStep, teamLeaderStep, projectManagerStep);
			stepList.setWidthFull();
			stepList.setClassName("progressbar");

			HorizontalLayout stepBarContainer = new HorizontalLayout(stepList);
			stepBarContainer.setWidthFull();
			stepBarContainer.setAlignItems(Alignment.CENTER);
			stepBarContainer.setJustifyContentMode(JustifyContentMode.CENTER);
			return stepBarContainer;
		});
	}

	private HorizontalLayout getActionButtons(HolidayRequest request) {
		Button btnEdit = new Button("Editeaza", VaadinIcon.EDIT.create(), e -> {
			editor.editHolidayRequest(request);
			mountEditorInDialog(true);
		});
		Button btnPrint = new Button("Tipareste", VaadinIcon.PRINT.create(), event -> {
			try {
				fillHolidayRequest(request, request.getRequester());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		btnPrint.addThemeName("success");
		btnPrint.addThemeName("primary");

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		if (request.isStillEditable()) {
			horizontalLayout.add(btnEdit);
		} else {
			horizontalLayout.add(btnPrint);
		}
		return horizontalLayout;
	}

	private void listHolidayRequests() {
		List<HolidayRequest> requests = requestRepository.findAllByRequesterEmail(userId);
		if (requests.isEmpty()) {
			grid.setVisible(false);
			heading.setText("Nu exista nici o cerere de concediu");
			heading.setVisible(true);
		} else {
			heading.setVisible(false);
			grid.setVisible(true);
			grid.setItems(requests);
		}
	}

	private void mountEditorInDialog(boolean mount) {
		if (mount && editor.isVisible()) {
			dialog.removeAll();
			dialog.addComponentAsFirst(this.editor);
			dialog.open();
		} else {
			dialog.close();
			dialog.removeAll();
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		listHolidayRequests();
	}
}