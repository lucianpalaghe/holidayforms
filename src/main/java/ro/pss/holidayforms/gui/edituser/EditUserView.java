package ro.pss.holidayforms.gui.edituser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
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
import de.wathoserver.vaadin.MultiselectComboBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.domain.Role;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.domain.repo.UserRoleRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringComponent
@UIScope
@Slf4j
@Route(value = "edituser", layout = HolidayAppLayout.class)
public class EditUserView extends HorizontalLayout implements AfterNavigationObserver, BeforeLeaveObserver, HasDynamicTitle {

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private Set<Role> allRoles;
    private List<User> users;
    private final Grid<User> grid;

    @Autowired
    public EditUserView(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.allRoles = new LinkedHashSet<>(userRoleRepository.findAll());
        grid = new Grid<>();
        grid.addColumn(new ComponentRenderer<>(this::getNameRenderer)).setHeader(MessageRetriever.get("editUserGridColName")).setFlexGrow(2);
        grid.addColumn(new ComponentRenderer<>(this::getRolesRenderer)).setHeader(MessageRetriever.get("editUserGridColRole")).setFlexGrow(5);
        grid.addColumn(new ComponentRenderer<>(this::getVacationDaysRenderer)).setHeader(MessageRetriever.get("editUserGridVacationDays")).setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(this::getActionButtons)).setFlexGrow(2);
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.MATERIAL_COLUMN_DIVIDERS, GridVariant.LUMO_ROW_STRIPES);
        grid.setEnabled(true);
        populateGridWithData();
        VerticalLayout container = new VerticalLayout();
        container.add(new H3(MessageRetriever.get("editUserHeader")), grid);
        container.setWidth("90%");
       // container.setMaxWidth("90em");
        //container.setHeightFull();
        container.setHeight("85%");

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(container);
        setHeightFull();
    }

    private HorizontalLayout getVacationDaysRenderer(User user) {
        ComboBox<Integer> cmbVacationDays = new ComboBox<>();
        cmbVacationDays.setDataProvider(new ListDataProvider<>(IntStream.rangeClosed(15, 30).boxed().collect(Collectors.toList())));
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
    private MultiselectComboBox<String> getRolesRenderer(User user) {
        MultiselectComboBox<String> roles = new MultiselectComboBox<>();
        //roles.setCompactMode(false);
        roles.setItems(this.allRoles.stream().map(r -> r.getName().toString()).collect(Collectors.toSet()));
        roles.setValue(user.getRoles().stream().map(r -> r.getName().toString()).collect(Collectors.toSet()));
        roles.addValueChangeListener(listener -> {
            if(listener.getValue().isEmpty()) {
                // a user must have at least one role, use the default one(user)
                roles.setValue(allRoles.stream().filter(r -> r.getName().equals(Role.RoleName.USER)).map(f -> f.getName().toString()).collect(Collectors.toSet()));
            } else {
                roles.setValue(listener.getValue());
            }
            Set<Role> newRoles = allRoles.stream().filter(role ->
                    roles.getValue().stream()
                            .anyMatch(s -> role.getName().toString().equalsIgnoreCase(s))).collect(Collectors.toSet());
            user.setRoles(newRoles);
        });
        return roles;
    }

    private HorizontalLayout getNameRenderer(User user) {
        TextField textField = new TextField();
        textField.setWidth("80em");
        textField.setValue(user.getName());
        textField.setEnabled(true);
        textField.addValueChangeListener(listener -> {
            if(listener.getValue().isEmpty()) {
                textField.setValue(listener.getOldValue());
            } else {
                textField.setValue(listener.getValue());
            }
            user.setName(textField.getValue());
        });
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(textField);
        return horizontalLayout;
    }

    private HorizontalLayout getActionButtons(User user) {
        Button btnSave = new Button(MessageRetriever.get("btnSaveLbl"), VaadinIcon.PRINT.create(), event -> {
            userRepository.save(user);
            Notification.show(MessageRetriever.get("editUserSaved"), 3000, Notification.Position.TOP_CENTER);
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

    private void populateGridWithData() {
        this.users = userRepository.findAll();
        this.grid.setItems(users);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {

    }

    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {

    }

    @Override
    public String getPageTitle() {
        return MessageRetriever.get("titleEditUser");
    }
}
