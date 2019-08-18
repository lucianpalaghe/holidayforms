package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.pss.holidayforms.domain.SubstitutionRequest;

import java.util.List;

@Repository
public interface SubstitutionRequestRepository extends JpaRepository<SubstitutionRequest, Long> {
	List<SubstitutionRequest> findAllBySubstituteEmail(String email);

	List<SubstitutionRequest> findAllBySubstituteEmailAndStatus(String email, SubstitutionRequest.Status status);
}