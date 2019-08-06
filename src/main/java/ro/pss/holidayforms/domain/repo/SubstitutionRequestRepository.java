package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.pss.holidayforms.domain.SubstitutionRequest;

import java.util.List;

public interface SubstitutionRequestRepository extends JpaRepository<SubstitutionRequest, Long> {
	public List<SubstitutionRequest> findAllBySubstituteEmail(String email);
}