package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@NoArgsConstructor
public class UserPreferences {
    @GeneratedValue
    @Getter
    @Id
    private Long id;

    @Getter
    @Setter
    @OneToOne
    private User employee;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private LocaleOption localeOption;

    @Getter
    @Setter
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<EmailOption> emailOption;

    @Getter
    @Setter
    private boolean showNotifications;

    public enum LocaleOption {
        ROMANIAN, ENGLISH
    }

    public enum EmailOption {
       ON_NEW_REQUEST, ON_FINAL_APPROVAL, ON_REPLACER_ACTION, ON_APPROVER_ACTION
    }
}
