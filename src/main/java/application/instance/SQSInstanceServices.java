package application.instance;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import application.service.SQSClientServices;

@Service
public class SQSInstanceServices {

	@Autowired
	private SQSClientServices sqsClient;
	private static final Logger LOGGER = Logger.getLogger(SQSInstanceServices.class.getName());

	/**
	 * This function will add input in the Queue
	 */
	public String push(String input) {
		LOGGER.info("Adding input to the Queue " + input);
		SendMessageRequest sendMessageRequest = new SendMessageRequest(sqsClient.getSqsQueueUrl(), input);
		sendMessageRequest.setMessageGroupId("cloudPi");

		SendMessageResult sendMessageResult = sqsClient.getSqsClient().sendMessage(sendMessageRequest);
		String sequenceNumber = sendMessageResult.getSequenceNumber();
		String messageId = sendMessageResult.getMessageId();

		return sequenceNumber + " " + messageId;
	}

	/**
	 * This function will return the first message in the Queue
	 */
	public String pop() {
		LOGGER.info("Retriving input from the Queue ");
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsClient.getSqsQueueUrl());
		List<Message> messages = sqsClient.getSqsClient().receiveMessage(receiveMessageRequest).getMessages();

		if (messages.size() > 0) {
			LOGGER.info("Deleting the message."+messages.get(0).getBody());
			String messageReceiptHandle = messages.get(0).getReceiptHandle();
			sqsClient.getSqsClient()
					.deleteMessage(new DeleteMessageRequest(sqsClient.getSqsQueueUrl(), messageReceiptHandle));
			return messages.get(0).getBody();
		} else {
			return null;
		}
	}

}
