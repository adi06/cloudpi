package application.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import application.service.SQSClientServices;

@Service
public class SQSInstanceServices {

	@Autowired
	private SQSClientServices sqsClient;

	/**
	 * This function will add input in the Queue
	 */
	public String push(String input) {
		SendMessageRequest sendMessageRequest = new SendMessageRequest(sqsClient.getSqsQueueUrl(), input);
		sendMessageRequest.setMessageGroupId("cloudPi");

		SendMessageResult sendMessageResult = sqsClient.getSqsClient().sendMessage(sendMessageRequest);
		String sequenceNumber = sendMessageResult.getSequenceNumber();
		String messageId = sendMessageResult.getMessageId();
		
		return sequenceNumber+" "+messageId;
	}

}
