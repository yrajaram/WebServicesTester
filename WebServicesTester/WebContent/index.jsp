<%@page import="com.herakles.test.CallWS"%>
<%@page import="com.herakles.test.JspHelper"%>
<%@page import="org.apache.log4j.LogManager,org.apache.log4j.Logger" %>
<%@page import="java.util.*,java.sql.*"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"    pageEncoding="ISO-8859-1"%>

<jsp:useBean id="form" class="com.herakles.test.JspHelper" scope="request"/>
<jsp:setProperty name="form" property="*" /> 
 
<%!
private static final Logger log = LogManager.getLogger("index_jsp.class");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Test Your WebService</title>
<link rel="stylesheet" type="text/css" href="light-blue.css" /> 
</head>
<body> 

<form name="invokeWS" method="POST" action="" >
<table border="0" cellpadding="0">
<tr>
<th colspan="2">Test your Webservice</th>
</tr>
<tr>
	<td width="15%">End Point URL</td>
	<td><input type="text" name="targetURL" size="50" value="${form.targetURL}" /></td>
</tr>
<tr>
	<td width="15%">SOAP Action</td>
	<td><input type="text" name="action" size="50" value="${form.action}" /></td>
</tr>
<tr>
	<td width="15%">Fill AS4 Headers</td>
	<td><input type="text" name="sendAS4Headers" size="50" value="${form.sendAS4Headers}"  /></td>
</tr>
<tr>
	<td width="15%">Submit AS4 PULL</td>
	<td><input type="text" name="sendAS4Pull" size="50" value="${form.sendAS4Pull}"  /></td>
</tr>
<tr>
	<td width="15%">Username</td>
	<td><input type="text" name="username" size="50" value="${form.userID}" /></td>
</tr>
<tr>
	<td width="15%">Password</td>
	<td><input type="password" name="password" size="50" value="${form.password}" /></td>
</tr>
<tr>
	<td width="15%">From</td>
	<td><input type="text" name="fromURI" size="50" value="${form.fromURI}" /></td>
</tr>
<tr>
	<td width="15%">To</td>
	<td><input type="text" name="toURI" size="50" value="${form.toURI}" /></td>
</tr>
<tr>
	<td width="15%">Submit Attachments</td>
	<td><input type="text" name="attachment" value="${form.attachment}" /></td>
</tr>
<tr>
	<td width="15%">Use MTOM</td>
	<%--
		<td><input type="checkbox" name="useMTOM" value="${form.useMTOM}" checked="${form.useMTOM}"/></td>
	 --%>
	<td><input type="text" name="useMTOM" value="${form.useMTOM}" /></td>
</tr>
<tr>
	<td width="15%">Request File Name</td>
	<td><input type="text" name="requestFileName" size="50" value="${form.requestFileName}" /></td>
</tr>
<tr>
	<td width="15%">Target Namespace</td>
	<td><input type="text" name="targetNamespace" size="50" value="${form.targetNamespace}" /></td>
</tr>
<tr>
	<td width="15%">Use TCPmon</td>
	<td><input type="text" name="useTcpMon" value="${form.useTcpMon}"  /></td>
</tr>
<tr>
	<td width="15%">TCPmon Host</td>
	<td><input type="text" name="tcpMonHost" size="50" value="${form.tcpMonHost}" /></td>
</tr>
<tr>
	<td width="15%">TCPmon Port</td>
	<td><input type="text" name="tcpMonPort" size="50" value="${form.tcpMonPort}" /></td>
</tr>
<tr>
		<td>
			<input type="hidden" name="submitted" value="false"/>
			<input type="submit" name="go" value="Execute API" onclick="{document.invokeWS.submitted.value='true';}"/> 
		</td>
		<td><input type="reset" name="toSquareOne"/></td>
</tr>
<tr>
<%
java.util.Date date = new java.util.Date();
String tmp = request.getParameter("submitted");
if (tmp !=null && tmp.equalsIgnoreCase("true")){
	com.herakles.test.CallWS.submitSOAPRequest();
}
%>
<td width="15%">Response at <%=new Timestamp(date.getTime())%></td>
<td><textarea rows="20" cols="100%" name="result">${form.result}</textarea></td>
</tr>
</table>
</form>
</body>
</html>