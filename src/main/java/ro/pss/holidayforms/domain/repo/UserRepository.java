package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.pss.holidayforms.domain.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
	List<Object> findByClockingUid(String s);
}