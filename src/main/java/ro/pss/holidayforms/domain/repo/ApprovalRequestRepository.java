package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.pss.holidayforms.domain.ApprovalRequest;

import java.util.List;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
	public List<ApprovalRequest> findAllByApproverEmail(String email);
}