package dk.sts.appointment.dto;

import java.util.Date;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CdaMetadata {

	public CdaMetadata(){}
	
	public Code classCode;
	
	public Code formatCode;
	
	public Code healthcareFacilityTypeCode;
	
	public Code practiceSettingCode;

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	public Date submissionTime;

	public String getMimeType() {
		return "text/xml";
	}

	public Code getClassCode() {
		return classCode;
	}

	public void setClassCode(Code classCode) {
		this.classCode = classCode;
	}

	public Code getFormatCode() {
		return formatCode;
	}

	public void setFormatCode(Code formatCode) {
		this.formatCode = formatCode;
	}

	public Code getHealthcareFacilityTypeCode() {
		return healthcareFacilityTypeCode;
	}

	public void setHealthcareFacilityTypeCode(Code healthcareFacilityTypeCode) {
		this.healthcareFacilityTypeCode = healthcareFacilityTypeCode;
	}

	public Date getSubmissionTime() {
		return submissionTime;
	}

	public void setSubmissionTime(Date submissionTime) {
		this.submissionTime = submissionTime;
	}
	
	public Code getPracticeSettingCode() {
		return practiceSettingCode;
	}

	public void setPracticeSettingCode(Code practiceSettingCode) {
		this.practiceSettingCode = practiceSettingCode;
	}
}
