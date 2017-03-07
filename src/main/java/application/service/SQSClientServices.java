package application.service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

import application.credentials.AwsCredentials;
import application.credentials.SQSConfiguration;

@Service
public class SQSClientServices {
	private AmazonSQS sqsClient;
	private String sqsQueueUrl;

	@Autowired
	private SQSConfiguration sqsConfig;
	private static final Logger LOGGER = Logger.getLogger(SQSClientServices.class.getName());

	@SuppressWarnings("deprecation")
	@PostConstruct
	public void init() {
		LOGGER.info("Creating new SQS FIFO queue");
		sqsClient = new AmazonSQSClient().withRegion(Regions.US_WEST_2);

		CreateQueueRequest createQueueRequest = new CreateQueueRequest(sqsConfig.getQueueName())
				.withAttributes(sqsConfig.getAttributes());
		sqsQueueUrl = sqsClient.createQueue(createQueueRequest).getQueueUrl();
	}

	public String getSqsQueueUrl() {
		return sqsQueueUrl;
	}

	public AmazonSQS getSqsClient() {
		return sqsClient;
	}
}
