package ro.pss.holidayforms.gui.request;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
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
public class HolidayRequestView extends VerticalLayout {
	final Grid<HolidayRequest> grid;
	private final HolidayRequestRepository requestRepository;
	private final HolidayRequestEditor editor;
	private final Button addNewBtn;
	private final Dialog dialog;

	public HolidayRequestView(HolidayRequestRepository repo, HolidayRequestEditor editor) {
		this.requestRepository = repo;
		this.editor = editor;
		this.grid = new Grid<>();
		this.addNewBtn = new Button("New HolidayRequest", VaadinIcon.PLUS.create());
		this.dialog = new Dialog(editor);
		this.dialog.setCloseOnOutsideClick(false);

		HorizontalLayout actions = new HorizontalLayout(addNewBtn);
		add(actions, grid, this.editor);
		setHeightFull();
		grid.addColumn(HolidayRequest::getNumberOfDays).setHeader("Numar de zile").setFlexGrow(1);
		grid.addColumn(HolidayRequest::getDateFrom).setHeader("Incepand cu data").setFlexGrow(1);
		grid.addColumn(HolidayRequest::getSubstitute).setHeader("Inlocuitor").setFlexGrow(10);

//		grid.setWidthFull();
//		grid.setHeightFull();

		grid.asSingleSelect().addValueChangeListener(e -> {
			editor.editHolidayRequest(e.getValue());
			mountEditorInDialog(true);
		});

		addNewBtn.addClickListener(e -> {
			editor.editHolidayRequest(new HolidayRequest());
			mountEditorInDialog(true);
		});

		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listHolidayRequests();
			mountEditorInDialog(false);
		});

		listHolidayRequests();
	}

	void listHolidayRequests() {
		List<HolidayRequest> requests = requestRepository.findAllByRequesterEmail("luminita.petre@pss.ro");
		if (requests.isEmpty()) {
			grid.setVisible(false);
		} else {
			grid.setVisible(true);
			grid.setItems(requests);
		}
	}

	void mountEditorInDialog(boolean mount) {
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