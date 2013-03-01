package com.herakles.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.transport.http.HttpTransportProperties.Authenticator;
import org.apache.axis2.transport.http.HttpTransportProperties.ProxyProperties;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class CallWS {
	private static final Logger log = LogManager.getLogger(CallWS.class);

	private static OMFactory fac = OMAbstractFactory.getOMFactory();
	private static OMNamespace omNs = fac.createOMNamespace(PropertyHandler.getTargetNamespace(), "myNs");
	private static SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();

	public static void main(String[] args) {
		submitSOAPRequest();
		log.debug("-----------End---------");
	}

	public static void submitSOAPRequest() {
		try {
			MessageContext mc = new MessageContext();
			mc.setEnvelope(OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());

			Options options = new Options();
			options.setAction(PropertyHandler.getAction());
			options.setTo(new EndpointReference(PropertyHandler.getTargetURL()));

			options.setTimeOutInMilliSeconds(200 * 60 * 60);
			options.setProperty("__CHUNKED__", Boolean.FALSE);

			HttpTransportProperties.Authenticator basicAuth = new HttpTransportProperties.Authenticator();
			log.debug("after creating basic auth object");
			List auth = new ArrayList();
			auth.add(Authenticator.BASIC);
			basicAuth.setAuthSchemes(auth);
			basicAuth.setUsername(PropertyHandler.getUserID());
			basicAuth.setPassword(PropertyHandler.getPassword());
			log.debug("after setting username and pwd");
			basicAuth.setPreemptiveAuthentication(true);
			options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,	basicAuth);

			//for proxy
			if (PropertyHandler.isUseTcpMon()){
				log.debug("before calling, setting up proxy");
				ProxyProperties pp = new ProxyProperties();
				pp.setProxyName(PropertyHandler.getTcpMonHost());
				pp.setProxyPort(Integer.parseInt(PropertyHandler.getTcpMonPort()));
				options.setProperty(HTTPConstants.PROXY, pp);
			}
			
			String[] ids = handleInlineOrAttachment(mc, options);
			
			ServiceClient sender = null;
			if (PropertyHandler.isSendAS4Pull()) {
				sender = getServiceClientWithAS4PullHeaders(mc);
			} else {
				sender = getServiceClientWithAS4PushHeaders(mc, ids);
			}

			sender.setOptions(options);
			//-------
            options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, "true"); 
            options.setCallTransportCleanup(true); 
            MultiThreadedHttpConnectionManager conmgr = new MultiThreadedHttpConnectionManager(); 
            conmgr.getParams().setDefaultMaxConnectionsPerHost(Integer.MAX_VALUE); 
            conmgr.getParams().setMaxTotalConnections(Integer.MAX_VALUE); 
            conmgr.getParams().setLinger(0); 
            
            HttpClientParams clientParams = new HttpClientParams(); 
            clientParams.setConnectionManagerTimeout(30); 
            HttpClient httpClient = new HttpClient(conmgr); 
            httpClient.setParams(clientParams); 
            ConfigurationContext context = sender.getServiceContext().getConfigurationContext(); 
            context.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true); 
            context.setProperty(HTTPConstants.CACHED_HTTP_CLIENT,httpClient); 
            context.setProperty(HTTPConstants.AUTO_RELEASE_CONNECTION, true); 
            context.setProperty(HTTPConstants.HTTP_PROTOCOL_VERSION, HTTPConstants.HEADER_PROTOCOL_11); 
			//-------
            
			OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);
			mepClient.addMessageContext(mc);
			log.debug("before calling the service");
			
			log.debug("SOAP request before submitting request: "+ mc.getEnvelope());
			mepClient.execute(true);
			MessageContext response = mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			log.debug("Got response");
			log.debug("; the response is:" + response.getEnvelope());
			JspHelper.setResult(prettyFormat(response.getEnvelope().toString(),2));

			sender.cleanup();
			sender = null;
			options = null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static String[] handleInlineOrAttachment(MessageContext mc,	Options options) throws OMException, SOAPProcessingException {
		String[] tmp = null;
		if (PropertyHandler.isSendAS4Pull()) return tmp;
		
		File attachment = new File(PropertyHandler.getRequestFileName());
		log.debug("file:" + attachment + "|" + (attachment.length() ) + " Bytes");
		if (!PropertyHandler.isAttachment()) {
			String quoteRequest = readFile(attachment);
			mc.getEnvelope().getBody().addChild(getPayload(quoteRequest));
		} else {
			if (PropertyHandler.isUseMTOM()) {
				options.setProperty(Constants.Configuration.ENABLE_MTOM,	Constants.VALUE_TRUE);
			} else {
				options.setProperty(Constants.Configuration.ENABLE_SWA,	Constants.VALUE_TRUE);
			}

			File fileXml = attachment;

			String mimeTypes = null;
			if (fileXml.exists()) {
				FileDataSource fileDataSource = new FileDataSource(fileXml);
				DataHandler dataHandler = new DataHandler(fileDataSource);
				mc.addAttachment(dataHandler);

				if (mimeTypes == null) {
					mimeTypes = new MimetypesFileTypeMap().getContentType(fileXml);
				}
				
				if (PropertyHandler.isUseMTOM()) { // XOP reference - MTOM will only work with this!
					OMText textData = fac.createOMText(dataHandler, true);
					mc.getEnvelope().getBody().addChild(textData);
				}
			} else {
				log.error("File does not exist");
			}
			log.debug("mimeTypes = " + mimeTypes);

			log.debug("After Adding the Atachement with content IDs:");
			tmp = mc.attachments.getAllContentIDs();
			for (String string : tmp) {
				log.debug(string+"<------------");
			}
		}
		return tmp;
	}
	
	public static ServiceClient getServiceClientWithAS4PullHeaders(MessageContext messageContext) throws AxisFault {
		log.debug("in Pull Request");
		ServiceClient sender = new ServiceClient();

		try {
			String ret = UUIDGenerator.getUUID();
			ret = ret + "@" + PropertyHandler.getFromURI();
			if (PropertyHandler.isSendAS4Headers() && PropertyHandler.isSendAS4Pull() ){
				ret = "urn:uuid:ForEmanMonitoringDoNotDelete@" + PropertyHandler.getFromURI();
			}
			log.debug("the UUIDGenerator number==" + ret);
			messageContext.setMessageID(ret);

			SOAPHeaderBlock messaging = soapFactory.createSOAPHeaderBlock("Messaging", omNs);
			messaging.addAttribute(soapFactory.createOMAttribute("schemaLocation", omNs,
									"http://www.w3.org/2001/XMLSchema-instance"
											+ " "
											+ "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/core/ebms-header-3_0-200704.xsd"));
			messaging.setMustUnderstand(false);
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMNamespace ebOmNs = fac.createOMNamespace("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", "eb");
			OMElement signalMessage = fac.createOMElement("SignalMessage", ebOmNs);
			OMElement messageInfo = fac.createOMElement("MessageInfo", ebOmNs);
			OMElement timeStamp = fac.createOMElement("Timestamp", ebOmNs);
			OMElement messageId = fac.createOMElement("MessageId", ebOmNs);
			timeStamp.addChild(fac.createOMText(messageInfo, getZuluTimeStamp()));
			messageId.addChild(fac.createOMText(messageId, ret));
			messageInfo.addChild(timeStamp);
			messageInfo.addChild(messageId);
			OMElement pullRequest = fac.createOMElement("PullRequest", ebOmNs);
			signalMessage.addChild(messageInfo);
			signalMessage.addChild(pullRequest);
			messaging.addChild(ElementHelper.toSOAPHeaderBlock(signalMessage, soapFactory));
			
			if (PropertyHandler.isSendAS4Headers() && PropertyHandler.isSendAS4Pull() ){
				messaging=ElementHelper.toSOAPHeaderBlock(org.apache.axiom.om.util.AXIOMUtil.stringToOM(getPullRequest()), soapFactory);
				messaging.setMustUnderstand(false);
			}
			
			if (PropertyHandler.isSendAS4Headers()) {
				log.debug("the messaging tag after adding SignalMessage ="	+ messaging);
					messageContext.getEnvelope().getHeader().addChild(messaging);
					log.debug("After setting setAS4 PULL Headers()");
				}
			log.debug("going out of setThinClientPullRequest ");
		}
		catch (Exception exception) 
		{
			exception.printStackTrace();
			System.out.println(exception);
		}
		return sender;
	}

	public static ServiceClient getServiceClientWithAS4PushHeaders(MessageContext messageContext, String[] ids) throws AxisFault {
		ServiceClient sender = new ServiceClient();
		log.debug("in setAS4Headers()");
		try {
			String ret = UUIDGenerator.getUUID();
			ret = ret + "@" + PropertyHandler.getFromURI();
			log.debug("the UUIDGenerator number==" + ret);
			messageContext.setMessageID(ret);
			SOAPHeaderBlock messaging = soapFactory.createSOAPHeaderBlock("Messaging", omNs);
			messaging.addAttribute(soapFactory
							.createOMAttribute(
									"schemaLocation",
									omNs,
									"http://www.w3.org/2001/XMLSchema-instance"
											+ " "
											+ "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/core/ebms-header-3_0-200704.xsd"));
			messaging.setMustUnderstand(false);
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMNamespace ebOmNs = fac
					.createOMNamespace(
							"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/",
							"eb");
			OMElement userMessage = fac.createOMElement("UserMessage", ebOmNs);
			OMElement messageInfo = fac.createOMElement("MessageInfo", ebOmNs);
			OMElement timeStamp = fac.createOMElement("Timestamp", ebOmNs);
			OMElement messageId = fac.createOMElement("MessageId", ebOmNs);
			timeStamp.addChild(fac.createOMText(messageInfo, getZuluTimeStamp()));
			messageId.addChild(fac.createOMText(messageId, ret));
			messageInfo.addChild(timeStamp);
			messageInfo.addChild(messageId);
			userMessage.addChild(messageInfo);
			OMElement partyInfo = fac.createOMElement("PartyInfo", ebOmNs);
			OMElement partyInfoFrom = fac.createOMElement("From", ebOmNs);
			OMElement partyInfoFromPartyId = fac.createOMElement("PartyId",	ebOmNs);
			OMElement partyInfoFromRole = fac.createOMElement("Role", ebOmNs);
			OMElement partyInfoTo = fac.createOMElement("To", ebOmNs);
			OMElement partyInfoToPartyId = fac.createOMElement("PartyId",	ebOmNs);
			OMElement partyInfoToRole = fac.createOMElement("Role", ebOmNs);
			partyInfoFromPartyId.addChild(fac.createOMText(	partyInfoFromPartyId, PropertyHandler.getFromURI()));
			partyInfoFromRole.addChild(fac.createOMText(partyInfoFromRole,	"http://example.org/roles/Buyer"));
			partyInfoToPartyId.addChild(fac.createOMText(partyInfoToPartyId, PropertyHandler.getToURI()));
			partyInfoToRole.addChild(fac.createOMText(partyInfoToRole,	"http://example.org/roles/Seller"));
			partyInfoFrom.addChild(partyInfoFromPartyId);
			partyInfoFrom.addChild(partyInfoFromRole);
			partyInfoTo.addChild(partyInfoToPartyId);
			partyInfoTo.addChild(partyInfoToRole);
			partyInfo.addChild(partyInfoFrom);
			partyInfo.addChild(partyInfoTo);
			userMessage.addChild(partyInfo);
			OMElement collaborationInfo = fac.createOMElement("CollaborationInfo", ebOmNs);
			OMElement collaborationInfoAgreementRef = fac.createOMElement("AgreementRef", ebOmNs);
			OMElement collaborationInfoService = fac.createOMElement("Service",	ebOmNs);
			OMElement collaborationInfoAction = fac.createOMElement("Action", ebOmNs);
			OMElement collaborationInfoConversationId = fac.createOMElement("ConversationId", ebOmNs);
			collaborationInfoAgreementRef.addChild(fac.createOMText(collaborationInfoAgreementRef,	"http://registry.example.com/cpa/123456"));
			collaborationInfoService.addChild(fac.createOMText(collaborationInfoService, "QuoteToCollect"));
			collaborationInfoAction.addChild(fac.createOMText(collaborationInfoAction, "DummyAction"));
			collaborationInfoConversationId.addChild(fac.createOMText(collaborationInfoConversationId, "ConversationAt"+getZuluTimeStamp()));
			collaborationInfoService.addAttribute("type", "MyServiceTypes",	null);
			userMessage.addChild(collaborationInfo);
			OMElement messageProperties = fac.createOMElement("MessageProperties", ebOmNs);
			OMElement messagePropertiesProperty1 = fac.createOMElement(	"Property", ebOmNs);
			OMElement messagePropertiesProperty2 = fac.createOMElement(	"Property", ebOmNs);
			messagePropertiesProperty1.addChild(fac.createOMText(messagePropertiesProperty1, "PurchaseOrder:123456"));
			messagePropertiesProperty2.addChild(fac.createOMText(messagePropertiesProperty2, "987654321"));
			messagePropertiesProperty1.addAttribute("name", "ProcessInst", null);
			messagePropertiesProperty2.addAttribute("name", "ContextID", null);
			userMessage.addChild(messageProperties);
			OMElement payloadInfo = fac.createOMElement("PayloadInfo", ebOmNs);
			OMElement payloadInfoPartInfo = fac.createOMElement("PartInfo",	ebOmNs);
			if (!PropertyHandler.isAttachment()) {
			payloadInfoPartInfo.addAttribute("href", "cid:no.attachment.sent", null);
			} else {
			payloadInfoPartInfo.addAttribute("href", ids[0], null);
			}

			OMElement payloadInfoPartInfoSchema = fac.createOMElement("Schema",	ebOmNs);
			payloadInfoPartInfoSchema.addAttribute("location",	"http://localhost:8080/axis2/axis2-web/book.xsd", null);
			payloadInfoPartInfoSchema.addAttribute("version", "2.0", null);
			OMElement payloadInfoPartInfoPartProperties = fac.createOMElement("PartProperties", ebOmNs);
			OMElement payloadInfoPartInfoPartPropertiesProperty1 = fac.createOMElement("Property", ebOmNs);
			payloadInfoPartInfoPartPropertiesProperty1.addAttribute("name",	"Description", null);
			payloadInfoPartInfoPartPropertiesProperty1.addChild(fac.createOMText(payloadInfoPartInfoPartPropertiesProperty1,"Purchase Order for 11 Widgets"));
			OMElement payloadInfoPartInfoPartPropertiesProperty2 = fac.createOMElement("Property", ebOmNs);
			payloadInfoPartInfoPartPropertiesProperty2.addAttribute("name",	"MimeType", null);
			payloadInfoPartInfoPartPropertiesProperty2.addChild(fac.createOMText(payloadInfoPartInfoPartPropertiesProperty2,"application/xml"));
			payloadInfoPartInfoPartProperties.addChild(payloadInfoPartInfoPartPropertiesProperty1);
			payloadInfoPartInfoPartProperties.addChild(payloadInfoPartInfoPartPropertiesProperty2);
			payloadInfoPartInfo.addChild(payloadInfoPartInfoSchema);
			payloadInfoPartInfo.addChild(payloadInfoPartInfoPartProperties);
			payloadInfo.addChild(payloadInfoPartInfo);
			userMessage.addChild(payloadInfo);
			messaging.addChild(ElementHelper.toSOAPHeaderBlock(userMessage,	soapFactory));
			if (PropertyHandler.isSendAS4Headers()) {
			log.debug("the messaging tag after adding UserMessage ="	+ messaging);
				messageContext.getEnvelope().getHeader().addChild(messaging);
				log.debug("After setting setAS4PUSHHeaders()");
			}
		} catch (Exception exception) {
			log.error(exception.getMessage(),exception);
		}
		return sender;
	}

	public static String getZuluTimeStamp() {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormatter.format(new java.util.Date());
	}

	private static OMElement getPayload(String quoteDoc) {
		OMElement document = null;
		try {
			document = AXIOMUtil.stringToOM(quoteDoc);
			document.setNamespace(omNs);
		} catch (XMLStreamException e) {
			log.error(e.getMessage(),e);
		}
		return document;
	}

	public static String readFile(File file) {
		String quoteDoc = null;
		InputStream in = null;
		try {
			long length = file.length();
			int clobLenth = (int) length;
			log.debug("length: " + length + " cloblength: "	+ clobLenth);
			byte[] array = new byte[clobLenth];
			in = new FileInputStream(file);
			int offset = 0;
			int numRead;
			do {
				numRead = in.read(array, offset, clobLenth - offset);
			} while (numRead != -1);
			quoteDoc = new String(array);
		} catch (Exception e) {
			log.debug("error while reading quote xml document:" + file.getName());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				log.error(e.getMessage(),e);
			}
		}
		return quoteDoc;
	}
	
	private static String prettyFormat(String input, int indent) {
		String ret = null;
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);
	        ret = xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	        log.debug(e.getMessage(), e); // simple exception handling, please review it
	    } finally {
	    	if (ret == null)
	    		ret = input;
	    }
        log.debug(ret);
	    return ret;
	}

	private static String getPullRequest() {
		StringBuffer sb = new StringBuffer();
		sb.append("<eb:Messaging xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xsi:schemaLocation=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/ " +
				"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/core/ebms-header-3_0-200704.xsd\" " +
				"xmlns:eb=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/\">");
		sb.append("<eb:SignalMessage>");
		sb.append("<eb:MessageInfo><eb:Timestamp>2013-02-08T20:40:08Z</eb:Timestamp>" +
				"<eb:MessageId>urn:uuid:ForEmanMonitoringDoNotDelete@yrajaram.company.com</eb:MessageId>" +
				"</eb:MessageInfo><eb:PullRequest /></eb:SignalMessage></eb:Messaging>");
		return sb.toString();
	}


}
