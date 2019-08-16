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
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.olli.FileDownloadWrapper;
import ro.pss.holidayforms.config.security.CustomUserPrincipal;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static ro.pss.holidayforms.pdf.PDFGenerator.fillHolidayRequest;

@SpringComponent
@UIScope
@Slf4j
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

			ListItem substituteStep = new ListItem(holidayRequest.getSubstitutionRequest().getSubstitute().getName());
            if (holidayRequest.getSubstitutionRequest().getStatus() == SubstitutionRequest.Status.APPROVED) {
                substituteStep.addClassName("active");
            } else if (holidayRequest.getSubstitutionRequest().getStatus() == SubstitutionRequest.Status.DENIED) {
                substituteStep.addClassName("denied");
            }

			ApprovalRequest approvalRequest = holidayRequest.getApprovalRequests().get(0);
			ListItem teamLeaderStep = new ListItem(approvalRequest.getApprover().getName());
			if (approvalRequest.getStatus() == ApprovalRequest.Status.APPROVED) {
                teamLeaderStep.addClassName("active");
			} else if (approvalRequest.getStatus() == ApprovalRequest.Status.DENIED) {
                teamLeaderStep.addClassName("denied");
            }

			ApprovalRequest approvalRequest2 = holidayRequest.getApprovalRequests().get(1);
			ListItem projectManagerStep = new ListItem(approvalRequest2.getApprover().getName());
			if (approvalRequest2.getStatus() == ApprovalRequest.Status.APPROVED) {
                projectManagerStep.addClassName("active");
			} else if (approvalRequest2.getStatus() == ApprovalRequest.Status.DENIED) {
                projectManagerStep.addClassName("denied");
            }

			ApprovalRequest approvalRequest3 = holidayRequest.getApprovalRequests().get(2);
			ListItem hrStep = new ListItem(approvalRequest3.getApprover().getName());
			if (approvalRequest3.getStatus() == ApprovalRequest.Status.APPROVED) {
				hrStep.addClassName("active");
			} else if (approvalRequest3.getStatus() == ApprovalRequest.Status.DENIED) {
				hrStep.addClassName("denied");
			}

			UnorderedList stepList = new UnorderedList(initialStep, substituteStep, teamLeaderStep, projectManagerStep, hrStep);
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
        Button btnSave = new Button(MessageRetriever.get("saveHoliday"), VaadinIcon.PRINT.create(), event -> {
        });
		btnSave.addThemeName("success");
		btnSave.addThemeName("primary");
		btnSave.addClassName("responsive");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
//		horizontalLayout.setSpacing(false);
        if (request.isStillEditable()) {
            horizontalLayout.add(btnEdit);
        } else {
            horizontalLayout.add(getButtonWrapperWithPdfDocument(btnSave, request));
        }
        return horizontalLayout;
    }

    private FileDownloadWrapper getButtonWrapperWithPdfDocument(Button btn, HolidayRequest request) {
        FileDownloadWrapper buttonWrapper = null;
        StreamResource res = null;
        try {
            PDDocument doc = fillHolidayRequest(request);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.getCurrentAccessPermission().setReadOnly();
            doc.save(baos);
            baos.close();
            doc.close();
            String filename = request.getType() + "_" + request.getRequester().getName().replace(" ", "_") + "_" + request.getDateFrom() + ".pdf";
            res = new StreamResource(filename, (InputStreamFactory) () -> new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            log.debug("Error getting the wrapper for save pdf button", e);
        }
        buttonWrapper = (new FileDownloadWrapper(res));
        buttonWrapper.wrapComponent(btn);
        return buttonWrapper;
    }

    private void listHolidayRequests() {
        User user = ((CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<HolidayRequest> requests = requestRepository.findAllByRequesterEmail(user.getEmail());
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