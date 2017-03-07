package application.service;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectAclRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;

@Service
public class AWSS3Service {
	@Autowired
	private AWSClientService awsClientService;
	private static final Logger logger = Logger.getLogger(AWSS3Service.class.getName());
	//TODO move to config file
	private static final String bucketName = "s3-cloudpi";
	
	public void createBucket(String name){
		try {
			Bucket bucket = awsClientService.getS3Client().createBucket(bucketName);
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
				Thread.sleep(2);
				hasObject = awsClientService.getS3Client().doesObjectExist(bucketName, key);
				logger.info("waiting for the upload to complete..");
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
