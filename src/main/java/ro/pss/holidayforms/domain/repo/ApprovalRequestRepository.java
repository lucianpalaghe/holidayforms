package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.pss.holidayforms.domain.ApprovalRequest;

import java.util.List;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
	List<ApprovalRequest> findAllByApproverEmail(String email);
	List<ApprovalRequest> findAllByApproverEmailAndStatus(String email, ApprovalRequest.Status status);
}