package com.herakles.test;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PropertyHandler {
	private final static ResourceBundle PROPS = ResourceBundle.getBundle("callws");
	private static final Logger log = LogManager.getLogger(PropertyHandler.class);

	static {
		init();
	}

	private static String TargetURL = PROPS.getString("target.url");
	private static String Action = PROPS.getString("soap.action");
	private static String UserID = PROPS.getString("user.id");
	private static String Password = PROPS.getString("user.password");
	private static Boolean Attachment = Boolean.parseBoolean(PROPS.getString("submit.attachment"));
	private static String RequestFileName = PROPS.getString("request.filename");
	private static String FromURI = PROPS.getString("from");
	private static String ToURI = PROPS.getString("to");
	private static String TargetNamespace = PROPS.getString("target.namespace");
	private static Boolean UseTcpMon = Boolean.parseBoolean(PROPS.getString("use.tcpmon"));
	private static String TcpHost = PROPS.getString("tcpmon.host");
	private static Boolean UseMTOM = Boolean.parseBoolean(PROPS.getString("use.mtom"));
	private static String TcpMonPort = PROPS.getString("tcpmon.port");
	private static Boolean SendAS4Headers = Boolean.parseBoolean(PROPS.getString("send.as4.headers"));
	private static Boolean SendAS4Pull = Boolean.parseBoolean(PROPS.getString("send.as4.pull"));

	public static void init() {
		log.debug("Obtained the following propertes");
		for (Enumeration<String> iterator = PROPS.getKeys(); iterator.hasMoreElements();) {
			String tmp = (String) iterator.nextElement();
			log.debug(tmp + ":" + PROPS.getString(tmp));
		}
	}
	
	public static String getTargetURL() {
		return TargetURL;
	}
	public static void setTargetURL(String targetURL) {
		TargetURL = targetURL;
	}
	public static String getAction() {
		return Action;
	}
	public static void setAction(String action) {
		Action = action;
	}
	public static String getUserID() {
		return UserID;
	}
	public static void setUserID(String userID) {
		UserID = userID;
	}
	public static String getPassword() {
		return Password;
	}
	public static void setPassword(String password) {
		Password = password;
	}
	public static Boolean isAttachment() {
		return Attachment;
	}
	public static void setAttachment(Boolean isAttachment) {
		PropertyHandler.Attachment = isAttachment;
	}
	public static String getRequestFileName() {
		return RequestFileName;
	}
	public static void setRequestFileName(String requestFileName) {
		RequestFileName = requestFileName;
	}
	public static String getFromURI() {
		return FromURI;
	}
	public static void setFromURI(String fromURI) {
		FromURI = fromURI;
	}
	public static String getToURI() {
		return ToURI;
	}
	public static void setToURI(String toURI) {
		ToURI = toURI;
	}
	public static String getTargetNamespace() {
		return TargetNamespace;
	}
	public static void setTargetNamespace(String targetNamespace) {
		TargetNamespace = targetNamespace;
	}
	public static Boolean isUseTcpMon() {
		return UseTcpMon;
	}
	public static void setUseTcpMon(Boolean flag) {
		UseTcpMon = flag;
	}
	public static String getTcpMonHost() {
		return TcpHost;
	}
	public static void setTcpMonHost(String tcpHost) {
		TcpHost = tcpHost;
	}
	public static Boolean isUseMTOM() {
		return UseMTOM;
	}
	public static void setUseMTOM(Boolean flag) {
		UseMTOM = flag;
		log.debug("Current value of MTOM = "+UseMTOM);
	}
	public static String getTcpMonPort() {
		return TcpMonPort;
	}
	public static void setTcpMonPort(String tcpMonPort) {
		TcpMonPort = tcpMonPort;
	}
	public static Boolean isSendAS4Headers() {
		return SendAS4Headers;
	}
	public static void setSendAS4Headers(Boolean flag) {
		SendAS4Headers = flag;
	}
	public static Boolean isSendAS4Pull() {
		return SendAS4Pull;
	}
	public static void setSendAS4Pull(Boolean flag) {
		SendAS4Pull = flag;
	}
}
