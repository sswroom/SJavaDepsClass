package org.sswr.util.web.spring;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;
import org.sswr.util.io.LogGroup;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.io.LogType;

public class AccessLogFilter extends GenericFilterBean
{
	private static class RequestStatus
	{
		public long startTime;
		public Thread thread;
		public boolean logged;
	}

	private LogTool logger;
	private Map<Long, RequestStatus> threadMap;

	public AccessLogFilter(@Nonnull String logPath)
	{
		this.threadMap = new HashMap<Long, RequestStatus>();
		this.logger = new LogTool();
		this.logger.addFileLog(logPath, LogType.PER_DAY, LogGroup.PER_MONTH, LogLevel.RAW, "yyyy-MM-dd HH:mm:ss.fffffffff", false);
	}

	public void close()
	{
		if (this.logger != null)
		{
			this.logger.close();
			this.logger = null;
		}
	}

	public void check(long timeoutMs)
	{
		long t = System.currentTimeMillis();
		synchronized (this.threadMap)
		{
			Iterator<RequestStatus> it = this.threadMap.values().iterator();
			while (it.hasNext())
			{
				RequestStatus status = it.next();
				if (!status.logged && t - status.startTime >= timeoutMs)
				{
					status.logged = true;
					StringBuilder sb = new StringBuilder();
					StackTraceElement[] stacks = status.thread.getStackTrace();
					sb.append(status.thread.getName() + " process timeout:");
					int i = 0;
					int j = stacks.length;
					while (i < j)
					{
						sb.append("\r\n\t");
						sb.append(stacks[i].toString());
						i++;
					}
					this.logger.logMessage(sb.toString(), LogLevel.ERROR);
				}
			}
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		boolean found = false;
		RequestStatus status = new RequestStatus();
		status.thread = Thread.currentThread();
		status.startTime = System.currentTimeMillis();
		status.logged = false;
		synchronized (this.threadMap)
		{
			if (this.threadMap.get(status.thread.getId()) == null)
			{
				this.threadMap.put(status.thread.getId(), status);
			}
			else
			{
				found = true;
			}
		}
		if (!found)
		{
			String threadName = status.thread.getName();
			String msg;
			if (this.logger != null)
			{
				msg = threadName + " start: ";
				if (request instanceof HttpServletRequest)
				{
					HttpServletRequest req = (HttpServletRequest)request;
					msg += req.getRemoteAddr()+":"+req.getRemotePort()+" "+req.getMethod()+" "+req.getRequestURI();
				}
				this.logger.logMessage(msg, LogLevel.COMMAND);
			}
			try
			{
				chain.doFilter(request, response);
			}
			catch (Exception ex)
			{
				if (this.logger != null)
				{
					this.logger.logMessage(threadName+" Exception occurs:", LogLevel.ERROR);
					this.logger.logException(ex);
				}
				synchronized (this.threadMap)
				{
					this.threadMap.remove(status.thread.getId());
				}
				throw ex;
			}
			if (this.logger != null)
			{
				msg = threadName + " end: ";
				if (response instanceof HttpServletResponse)
				{
					HttpServletResponse resp = (HttpServletResponse)response;
					msg += resp.getStatus();
				}
				this.logger.logMessage(msg, LogLevel.COMMAND);
			}
			synchronized (this.threadMap)
			{
				this.threadMap.remove(status.thread.getId());
			}
		}
		else
		{
			chain.doFilter(request, response);
		}
	}
}
