package org.sswr.util.web.spring;

import java.io.File;

import org.sswr.util.web.ProjectPathSetting;

import jakarta.annotation.Nonnull;

public interface LogZipperEventHandler
{
	public void processedNonDeleteFile(@Nonnull ProjectPathSetting setting, @Nonnull File file);
	public void zipFinished(@Nonnull File zipFile);
}
