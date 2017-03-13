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
		// Check if the output is already computed?
		if (!s3ClientService.hasObject(input)) {
			sqsClientService.push(input);
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
								return returnOutput(key, instanceID);
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
		} else {
			// Output already exists no new instance will be created
			logger.info("Output already calculated");
			return returnOutput(input, null);
		}
		return null;
	}

	/**
	 * This function will fetch the output from S3. Terminate the instance if one was created. 
	 */
	public String returnOutput(String key, String instanceID) {
		String output = s3ClientService.getObject(key);
		logger.info("key found " + key);
		logger.info("output " + output);
		if (instanceID != null) {
			ec2ClientService.terminateInstance(instanceID);
			instanceIdMap.remove(instanceID);
		}
		return output;
	}
}
