package application.service;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;

import application.configuration.AWSConfiguration;

@Service
public class AWSS3ClientService {
	
	private AmazonS3 s3Client;
	private BasicAWSCredentials basicAwsCreds;
	@Autowired
	private AWSConfiguration awsConfig;
	private static final Logger logger = Logger.getLogger(AWSS3ClientService.class.getName());
	//TODO move to config file
	private static final String bucketName = "s3-cloudpi";
	
	@PostConstruct
	public void init(){
		logger.info("Initializing s3client");
		basicAwsCreds = new BasicAWSCredentials(awsConfig.getAccessKey(), awsConfig.getSecretKey());
		s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAwsCreds))
				.withRegion(Regions.US_WEST_2).build();
	}
	
	public void createBucket(String name){
		try {
			Bucket bucket = s3Client.createBucket(bucketName);
			logger.info("Bucket created "+bucket.getName());
		} catch (AmazonServiceException e) {
			logger.severe("Error creating bucket");
			logger.severe("Caught Exception: " + e.getMessage());
			logger.severe("Reponse Status Code: " + e.getStatusCode());
			logger.severe("Error Code: " + e.getErrorCode());
			logger.severe("Request ID: " + e.getRequestId());
		} catch (Exception e) {
			logger.severe("Caught Exception: " + e.getMessage());
		}
	}
	
	public boolean hasObject(String key){
		boolean hasObject = false;
		try{
			while(!hasObject) {
				Thread.sleep(2000);
				hasObject = s3Client.doesObjectExist(bucketName, key);
			}
			logger.info("object found in bucket..");
		}catch (AmazonServiceException e) {
			logger.severe("Error fetching key in bucket");
			logger.severe("Caught Exception: " + e.getMessage());
			logger.severe("Reponse Status Code: " + e.getStatusCode());
			logger.severe("Error Code: " + e.getErrorCode());
			logger.severe("Request ID: " + e.getRequestId());
		} catch (Exception e) {
			logger.severe("Caught Exception: " + e.getMessage());
		}
		return hasObject;
	}
}
