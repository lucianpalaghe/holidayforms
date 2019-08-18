package ro.pss.holidayforms.gui.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.domain.notification.Notification;
import ro.pss.holidayforms.domain.repo.NotificationRepository;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;

import java.time.LocalDateTime;

@Service
public class NotificationService {
	@Autowired
	private NotificationRepository notificationRepository;

	public void requestCreated(HolidayRequest holidayRequest) {
		notifySubstitutes(holidayRequest, BroadcastEvent.Type.SUBSTITUTE_ADDED);
		notifyApprovers(holidayRequest, BroadcastEvent.Type.APPROVE_ADDED);
	}

	public void requestDeleted(HolidayRequest holidayRequest) {
		notifySubstitutes(holidayRequest, BroadcastEvent.Type.SUBSTITUTE_DELETED);
		notifyApprovers(holidayRequest, BroadcastEvent.Type.APPROVE_DELETED);
	}

	public void requestEdited(HolidayRequest holidayRequest) {
		notifySubstitutes(holidayRequest, BroadcastEvent.Type.SUBSTITUTE_CHANGED);
		notifyApprovers(holidayRequest, BroadcastEvent.Type.APPROVE_CHANGED);
	}

	private void notifySubstitutes(HolidayRequest holidayRequest, BroadcastEvent.Type substituteAdded) {
		sendBroadcast(new BroadcastEvent(holidayRequest.getSubstitute().getEmail(),
						substituteAdded,
						holidayRequest.getRequester().getName()));
	}

	private void notifyApprovers(HolidayRequest holidayRequest, BroadcastEvent.Type approveAdded) {
		for (ApprovalRequest approvalRequest : holidayRequest.getApprovalRequests()) {
			sendBroadcast(new BroadcastEvent(approvalRequest.getApprover().getEmail(),
							approveAdded,
							holidayRequest.getRequester().getName()));
		}
	}

	public void approvalAccepted(ApprovalRequest approvalRequest) {
		sendBroadcast(new BroadcastEvent(approvalRequest.getRequest().getRequester().getEmail(),
				BroadcastEvent.Type.APPROVER_ACCEPTED,
				SecurityUtils.getLoggedInUser().getName()));
	}

	public void approvalDenied(ApprovalRequest approvalRequest) {
		sendBroadcast(new BroadcastEvent(approvalRequest.getRequest().getRequester().getEmail(),
				BroadcastEvent.Type.APPROVER_DENIED,
				SecurityUtils.getLoggedInUser().getName()));
	}

	public void substitutionAccepted(SubstitutionRequest substitutionRequest) {
		sendBroadcast(new BroadcastEvent(substitutionRequest.getRequest().getRequester().getEmail(),
				BroadcastEvent.Type.SUBSTITUTE_ACCEPTED,
				SecurityUtils.getLoggedInUser().getName()));
	}

	public void substitutionDenied(SubstitutionRequest substitutionRequest) {
		sendBroadcast(new BroadcastEvent(substitutionRequest.getRequest().getRequester().getEmail(),
				BroadcastEvent.Type.SUBSTITUTE_DENIED,
				SecurityUtils.getLoggedInUser().getName()));
	}

	private void sendBroadcast(BroadcastEvent event) {
		Notification savedNotification = notificationRepository.save(new Notification(LocalDateTime.now(), null, event.getType().name(),
				event.getTargetUserId(), SecurityUtils.getLoggedInUser().getName(), event.getType(), Notification.Status.NEW, Notification.Priority.HIGH));
		event.setNotification(savedNotification);
		Broadcaster.broadcast(event);
	}
}
