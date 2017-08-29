package dk.sts.appointment.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.openehealth.ipf.commons.ihe.xds.core.ebxml.EbXMLExtrinsicObject;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.EbXMLFactory;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.EbXMLObjectLibrary;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.EbXMLSubmitObjectsRequest;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.EbXMLFactory30;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.EbXMLQueryResponse30;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.ProvideAndRegisterDocumentSetRequestType;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.RetrieveDocumentSetResponseType;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.RetrieveDocumentSetResponseType.DocumentResponse;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.lcm.SubmitObjectsRequest;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryRequest;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryResponse;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryError;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryResponseType;
import org.openehealth.ipf.commons.ihe.xds.core.transform.requests.RegisterDocumentSetTransformer;
import org.openehealth.ipf.commons.ihe.xds.core.transform.responses.QueryResponseTransformer;
import org.openehealth.ipf.commons.ihe.xds.iti18.Iti18PortType;
import org.openehealth.ipf.commons.ihe.xds.iti41.Iti41PortType;
import org.openehealth.ipf.commons.ihe.xds.iti43.Iti43PortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import dk.sts.appointment.dto.DocumentMetadata;

public class AppointmentXdsRequestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentXdsRequestService.class);

	private static final EbXMLFactory ebXMLFactory = new EbXMLFactory30();

	@Autowired
	AppointmentXdsRequestBuilderService appointmentXdsRequestBuilderService;

	//@Autowired
	//Iti57PortType iti57PortType;

	@Autowired
	Iti43PortType iti43PortType;

	@Autowired
	Iti18PortType iti18PortType;

	@Autowired
	Iti41PortType iti41PortType;

	public List<DocumentEntry> getAppointmentsForPatient(String citizenId) throws XdsException {
		AdhocQueryRequest adhocQueryRequest = appointmentXdsRequestBuilderService.buildAdhocQueryRequest(citizenId, null);
		LOGGER.info("before xds call: documentRegistryRegistryStoredQuery");
		AdhocQueryResponse adhocQueryResponse = iti18PortType.documentRegistryRegistryStoredQuery(adhocQueryRequest);
		LOGGER.info("after xds call: documentRegistryRegistryStoredQuery");
		if (adhocQueryResponse.getRegistryErrorList() != null && !adhocQueryResponse.getRegistryErrorList().getRegistryError().isEmpty()) {
			throw new XdsException(adhocQueryResponse.getRegistryErrorList());
		} else {
			QueryResponseTransformer queryResponseTransformer = new QueryResponseTransformer(getEbXmlFactory());
			EbXMLQueryResponse30 ebXmlresponse = new EbXMLQueryResponse30(adhocQueryResponse);
			QueryResponse queryResponse = queryResponseTransformer.fromEbXML(ebXmlresponse);
			List<DocumentEntry> docEntries = queryResponse.getDocumentEntries();
			return docEntries;
		}
	}

	public String fetchDocument(String documentId) throws IOException, XdsException {
		List<String> documentIds = new LinkedList<String>();
		documentIds.add(documentId);
		RetrieveDocumentSetResponseType repositoryResponse= iti43PortType.documentRepositoryRetrieveDocumentSet(appointmentXdsRequestBuilderService.buildRetrieveDocumentSetRequestType(documentIds));
		if (repositoryResponse.getRegistryResponse().getRegistryErrorList() == null || repositoryResponse.getRegistryResponse().getRegistryErrorList().getRegistryError() == null || repositoryResponse.getRegistryResponse().getRegistryErrorList().getRegistryError().isEmpty()) {
			// if no documents an error is produced, get(0) should work.
			DocumentResponse documentResponse = repositoryResponse.getDocumentResponse().get(0);
			String documentString = new BufferedReader(new InputStreamReader(documentResponse.getDocument().getInputStream())).lines().collect(Collectors.joining());
			return documentString;
			
		} else {
			XdsException e = new XdsException();
			for (RegistryError registryError :repositoryResponse.getRegistryResponse().getRegistryErrorList().getRegistryError()) {
				e.addError(registryError.getCodeContext());
			}
			throw e;
		}
	}	

		
	public String createAndRegisterDocument(String externalId, String document, DocumentMetadata documentMetadata) throws XdsException {
		ProvideAndRegisterDocumentSetRequestType provideAndRegisterDocumentSetRequest = appointmentXdsRequestBuilderService.buildProvideAndRegisterDocumentSetRequest(externalId, document, documentMetadata);
		RegistryResponseType registryResponse = iti41PortType.documentRepositoryProvideAndRegisterDocumentSetB(provideAndRegisterDocumentSetRequest);
		if (registryResponse.getRegistryErrorList() == null || registryResponse.getRegistryErrorList().getRegistryError() == null || registryResponse.getRegistryErrorList().getRegistryError().isEmpty()) {
			return externalId;
		} else {
			XdsException e = new XdsException();
			for (RegistryError registryError :registryResponse.getRegistryErrorList().getRegistryError()) {
				e.addError(registryError.getCodeContext());
			}
			throw e;
		}
	}


	protected EbXMLFactory getEbXmlFactory() {
		return ebXMLFactory;
	}


	public void invalidateDocument(String documentId) {
//		SubmitObjectsRequest submitObjectsRequest = new SubmitObjectsRequest();
		
		EbXMLFactory30 fact = new EbXMLFactory30();
		EbXMLSubmitObjectsRequest esor = fact.createSubmitObjectsRequest();
		EbXMLObjectLibrary objectLibrary = new EbXMLObjectLibrary();
		EbXMLExtrinsicObject extrinsic = fact.createExtrinsic(documentId, objectLibrary);
		extrinsic.setStatus(AvailabilityStatus.DEPRECATED);
		esor.addExtrinsicObject(extrinsic);
		
		RegisterDocumentSetTransformer transforemer = new RegisterDocumentSetTransformer(getEbXmlFactory());
		
		
		SubmitObjectsRequest body = new SubmitObjectsRequest();
	//	body.setRegistryObjectList(esor.get);
	//	iti57PortType.documentRegistryUpdateDocumentSet(body);
	}

}
