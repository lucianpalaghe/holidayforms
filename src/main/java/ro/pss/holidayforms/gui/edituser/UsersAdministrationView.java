package ro.pss.holidayforms.gui.edituser;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.vaadin.gatanaso.MultiselectComboBox;
import ro.pss.holidayforms.config.security.CustomUserPrincipal;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;

import java.util.List;
import java.util.Set;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

@SpringComponent
@UIScope
@Slf4j
@Route(value = "users", layout = HolidayAppLayout.class)
@Secured("ADMIN")
public class UsersAdministrationView extends HorizontalLayout implements AfterNavigationObserver, BeforeLeaveObserver, HasDynamicTitle {
	private final H2 heading;
	private final Grid<User> grid;
	private final TextField filterUser = new TextField();
	private UserRepository userRepository;
	private List<User> users;
	@Autowired
	private SessionRegistry registry;

	@Autowired
	public UsersAdministrationView(UserRepository userRepository) {
		this.userRepository = userRepository;
		grid = new Grid<>();
		grid.addColumn(User::getName).setHeader(MessageRetriever.get("editUserGridColName")).setFlexGrow(2);
		grid.addColumn(new ComponentRenderer<>(this::getRolesRenderer)).setHeader(MessageRetriever.get("editUserGridColRole")).setFlexGrow(2);
		grid.addColumn(new ComponentRenderer<>(this::getVacationDaysRenderer)).setHeader(MessageRetriever.get("editUserGridVacationDays")).setFlexGrow(1);
		grid.addColumn(new ComponentRenderer<>(this::getActionButtons)).setFlexGrow(2);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.MATERIAL_COLUMN_DIVIDERS, GridVariant.LUMO_ROW_STRIPES);
		grid.setEnabled(true);

//		filterUser.setDataProvider(new ListDataProvider<>(userRepository.findAll()));
		filterUser.setWidthFull();
		filterUser.setPlaceholder(MessageRetriever.get("searchBoxPlaceholder"));
		filterUser.addValueChangeListener(e -> filterUsers());

		heading = new H2();
		heading.setVisible(false);

		HorizontalLayout searchControls = new HorizontalLayout(filterUser);
		VerticalLayout container = new VerticalLayout();
		container.add(searchControls, heading, grid);
		container.setWidth("100%");
		container.setMaxWidth("90em");
		container.setHeightFull();

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(container);
		setHeightFull();
	}

	private HorizontalLayout getVacationDaysRenderer(User user) {
		ComboBox<Integer> cmbVacationDays = new ComboBox<>();
		cmbVacationDays.setDataProvider(new ListDataProvider<>(IntStream.rangeClosed(15, 30).boxed().collect(toList())));
		cmbVacationDays.setValue(user.getAvailableVacationDays());
		cmbVacationDays.setAllowCustomValue(false);
		cmbVacationDays.setRequired(true);
		cmbVacationDays.addValueChangeListener((listener) -> {
			Integer value = listener.getValue();
			if (value == null) {
				cmbVacationDays.setValue(listener.getOldValue());
			}
			user.setAvailableVacationDays(cmbVacationDays.getValue());
		});
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.add(cmbVacationDays);
		return horizontalLayout;
	}

	private MultiselectComboBox<User.Role> getRolesRenderer(User user) {
		MultiselectComboBox<User.Role> roles = new MultiselectComboBox<>();
		roles.setItems(User.Role.values());
		roles.setValue(user.getRoles());
		roles.addValueChangeListener(listener -> {
			if (listener.getValue().isEmpty()) {
				roles.setValue(Set.of(User.Role.USER)); // a user must have at least one role, use the default one(user)
			} else {
				roles.setValue(listener.getValue());
			}
			roles.setCompactMode(roles.getSelectedItems().size() > 1);
			user.setRoles(roles.getValue());
		});
		roles.setSizeFull();
		roles.setCompactMode(roles.getSelectedItems().size() > 1);
		return roles;
	}

	private HorizontalLayout getActionButtons(User user) {
		Button btnSave = new Button(MessageRetriever.get("btnSaveLbl"), VaadinIcon.PRINT.create(), event -> {
			userRepository.save(user);
			// TODO: add ifs around this
			if (true) {
				List<CustomUserPrincipal> userPrincipals = registry.getAllPrincipals().stream()
						.map(o -> (CustomUserPrincipal) o)
						.filter(up -> up.getUsername().equalsIgnoreCase(user.getEmail()))
						.collect(toList());
				List<SessionInformation> sessionInfos = userPrincipals.stream().map(up -> registry.getAllSessions(up, false)).flatMap(List::stream).collect(toList());

				Notification.show(MessageRetriever.get("editUserSaved"), 3000, Notification.Position.TOP_CENTER);
				UI.getCurrent().getSession().close();
				sessionInfos.stream().forEach(s -> s.expireNow());
			}
		});
		btnSave.addThemeName("success");
		btnSave.addThemeName("secondary");
		btnSave.addClassName("responsive");


		Button btnDelete = new Button(MessageRetriever.get("btnDeleteLbl"), VaadinIcon.ERASER.create(), e -> {
			this.users.remove(user);
			userRepository.delete(user);
			Notification.show(MessageRetriever.get("editUserDeleted"), 3000, Notification.Position.TOP_CENTER);
		});
		btnDelete.addThemeNames("secondary", "error");
		btnDelete.addClassName("responsive");
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.add(btnSave, btnDelete);
		return horizontalLayout;
	}

	private void listUsers() {
		this.users = userRepository.findAll();
		if (users.isEmpty()) {
			grid.setVisible(false);
			heading.setText(MessageRetriever.get("noUsers"));
			heading.setVisible(true);
		} else {
			heading.setVisible(false);
			grid.setVisible(true);
			this.grid.setItems(users);
		}
	}

	private void filterUsers() {
		Stream<User> stream = userRepository.findAll().stream();
		if (filterUser.getValue() != null) {
			stream = stream.filter(e -> e.getName().toLowerCase().contains(filterUser.getValue().toLowerCase()));
		}
		grid.setItems(stream.collect(toList()));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		listUsers();
	}

	@Override
	public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {

	}

	@Override
	public String getPageTitle() {
		return MessageRetriever.get("titleUserAdministration");
	}
}
