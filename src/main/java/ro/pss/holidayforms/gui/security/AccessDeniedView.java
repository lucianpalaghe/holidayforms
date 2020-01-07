package ro.pss.holidayforms.gui.security;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;

@SpringComponent
@UIScope
@Slf4j
@Route(layout = HolidayAppLayout.class)
public class AccessDeniedView extends HorizontalLayout implements HasDynamicTitle {
	private final H2 heading;

	public AccessDeniedView() {
		heading = new H2(MessageRetriever.get("accessDenied"));

		VerticalLayout container = new VerticalLayout();
		container.add(heading);
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
		return MessageRetriever.get("accessDenied");
	}
}