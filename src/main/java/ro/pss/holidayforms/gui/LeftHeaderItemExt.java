package ro.pss.holidayforms.gui;

import com.github.appreciated.app.layout.component.menu.RoundImage;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class LeftHeaderItemExt extends Composite<HorizontalLayout> {

	public LeftHeaderItemExt(String title, String subtitle, String src) {
		HorizontalLayout content = getContent();
		content.setPadding(false);
		content.getStyle().set("padding", "var(--app-layout-menu-header-padding)");
		content.setMargin(false);
		setId("menu-header-wrapper");
		if (src != null) {
			content.add(new RoundImage(src, "40px", "40px"));
		}
		VerticalLayout subContent = new VerticalLayout();
		subContent.setPadding(false);
		subContent.setMargin(false);
		subContent.setSpacing(false);
		if (title != null) {
			Label titleLabel = new Label(title);
			titleLabel.setId("menu-header-title");
			subContent.add(titleLabel);
		}
		if (subtitle != null) {
			Label subtitleLabel = new Label(subtitle);
			subtitleLabel.setId("menu-header-subtitle");
			subContent.add(subtitleLabel);
		}
		content.add(subContent);
	}

}