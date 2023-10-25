package org.sswr.util.web;

public class ProjectPathSetting {
    private String path;
    private boolean deleteAfterProcess;
    
    public ProjectPathSetting(String path)
    {
        this.path = path;
        this.deleteAfterProcess = true;
    }

    public ProjectPathSetting(String path, boolean deleteAfterProcess)
    {
        this.path = path;
        this.deleteAfterProcess = deleteAfterProcess;
    }

    public String getPath()
    {
        return this.path;
    }

    public boolean isDeleteAfterProcess()
    {
        return this.deleteAfterProcess;
    }
}
