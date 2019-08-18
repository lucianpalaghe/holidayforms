package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.notification.NotificationService;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.*;

@Service
public class HolidayRequestService {
	@Autowired
	private HolidayRequestRepository requestRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NotificationService notificationService;

	private final List<String> approverIds = Arrays.asList("lucian.palaghe", "claudia.gican", "luminita.petre");

	public List<HolidayRequest> getHolidayRequests(String userEmail) {
		return requestRepository.findAllByRequesterEmail(userEmail);
	}

	public List<User> getAvailableSubstitutes() {
		return userRepository.findAll();
	}

	public void createRequest(HolidayRequest holidayRequest) {
		if (holidayRequest.getApprovalRequests().size() > 0) { // if this request is edited, remove all previous approvals
			holidayRequest.getApprovalRequests().clear();
		}

		List<ApprovalRequest> approvalRequests = approverIds.stream().map(a -> { // TODO: refactor
			User approver = userRepository.getOne(a);
			return new ApprovalRequest(approver, ApprovalRequest.Status.NEW);
		}).collect(toList());
		approvalRequests.forEach(holidayRequest::addApproval);

		boolean isNewRequest = holidayRequest.getId() == null;
		if (isNewRequest) {
			notificationService.requestCreated(holidayRequest);
		} else {
			notificationService.requestEdited(holidayRequest);
		}
		requestRepository.save(holidayRequest);
	}

	public void remove(HolidayRequest holidayRequest) {
		requestRepository.delete(holidayRequest);
		notificationService.requestDeleted(holidayRequest);
	}
}
