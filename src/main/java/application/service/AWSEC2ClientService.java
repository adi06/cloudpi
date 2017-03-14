package application.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

import application.configuration.AWSConfiguration;

@Service
public class AWSEC2ClientService {
	private AmazonEC2 ec2Client;
	private BasicAWSCredentials basicAwsCreds;
	@Autowired
	private AWSConfiguration awsConfig;
	private static final Object mutex = new Object();
	private static final Logger logger = Logger.getLogger(AWSEC2ClientService.class.getName());

	@PostConstruct
	public void init() {
		logger.info("Initializing ec2client");
		basicAwsCreds = new BasicAWSCredentials(awsConfig.getAccessKey(), awsConfig.getSecretKey());
		ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAwsCreds))
				.withRegion(Regions.US_WEST_2).build();
	}

	public String createInstance(String iterations) {
		logger.info("creating instance");
		String instanceId = null;
		try {

			String cmd = "#!/bin/bash\n" + "echo {0} > ~/pifft/test-{0}.in\n"
					+ "~/pifft/pifft ~/pifft/test-{0}.in > /tmp/pifft-{0}.txt\n" + "~/bin/aws s3api put-object"
					+ " --bucket s3-cloudpi" + " --key \"`cat ~/pifft/test-{0}.in`\"" + " --body /tmp/pifft-{0}.txt";

			cmd = MessageFormat.format(cmd, new Object[] { iterations });
			logger.info("Command " + cmd);
			String cmdEncoding = Base64.getEncoder().encodeToString(cmd.getBytes());
			// TODO move to configuration file.
			RunInstancesRequest runInstanceRequest = new RunInstancesRequest().withImageId(awsConfig.getAmi())
					.withMinCount(1).withMaxCount(1).withInstanceType(awsConfig.getInstanceType()).withKeyName(awsConfig.getKeypair())
					.withSecurityGroups(awsConfig.getSecurityGroup()).withUserData(cmdEncoding);

			RunInstancesResult instancesResult = ec2Client.runInstances(runInstanceRequest);
			instanceId = instancesResult.getReservation().getInstances().get(0).getInstanceId();
			
		} catch (AmazonServiceException e) {
			logger.severe("Error creating instances");
			logger.severe("Caught Exception: " + e.getMessage());
			logger.severe("Reponse Status Code: " + e.getStatusCode());
			logger.severe("Error Code: " + e.getErrorCode());
			logger.severe("Request ID: " + e.getRequestId());
		} catch (Exception e) {
			logger.severe("Caught Exception: " + e.getMessage());
		}
		return instanceId;
	}

	public void stopInstance(String instanceId) {
		// TODO
	}

	public void startInstance(String instanceId) {
		// TODO
	}

	public void terminateInstance(String... instanceIds) {
		try {
			logger.info("terminating instance "+instanceIds[0]);
			TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(Arrays.asList(instanceIds));
			ec2Client.terminateInstances(terminateRequest);
		} catch (AmazonServiceException e) {
			logger.severe("Error terminating instances");
			logger.severe("Caught Exception: " + e.getMessage());
			logger.severe("Reponse Status Code: " + e.getStatusCode());
			logger.severe("Error Code: " + e.getErrorCode());
			logger.severe("Request ID: " + e.getRequestId());
		} catch (Exception e) {
			logger.severe("Caught Exception " + e.getMessage());
		}
	}

	public int getRunningInstanceCount() {
		int count = 0;
		try {
			List<String> states = new ArrayList<String>();
			states.add("pending");
			states.add("running");
			Filter filter = new Filter("instance-state-name", states);
			DescribeInstancesRequest request = new DescribeInstancesRequest().withFilters(filter);
			DescribeInstancesResult result;
			synchronized (mutex) {
				result = ec2Client.describeInstances(request);
		}
			count = result.getReservations().size();
			logger.info("instance count "+count);
		} catch (AmazonServiceException e) {
			logger.severe("Error terminating instances");
			logger.severe("Caught Exception: " + e.getMessage());
			logger.severe("Reponse Status Code: " + e.getStatusCode());
			logger.severe("Error Code: " + e.getErrorCode());
			logger.severe("Request ID: " + e.getRequestId());
		} catch (Exception e) {
			logger.severe("Caught Exception " + e.getMessage());
		}
		return count;
	}

}
