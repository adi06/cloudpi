package application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import application.configuration.AWSConfiguration;
import application.configuration.SQSConfiguration;

@Service
public class AWSSQSClientService {
	private AmazonSQS sqsClient;
	private String sqsQueueUrl;
	@Autowired
	private AWSConfiguration awsConfig;
	@Autowired
	private SQSConfiguration sqsConfig;
	private BasicAWSCredentials basicAwsCreds;
	private static final Object mutex = new Object();
	private static final Logger logger = Logger.getLogger(AWSSQSClientService.class.getName());
	
	@PostConstruct
	public void init(){
		logger.info("Initializing sqs client");
		basicAwsCreds = new BasicAWSCredentials(awsConfig.getAccessKey(), awsConfig.getSecretKey());
		sqsClient = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAwsCreds))
				.withRegion(Regions.US_WEST_2).build();
		createQueue();
	}
	
	private void createQueue(){
		try{
			CreateQueueRequest createQueueRequest = new CreateQueueRequest(sqsConfig.getQueueName())
					.withAttributes(sqsConfig.getAttributes());
			this.sqsQueueUrl = sqsClient.createQueue(createQueueRequest).getQueueUrl();
			
		}catch (AmazonServiceException ase) {
			logger.severe("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SQS, but was rejected with an error response for some reason.");
			logger.severe("Error Message:    " + ase.getMessage());
			logger.severe("HTTP Status Code: " + ase.getStatusCode());
			logger.severe("AWS Error Code:   " + ase.getErrorCode());
			logger.severe("Error Type:       " + ase.getErrorType());
			logger.severe("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			logger.severe("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SQS, such as not "
					+ "being able to access the network.");
			logger.severe("Error Message: " + ace.getMessage());
		} catch (Exception e) {
			logger.severe("" + e);
		}
	}
	public void push(String input) {
		try{
		SendMessageRequest sendMessageRequest = new SendMessageRequest(sqsQueueUrl, input);
		sendMessageRequest.setMessageGroupId("cloudPi");

		sqsClient.sendMessage(sendMessageRequest);
		logger.info("message " + input);
		}catch (AmazonServiceException ase) {
			logger.severe("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SQS, but was rejected with an error response for some reason.");
			logger.severe("Error Message:    " + ase.getMessage());
			logger.severe("HTTP Status Code: " + ase.getStatusCode());
			logger.severe("AWS Error Code:   " + ase.getErrorCode());
			logger.severe("Error Type:       " + ase.getErrorType());
			logger.severe("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			logger.severe("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SQS, such as not "
					+ "being able to access the network.");
			logger.severe("Error Message: " + ace.getMessage());
		} catch (Exception e) {
			logger.severe("" + e);
		}
	}

	public String pop() {
		String msg = null;
		try {
			logger.info("Receiving messages from MyFifoQueue.fifo.\n");
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsQueueUrl);
			
			List<Message> messages = new ArrayList<Message>();
			while(messages.isEmpty() || messages.size() == 0)
				messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
			
			msg = messages.get(0).getBody();

			// Delete the message
			logger.info("Deleting the message. "+msg);
			String messageReceiptHandle = messages.get(0).getReceiptHandle();
			sqsClient.deleteMessage(new DeleteMessageRequest(sqsQueueUrl, messageReceiptHandle));
			
		} catch (AmazonServiceException ase) {
			logger.severe("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SQS, but was rejected with an error response for some reason.");
			logger.severe("Error Message:    " + ase.getMessage());
			logger.severe("HTTP Status Code: " + ase.getStatusCode());
			logger.severe("AWS Error Code:   " + ase.getErrorCode());
			logger.severe("Error Type:       " + ase.getErrorType());
			logger.severe("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			logger.severe("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SQS, such as not "
					+ "being able to access the network.");
			logger.severe("Error Message: " + ace.getMessage());
		} catch (Exception e) {
			logger.severe("" + e);
		}
		return msg;
	}

	public int getNumberOfMessages() {
		GetQueueAttributesResult result = null;
		try {
			GetQueueAttributesRequest request = new GetQueueAttributesRequest().withQueueUrl(sqsQueueUrl)
					.withAttributeNames(QueueAttributeName.ApproximateNumberOfMessages);
			synchronized(mutex){
				Thread.sleep(2000);
				result = sqsClient.getQueueAttributes(request);
			}
		} catch (AmazonServiceException ase) {
			logger.severe("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SQS, but was rejected with an error response for some reason.");
			logger.severe("Error Message:    " + ase.getMessage());
			logger.severe("HTTP Status Code: " + ase.getStatusCode());
			logger.severe("AWS Error Code:   " + ase.getErrorCode());
			logger.severe("Error Type:       " + ase.getErrorType());
			logger.severe("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			logger.severe("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SQS, such as not "
					+ "being able to access the network.");
			logger.severe("Error Message: " + ace.getMessage());
		} catch (Exception e) {
			logger.severe("" + e);
		}
		
		String size = result.getAttributes().get(QueueAttributeName.ApproximateNumberOfMessages.toString());
		logger.info("queue size "+size);
		return Integer.valueOf(size);

	}

}
