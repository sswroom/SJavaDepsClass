package org.sswr.util.web;

import jakarta.annotation.Nonnull;

public class ProjectPathSetting {
    private String path;
    private boolean deleteAfterProcess;
    
    public ProjectPathSetting(@Nonnull String path)
    {
        this.path = path;
        this.deleteAfterProcess = true;
    }

    public ProjectPathSetting(@Nonnull String path, boolean deleteAfterProcess)
    {
        this.path = path;
        this.deleteAfterProcess = deleteAfterProcess;
    }

    @Nonnull 
    public String getPath()
    {
        return this.path;
    }

    public boolean isDeleteAfterProcess()
    {
        return this.deleteAfterProcess;
    }
}
