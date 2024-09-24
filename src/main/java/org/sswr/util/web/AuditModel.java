package org.sswr.util.web;

import jakarta.annotation.Nullable;
import jakarta.persistence.Transient;

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
    @Nullable
    public String getLastValue()
    {
        return this.lastValue;
    }
}
