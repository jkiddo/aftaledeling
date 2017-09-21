package dk.sts.appointment;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.LocalizedString;

public class AppointmentConstants {

	private static String LOINC_CODE_SYSTEM_OID= "2.16.840.1.113883.6.1";
	private static String LOINC_CODE_APPOINTMENT_CODE = "39289-4";
	private static String LOINC_CODE_APPOINTMENT_DISPLAY_NAME = "Dato og tidspunkt for møde mellem patient og sundhedsperson";
	
	public static Code APPOINTMENT_CODE = new Code(LOINC_CODE_APPOINTMENT_CODE, new LocalizedString(LOINC_CODE_APPOINTMENT_DISPLAY_NAME), LOINC_CODE_SYSTEM_OID);

}
