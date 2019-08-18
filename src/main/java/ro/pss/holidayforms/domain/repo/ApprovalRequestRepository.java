package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.pss.holidayforms.domain.ApprovalRequest;

import java.util.List;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
	List<ApprovalRequest> findAllByApproverEmail(String email);

	List<ApprovalRequest> findAllByApproverEmailAndStatus(String email, ApprovalRequest.Status status);
}