package dk.sts.appointment;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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
		DocumentMetadata appointmentCdaMetadata = new DocumentMetadata();
		appointmentCdaMetadata.setTitle("Aftale for "+PATIENT_ID);
		appointmentCdaMetadata.setPatientId(new Code(PATIENT_ID, null, Codes.DK_CPR_OID));
		appointmentCdaMetadata.setReportTime(new SimpleDateFormat("yyyyMMddHHmmssZ").parse("20170113100000+0100"));
		appointmentCdaMetadata.setOrganisation(new Code("242621000016001", new LocalizedString("OUH Radiologisk Afdeling (Svendborg)"), Codes.DK_SOR_CLASSIFICAION_OID));
		appointmentCdaMetadata.setClassCode(new Code("001", new LocalizedString("Klinisk rapport"), "1.2.208.184.100.9"));
		appointmentCdaMetadata.setFormatCode(new Code("urn:ad:dk:medcom:appointment", new LocalizedString("DK CDA APD") ,"1.2.208.184.14.1"));
		appointmentCdaMetadata.setHealthcareFacilityTypeCode(new Code("22232009", new LocalizedString("hospital") ,"2.16.840.1.113883.6.96"));
		appointmentCdaMetadata.setPracticeSettingCode(new Code("408443003", new LocalizedString("almen medicin"),"2.16.840.1.113883.6.96"));
		appointmentCdaMetadata.setSubmissionTime(new Date());
		appointmentCdaMetadata.setContentTypeCode(AppointmentConstants.APPOINTMENT_CODE);
		appointmentCdaMetadata.setTypeCode(AppointmentConstants.APPOINTMENT_CODE);
		appointmentCdaMetadata.setServiceStartTime(new SimpleDateFormat("yyyyMMddHHmmssZ").parse("20170531110000+0100"));
		appointmentCdaMetadata.setServiceStopTime(new SimpleDateFormat("yyyyMMddHHmmssZ").parse("20170531120000+0100"));
		String externalIdForNewDocument = generateUUID();
		String documentId = appointmentXdsRequestService.createAndRegisterDocument(externalIdForNewDocument, appointmentXmlDocument, appointmentCdaMetadata);
		System.out.println("We registered a new appointment with documentId="+documentId);

		// Search document for patient (we assume there is one more now)
		List<DocumentEntry> currentAppointmentsAfterNewAppointment = appointmentXdsRequestService.getAllAppointmentsForPatient(PATIENT_ID);
		System.out.println("The patient with id="+PATIENT_ID+" now has "+currentAppointmentsAfterNewAppointment.size()+" registered in the XDS registry.");

		// Get appointment document from id
		String document = appointmentXdsRequestService.fetchDocument(externalIdForNewDocument);
		System.out.println(document);
		
		// Now we want to deprecate a document...but first we have to get the document entry from the registry
		DocumentEntry toBeDeprecated = appointmentXdsRequestService.getAppointmentDocumentEntry(documentId);		
		// ... then deprecate
		appointmentXdsRequestService.deprecateDocument(toBeDeprecated);

		//Fremsøg aftaler for patienten
		List<DocumentEntry> currentAppointmentsAfterDeprecation = appointmentXdsRequestService.getAllAppointmentsForPatient(PATIENT_ID);
		System.out.println("The patient with id="+PATIENT_ID+" now has "+currentAppointmentsAfterDeprecation.size()+" registered in the XDS registry.");

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

}
