package de.iteconomics.confluence.plugins.rest;

import javax.xml.bind.annotation.*;
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class JobTestRestServiceModel {

    @XmlElement(name = "value")
    private String message;

    public JobTestRestServiceModel() {
    }

    public JobTestRestServiceModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}