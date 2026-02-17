package com.certifolio.server.Form.dto;

import com.certifolio.server.Form.Activity.dto.ActivityDTO;
import com.certifolio.server.Form.Project.dto.ProjectDTO;
import com.certifolio.server.Form.Certificate.dto.CertificateDTO;
import lombok.Data;
import java.util.List;

@Data
public class ProfileUploadDTO {
    private HighSchoolDTO highSchool;
    private UniversityDTO university;
    private MilitaryDTO military;
    private List<ProjectDTO> projects;
    private List<ActivityDTO> activities;
    private List<CertificateDTO> certificates;
    private ExperienceDTO experience;

    @Data
    public static class HighSchoolDTO {
        private String name;
        private String location;
        private String entranceDate;
        private String graduationDate;
        private String gpa;
        private String type;
    }

    @Data
    public static class UniversityDTO {
        private String name;
        private String major;
        private String degree;
        private String entranceDate;
        private String graduationDate;
        private String gpa;
        private String status;
    }

    @Data
    public static class MilitaryDTO {
        private String status;
        private String branch;
        private String rank;
        private String period;
        private String specialty;
    }

    @Data
    public static class ExperienceDTO {
        private List<InternshipDTO> internships;
        private List<JobDTO> jobs;
    }

    @Data
    public static class InternshipDTO {
        private String company;
        private String position;
        private String period;
        private String type;
        private String description;
    }

    @Data
    public static class JobDTO {
        private String company;
        private String position;
        private String department;
        private String period;
        private String description;
    }
}
