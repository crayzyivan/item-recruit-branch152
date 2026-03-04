package com.item.dto.naukri;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NaukriApplicationDTO {
    private String id;
    private ApplyData applyData;
    private String applicantId;
    private Long companyId;
    private String jobId;
    private String referenceCode;
    private String resumeFileId;
    private String resume;
    private Map<String, Object> scores;
    private String scoringStatus;
    private String applySource;
    private String createdDate;

  

    @Data
    public static class ApplyData {
        @JsonProperty("Salary")
        private String Salary;
        @JsonProperty("CurrEmployer")
        private String CurrEmployer;
        @JsonProperty("Email")
        private String Email;
        @JsonProperty("Gender")
        private String Gender;
        @JsonProperty("FileFormat")
        private String FileFormat;
        @JsonProperty("Salutation")
        private String Salutation;
        @JsonProperty("KeySkills")
        private String KeySkills;
        @JsonProperty("AppliedAt")
        private String AppliedAt;
        @JsonProperty("Education")
        private Education Education;
        @JsonProperty("RefCode")
        private String RefCode;
        @JsonProperty("Currency")
        private String Currency;
        @JsonProperty("CurrentLocation")
        private String CurrentLocation;
        @JsonProperty("DateOfBirth")
        private String DateOfBirth;
        @JsonProperty("Designation")
        private String Designation;
        @JsonProperty("QuestionaireData")
        private Map<String, Object> QuestionaireData;
        @JsonProperty("FirstName")
        private String FirstName;
        @JsonProperty("TotalExp")
        private String TotalExp;
        private String applySource;
        @JsonProperty("MiddleName")
        private String MiddleName;
        @JsonProperty("ResumeExtension")
        private String ResumeExtension;
        @JsonProperty("modified_date")
        private String modified_date;
        @JsonProperty("Mobile")
        private String Mobile;
        private List<Object> enrichedQuestionaireData;
        private Long companyId;
        private Cv cv;
        @JsonProperty("ApplicantID")
        private String ApplicantID;
        @JsonProperty("job_id")
        private String job_id;
        @JsonProperty("HighestEducation")
        private String HighestEducation;
        @JsonProperty("LastName")
        private String LastName;
        @JsonProperty("WorkExperiences")
        private WorkExperiences WorkExperiences;
    }

    @Data
    public static class Cv {
        @JsonProperty("FileName")
        private String FileName;
        @JsonProperty("ResumeExtension")
        private String ResumeExtension;
    }

    @Data
    public static class WorkExperiences {
        @JsonProperty("OtherCompany")
        private List<CompanyInfo> otherCompany;
        @JsonProperty("PreviousCompany")
        private CompanyInfo previousCompany;
        @JsonProperty("CurrentCompany")
        private CompanyInfo currentCompany;
    }

    @Data
    public static class CompanyInfo {
        @JsonProperty("OrganizationName")
        private String OrganizationName;
        @JsonProperty("StartDate")
        private String StartDate;
        @JsonProperty("Designation")
        private String Designation;
        @JsonProperty("EmploymentType")
        private String EmploymentType;
        @JsonProperty("EndDate")
        private String EndDate;
        @JsonProperty("KeySkills")
        private String KeySkills;
        @JsonProperty("JobProfile")
        private String JobProfile;
    }
  
    @Data
    public static class Education {
        @JsonProperty("ug")
        private Ug ug;
        @JsonProperty("pg")
        private Ug pg;
        @JsonProperty("phd")
        private Ug phd;

        // capture any additional levels (e.g., pg, phd) without breaking parsing
        private Map<String, Object> additionalLevels;

        @JsonAnySetter
        public void putAdditional(String key, Object value) {
            if (this.additionalLevels == null) {
                this.additionalLevels = new java.util.HashMap<>();
            }
            this.additionalLevels.put(key, value);
        }
    }

    @Data
    public static class Ug {
        @JsonProperty("qualification")
        private String qualification;
        @JsonProperty("qualificationType")
        private String qualificationType;
        @JsonProperty("courseType")
        private String courseType;
        @JsonProperty("yearOfPassing")
        private String yearOfPassing;
        @JsonProperty("university")
        private String university;
        @JsonProperty("specialization")
        private String specialization;
    }
}



