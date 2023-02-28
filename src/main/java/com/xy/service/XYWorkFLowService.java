package com.xy.service;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.EPMPerformSignoffTask;
import com.teamcenter.soa.client.model.strong.User;
import com.xy.management.WorkflowManagement;
import com.xy.util.DBUtil;
import com.xy.util.XyUtils;

@Service
public class XYWorkFLowService {
	
	private Logger logger = Logger.getLogger(getClass());
	private String account;//用户名
	private String password;//密码
	private String uid;//UID
	private String dealType;//审批类型
	private String comment;//评论
	private String host;//TC服务地址
	private AppXSession session;

	/**
	 * 对审批流进行处理
	 * @param jsonBody 用户名，审批类型(Approve,Reject)，UID，评论
	 * @return 处理结果 OK:处理成功，NG:发生异常
	 */
	public String run(String jsonBody){
		// 获取处理参数
		JSONObject jsonObject = JSONObject.parseObject(jsonBody);
		account = jsonObject.getString("account");//用户名
		uid = jsonObject.getString("uid");//UID
		dealType = jsonObject.getString("dealType");//审批类型
		comment = jsonObject.getString("comment");//评论
		logger.info("account: " + account + ", uid: " + uid + ", dealType: " + dealType + ", comment: " + comment);
		
		// 存在空值的场合，返回NG
		if (account == null || uid == null || dealType == null || comment == null ||
				"".equals(account) || "".equals(uid) || "".equals(dealType)){
			logger.warn("empty or null exits.");
			return "NG";
		}
		
		try {
			// 获取密码
			password = DBUtil.getUserInfo(account);
			if ("".equals(password)){
				logger.info("The login information has not yet been logged into the database.");
				return "NG";
			} else {
				password = XyUtils.decoder(password);
			}
			
			//获取TC服务地址
			Properties properties = new Properties();
			FileInputStream in = new FileInputStream("application.properties");
			properties.load(in);
			host = properties.getProperty("tc.host");
			logger.info("TC host:" + host);
			
			//登录TC
			session = new AppXSession("http://" + host + "/tc", account, password);
	        session.login();
	        
	        WorkflowManagement wm=new WorkflowManagement(AppXSession.getConnection());
	        ModelObject loadObject = XyUtils.loadObject(uid);
	        if ("Approve".equals(dealType)){
				//通过流程
				wm.performEPMPerformSignoffTaskWithApprove((EPMPerformSignoffTask)loadObject, comment, "",(User)null);
			} else if ("Reject".equals(dealType)){
				//拒绝流程
				wm.performEPMPerformSignoffTaskWithReject((EPMPerformSignoffTask)loadObject, comment, "",(User)null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(XyUtils.getErrorInfoFromException(e));
			return "NG";
		} finally {
			if (session != null)
			session.logout();
		}
		
		logger.info("dealWorkFlow end");
		return "OK";
	}
}
