package application.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesResult;

import application.instance.AWSEC2InstanceService;

@Service
public class CloudPiService {

	@Autowired
	private AWSEC2InstanceService awsec2InstanceService;
	private static final Logger logger = Logger.getLogger(CloudPiService.class.getName());
	
	
	public String calculatePi(int input){
		RunInstancesResult runInstanceResult = awsec2InstanceService.createInstance();
		List<Instance> instances = runInstanceResult.getReservation().getInstances();
		logger.info("instanceId "+instances.get(0).getInstanceId());
		return instances.get(0).getInstanceId();
	}
}