package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.HolidayRequestRepository;
import ro.pss.holidayforms.domain.repo.UserRepository;
import ro.pss.holidayforms.gui.notification.NotificationService;
import ro.pss.holidayforms.integrations.tempo.TempoService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.*;

@Service
public class HolidayRequestService {
	@Autowired
	private HolidayRequestRepository requestRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private TempoService tempoService;

	private final List<String> approverIds = Arrays.asList("lucian.palaghe", "luminita.petre");

	public List<HolidayRequest> getHolidayRequests(String userEmail) {
		return requestRepository.findAllByRequesterEmail(userEmail);
	}

	public List<User> getAvailableSubstitutes() {
		return userRepository.findAll();
	}

	public void saveRequest(HolidayRequest holidayRequest) {
		if (holidayRequest.getApprovalRequests().size() > 0) { // if this request is edited, remove all previous approvals
			holidayRequest.getApprovalRequests().clear();
		}

		List<ApprovalRequest> approvalRequests = approverIds.stream().map(a -> { // TODO: refactor
			User approver = userRepository.getOne(a);
			return new ApprovalRequest(approver, ApprovalRequest.Status.NEW);
		}).collect(toList());
		approvalRequests.forEach(holidayRequest::addApproval);

		boolean isNewRequest = holidayRequest.getId() == null; // check if the entity has id BEFORE SAVING IT
		requestRepository.save(holidayRequest);

		if (isNewRequest) {
			notificationService.requestCreated(holidayRequest);
		} else {
			notificationService.requestEdited(holidayRequest);
		}
	}

	public void removeRequest(HolidayRequest holidayRequest) {
		requestRepository.delete(holidayRequest);
		notificationService.requestDeleted(holidayRequest);
	}

	void approvalsChanged(HolidayRequest holidayRequest) {
		holidayRequest = findById(holidayRequest.getId()); // other approvals might have been committed to the database in the meantime, so get the latest request data
		boolean substituteApproved = holidayRequest.getSubstitutionRequest().getStatus() == SubstitutionRequest.Status.APPROVED;
		boolean allOthersApproved = holidayRequest.getApprovalRequests().stream().allMatch(ApprovalRequest::isApproved);
		if (substituteApproved && allOthersApproved) {
			HolidayRequest finalHolidayRequest = holidayRequest;

			CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
				tempoService.postHolidayWorklog(finalHolidayRequest);//TODO: add response to tempo service
				return null;
			});

			future.thenAcceptAsync(aVoid -> notificationService.worklogsPosted(finalHolidayRequest));
		}
	}

	public HolidayRequest findById(Long id) {
		return requestRepository.findById(id).orElseThrow();
	}
}
