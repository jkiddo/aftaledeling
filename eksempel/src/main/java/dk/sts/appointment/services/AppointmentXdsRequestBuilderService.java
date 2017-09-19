package dk.sts.appointment.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.activation.DataHandler;

import org.openehealth.ipf.commons.ihe.xds.core.ebxml.EbXMLAdhocQueryRequest;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.EbXMLFactory;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.EbXMLFactory30;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.EbXMLProvideAndRegisterDocumentSetRequest30;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.ProvideAndRegisterDocumentSetRequestType;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.RetrieveDocumentSetRequestType;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AssigningAuthority;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Association;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AssociationLabel;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AssociationType;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Author;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Document;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntryType;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Identifiable;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.LocalizedString;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Name;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Organization;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.PatientInfo;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.SubmissionSet;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.XpnName;
import org.openehealth.ipf.commons.ihe.xds.core.requests.ProvideAndRegisterDocumentSet;
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry;
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.FindDocumentsQuery;
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.QueryReturnType;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.lcm.SubmitObjectsRequest;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryRequest;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.AssociationType1;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.ClassificationType;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.ExternalIdentifierType;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.InternationalStringType;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.LocalizedStringType;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.ObjectFactory;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.RegistryObjectListType;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.RegistryPackageType;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.SlotType1;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.ValueListType;
import org.openehealth.ipf.commons.ihe.xds.core.transform.requests.ProvideAndRegisterDocumentSetTransformer;
import org.openehealth.ipf.commons.ihe.xds.core.transform.requests.QueryRegistryTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sun.istack.ByteArrayDataSource;

import dk.sts.appointment.dto.DocumentMetadata;
import dk.sts.appointment.utilities.PatientIdAuthority;
import dk.sts.appointment.utilities.UUID;

public class AppointmentXdsRequestBuilderService {

	@Value("${xds.repositoryuniqueid}")
	String repositoryUniqueId;
	
	@Autowired
	PatientIdAuthority patientIdAuthority;
	
	public ProvideAndRegisterDocumentSetRequestType buildProvideAndRegisterDocumentSetRequest(String extenalDocumentId, String documentPayload, DocumentMetadata documentMetadata) {

		DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		ProvideAndRegisterDocumentSet provideAndRegisterDocumentSet = new ProvideAndRegisterDocumentSet();
		
		Identifiable patientIdentifiable = null;
		if (documentMetadata.getPatientId() != null) {
			AssigningAuthority patientIdAssigningAuthority = new AssigningAuthority(documentMetadata.getPatientId().getSchemeName());
			patientIdentifiable = new Identifiable(documentMetadata.getPatientId().getCode(), patientIdAssigningAuthority);
		}

		AssigningAuthority organisationAssigningAuthority = new AssigningAuthority(documentMetadata.getOrganisation().getSchemeName());
		Author author = new Author();
		if (documentMetadata.getOrganisation() != null && documentMetadata.getOrganisation().getCode() != null) {
			Organization authorOrganisation = new Organization(documentMetadata.getOrganisation().getDisplayName().getValue(), documentMetadata.getOrganisation().getCode(), organisationAssigningAuthority);
			author.getAuthorInstitution().add(authorOrganisation);
		}

		SubmissionSet submissionSet = createSubmissionSet(author, patientIdentifiable);
		
		//submissionSet.setContentTypeCode(createCode(documentMetadata.getContentTypeCode()));
		
		if (documentMetadata.getReportTime() != null) {
			String timestamp = dateTimeFormat.format(documentMetadata.getReportTime());
			submissionSet.setSubmissionTime(timestamp);
		}
		provideAndRegisterDocumentSet.setSubmissionSet(submissionSet);
		
		String documentUuid = generateUUID();
		
		DocumentEntry documentEntry = new DocumentEntry();
		
		// 4.1 Patient Identification
		documentEntry.setPatientId(patientIdentifiable);
		documentEntry.setSourcePatientId(patientIdentifiable);

		// 4.2 Name, Address and Telecommunications
		PatientInfo sourcePatientInfo = new PatientInfo();
		documentEntry.setSourcePatientInfo(sourcePatientInfo);

		// 4.2.1 Name
		Name<?> name = new XpnName();
		
		sourcePatientInfo.setName(name);
		documentEntry.setEntryUuid(generateUUID());
		documentEntry.setAuthor(author);
		documentEntry.setAvailabilityStatus(AvailabilityStatus.APPROVED);
		if (documentMetadata.getClassCode() != null) {
			documentEntry.setClassCode(documentMetadata.getClassCode());
		}
		if (documentMetadata.getConfidentialityCode() != null) {
			documentEntry.getConfidentialityCodes().add(documentMetadata.getConfidentialityCode());
		}
		documentEntry.setCreationTime(dateTimeFormat.format(documentMetadata.getReportTime()));
		
		documentEntry.setServiceStartTime(dateTimeFormat.format(documentMetadata.getServiceStartTime()));
		
		List<Code> eventCodesEntry = documentEntry.getEventCodeList();
		if (documentMetadata.getEventCodes() != null) {
			for (Code eventCode : documentMetadata.getEventCodes()) {
				eventCodesEntry.add(eventCode);
			}
		}
		if (documentMetadata.getFormatCode() != null) {
			documentEntry.setFormatCode(documentMetadata.getFormatCode());
		}
		if (documentMetadata.getHealthcareFacilityTypeCode() != null) {
			documentEntry.setHealthcareFacilityTypeCode(documentMetadata.getHealthcareFacilityTypeCode());
		}
		if (documentMetadata.getLanguageCode() != null) {
			documentEntry.setLanguageCode(documentMetadata.getLanguageCode());
		}
		if (documentMetadata.getMimeType() != null) {
			documentEntry.setMimeType(documentMetadata.getMimeType());
		}
		documentEntry.setType(DocumentEntryType.STABLE);
		if (documentMetadata.getTitle() != null) {
			documentEntry.setTitle(new LocalizedString(documentMetadata.getTitle()));
		}
		if (documentMetadata.getTypeCode() != null) {
			documentEntry.setTypeCode(documentMetadata.getTypeCode());
		}
		if (documentMetadata.getPracticeSettingCode() != null) {
			documentEntry.setPracticeSettingCode(documentMetadata.getPracticeSettingCode());
		}
		
		documentEntry.setUniqueId(extenalDocumentId);
		documentEntry.setLogicalUuid(documentUuid);

		Document document = new Document(documentEntry, new DataHandler(new ByteArrayDataSource(documentPayload.getBytes(), documentMetadata.getMimeType())));
		provideAndRegisterDocumentSet.getDocuments().add(document);

		Association association = new Association();
		association.setAssociationType(AssociationType.HAS_MEMBER);
		association.setEntryUuid(generateUUID());
		association.setSourceUuid(submissionSet.getEntryUuid());
		association.setTargetUuid(documentEntry.getEntryUuid());
		association.setAvailabilityStatus(AvailabilityStatus.APPROVED);
		association.setOriginalStatus(AvailabilityStatus.APPROVED);
		association.setNewStatus(AvailabilityStatus.APPROVED);
		association.setLabel(AssociationLabel.ORIGINAL);
		provideAndRegisterDocumentSet.getAssociations().add(association);

		ProvideAndRegisterDocumentSetTransformer registerDocumentSetTransformer = new ProvideAndRegisterDocumentSetTransformer(getEbXmlFactory());
		EbXMLProvideAndRegisterDocumentSetRequest30 ebxmlRequest = (EbXMLProvideAndRegisterDocumentSetRequest30) registerDocumentSetTransformer.toEbXML(provideAndRegisterDocumentSet);
		ProvideAndRegisterDocumentSetRequestType provideAndRegisterDocumentSetRequestType = ebxmlRequest.getInternal();

		return provideAndRegisterDocumentSetRequestType;
	}


	public RetrieveDocumentSetRequestType buildRetrieveDocumentSetRequestType(List<String> documentIds){
		RetrieveDocumentSetRequestType retrieveDocumentSetRequestType = new RetrieveDocumentSetRequestType();

		for (Iterator<String> iterator = documentIds.iterator(); iterator.hasNext();) {
			RetrieveDocumentSetRequestType.DocumentRequest documentRequest = new RetrieveDocumentSetRequestType.DocumentRequest();
			documentRequest.setRepositoryUniqueId(repositoryUniqueId);
			documentRequest.setDocumentUniqueId(iterator.next());
			retrieveDocumentSetRequestType.getDocumentRequest().add(documentRequest);
		}
		return retrieveDocumentSetRequestType;
	}

	public AdhocQueryRequest buildAdhocQueryRequest(String citizenId, List<Code> typeCodes, Date start, Date end) {
		return buildAdhocQueryRequest(citizenId, typeCodes, AvailabilityStatus.APPROVED, start, end);
	}
	
	public AdhocQueryRequest buildAdhocQueryRequest(String citizenId, List<Code> typeCodes, AvailabilityStatus availabilityStatus, Date start, Date end) {
		FindDocumentsQuery fdq = new FindDocumentsQuery();
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		AssigningAuthority authority = new AssigningAuthority(patientIdAuthority.getPatientIdAuthority());

		// Patient ID
		Identifiable patientIdentifiable = new Identifiable(citizenId, authority);
		fdq.setPatientId(patientIdentifiable);

		// Availability Status
		List<AvailabilityStatus> availabilityStati = new LinkedList<AvailabilityStatus>();
		availabilityStati.add(availabilityStatus);
		fdq.setStatus(availabilityStati);

		if (typeCodes != null) {
			fdq.setTypeCodes(typeCodes);
		}
		
		if (start != null) {
			fdq.getServiceStartTime().setFrom(dateTimeFormat.format(start));
		}
		
		if (end != null) {
			fdq.getServiceStartTime().setTo(dateTimeFormat.format(end));
		}
		

		QueryRegistry queryRegistry = new QueryRegistry(fdq);
		QueryReturnType qrt = QueryReturnType.LEAF_CLASS;
		
		if (qrt != null) {
			queryRegistry.setReturnType(qrt);
		}
		
		QueryRegistryTransformer queryRegistryTransformer = new QueryRegistryTransformer();
		EbXMLAdhocQueryRequest ebxmlAdhocQueryRequest = queryRegistryTransformer.toEbXML(queryRegistry);
		AdhocQueryRequest internal = (AdhocQueryRequest)ebxmlAdhocQueryRequest.getInternal();

		return internal;
	}

	protected SubmissionSet createSubmissionSet(Author author, Identifiable patientIdentifiable) {
		String submissionSetUuid = generateUUID();
		SubmissionSet submissionSet = new SubmissionSet();
		submissionSet.setUniqueId(submissionSetUuid);
		submissionSet.setSourceId(submissionSetUuid);
		submissionSet.setLogicalUuid(submissionSetUuid);
		submissionSet.setEntryUuid(submissionSetUuid);
		submissionSet.setPatientId(patientIdentifiable);
		submissionSet.setTitle(new LocalizedString(submissionSetUuid));
		submissionSet.setAuthor(author);
		// What to do here???
		submissionSet.setContentTypeCode(new Code("NscContentType", new LocalizedString("NscContentType"), UUID.XDSSubmissionSet_contentTypeCode));
		submissionSet.setAvailabilityStatus(AvailabilityStatus.APPROVED);
		return submissionSet;
	}

	private static final EbXMLFactory ebXMLFactory = new EbXMLFactory30();

	protected EbXMLFactory getEbXmlFactory() {
		return ebXMLFactory;
	}

	private String generateUUID() {
		java.util.UUID uuid = java.util.UUID.randomUUID();
		return Math.abs(uuid.getLeastSignificantBits()) + "." + Math.abs(uuid.getMostSignificantBits())+"."+Calendar.getInstance().getTimeInMillis();
	}


	public SubmitObjectsRequest buildDeprecateSubmitObjectsRequest(String cpr, String documentEntryUUID, String repositoryId, String originalStatus) {
		
		ObjectFactory factory = new ObjectFactory();
		
		SubmitObjectsRequest body = new SubmitObjectsRequest();
		
		RegistryObjectListType registryObjectList = factory.createRegistryObjectListType();
		registryObjectList.getIdentifiable().add(factory.createRegistryPackage(makeRegistryPackageType(cpr,repositoryId,documentEntryUUID,"NscContentType","CodingScheme")));
		registryObjectList.getIdentifiable().add(factory.createAssociation(makeAssociation(documentEntryUUID, originalStatus, AvailabilityStatus.DEPRECATED.getQueryOpcode())));
		
		body.setRegistryObjectList(registryObjectList);
		
		return body;
	}
	
	private AssociationType1 makeAssociation(String documentEntryUUID, String originalStatus, String newStatus) {
		AssociationType1 assocation = new AssociationType1();
		
		assocation.setAssociationType(AssociationType.UPDATE_AVAILABILITY_STATUS.getOpcode30());
		assocation.setSourceObject("SubmissionSet");
		assocation.setTargetObject(documentEntryUUID);
		assocation.getSlot().add(makeSlot("OriginalStatus", originalStatus));
		assocation.getSlot().add(makeSlot("NewStatus", newStatus));
		
		return assocation;
		
	}
	
	private RegistryPackageType makeRegistryPackageType(String cpr, String sourceId, String uniqueId, String contentType, String contentTypeCodingScheme) {
		RegistryPackageType registryPackage = new RegistryPackageType();
		
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		
		ClassificationType classificationNode = new ClassificationType();
		classificationNode.setClassifiedObject("SubmissionSet");
		classificationNode.setClassificationNode(UUID.XDSSubmissionSet_classificationNode);
		
		ClassificationType contentTypeClassification = new ClassificationType();
		contentTypeClassification.setClassifiedObject("SubmissionSet");
		contentTypeClassification.setClassificationScheme(UUID.XDSSubmissionSet_contentTypeCode);		
		contentTypeClassification.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification");
		contentTypeClassification.setNodeRepresentation(contentType);
		contentTypeClassification.setName(makeInternationalStringType(contentType));		
		contentTypeClassification.getSlot().add(makeSlot("codingScheme", contentTypeCodingScheme));
		
		registryPackage.setId("SubmissionSet");
		registryPackage.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage");
		
		registryPackage.getExternalIdentifier().add(makeExternalIdentifier(UUID.XDSSubmissionSet_uniqueId, uniqueId));
		registryPackage.getExternalIdentifier().add(makeExternalIdentifier(UUID.XDSSubmissionSet_patientId, patientIdAuthority.formatPatientIdentifier(cpr)));
		registryPackage.getExternalIdentifier().add(makeExternalIdentifier(UUID.XDSSubmissionSet_sourceId, sourceId));
		
		registryPackage.getClassification().add(classificationNode);
		registryPackage.getClassification().add(contentTypeClassification);
		
		registryPackage.getSlot().add(makeSlot("submissionTime", dateTimeFormat.format(new Date())));
		
		return registryPackage;
	}
	
	private SlotType1 makeSlot(String name, String value) {
		SlotType1 slot = new SlotType1();
		ValueListType slotList = new ValueListType();		
		slot.setName(name);
		slotList.getValue().add(value);
		slot.setValueList(slotList);		
		return slot;	
	}
	
	private InternationalStringType makeInternationalStringType(String value) {
		InternationalStringType ist = new InternationalStringType();
		LocalizedStringType lst = new LocalizedStringType();		
		lst.setValue(value);
		ist.getLocalizedString().add(lst);
		return ist;
	}
	
	private ExternalIdentifierType makeExternalIdentifier(String identificationScheme, String value) {
		ExternalIdentifierType externalIdentifier = new ExternalIdentifierType();
		externalIdentifier.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier");
		externalIdentifier.setIdentificationScheme(identificationScheme);
		externalIdentifier.setValue(value);
		externalIdentifier.setRegistryObject("SubmissionSet");
		return externalIdentifier;
	}

	
}
