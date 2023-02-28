package com.xy.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import com.teamcenter.clientx.AppXSession;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;

public class XyUtils {
	
	/**
	 * 根据UID获取ModelObject对象
	 * @param uid
	 * @return ModelObject对象
	 * @throws Exception
	 */
	public static ModelObject loadObject(String uid) throws Exception {
		ModelObject[] objs = loadObject(new String[] { uid });
		if (null != objs && objs.length > 0) {
			return objs[0];
		}
		return null;
	}
	
	/**
	 * 根据UID获取ModelObject对象
	 * @param Uids
	 * @return ModelObject对象
	 * @throws Exception
	 */
	public static ModelObject[] loadObject(String[] Uids) throws Exception {
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		ServiceData sd = dmService.loadObjects(Uids);
		if (sd.sizeOfPartialErrors() > 0) {
			throw new Exception(sd.getPartialError(0).toString());
		} else {
			ModelObject[] ObjArray = new ModelObject[sd.sizeOfPlainObjects()];
			for (int i = 0; i < sd.sizeOfPlainObjects(); i++) {
				ObjArray[i] = sd.getPlainObject(i);
			}
			return ObjArray;
		}
	}
	
	/**
	 * 打印exception的堆栈信息
	 * @param e
	 * @return 堆栈信息
	 */
	public static String getErrorInfoFromException(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			sw.close();
			pw.close();
			return "\r\n" + sw.toString() + "\r\n";
		} catch (Exception e2) {
			return "ErrorInfoFromException";
		}
	}
	
	/**
	 * DES加密
	 * @param pass 密码明文
	 * @return	加密后的密码
	 * @throws Exception
	 */
	public static String encode(String pass) throws Exception{
		KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed("UDSJAVASERVER".getBytes());
		keyGenerator.init(secureRandom);
		Key key = keyGenerator.generateKey();
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(1, key);
		
		byte[] doFinal = cipher.doFinal(pass.getBytes("utf-8"));
		
		Encoder encoder = Base64.getEncoder();
		String encodeBuffer = encoder.encodeToString(doFinal);
		return new String(encodeBuffer);
	}
	
	/**
	   * DES解密
	   * @param paramString 加密后的密码
	   * @return 密码明文
	   * @throws Exception
	   */
	  public static String decoder(String paramString) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed("UDSJAVASERVER".getBytes());
		keyGenerator.init(secureRandom);
		Key key = keyGenerator.generateKey();
		Decoder decoder = Base64.getDecoder();
		try {
			byte[] arrayOfByte1 = decoder.decode(paramString);
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(2, key);
			byte[] arrayOfByte2 = cipher.doFinal(arrayOfByte1);
			return new String(arrayOfByte2, "UTF-8");
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		} 
	}
}
