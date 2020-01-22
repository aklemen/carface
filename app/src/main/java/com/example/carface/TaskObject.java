package com.example.carface;


public class TaskObject {
    private String name;
    private String recipient;
    private String property;
    private String init;
    private String target;
    private String description;
    private String idApp;

    public TaskObject(String name, String recipient, String property, String init, String target, String description, String idApp) {
        this.name = name;
        this.recipient = recipient;
        this.property = property;
        this.init = init;
        this.target = target;
        this.description = description;
        this.idApp = idApp;
    }

    public String getName() {
        return name;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getProperty() {
        return property;
    }

    public String getInit() {
        return init;
    }

    public String getTarget() {
        return target;
    }

    public String getDescription() {
        return description;
    }

    public String getIdApp() {
        return idApp;
    }

}