package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.domain.repo.SubstitutionRequestRepository;
import ro.pss.holidayforms.gui.notification.NotificationService;

import java.util.List;

@Service
public class HolidaySubstitutionService {
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private SubstitutionRequestRepository substitutionRepo;

	public List<SubstitutionRequest> getSubstitutionRequests(String userEmail) {
		return substitutionRepo.findAllBySubstituteEmail(userEmail);
	}

	public void approveRequest(SubstitutionRequest request) {
		request.approve();
		substitutionRepo.save(request);
		notificationService.substitutionAccepted(request);
	}

	public void denyRequest(SubstitutionRequest request) {
		request.deny();
		substitutionRepo.save(request);
		notificationService.substitutionDenied(request);
	}
}
