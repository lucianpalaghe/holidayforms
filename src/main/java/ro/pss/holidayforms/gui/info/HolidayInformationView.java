package ro.pss.holidayforms.gui.info;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;

import java.util.stream.*;

@SpringComponent
@UIScope
@Slf4j
@Route(value = "guide", layout = HolidayAppLayout.class)
public class HolidayInformationView extends HorizontalLayout implements HasDynamicTitle {
	private final H2 heading;

	public HolidayInformationView() {
		heading = new H2(MessageRetriever.get("infoHeader"));

		VerticalLayout container = new VerticalLayout();
		container.add(heading);
		Stream.of(HolidayRequest.Type.values()).forEach(t -> {
			Paragraph p = new Paragraph(MessageRetriever.get("info_" + t.toString()));
			container.add(p);
		});

		container.setWidth("100%");
		container.setMaxWidth("70em");
		container.setHeightFull();

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(container);
		setHeightFull();
	}

	@Override
	public String getPageTitle() {
		return MessageRetriever.get("titleInfo");
	}
}