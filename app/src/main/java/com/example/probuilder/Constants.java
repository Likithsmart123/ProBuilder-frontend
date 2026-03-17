package com.example.probuilder;

public class Constants {

    // Change ONLY this when IP changes
    // For android emulator use 10.0.2.2
    private static final String SERVER_IP = "14.139.187.229";

// PHP Backend
public static final String BASE_URL =
        "http://" + SERVER_IP + ":8081/oct/spic_730/probuilder/";

// AI Backend (if running on same PC)
public static final String PREDICTION_BASE_URL =
        "http://" + SERVER_IP + ":8000/";

// Active Projects AI URL (port 5000)
public static final String ACTIVE_PROJECTS_URL =
        "http://" + SERVER_IP + ":5000/";
}