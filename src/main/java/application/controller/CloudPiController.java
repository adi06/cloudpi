package application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import application.service.CloudPiService;

@RestController
public class CloudPiController {
	@Autowired
	private CloudPiService cloudPiService;
	
	@RequestMapping(value = "/cloudpi", method = RequestMethod.GET)
	public String calculatePi(@RequestParam(value = "input", required = true)int input){
		return cloudPiService.calculatePi(input);
		
	}
}
