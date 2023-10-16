package org.sswr.util.web.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.sswr.util.io.FileUtil;
import org.sswr.util.io.Log4JHandler;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.web.ProjectConfig;
import org.sswr.util.web.ProjectPathSetting;

@Component
public class LogZipper
{
	private static final LogTool log = Log4JHandler.createLogTool(LogZipper.class);
	private int lastExecYear = 0;
	private int lastExecMonth = 0;
	private int lastExecDay = 0;

	@Autowired
	private ProjectConfig cfg;

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
		HashMap<Integer, Integer> timeMap = new HashMap<Integer, Integer>();
		int iDate = this.lastExecYear * 10000 + this.lastExecMonth * 100 + this.lastExecDay;
		List<ProjectPathSetting> logPaths = cfg.getLogPathsToZip();
		int i = 0;
		int j = logPaths.size();
		while (i < j)
		{
			File logDir = new File(FileUtil.getRealPath(logPaths.get(i).getPath(), false));
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
				ZipFile in = null;
				try
				{
					if (zipFile.exists())
					{
						File tempFile = new File(zipMonthDir.getPath() + File.separator + "temp.zip");
						tempFile.delete();
						zipFile.renameTo(tempFile);
						zipFile = new File(zipMonthDir.getPath() + File.separator + fileDate + ".zip");
						in = new ZipFile(tempFile);
					}
				}
				catch (IOException ex)
				{

				}

				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
				if (in != null)
				{
					byte[] buff = new byte[2048];
					Enumeration<? extends ZipEntry> entries = in.entries();
					while (entries.hasMoreElements())
					{
						ZipEntry e = entries.nextElement();
						out.putNextEntry(e);
						if (!e.isDirectory())
						{
							InputStream stm = in.getInputStream(e);
							while (true)
							{
								i = stm.read(buff);
								if (i <= 0)
								{
									break;
								}
								out.write(buff, 0, i);
							}
						}
						out.closeEntry();
					}
					in.close();
					new File(zipMonthDir.getPath() + File.separator + "temp.zip").delete();
				}
				i = 0;
				while (i < j)
				{
					ProjectPathSetting setting = logPaths.get(i);
					File logDir = new File(FileUtil.getRealPath(setting.getPath(), false));
					logZipDate(out, logDir, fileDate, setting.isNeedDelete());
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
			if (monthList[i].isDirectory())
			{
				if (monthList[i].getName().charAt(0) != '.')
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
			}
			else if (monthList[i].isFile())
			{
				int m;
				m = monthList[i].getName().indexOf(".");
				if (m >= 8)
				{
					try
					{
						int iDate = Integer.parseInt(monthList[i].getName().substring(m - 8, m));
						if (iDate < currDate)
						{
							timeMap.put(iDate, iDate);
						}
					}
					catch (Exception ex)
					{

					}
				}
			}
			i++;
		}
	}

	private void logZipDateFile(ZipOutputStream zip, File file, int fileDate, String filePath, boolean needDelete)
	{
		int m;
		m = file.getName().indexOf(".");
		if (m >= 8)
		{
			boolean opened = false;
			try
			{
				int iDate = Integer.parseInt(file.getName().substring(m - 8, m));
				if (iDate == fileDate)
				{
					long fileLeng = file.length();
					FileInputStream fis = new FileInputStream(file);
					ZipEntry e = new ZipEntry(filePath+"/"+file.getName());
					try
					{
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
					}
					catch (ZipException ex)
					{
						log.logException(ex);
					}
					fis.close();

					if (needDelete && !file.delete())
					{
						log.logMessage("Error in deleting file "+file.getAbsolutePath(), LogLevel.ERROR);
					}
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
	}

	public void logZipDate(ZipOutputStream zip, File fileDir, int fileDate, boolean needDelete)
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
					while (k < l)
					{
						logZipDateFile(zip, logFiles[k], fileDate, fileDir.getName(), needDelete);
						k++;
					}

					monthList[i].delete();
				}
			}
			else if (monthList[i].isFile())
			{
				logZipDateFile(zip, monthList[i], fileDate, fileDir.getName(), needDelete);
			}
			i++;
		}
	}
}
