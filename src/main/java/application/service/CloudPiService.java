package application.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
	// TODO move to config file
	private static final int INSTANCE_POOL_SIZE = 10;
	private static final ConcurrentHashMap<String, String> instanceIdMap = new ConcurrentHashMap<String, String>();
	private static final Logger logger = Logger.getLogger(CloudPiService.class.getName());

	public String calculatePi(String input) {
		sqsClientService.push(input);
		String output="";

		while (sqsClientService.getNumberOfMessages() > 0) {
			int count = ec2ClientService.getRunningInstanceCount();
			if (count < INSTANCE_POOL_SIZE) {
				String msg = sqsClientService.pop();
				String instanceId = ec2ClientService.createInstance(msg);
				instanceIdMap.put(instanceId, msg);

				while (instanceIdMap.size() > 0) {
					Set<String> instanceIds = instanceIdMap.keySet();
					for (String instanceID : instanceIds) {
						String key = instanceIdMap.get(instanceID);
						if (s3ClientService.hasObject(key)) {
							output = s3ClientService.getObject(key);
							logger.info("key found " + key);
							logger.info("output " + output);
							ec2ClientService.terminateInstance(instanceID);
							instanceIdMap.remove(instanceID);
						}
					}
				}
			} else {
				try {
					logger.info("instance pool reached.." + count);
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					logger.severe("" + e);
				}
			}
		}
		return output;
	}
}
