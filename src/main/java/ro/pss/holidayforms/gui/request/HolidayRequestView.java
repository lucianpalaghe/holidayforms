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
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.layout.HolidayAppLayout;

import java.io.IOException;
import java.util.List;

import static ro.pss.holidayforms.pdf.PDFGenerator.fillHolidayRequest;

@SpringComponent
@UIScope
@Route(value = "requests", layout = HolidayAppLayout.class)
@StyleSheet("step-progress-bar.css")
@StyleSheet("responsive-buttons.css")
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
		grid.addColumn(HolidayRequest::getNumberOfDays).setHeader(MessageRetriever.get("gridColDaysHeader")).setWidth("min-content").setFlexGrow(1);
		grid.addColumn(HolidayRequest::getType).setHeader(MessageRetriever.get("gridColType")).setFlexGrow(1);
		grid.addColumn(HolidayRequest::getDateFrom).setHeader(MessageRetriever.get("gridColFromDate")).setFlexGrow(1);
		grid.addColumn(HolidayRequest::getSubstitute).setHeader(MessageRetriever.get("gridColReplacer")).setFlexGrow(1);
		grid.addColumn(new ComponentRenderer<>(this::getActionButtons)).setFlexGrow(2);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
		grid.setItemDetailsRenderer(getRequestStatusRenderer());

		Button btnAdd = new Button(MessageRetriever.get("addHolidayRequest"), VaadinIcon.PLUS.create());
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
			ListItem initialStep = new ListItem(MessageRetriever.get("created"));
			initialStep.addClassName("active");

			ListItem substituteStep = new ListItem(MessageRetriever.get("replacerName"));
			if (holidayRequest.getSubstitutionRequest().getStatus() == SubstitutionRequest.Status.APPROVED) {
				substituteStep.addClassName("active");
			} else if (holidayRequest.getSubstitutionRequest().getStatus() == SubstitutionRequest.Status.DENIED) {
				substituteStep.addClassName("denied");
			}

			ListItem teamLeaderStep = new ListItem(MessageRetriever.get("teamLeader"));
			if (holidayRequest.getApprovalRequests().get(0).getStatus() == ApprovalRequest.Status.APPROVED) {
				teamLeaderStep.addClassName("active");
			} else if (holidayRequest.getApprovalRequests().get(0).getStatus() == ApprovalRequest.Status.DENIED) {
				teamLeaderStep.addClassName("denied");
			}

			ListItem projectManagerStep = new ListItem(MessageRetriever.get("projectManager"));
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
		Button btnEdit = new Button(MessageRetriever.get("editHoliday"), VaadinIcon.EDIT.create(), e -> {
			editor.editHolidayRequest(request);
			mountEditorInDialog(true);
		});
		btnEdit.addClassName("responsive");
		btnEdit.addClassName("responsive");

		Button btnPrint = new Button(MessageRetriever.get("printHoliday"), VaadinIcon.PRINT.create(), event -> {
			try {
				fillHolidayRequest(request, request.getRequester());

				// Create the stream resource and give it a file name
//				String filename = "CO_Lucian Palaghe_2019-08-14.pdf";
//				StreamResource resource = new StreamResource(filename, () -> {
//					try {
//						return new FileInputStream(filename);
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					}
//				});
//
//				// These settings are not usually necessary. MIME type
//				// is detected automatically from the file name, but
//				// setting it explicitly may be necessary if the file
//				// suffix is not ".pdf".
//				resource.setContentType("application/pdf");
//				resource.getStream().setParameter(
//						"Content-Disposition",
//						"attachment; filename="+filename);
//
//				// Extend the print button with an opener
//				// for the PDF resource
//				BrowserWindowOpener opener = new BrowserWindowOpener(resource);
//				opener.extend(print);
//
//				name.setEnabled(false);
//				ok.setEnabled(false);
//				print.setEnabled(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		btnPrint.addThemeName("success");
		btnPrint.addThemeName("primary");
		btnPrint.addClassName("responsive");

		HorizontalLayout horizontalLayout = new HorizontalLayout();
//		horizontalLayout.setSpacing(false);
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
			heading.setText(MessageRetriever.get("noHolidayRequest"));
			heading.setVisible(true);
		} else {
			heading.setVisible(false);
			grid.setVisible(true);
			grid.setItems(requests);
//			grid.setDetailsVisible(requests.get(0), true);
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