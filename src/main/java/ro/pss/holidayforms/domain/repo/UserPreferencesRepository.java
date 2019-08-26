package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.UserPreferences;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    Optional<UserPreferences> findByEmployeeEmail(String email);
    Optional<UserPreferences> findByEmployee(User employee);
}
