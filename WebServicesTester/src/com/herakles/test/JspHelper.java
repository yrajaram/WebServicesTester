package com.herakles.test;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class JspHelper {
	private static final Logger log = LogManager.getLogger(JspHelper.class);

	private static String result;
	
	public  String getTargetURL() {
		return PropertyHandler.getTargetURL();
	}
	public  void setTargetURL(String targetURL) {
		PropertyHandler.setTargetURL(targetURL);
	}
	public  String getAction() {
		return PropertyHandler.getAction();
	}
	public  void setAction(String action) {
		PropertyHandler.setAction(action);
	}
	public  String getUserID() {
		return PropertyHandler.getUserID();
	}
	public  void setUserID(String userID) {
		PropertyHandler.setUserID(userID);
	}
	public  String getPassword() {
		return PropertyHandler.getPassword();
	}
	public  void setPassword(String password) {
		PropertyHandler.setPassword(password);
	}
	public  String getAttachment() {
		return PropertyHandler.isAttachment().toString();
	}
	public  void setAttachment(String flag) {
		log.debug("Send attachments?:"+flag);
		PropertyHandler.setAttachment(Boolean.parseBoolean(flag));
	}
	public  String getRequestFileName() {
		return PropertyHandler.getRequestFileName();
	}
	public  void setRequestFileName(String requestFileName) {
		PropertyHandler.setRequestFileName(requestFileName);
	}
	public  String getFromURI() {
		return PropertyHandler.getFromURI();
	}
	public  void setFromURI(String fromURI) {
		PropertyHandler.setFromURI(fromURI);
	}
	public  String getToURI() {
		return PropertyHandler.getToURI();
	}
	public  void setToURI(String toURI) {
		PropertyHandler.setToURI(toURI);
	}
	public  String getTargetNamespace() {
		return PropertyHandler.getTargetNamespace();
	}
	public  void setTargetNamespace(String targetNamespace) {
		PropertyHandler.setTargetNamespace(targetNamespace);
	}
	public  String getUseTcpMon() {
		return PropertyHandler.isUseTcpMon().toString();
	}
	public  void setUseTcpMon(String flag) {
		PropertyHandler.setUseTcpMon(Boolean.parseBoolean(flag));
	}
	public  String getTcpMonHost() {
		return PropertyHandler.getTcpMonHost();
	}
	public  void setTcpMonHost(String tcpHost) {
		PropertyHandler.setTcpMonHost(tcpHost);
	}
	public  String getUseMTOM() {
		return PropertyHandler.isUseMTOM().toString();
	}
	public  void setUseMTOM(String flag) {
		log.debug("Current value of MTOM = "+flag);
		PropertyHandler.setUseMTOM(Boolean.parseBoolean(flag));
	}
	public  String getTcpMonPort() {
		return PropertyHandler.getTcpMonPort();
	}
	public  void setTcpMonPort(String tcpMonPort) {
		PropertyHandler.setTcpMonPort(tcpMonPort);
	}
	public String getSendAS4Pull() {
		return PropertyHandler.isSendAS4Pull().toString();
	}
	public void setSendAS4Pull(String flag) {
		PropertyHandler.setSendAS4Pull(Boolean.parseBoolean(flag));
	}
	public void setSendAS4Headers (String flag) {
		log.debug("Send AS4 = "+flag);
		PropertyHandler.setSendAS4Headers(Boolean.parseBoolean(flag));
	}
	public String getSendAS4Headers () {
		return PropertyHandler.isSendAS4Headers().toString();
	}
	public String getResult() {
		return result;
	}
	public void setToSquareOne() {
		log.debug("Initializing...");
		PropertyHandler.init();
	}
	public void setGo() {
		log.debug("Invoking...");
		com.herakles.test.CallWS.submitSOAPRequest();
	}
	public static void setResult(String res) {
		result = res;
	}
}
