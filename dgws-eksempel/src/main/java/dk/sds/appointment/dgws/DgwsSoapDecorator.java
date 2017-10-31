package dk.sds.appointment.dgws;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.Soap11;
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
			// DGWS is SOAP11
			message.setVersion(Soap11.getInstance());

			// Add the DGWS headers
			Document sosi = getSosiDocument();
			NodeList children = sosi.getDocumentElement().getFirstChild().getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node element = children.item(i);
				QName qname = new QName(element.getNamespaceURI(), element.getLocalName());
				Header dgwsHeader = new Header(qname, element);
				message.getHeaders().add(dgwsHeader);
			}
			
		} catch (IOException e) {
			throw new Fault(e);
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
