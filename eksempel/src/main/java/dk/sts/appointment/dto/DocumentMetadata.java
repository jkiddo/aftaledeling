package dk.sts.appointment.dto;

import java.util.Date;
import java.util.List;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DocumentMetadata extends CdaMetadata {

	public Code patientId;

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	public Date reportTime;
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	public Date serviceStartTime;	

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	public Date serviceStopTime;	

	public Code organisation;

	public Code typeCode;
	
	public List<Code> eventCodes;
	
	public String languageCode;
	
	public String title;
	
	public Code contentTypeCode;
	
	public String mimeType;
	
	private Code confidentialityCode;
	
	@Override
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	
	public Code getPatientId() {
		return patientId;
	}

	public void setPatientId(Code patientId) {
		this.patientId = patientId;
	}

	public Date getReportTime() {
		return reportTime;
	}

	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

	public Code getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Code organisation) {
		this.organisation = organisation;
	}

	public List<Code> getEventCodes() {
		return eventCodes;
	}

	public void setEventCodes(List<Code> eventCodes) {
		this.eventCodes = eventCodes;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Code getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(Code typeCode) {
		this.typeCode = typeCode;
	}

	public Code getContentTypeCode() {
		return contentTypeCode;
	}

	public void setContentTypeCode(Code contentTypeCode) {
		this.contentTypeCode = contentTypeCode;
	}

	public Code getConfidentialityCode() {
		return confidentialityCode;
	}

	public void setConfidentialityCode(Code confidentialityCode) {
		this.confidentialityCode = confidentialityCode;
	}
	
	public Date getServiceStartTime() {
		return serviceStartTime;
	}

	public void setServiceStartTime(Date serviceStartTime) {
		this.serviceStartTime = serviceStartTime;
	}

	public void setServiceStopTime(Date serviceStopTime) {
		this.serviceStopTime = serviceStopTime;
	}

	public Date getServiceStopTime() {
		return this.serviceStopTime;
	}

}
