package org.sswr.util.web;

import java.util.List;

import org.sswr.util.web.spring.LogZipperEventHandler;

public abstract class ProjectConfig
{
	public abstract boolean enableOTP();
	public abstract boolean enableLogZipper();
	public abstract List<ProjectPathSetting> getLogPathsToZip();
	public abstract String getLogZipPath();
	public abstract LogZipperEventHandler getLogZipperEventHandler();
	
}
