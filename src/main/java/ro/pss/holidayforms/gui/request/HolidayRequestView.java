package ro.pss.holidayforms.gui.request;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.gui.HolidayAppLayout;

import java.util.List;

@SpringComponent
@UIScope
@Route(value = "requests", layout = HolidayAppLayout.class)
public class HolidayRequestView extends HorizontalLayout {
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
		grid.addColumn(HolidayRequest::getNumberOfDays).setHeader("Nr. de zile").setFlexGrow(1);
		grid.addColumn(HolidayRequest::getDateFrom).setHeader("Incepand cu data").setFlexGrow(2);
		grid.addColumn(HolidayRequest::getSubstitute).setHeader("Inlocuitor").setFlexGrow(5);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
		grid.asSingleSelect().addValueChangeListener(e -> {
			editor.editHolidayRequest(e.getValue());
			mountEditorInDialog(true);
		});

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
}