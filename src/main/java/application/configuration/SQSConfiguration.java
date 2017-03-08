package application.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="pi.sqs")
public class SQSConfiguration {
	private String queueName;
	private Map<String, String> attributes = new HashMap<String, String>();

	public SQSConfiguration() {
		attributes.put("FifoQueue", "true");
		attributes.put("ContentBasedDeduplication", "true");
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

}
