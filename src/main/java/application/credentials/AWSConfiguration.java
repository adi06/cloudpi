package application.credentials;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="pi.aws")
public class AWSConfiguration {
	
	private String accessKey;
	private String secretKey;
	private String region;
	private String ami;
	private String keypair;
	private String securityGroup;
	private String instanceType;
	
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getAmi() {
		return ami;
	}
	public void setAmi(String ami) {
		this.ami = ami;
	}
	public String getKeypair() {
		return keypair;
	}
	public void setKeypair(String keypair) {
		this.keypair = keypair;
	}
	public String getSecurityGroup() {
		return securityGroup;
	}
	public void setSecurityGroup(String securityGroup) {
		this.securityGroup = securityGroup;
	}
	public String getInstanceType() {
		return instanceType;
	}
	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}
}
