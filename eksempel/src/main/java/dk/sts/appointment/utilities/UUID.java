package dk.sts.appointment.utilities;

/** All the UUIDs used in XDS.
 * 
 * References:
 * 
 * All the UUIDs
 * 
 * http://ihewiki.wustl.edu/wiki/index.php/Notes_on_XDS_Profile#UUIDs_Defined_by_XDS
 * 
 * http://ihewiki.wustl.edu/wiki/index.php/Notes_on_XDS_Profile
 * 
 * IT Infrastructure
 * Technical Framework
 * Volume 3
 * 10 (ITI TF-3)
 * Cross-Transaction Specifications
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class UUID {

  public static final String XDSDocumentEntry = "urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1";
  
  // codes for the attributes of the XDSDocumentEntry
  // TF3 p 23
  public static final String XDSDocumentEntry_formatCode            	 = "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d";
  public static final String XDSDocumentEntry_classCode             	 = "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a";
  public static final String XDSDocumentEntry_typeCode              	 = "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983";
  public static final String XDSDocumentEntry_eventCodeList         	 = "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4";
  public static final String XDSDocumentEntry_confidentialityCode   	 = "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f";
  public static final String XDSDocumentEntry_healthcareFacilityTypeCode = "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1";
  public static final String XDSDocumentEntry_practiceSettingCode   = "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead";
  public static final String XDSDocumentEntry_documentEntryUniqueId = "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab";
  public static final String XDSDocumentEntry_patientId             = "urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427";
  public static final String XDSDocumentEntry_author	            = "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d";

  public static final String XDSSubmissionSet_classificationNode    = "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd";

  
  
  public static final String XDSSubmissionSet_uniqueId              = "urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8";
  public static final String XDSSubmissionSet_sourceId              = "urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832";
  public static final String XDSSubmissionSet_patientId             = "urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446";
  public static final String XDSSubmissionSet_author                = "urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d";
  public static final String XDSSubmissionSet_contentTypeCode       = "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500";
}