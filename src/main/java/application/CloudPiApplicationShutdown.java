package application;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import application.service.AWSS3ClientService;

@Component
public class CloudPiApplicationShutdown implements DisposableBean {

	@Autowired
	private AWSS3ClientService s3ClientService;
	
	@Override
	public void destroy() throws Exception {
		//delete all objects from current application run
		s3ClientService.deleteAllObjects();
	}

}
