package dk.sts.appointment;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.LocalizedString;

import dk.s4.hl7.cda.codes.Loinc;

public class AppointmentConstants {

	public static Code APPOINTMENT_CODE = new Code(Loinc.APD_CODE, new LocalizedString(Loinc.APD_DISPLAYNAME), Loinc.OID);

}
