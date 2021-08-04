package org.sswr.util.web.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.sswr.util.io.FileUtil;
import org.sswr.util.io.Log4JHandler;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.web.ProjectConfig;

@Component
public class LogZipper
{
	private static final LogTool log = Log4JHandler.createLogTool(LogZipper.class);
	private int lastExecYear = 0;
	private int lastExecMonth = 0;
	private int lastExecDay = 0;

	@Autowired
	private ProjectConfig cfg;
//
	@Scheduled(fixedRate = 5000)
	public void checkLogFiles() {
		if (cfg.enableLogZipper())
		{
			LocalDateTime date = LocalDateTime.now();
			if ((date.getYear() != this.lastExecYear || date.getMonth().getValue() != this.lastExecMonth || date.getDayOfMonth() != this.lastExecDay) && date.getHour() >= 1)
			{
				this.lastExecYear = date.getYear();
				this.lastExecMonth = date.getMonth().getValue();
				this.lastExecDay = date.getDayOfMonth();
				log.logMessage("LogZipper triggered", LogLevel.COMMAND);
				this.logArchive();
			}
		}
	}

	private void logArchive()
	{
//		File actionLogDir = new File(getTruePath(actionLogPath));
		HashMap<Integer, Integer> timeMap = new HashMap<Integer, Integer>();
		int iDate = this.lastExecYear * 10000 + this.lastExecMonth * 100 + this.lastExecDay;
		List<String> logPaths = cfg.getLogPathsToZip();
		int i = 0;
		int j = logPaths.size();
		while (i < j)
		{
			File logDir = new File(FileUtil.getRealPath(logPaths.get(i), false));
			logFindDates(timeMap, logDir, iDate);
			i++;
		}

		File zipLogDir = new File(FileUtil.getRealPath(cfg.getLogZipPath(), false));
		Collection<Integer> times = timeMap.values();
		Iterator<Integer> it = times.iterator();
		while (it.hasNext())
		{
			int fileDate = it.next();
			File zipMonthDir = new File(zipLogDir.getPath() + File.separator + (fileDate / 100));
			zipMonthDir.mkdirs();
			File zipFile = new File(zipMonthDir.getPath() + File.separator + fileDate + ".zip");
			try
			{
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
				i = 0;
				while (i < j)
				{
					File logDir = new File(FileUtil.getRealPath(logPaths.get(i), false));
					logZipDate(out, logDir, fileDate);
					i++;
				}
	
				out.close();
			}
			catch (IOException ex)
			{
				log.logException(ex);
			}
		}
	}

	private void logFindDates(HashMap<Integer, Integer> timeMap, File fileDir, int currDate)
	{
		File monthList[] = fileDir.listFiles();
		if (monthList == null)
		{
			return;
		}
		int i = 0;
		int j = monthList.length;
		while (i < j)
		{
			if (monthList[i].isDirectory() && monthList[i].getName().charAt(0) != '.')
			{
				File logFiles[] = monthList[i].listFiles();
				if (logFiles != null)
				{
					int k = 0;
					int l = logFiles.length;
					int m;
					while (k < l)
					{
						m = logFiles[k].getName().indexOf(".");
						if (m >= 8)
						{
							try
							{
								int iDate = Integer.parseInt(logFiles[k].getName().substring(m - 8, m));
								if (iDate < currDate)
								{
									timeMap.put(iDate, iDate);
								}
							}
							catch (Exception ex)
							{

							}
						}
						k++;
					}
				}
			}
			i++;
		}
	}

	public void logZipDate(ZipOutputStream zip, File fileDir, int fileDate)
	{
		File monthList[] = fileDir.listFiles();
		if (monthList == null)
		{
			return;
		}
		int i = 0;
		int j = monthList.length;
		while (i < j)
		{
			if (monthList[i].isDirectory() && monthList[i].getName().charAt(0) != '.')
			{
				File logFiles[] = monthList[i].listFiles();
				if (logFiles != null)
				{
					int k = 0;
					int l = logFiles.length;
					int m;
					while (k < l)
					{
						m = logFiles[k].getName().indexOf(".");
						if (m >= 8)
						{
							boolean opened = false;
							try
							{
								int iDate = Integer.parseInt(logFiles[k].getName().substring(m - 8, m));
								if (iDate == fileDate)
								{
									long fileLeng = logFiles[k].length();
									FileInputStream fis = new FileInputStream(logFiles[k]);
									ZipEntry e = new ZipEntry(logFiles[k].getName());
									zip.putNextEntry(e);
									opened = true;

									if (fileLeng <= 1048576)
									{
										byte fileContent[] = fis.readAllBytes();
										zip.write(fileContent, 0, fileContent.length);
									}
									else
									{
										byte fileContent[] = new byte[1048576];
										int readSize;
										while ((readSize = fis.read(fileContent, 0, 1048576)) >= 0)
										{
											zip.write(fileContent, 0, readSize);
										}
									}
									fis.close();

									logFiles[k].delete();
								}
							}
							catch (Exception ex)
							{
								log.logException(ex);
							}
							if (opened)
							{
								try
								{
									zip.closeEntry();
								}
								catch (IOException ex)
								{
									log.logException(ex);
								}
							}
						}
						k++;
					}

					monthList[i].delete();
				}
			}
			i++;
		}
	}
}
