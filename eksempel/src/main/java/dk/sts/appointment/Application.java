package dk.sts.appointment;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.LocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import dk.sts.appointment.configuration.ApplicationConfiguration;
import dk.sts.appointment.dto.DocumentMetadata;
import dk.sts.appointment.services.AppointmentXdsRequestService;
import dk.sts.appointment.utilities.Codes;


@Import(ApplicationConfiguration.class)
@EnableAutoConfiguration
public class Application implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	private static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyyMMddHHmmssZ");
	
	@Autowired
	AppointmentXdsRequestService appointmentXdsRequestService;
	
	private static String PATIENT_ID = "2512489996";
	

	public static void main(String[] args) throws Exception {
		LOGGER.debug("Starting application");
		SpringApplicationBuilder sab = new SpringApplicationBuilder(Application.class);
		sab.web(false);
		sab.run(args);
	}	

	public void run(String... args) throws Exception {

		// Search documents for patient
		List<DocumentEntry> currentAppointments = appointmentXdsRequestService.getAllAppointmentsForPatient(PATIENT_ID);
		System.out.println("The patient with id="+PATIENT_ID+" has "+currentAppointments.size()+" registered in the XDS registry.");

		// Register appointment (data extracted from the example file)
		String appointmentXmlDocument = getXmlFileContent("/DK-APD_Example_1.xml");
		String startAppointment = "20170531110000+0100";
		String endAppointment = "20170531120000+0100";
		DocumentMetadata appointmentCdaMetadata = createDocumentMetadata(startAppointment, endAppointment);
				
		String externalIdForNewDocument = generateUUID();
		String documentId = appointmentXdsRequestService.createAndRegisterDocument(externalIdForNewDocument, appointmentXmlDocument, appointmentCdaMetadata);
		System.out.println("We registered a new appointment with documentId="+documentId);

		// Search document for patient (we assume there is one more now)
		List<DocumentEntry> currentAppointmentsAfterNewAppointment = appointmentXdsRequestService.getAllAppointmentsForPatient(PATIENT_ID);
		boolean documentIsInList1 = isDocumentIdInList(documentId, currentAppointmentsAfterNewAppointment);
		System.out.println("The patient with id="+PATIENT_ID+" now has "+currentAppointmentsAfterNewAppointment.size()+" registered in the XDS registry after create. DocumentId:"+documentId+" "+(documentIsInList1 ? "could": "COULDN'T BUT SHOULD")+" be found.");

		Date restrictSearchEnd = DATEFORMAT.parse("20170531123000+0100");
		List<DocumentEntry> searchWithDateRestriction = appointmentXdsRequestService.getAllAppointmentsForPatient(PATIENT_ID, null, restrictSearchEnd);
		boolean isInListWhenRestrictingOnDate = isDocumentIdInList(documentId, searchWithDateRestriction);
		System.out.println("When searching with date restriction the documentId:"+documentId+" "+(isInListWhenRestrictingOnDate ? "could not": "CAN BUT SHOULDN'T")+" be found.");
		
		// Get appointment document from id
		String document = appointmentXdsRequestService.fetchDocument(externalIdForNewDocument);
		
		// Update the document with a new one
		DocumentEntry toBeUpdated = appointmentXdsRequestService.getAppointmentDocumentEntry(documentId);		
		String updatedAppointmentXmlDocument = getXmlFileContent("/DK-APD_Example_1_newTimeAndPlace.xml");
		
		String startUpdated = "20170531120000+0100";
		String endUpdated = "20170531130000+0100";
		DocumentMetadata updatedAppointmentCdaMetadata = createDocumentMetadata(startUpdated, endUpdated);
		String externalIdForUpdatedDocument = generateUUID();
		String newDocumentId = appointmentXdsRequestService.createAndRegisterDocumentAsReplacement(externalIdForUpdatedDocument, updatedAppointmentXmlDocument, updatedAppointmentCdaMetadata, toBeUpdated.getEntryUuid());
		
		List<DocumentEntry> currentAppointmentsAfterUpdatedAppointment = appointmentXdsRequestService.getAllAppointmentsForPatient(PATIENT_ID);
		boolean couldFindOld = isDocumentIdInList(documentId, currentAppointmentsAfterUpdatedAppointment);
		boolean couldFindNew = isDocumentIdInList(newDocumentId, currentAppointmentsAfterUpdatedAppointment);
		System.out.println("The patient with id="+PATIENT_ID+" now has "+currentAppointmentsAfterUpdatedAppointment.size()+" registered in the XDS registry after update. The old DocumentId:"+documentId+" "+(couldFindOld ? "COULD BUT SHOULDN'T" : "fortunately could not")+" be found in search. The new DocumentId:"+newDocumentId+" "+(couldFindNew ? "could" : "COULDN'T BUT SHOULD")+" be found.");
		
		
		// Now we want to deprecate a document...but first we have to get the document entry from the registry
		DocumentEntry toBeDeprecated = appointmentXdsRequestService.getAppointmentDocumentEntry(newDocumentId);		
		// ... then deprecate
		appointmentXdsRequestService.deprecateDocument(toBeDeprecated);

		//Fremsøg aftaler for patienten
		List<DocumentEntry> currentAppointmentsAfterDeprecation = appointmentXdsRequestService.getAllAppointmentsForPatient(PATIENT_ID);
		System.out.println("The patient with id="+PATIENT_ID+" now has "+currentAppointmentsAfterDeprecation.size()+" registered in the XDS registry after deprecate.");

	}
	
	public DocumentMetadata createDocumentMetadata(String startTime, String endTime) throws ParseException {
		DocumentMetadata appointmentCdaMetadata = new DocumentMetadata();
		appointmentCdaMetadata.setTitle("Aftale for "+PATIENT_ID);
		appointmentCdaMetadata.setPatientId(new Code(PATIENT_ID, null, Codes.DK_CPR_OID));
		appointmentCdaMetadata.setReportTime(DATEFORMAT.parse("20170113100000+0100"));
		appointmentCdaMetadata.setOrganisation(new Code("242621000016001", new LocalizedString("OUH Radiologisk Afdeling (Svendborg)"), Codes.DK_SOR_CLASSIFICAION_OID));
		appointmentCdaMetadata.setClassCode(new Code("001", new LocalizedString("Klinisk rapport"), "1.2.208.184.100.9"));
		appointmentCdaMetadata.setFormatCode(new Code("urn:ad:dk:medcom:appointment", new LocalizedString("DK CDA APD") ,"1.2.208.184.14.1"));
		appointmentCdaMetadata.setHealthcareFacilityTypeCode(new Code("22232009", new LocalizedString("hospital") ,"2.16.840.1.113883.6.96"));
		appointmentCdaMetadata.setPracticeSettingCode(new Code("408443003", new LocalizedString("almen medicin"),"2.16.840.1.113883.6.96"));
		appointmentCdaMetadata.setSubmissionTime(new Date());
		appointmentCdaMetadata.setContentTypeCode(AppointmentConstants.APPOINTMENT_CODE);
		appointmentCdaMetadata.setTypeCode(AppointmentConstants.APPOINTMENT_CODE);
		appointmentCdaMetadata.setServiceStartTime(DATEFORMAT.parse(startTime));
		appointmentCdaMetadata.setServiceStopTime(DATEFORMAT.parse(endTime));
		return appointmentCdaMetadata;
	}
	

	public String getXmlFileContent(String resource) throws SAXException, IOException, ParserConfigurationException, TransformerException {
		InputStream is = Application.class.getResourceAsStream(resource);
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(is);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		String output = writer.getBuffer().toString();
		String result = output;
		return result;
	}
	
	private String generateUUID() {
		java.util.UUID uuid = java.util.UUID.randomUUID();
		return Math.abs(uuid.getLeastSignificantBits()) + "." + Math.abs(uuid.getMostSignificantBits())+"."+Calendar.getInstance().getTimeInMillis();
	}

	private boolean isDocumentIdInList(String id, List<DocumentEntry> documentEntries) {
		for (DocumentEntry documentEntry : documentEntries) {
			if (documentEntry.getUniqueId().equals(id)) {
				return true;
			}
		}
		return false;
	}
}
