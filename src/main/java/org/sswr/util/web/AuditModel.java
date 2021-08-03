package org.sswr.util.web;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AuditModel {

    @Transient
    private String lastValue;

    protected AuditModel()
    {
        lastValue = null;
    }

    public void updateCurrVal()
    {
        this.lastValue = this.toString();
    }

    @JsonIgnore
    public String getLastValue()
    {
        return this.lastValue;
    }
}
