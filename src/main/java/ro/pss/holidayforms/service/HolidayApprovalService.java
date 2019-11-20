package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
	@Autowired
	private HolidayRequestService requestService;

	@Transactional
	public List<ApprovalRequest> getApprovalRequests(String userEmail) {
		List<ApprovalRequest> allByApproverEmail = approvalRepo.findAllByApproverEmail(userEmail);
		allByApproverEmail.stream().forEach(a -> a.getRequest().getSubstitutes()); // initialize lazy collection TODO: replace with some kind of query
		return allByApproverEmail;
	}

	public void approveRequest(ApprovalRequest request) {
		request.approve();
		approvalRepo.save(request);
		requestService.approvalsChanged(request.getRequest());
		notificationService.approvalAccepted(request);
	}

	public void denyRequest(ApprovalRequest request) {
		request.deny();
		approvalRepo.save(request);
		notificationService.approvalDenied(request);
	}
}
