package org.sswr.util.web;

import java.util.List;

public abstract class ProjectConfig
{
	public abstract boolean enableOTP();
	public abstract boolean enableLogZipper();
	public abstract List<ProjectPathSetting> getLogPathsToZip();
	public abstract String getLogZipPath();
	
}
