package ro.pss.holidayforms.gui.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ro.pss.holidayforms.domain.UserPreferences;
import ro.pss.holidayforms.gui.MessageRetriever;

@Route(value = LoginView.ROUTE)
@PageTitle("Login")
public class LoginView extends VerticalLayout {
	public static final String ROUTE = "login";

	public LoginView() {
		H3 loginOauth = new H3(MessageRetriever.get("loginOauth", UserPreferences.Locale.ro.locale));
		Button azureLogin = new Button(MessageRetriever.get("loginAzure", UserPreferences.Locale.ro.locale));
		azureLogin.addClickListener(e -> {
			UI ui = azureLogin.getUI().get();
			ui.getPage().executeJavaScript("window.location.href='oauth2/authorization/azure'");
			ui.getSession().close();
		});
		Image iconAzure = new Image("images/o365.png", "");
		iconAzure.setWidth("30px");
		iconAzure.setHeight("30px");
		iconAzure.getStyle().set("vertical-align", "middle");
		iconAzure.getStyle().set("margin-right", "10px");
		azureLogin.setIcon(iconAzure);
		azureLogin.addClickListener(e -> {
//			UI ui = jiraLogin.getUI().get();
//			ui.getPage().executeJavaScript("window.location.href='oauth2/authorization/azure'");
//			ui.getSession().close();
		});
		Button jiraLogin = new Button(MessageRetriever.get("loginJira", UserPreferences.Locale.ro.locale));
		jiraLogin.setEnabled(false);
		Image iconJira = new Image("images/jira.png", "");
		iconJira.setWidth("30px");
		iconJira.setHeight("30px");
		iconJira.getStyle().set("vertical-align", "middle");
		iconJira.getStyle().set("margin-right", "10px");
		iconJira.getStyle().set("opacity", "40%");
		jiraLogin.setIcon(iconJira);
		VerticalLayout vl = new VerticalLayout(loginOauth, azureLogin, jiraLogin);

		setHeightFull();
		setWidthFull();
		setHorizontalComponentAlignment(Alignment.CENTER);
		setAlignItems(Alignment.CENTER);
		setAlignSelf(Alignment.CENTER);
		HorizontalLayout hl = new HorizontalLayout(vl);
		hl.setAlignItems(Alignment.CENTER);
		hl.setVerticalComponentAlignment(Alignment.CENTER);
		hl.setHeight("60%");
		add(hl);
	}
}