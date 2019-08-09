package ro.pss.holidayforms.gui.layout;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class VersionMenuItem extends Composite<HorizontalLayout> {

	public VersionMenuItem(String versionString) {
		HorizontalLayout content = getContent();
		content.setPadding(false);
		content.getStyle().set("padding", "var(--app-layout-menu-header-padding)");
		content.getStyle().set("font-size", "x-small");
		content.setMargin(false);
		setId("menu-footer-version");

		VerticalLayout subContent = new VerticalLayout();
		subContent.setPadding(false);
		subContent.setMargin(false);
		subContent.setSpacing(false);
		if (versionString != null) {
			Label titleLabel = new Label(versionString);
			titleLabel.setId("menu-footer-version-string");
			subContent.add(titleLabel);
		}
		content.add(subContent);
	}

}