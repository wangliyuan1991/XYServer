package com.xy.util;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class DBUtil implements Serializable {
	private static final long serialVersionUID = 1L;
	private static String username = "uds";
	private static String password = "infodba";
	private static String diver = "oracle.jdbc.OracleDriver";
	private static String url = "jdbc:oracle:thin:@172.16.100.122:1521:materialcode";
	static Logger logger  =  null;
	
	public DBUtil(){
		logger = Logger.getLogger(DBUtil.class);
	}
	
	/**
	 * 获取数据库连接
	 * @return 数据库连接
	 */
	private synchronized static Connection getConnection() {
		Connection connection = null;
		try {
			Class.forName(diver);
			connection = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("class not find!", e);
		} catch (SQLException e2) {
			throw new RuntimeException("get connection error!", e2);
		}
		return connection;
	}
	
	/**
	 * 根据登录用户的用户名，获取数据库中的用户信息
	 * @param userID 用户名
	 * @return 数据库中的用户信息
	 * @throws Exception
	 */
	public static String getUserInfo(String userID) throws Exception {
		
		String password = "";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet executeQuery = null;
		
		try {
			conn = getConnection();
			String sql = "select \"password\" from \"XY_SOA_UserInfo\" where \"user_id\" = '" + userID + "'";
			pstmt = conn.prepareStatement(sql);
			executeQuery = pstmt.executeQuery();
			while(executeQuery.next()){
				password = executeQuery.getString("password");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
		return password;
	}
	
	/**
	 * 向用户表中插入一条数据
	 * @param userID 用户名
	 * @param password 密码
	 * @throws Exception
	 */
	public static void userInsert(String userID, String password) throws Exception{
		Connection conn = null;
		PreparedStatement pstmt = null;
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = formatter.format(date);
		try {
			conn = getConnection();
			String sql = "insert into \"XY_SOA_UserInfo\" (\"user_id\",\"password\",\"create_date\",\"update_date\") values(?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userID);
			pstmt.setString(2, password);
			pstmt.setString(3, dateStr);
			pstmt.setString(4, "");
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	/**
	 * 更新用户表中的数据
	 * @param userID 用户名
	 * @param password 密码
	 * @throws Exception
	 */
	public static void userUpdate(String userID, String password) throws Exception{
		Connection conn = null;
		PreparedStatement pstmt = null;
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = formatter.format(date);
		
		try {
			conn = getConnection();
			String sql = "update \"XY_SOA_UserInfo\" set \"password\" = '" + password + "',\"update_date\" = to_date(?,'yyyy-mm-dd hh24:mi:ss') where \"user_id\" = '" + userID + "'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, dateStr);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}
}
