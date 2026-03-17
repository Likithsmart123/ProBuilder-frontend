package com.example.probuilder;

import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

    private static ScheduleRepository instance;
    private final List<ProjectSchedule> projects;

    private ScheduleRepository() {
        projects = new ArrayList<>();
        // CORRECTED: Constructor calls now match the data model perfectly.
        projects.add(new ProjectSchedule("Villa Construction - Whitefield", "Rakesh Kumar", "On Schedule", 68, "24 Jan 2025", "15 Jun 2025", "15 Jun 2025", "", 45, 152, "100%", "", "Plastering & Painting"));
        projects.add(new ProjectSchedule("Office Renovation - Indiranagar", "TechGuru Solutions", "Minor Delay", 35, "1 Feb 2025", "30 Apr 2025", "12 May 2025", "+12d", 28, 89, "80%", "Project is slightly behind schedule.", "Electrical Works"));
        projects.add(new ProjectSchedule("Apartment Interiors - HSR Layout", "Priya Sharma", "At Risk", 25, "26 Jan 2025", "28 Mar 2025", "15 Apr 2025", "+20d", 40, 60, "70%", "Project is significantly behind schedule.", "Interior Finishing"));
        projects.add(new ProjectSchedule("Residential Complex - Sarjapur", "Grace Homes Pvt Ltd", "On Schedule", 88, "1 Nov 2024", "30 May 2025", "25 May 2025", "-5d", 110, 212, "100%", "", "External Painting"));
    }

    public static synchronized ScheduleRepository getInstance() {
        if (instance == null) {
            instance = new ScheduleRepository();
        }
        return instance;
    }

    public List<ProjectSchedule> getProjects() {
        return projects;
    }
}
