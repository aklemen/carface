package com.example.carface;

public class LogObject {
    private String IdApp;
    private String TaskName;
    private String ActionId;
    private String ActionDescription;
    private String TimeStamp;
    private String Done;
    private String TaskCompletionTime;

    public LogObject(String idApp, String taskName, String actionId, String actionDescription, String timeStamp, String done,
                     String taskCompletionTime) {
        IdApp = idApp;
        TaskName = taskName;
        ActionId = actionId;
        ActionDescription = actionDescription;
        TimeStamp = timeStamp;
        Done = done;
        TaskCompletionTime = taskCompletionTime;
    }
}
