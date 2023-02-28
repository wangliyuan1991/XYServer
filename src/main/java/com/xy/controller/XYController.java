package com.xy.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xy.service.XYWorkFLowService;

@Controller
@ResponseBody
@RequestMapping("/xy")
public class XYController {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private XYWorkFLowService service;
	
	@RequestMapping("/dealWorkFlow")
	public String dealWorkFlow(@RequestBody String jsonBody){
		
		logger.info("dealWorkFlow start:" + jsonBody);
		return service.run(jsonBody);
	}
}
