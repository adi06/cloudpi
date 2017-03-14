package application.service;

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
		String output = "";
		sqsClientService.push(input);

		while (sqsClientService.getNumberOfMessages() > 0) {
			int count = ec2ClientService.getRunningInstanceCount();
			if (count <= INSTANCE_POOL_SIZE) {
				String msg = sqsClientService.pop();
				String instanceId;
				instanceId = ec2ClientService.createInstance(msg);
				instanceIdMap.put(instanceId, msg);

				while (!s3ClientService.hasObject(instanceIdMap.get(instanceId))) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						logger.severe("" +e);
					}
				}
				ec2ClientService.terminateInstance(instanceId);
				output = s3ClientService.getObject(instanceIdMap.get(instanceId));
				instanceIdMap.remove(instanceId);

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
