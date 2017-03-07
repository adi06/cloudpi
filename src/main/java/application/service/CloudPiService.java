package application.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesResult;

import application.instance.AWSEC2InstanceService;
import application.instance.SQSInstanceServices;

@Service
public class CloudPiService {

	@Autowired
	private AWSEC2InstanceService awsec2InstanceService;
	@Autowired
	private SQSInstanceServices sqsInstanceService; 
	@Autowired
	private AWSS3Service s3Service;
	//TODO move to config file
	private static final int INSTANCE_POOL_SIZE = 10;
	private static final Logger logger = Logger.getLogger(CloudPiService.class.getName());
	
	
	public void calculatePi(String input){
	   
		String res=sqsInstanceService.push(input);
		
		//TODO replace queue size
		
		/*while(!queue.isEmpty()){
			if(awsec2InstanceService.getRunningInstanceCount() <= INSTANCE_POOL_SIZE){
				//queue.pop get input
				String instanceId = awsec2InstanceService.createInstance(input);
				if(s3Service.hasObject(input)){
					awsec2InstanceService.terminateInstance(instanceId);
				}
			}
			else{
				Thread.sleep(3);
			}
		}*/
	}
}
