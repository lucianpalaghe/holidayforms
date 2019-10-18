package ro.pss.holidayforms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @Column(length = 60)
    @Enumerated(EnumType.STRING)
    @Getter
    private RoleName name;

    public enum RoleName {
		USER, TEAM_LEADER, HR, PROJECT_MANGER, ADMIN
	}
    public Role(RoleName name) {
        this.name = name;
    }
}
