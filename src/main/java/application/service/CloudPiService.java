package application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CloudPiService {

	@Autowired
	private AWSEC2ClientService ec2ClientService;
	@Autowired
	private AWSSQSClientService sqsClientService; 
	@Autowired
	private AWSS3ClientService s3ClientService;
	//TODO move to config file
	private static final int INSTANCE_POOL_SIZE = 10;	
	
	public void calculatePi(String input){
	   
		sqsClientService.push(input);
		
		while(sqsClientService.getNumberOfMessages() > 0){
			if(ec2ClientService.getRunningInstanceCount() <= INSTANCE_POOL_SIZE){
				String msg = sqsClientService.pop();
				String instanceId = ec2ClientService.createInstance(msg);
				if(s3ClientService.hasObject(input)){
					ec2ClientService.terminateInstance(instanceId);
				}
			}
		}
	}
}
