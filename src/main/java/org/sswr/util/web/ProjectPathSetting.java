package org.sswr.util.web;

public class ProjectPathSetting {
    private String path;
    private boolean needDelete;
    
    public ProjectPathSetting(String path)
    {
        this.path = path;
        this.needDelete = true;
    }

    public ProjectPathSetting(String path, boolean needDelete)
    {
        this.path = path;
        this.needDelete = needDelete;
    }

    public String getPath()
    {
        return this.path;
    }

    public boolean isNeedDelete()
    {
        return this.needDelete;
    }
}
