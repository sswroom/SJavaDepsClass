package org.sswr.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4JHandler implements LogHandler
{
	private Logger logger;

	Log4JHandler(Logger logger)
	{
		this.logger = logger;
	}

	public void logAdded(long logTime, String logMsg, LogLevel logLev)
	{
		switch (logLev)
		{
			case FORCE:
				this.logger.error(logMsg);
				break;
			case ERROR:
				this.logger.error(logMsg);
				break;
			case ERR_DETAIL:
				this.logger.error(logMsg);
				break;
			case ACTION:
				this.logger.warn(logMsg);
				break;
			case ACTION_DETAIL:
				this.logger.warn(logMsg);
				break;
			case COMMAND:
				this.logger.info(logMsg);
				break;
			case COMMAND_DETAIL:
				this.logger.debug(logMsg);
				break;
			case RAW:
				this.logger.trace(logMsg);
				break;
			default:
				break;
		}
	}

	public void logClosed()
	{

	}

	public static LogTool createLogTool(Class<?> cls)
	{
		LogTool log = new LogTool();
		log.addLogHandler(new Log4JHandler(LoggerFactory.getLogger(cls)), LogLevel.RAW);
		return log;
	}
}
