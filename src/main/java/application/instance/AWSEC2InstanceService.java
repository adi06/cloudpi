package application.instance;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;

import application.credentials.AWSConfiguration;
import application.service.AWSClientService;

@Service
public class AWSEC2InstanceService {
	@Autowired
	private AWSClientService awsClientService;
	@Autowired
	private AWSConfiguration awsConfig;
	
	private static final Logger logger = Logger.getLogger(AWSEC2InstanceService.class.getName());

	public RunInstancesResult createInstance() {
		logger.info("creating instance");
		logger.info(awsConfig.getAmi());
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId(awsConfig.getAmi())
				.withInstanceType(awsConfig.getInstanceType()).withMinCount(1).withMaxCount(1)
				.withKeyName(awsConfig.getKeypair()).withSecurityGroups(awsConfig.getSecurityGroup());

		return awsClientService.getEC2Client().runInstances(runInstancesRequest);
	}

	public void stopInstance(String instanceId) {

	}

	public void startInstance(String instanceId) {

	}

	public void terminateInstance(String instanceId) {

	}

}
