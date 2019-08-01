package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.pss.holidayforms.domain.User;

public interface UserRepository extends JpaRepository<User, String> {
}