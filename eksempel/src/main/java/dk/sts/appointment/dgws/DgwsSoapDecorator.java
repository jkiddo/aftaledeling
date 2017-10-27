package dk.sts.appointment.dgws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.sosi.seal.SOSIFactory;
import dk.sosi.seal.model.AuthenticationLevel;
import dk.sosi.seal.model.CareProvider;
import dk.sosi.seal.model.IDCard;
import dk.sosi.seal.model.Request;
import dk.sosi.seal.model.SecurityTokenRequest;
import dk.sosi.seal.model.SecurityTokenResponse;
import dk.sosi.seal.model.SystemIDCard;
import dk.sosi.seal.model.constants.SubjectIdentifierTypeValues;
import dk.sosi.seal.vault.CredentialVault;
import dk.sosi.seal.xml.XmlUtil;

public class DgwsSoapDecorator extends AbstractSoapInterceptor {

	@Autowired
	private SOSIFactory sosiFactory;

	@Autowired
	private STSRequestHelper requestHelper;

	@Autowired
	private CredentialVault vault;

	@Value("${medcom.cvr}")
	private String cvr;

	@Value("${medcom.orgname}")
	private String orgname;

	@Value("${medcom.itsystem}")
	private String itsystem;

	public DgwsSoapDecorator() {
		super(Phase.PRE_STREAM);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		try {
			Document sosi = getSosiDocument();
			Node sosiElement = sosi.getDocumentElement().getFirstChild();
			Map<String, String> ns = new HashMap<>();
			collectNameSpaces(sosiElement, ns);
			String soapenvNs = ns.get("soapenv");
			ns.put("soapenv", "http://www.w3.org/2003/05/soap-envelope");
			message.put("soap.env.ns.map", ns); 
			QName qname = new QName(sosiElement.getNamespaceURI(), sosiElement.getLocalName());
			Header sosiHeader = new Header(qname, sosiElement.getFirstChild());
			message.getHeaders().add(sosiHeader);
			// TODO Auto-generated method stub
		} catch (IOException e) {
			throw new Fault(e);
		}
	}

	
	public static void collectNameSpaces(Node root, Map<String, String> nsMap){
		String prefix = root.getPrefix();
		String ns = root.getNamespaceURI();
		if (prefix != null) {
			nsMap.put(prefix, ns);
		}
		NodeList nl = root.getChildNodes();
		if (nl != null) {
			for (int i = 0; i < nl.getLength(); i++) {
				collectNameSpaces(nl.item(i), nsMap);
			}
		}
	}
	
	private Document getSosiDocument() throws IOException {
		Request request = sosiFactory.createNewRequest(false, null);
		request.setIDCard(getToken());
		return request.serialize2DOMDocument();
	}
	
	private IDCard getToken() throws IOException {
		CareProvider careProvider = new CareProvider(SubjectIdentifierTypeValues.CVR_NUMBER, cvr, orgname);
		SystemIDCard selfSignedSystemIdCard = sosiFactory.createNewSystemIDCard(itsystem, careProvider, AuthenticationLevel.VOCES_TRUSTED_SYSTEM, null, null, vault.getSystemCredentialPair().getCertificate(), null);

		SecurityTokenRequest securityTokenRequest = sosiFactory.createNewSecurityTokenRequest();
		securityTokenRequest.setIDCard(selfSignedSystemIdCard);
		Document doc = securityTokenRequest.serialize2DOMDocument();

		String requestXml = XmlUtil.node2String(doc, false, true);
		String responseXml = requestHelper.sendRequest(requestXml);
		SecurityTokenResponse securityTokenResponse = sosiFactory.deserializeSecurityTokenResponse(responseXml);

		if (securityTokenResponse.isFault() || securityTokenResponse.getIDCard() == null) {
			throw new RuntimeException("No ID card :-(");
		}
		else {
			SystemIDCard stsSignedIdCard = (SystemIDCard) securityTokenResponse.getIDCard();
			return stsSignedIdCard;
		}
	}

}
