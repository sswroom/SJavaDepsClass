package org.sswr.util.web.spring;

import java.io.File;

import org.sswr.util.web.ProjectPathSetting;

public interface LogZipperEventHandler
{
	public void processedNonDeleteFile(ProjectPathSetting setting, File file);
}
