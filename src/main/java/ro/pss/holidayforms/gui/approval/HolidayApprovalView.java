package ro.pss.holidayforms.gui.approval;

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
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.repo.ApprovalRequestRepository;
import ro.pss.holidayforms.gui.HolidayAppLayout;
import ro.pss.holidayforms.gui.HolidayConfirmationDialog;

@SpringComponent
@UIScope
@Route(value = "approvals", layout = HolidayAppLayout.class)
public class HolidayApprovalView extends HorizontalLayout implements AfterNavigationObserver {
    final Grid<ApprovalRequest> grid;
    private final ApprovalRequestRepository requestRepository;
    private final VerticalLayout container;
    private HolidayConfirmationDialog holidayConfDialog;

    public HolidayApprovalView(ApprovalRequestRepository repo) {
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

        listApprovalRequests();
    }

    void listApprovalRequests() {
        grid.setItems(requestRepository.findAllByApproverEmail("luminita.petre@pss.ro"));
    }

    private HorizontalLayout getActionButtons(ApprovalRequest request) {
        Button btnApprove = new Button("Aproba", VaadinIcon.CHECK_CIRCLE.create(), event -> {
        	String message = String.format("Vrei sa aprobi cererea de %s pentru %s?", request.getRequest().getType(), request.getRequest().getRequester().getName());
			holidayConfDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.APPROVAL, () -> confirmHolidayApproval(request), "Aproba cererea", message, "Aproba", "Inapoi");
			holidayConfDialog.open();
        });
        btnApprove.addThemeName("success");
        btnApprove.addThemeName("primary");

        Button btnDeny = new Button("Respinge", VaadinIcon.CLOSE_CIRCLE.create(), event -> {
			String message = String.format("Vrei sa respingi cererea de %s pentru %s?", request.getRequest().getType(), request.getRequest().getRequester().getName());
			holidayConfDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.DENIAL, () -> rejectHolidayApproval(request), "Respinge cererea", message, "Respinge", "Inapoi");
			holidayConfDialog.open();
        });
        btnDeny.addThemeName("error");

        if (request.getStatus() == ApprovalRequest.Status.NEW) {
            HorizontalLayout horizontalLayout = new HorizontalLayout(btnApprove, btnDeny);
            horizontalLayout.setMinWidth("10em");
            return horizontalLayout;
        }

        if (request.getStatus() == ApprovalRequest.Status.APPROVED) {
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

    private void confirmHolidayApproval(ApprovalRequest request) {
        request.approve();
        requestRepository.save(request);
        grid.getDataProvider().refreshItem(request);
    }

    private void rejectHolidayApproval(ApprovalRequest request) {
        request.deny();
        requestRepository.save(request);
        grid.getDataProvider().refreshItem(request);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        listApprovalRequests();
    }
}