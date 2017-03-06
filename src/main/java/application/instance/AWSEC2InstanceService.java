package application.instance;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

import application.credentials.AWSConfiguration;
import application.service.AWSClientService;

@Service
public class AWSEC2InstanceService {
	@Autowired
	private AWSClientService awsClientService;
	@Autowired
	private AWSConfiguration awsConfig;

	private static final Logger logger = Logger.getLogger(AWSEC2InstanceService.class.getName());

	public String createInstance(int iterations) {
		logger.info("creating instance");
		String input = String.valueOf(iterations);
		String instanceId = null;
		try {

			String cmd = "#!/bin/bash\n" 
							+ "echo {0} > ~/pifft/test-{0}.in\n"
							+ "~/pifft/pifft ~/pifft/test-{0}.in > /tmp/pifft-{0}.out\n"
							+ "~/bin/aws s3api put-object"
							+ " --bucket s3-cloudpi" + " --key \"`cat ~/pifft/test-{0}.in`\"" 
							+ " --body /tmp/pifft-{0}.out";
			cmd = MessageFormat.format(cmd, new Object[] { input });
			logger.info("Command " + cmd);
			String cmdEncoding = Base64.getEncoder().encodeToString(cmd.getBytes());
			//TODO move to configuration file.
			RunInstancesRequest runInstanceRequest = new RunInstancesRequest().withImageId("ami-5deb673d")
					.withMinCount(1).withMaxCount(1).withInstanceType("t2.micro").withKeyName("kp_cloudpi")
					.withSecurityGroups("sg_cloudpi").withUserData(cmdEncoding);

			RunInstancesResult instancesResult = awsClientService.getEC2Client().runInstances(runInstanceRequest);
			instanceId = instancesResult.getReservation().getInstances().get(0).getInstanceId();
			logger.info("InstanceId " + instanceId);

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

	}

	public void startInstance(String instanceId) {

	}

	public void terminateInstance(String ...instanceIds) {
		try {
			TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(Arrays.asList(instanceIds));
			TerminateInstancesResult result = awsClientService.getEC2Client().terminateInstances(terminateRequest);
			logger.info("terminate instance result "+result);
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

}
