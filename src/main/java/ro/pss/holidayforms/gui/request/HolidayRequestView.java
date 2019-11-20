package ro.pss.holidayforms.gui.request;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.olli.FileDownloadWrapper;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.gui.notification.Broadcaster;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;
import ro.pss.holidayforms.gui.notification.broadcast.UserUITuple;
import ro.pss.holidayforms.service.HolidayRequestService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.*;

import static ro.pss.holidayforms.pdf.PDFGenerator.fillHolidayRequest;

/**
 * There is an Vaadin issue manifested in this view, related to the currently selected grid item and refreshes/broadcasts from other users.
 * The problem causes a red pop-up notification to appear in the top-right corner of the page displaying the following error message:
 * <b>(TypeError) : Cannot read property 'doSelection' of undefined</b>
 * <br>You can find more info on the following threads:
 *
 * @see <a href="https://github.com/vaadin/flow/issues/4997">https://github.com/vaadin/flow/issues/4997</a>
 * @see <a href="https://vaadin.com/forum/thread/17527564/typeerror-cannot-read-property-dodeselector-of-undefined-vaadin-10">https://vaadin.com/forum/thread/17527564/typeerror-cannot-read-property-dodeselector-of-undefined-vaadin-10</a>
 */

@SpringComponent
@UIScope
@Slf4j
@Route(value = "requests", layout = HolidayAppLayout.class)
@StyleSheet("step-progress-bar.css")
@StyleSheet("responsive-buttons.css")
public class HolidayRequestView extends HorizontalLayout implements AfterNavigationObserver, Broadcaster.BroadcastListener, HasDynamicTitle {
	private final Grid<HolidayRequest> grid;
	private final HolidayRequestEditor editor;
	private final Dialog dialog;
	private final H2 heading;
	@Autowired
	private HolidayRequestService service;

	public HolidayRequestView(HolidayRequestEditor editor) {
		this.editor = editor;
		this.editor.setChangeHandler(() -> {
			this.editor.setVisible(false);
			listHolidayRequests(SecurityUtils.getLoggedInUser().getEmail());
			mountEditorInDialog(false);
		});

		grid = new Grid<>();
		grid.addColumn(HolidayRequest::getNumberOfDays).setHeader(MessageRetriever.get("gridColDaysHeader")).setWidth("min-content").setFlexGrow(1);
		grid.addColumn(HolidayRequest::getType).setHeader(MessageRetriever.get("gridColType")).setFlexGrow(1);
		grid.addColumn(HolidayRequest::getDateFrom).setHeader(MessageRetriever.get("gridColFromDate")).setFlexGrow(1);
		grid.addColumn(HolidayRequestView::getSubstituteList).setHeader(MessageRetriever.get("gridColReplacer")).setFlexGrow(1);
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
		dialog.setWidth("25em");

		heading = new H2();
		heading.setVisible(false);

		VerticalLayout container = new VerticalLayout();
		container.add(actions, heading, grid, this.editor);
		container.setWidth("100%");
		container.setMaxWidth("70em");
		container.setHeightFull();

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(container);
		setHeightFull();
	}

	private static String getSubstituteList(HolidayRequest holidayRequest) {
		return holidayRequest.getSubstitutes().stream()
				.map(User::getName)
				.collect(Collectors.joining(", "));
	}

	private ComponentRenderer<HorizontalLayout, HolidayRequest> getRequestStatusRenderer() {
		return new ComponentRenderer<>(holidayRequest -> {
			UnorderedList stepList = new UnorderedList();

			ListItem initialStep = new ListItem(MessageRetriever.get("created"));
			initialStep.addClassName("active");
			stepList.add(initialStep);

//			ListItem substituteStep = new ListItem(holidayRequest.getSubstitutionRequest().getSubstitute().getName());
//			if (holidayRequest.getSubstitutionRequest().getStatus() == SubstitutionRequest.Status.APPROVED) {
//				substituteStep.addClassName("active");
//			} else if (holidayRequest.getSubstitutionRequest().getStatus() == SubstitutionRequest.Status.DENIED) {
//				substituteStep.addClassName("denied");
//			}
//			stepList.add(substituteStep);

			holidayRequest.getApprovalRequests().stream().forEach(addApprovalStatusSteps(stepList));
			stepList.setWidthFull();
			stepList.setClassName("progressbar");

			HorizontalLayout stepBarContainer = new HorizontalLayout(stepList);
			stepBarContainer.setWidthFull();
			stepBarContainer.setAlignItems(Alignment.CENTER);
			stepBarContainer.setJustifyContentMode(JustifyContentMode.CENTER);
			return stepBarContainer;
		});
	}

	private Consumer<ApprovalRequest> addApprovalStatusSteps(UnorderedList stepList) {
		return approvalRequest -> {
			ListItem approvalStep = new ListItem(approvalRequest.getApprover().getName());
			if (approvalRequest.getStatus() == ApprovalRequest.Status.APPROVED) {
				approvalStep.addClassName("active");
			} else if (approvalRequest.getStatus() == ApprovalRequest.Status.DENIED) {
				approvalStep.addClassName("denied");
			}
			stepList.add(approvalStep);
		};
	}

	private HorizontalLayout getActionButtons(HolidayRequest request) {
		Button btnEdit = new Button(MessageRetriever.get("editHoliday"), VaadinIcon.EDIT.create(), e -> {
			editor.editHolidayRequest(request);
			mountEditorInDialog(true);
		});
		btnEdit.addClassName("responsive");
		Button btnSave = new Button(MessageRetriever.get("saveHoliday"), VaadinIcon.PRINT.create(), event -> {
		});
		btnSave.addThemeName("success");
		btnSave.addThemeName("primary");
		btnSave.addClassName("responsive");

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		if (request.isStillEditable()) {
			horizontalLayout.add(btnEdit);
		} else {
			horizontalLayout.add(getButtonWrapperWithPdfDocument(btnSave, request));
		}
		return horizontalLayout;
	}

	private FileDownloadWrapper getButtonWrapperWithPdfDocument(Button btn, HolidayRequest request) {
		FileDownloadWrapper buttonWrapper;
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

	private void listHolidayRequests(String userEmail) {
		List<HolidayRequest> requests = service.getHolidayRequests(userEmail);
		if (requests.isEmpty()) {
			grid.setVisible(false);
			heading.setText(MessageRetriever.get("noHolidayRequests"));
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
		listHolidayRequests(SecurityUtils.getLoggedInUser().getEmail());
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		Broadcaster.register(new UserUITuple(SecurityUtils.getLoggedInUser(), UI.getCurrent()), this);
	}

	@Override
	public void receiveBroadcast(UI ui, BroadcastEvent message) {
		switch (message.getType()) {
			case APPROVER_ACCEPTED:
			case APPROVER_DENIED:
			case SUBSTITUTE_ACCEPTED:
			case SUBSTITUTE_DENIED:
				ui.access(() -> {
					if (dialog != null) {
						if (dialog.isOpened()) {
							dialog.close();
						}
					}
					this.listHolidayRequests(message.getTargetUserId());
				});
				break;
		}
	}

	@Override
	public String getPageTitle() {
		return MessageRetriever.get("titleRequests");
	}
}