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
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Can't load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is a in valid format.", e);
		}

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
