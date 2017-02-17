package application.service;


import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import application.credentials.AWSConfiguration;

@Service
public class AWSClientService {
	private AmazonEC2 ec2Client;
	private AmazonS3 s3Client;
	private BasicAWSCredentials awsCreds;
	@Autowired
	private AWSConfiguration awsConfig;
	private static final Logger logger = Logger.getLogger(AWSClientService.class.getName());

	@PostConstruct
	public void init(){
		
		awsCreds = new BasicAWSCredentials(awsConfig.getAccessKey(), awsConfig.getSecretKey());
		logger.info("Initializing ec2client");
		
		ec2Client = AmazonEC2ClientBuilder.standard()
								.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
								.withRegion(Regions.US_EAST_1)
								.build();
		logger.info("Initializing s3client");
		s3Client = AmazonS3ClientBuilder.standard()
								.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
								.withRegion(Regions.US_WEST_2)
								.build();
	}

	public AmazonEC2 getEC2Client() {
		return ec2Client;
	}

	public AmazonS3 getS3Client() {
		return s3Client;
	}
}
