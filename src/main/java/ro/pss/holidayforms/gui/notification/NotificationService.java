package ro.pss.holidayforms.gui.notification;

import org.springframework.stereotype.Service;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.ApprovalRequest;
import ro.pss.holidayforms.domain.HolidayRequest;
import ro.pss.holidayforms.domain.SubstitutionRequest;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;

@Service
public class NotificationService {
	public void requestCreated(HolidayRequest holidayRequest) {
		notifySubstitutes(holidayRequest, BroadcastEvent.Type.SUBSTITUTE_ADDED, MessageRetriever.get("notificationSubstituteMessage"));
		notifyApprovers(holidayRequest, BroadcastEvent.Type.APPROVE_ADDED, MessageRetriever.get("notificationApproveMessage"));
	}

	public void requestDeleted(HolidayRequest holidayRequest) {
		notifySubstitutes(holidayRequest, BroadcastEvent.Type.SUBSTITUTE_DELETED, MessageRetriever.get("notificationSubstituteDeletedMessage"));
		notifyApprovers(holidayRequest, BroadcastEvent.Type.APPROVE_DELETED, MessageRetriever.get("notificationApproveDeletedMessage"));
	}

	public void requestEdited(HolidayRequest holidayRequest) {
		notifySubstitutes(holidayRequest, BroadcastEvent.Type.SUBSTITUTE_CHANGED, MessageRetriever.get("notificationSubstituteChangedMessage"));
		notifyApprovers(holidayRequest, BroadcastEvent.Type.APPROVE_CHANGED, MessageRetriever.get("notificationApproveChangedMessage"));
	}

	private void notifySubstitutes(HolidayRequest holidayRequest, BroadcastEvent.Type substituteAdded, String notificationSubstituteMessage) {
		Broadcaster.broadcast(
				new BroadcastEvent(holidayRequest.getSubstitute().getEmail(),
						substituteAdded,
						holidayRequest.getRequester().getName()));
	}

	private void notifyApprovers(HolidayRequest holidayRequest, BroadcastEvent.Type approveAdded, String notificationApproveMessage) {
		for (ApprovalRequest approvalRequest : holidayRequest.getApprovalRequests()) {
			Broadcaster.broadcast(
					new BroadcastEvent(approvalRequest.getApprover().getEmail(),
							approveAdded,
							holidayRequest.getRequester().getName()));
		}
	}

	public void approvalAccepted(ApprovalRequest approvalRequest) {
		Broadcaster.broadcast(new BroadcastEvent(approvalRequest.getRequest().getRequester().getEmail(),
				BroadcastEvent.Type.APPROVER_ACCEPTED,
				SecurityUtils.getLoggedInUser().getName()));
	}

	public void approvalDenied(ApprovalRequest approvalRequest) {
		Broadcaster.broadcast(new BroadcastEvent(approvalRequest.getRequest().getRequester().getEmail(),
				BroadcastEvent.Type.APPROVER_DENIED,
				SecurityUtils.getLoggedInUser().getName()));
	}

	public void substitutionAccepted(SubstitutionRequest substitutionRequest) {
		Broadcaster.broadcast(new BroadcastEvent(substitutionRequest.getRequest().getRequester().getEmail(),
				BroadcastEvent.Type.SUBSTITUTE_ACCEPTED,
				SecurityUtils.getLoggedInUser().getName()));
	}

	public void substitutionDenied(SubstitutionRequest substitutionRequest) {
		Broadcaster.broadcast(new BroadcastEvent(substitutionRequest.getRequest().getRequester().getEmail(),
				BroadcastEvent.Type.SUBSTITUTE_DENIED,
				SecurityUtils.getLoggedInUser().getName()));
	}
}
