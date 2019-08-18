package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.repo.ApprovalRequestRepository;
import ro.pss.holidayforms.gui.notification.NotificationService;

import java.util.List;

@Service
public class HolidayApprovalService {
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private ApprovalRequestRepository approvalRepo;

	public List<ApprovalRequest> getApprovalRequests(String userEmail) {
		return approvalRepo.findAllByApproverEmail(userEmail);
	}

	public void approveRequest(ApprovalRequest request) {
		request.approve();
		approvalRepo.save(request);
		notificationService.approvalAccepted(request);
	}

	public void denyRequest(ApprovalRequest request) {
		request.deny();
		approvalRepo.save(request);
		notificationService.approvalDenied(request);
	}
}
