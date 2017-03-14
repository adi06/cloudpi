package application.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;

import application.configuration.AWSConfiguration;
import application.configuration.S3Configuration;

@Service
public class AWSS3ClientService {

	private AmazonS3 s3Client;
	private BasicAWSCredentials basicAwsCreds;
	@Autowired
	private AWSConfiguration awsConfig;
	@Autowired
	private S3Configuration s3Config;
	private static final Logger logger = Logger.getLogger(AWSS3ClientService.class.getName());
	
	@PostConstruct
	public void init() {
		logger.info("Initializing s3client");
		basicAwsCreds = new BasicAWSCredentials(awsConfig.getAccessKey(), awsConfig.getSecretKey());
		s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAwsCreds))
				.withRegion(Regions.US_WEST_2).build();
		createBucket(s3Config.getBucket());
	}

	public void createBucket(String name) {
		try {
			Bucket bucket = s3Client.createBucket(s3Config.getBucket());
			logger.info("Bucket created " + bucket.getName());
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

	public boolean hasObject(String key) {
		boolean hasObject = false;
		try {
			hasObject = s3Client.doesObjectExist(s3Config.getBucket(), key);
		} catch (AmazonServiceException e) {
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

	/**
	 * This method returns the content of the file for the given key.
	 */
	public String getObject(String key) {
		InputStream input = null;
		BufferedReader reader = null;
		try {
			logger.info("Downloading an object");
			S3Object s3object = s3Client.getObject(new GetObjectRequest(s3Config.getBucket(), key));
			logger.info("Content-Type: " + s3object.getObjectMetadata().getContentType());
			input = s3object.getObjectContent();
			reader = new BufferedReader(new InputStreamReader(input));
			StringBuilder sb = new StringBuilder();
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;

				sb.append(line);
				return sb.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
				try {
					if(input != null)
						input.close();
					if(reader != null)
						reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	public void deleteAllObjects() {
		try {
			System.out.println(" - removing objects from bucket");
			ObjectListing object_listing = s3Client.listObjects(s3Config.getBucket());
			while (true) {
				for (Iterator<?> iterator = object_listing.getObjectSummaries().iterator(); iterator.hasNext();) {
					S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
					s3Client.deleteObject(s3Config.getBucket(), summary.getKey());
				}

				// more object_listing to retrieve?
				if (object_listing.isTruncated()) {
					object_listing = s3Client.listNextBatchOfObjects(object_listing);
				} else {
					break;
				}
			}

			logger.info(" - removing versions from bucket");
			VersionListing version_listing = s3Client
					.listVersions(new ListVersionsRequest().withBucketName(s3Config.getBucket()));
			while (true) {
				for (Iterator<?> iterator = version_listing.getVersionSummaries().iterator(); iterator.hasNext();) {
					S3VersionSummary vs = (S3VersionSummary) iterator.next();
					s3Client.deleteVersion(s3Config.getBucket(), vs.getKey(), vs.getVersionId());
				}

				if (version_listing.isTruncated()) {
					version_listing = s3Client.listNextBatchOfVersions(version_listing);
				} else {
					break;
				}
			}

			logger.info(" OK, bucket is empty!");
		} catch (AmazonServiceException e) {
			logger.severe(e.getErrorMessage());
		}
	}
}
