package org.sswr.util.web.spring;

import org.springframework.core.env.PropertyResolver;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.net.SSLEngine;
import org.sswr.util.net.email.AWSEmailControl;
import org.sswr.util.net.email.EmailControl;
import org.sswr.util.net.email.NullEmailControl;
import org.sswr.util.net.email.SMTPConnType;
import org.sswr.util.net.email.SMTPDirectEmailControl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.amazon.awssdk.regions.Region;

public class EmailControlConfig {
	@SuppressWarnings("unused")
	@Nullable
	public static EmailControl loadFromConfig(@Nullable SSLEngine ssl, @Nonnull PropertyResolver env, @Nullable String categoryName, @Nonnull LogTool log)
	{
		if (categoryName == null)
		{
			categoryName = "";
		}
		else
		{
			categoryName = categoryName + ".";
		}
		String s = env.getProperty(categoryName+"email.send.type");
		if (s == null)
		{
			return null;
		}
		if (s.equals("SMTP"))
		{
			String host;
			Integer port;
			SMTPConnType connType;
			String userName;
			String password;
			String fromEmail;
			if ((port = StringUtil.toInteger(env.getProperty(categoryName+"smtp.port"))) == null)
			{
				log.logMessage(categoryName+"smtp.port not found", LogLevel.ERROR);
				return null;
			}
			if (port <= 0 || port >= 65535)
			{
				log.logMessage(categoryName+"smtp.port not valid", LogLevel.ERROR);
				return null;
			}
			if ((host = env.getProperty(categoryName+"smtp.host")) == null)
			{
				log.logMessage(categoryName+"smtp.host not found", LogLevel.ERROR);
				return null;
			}
			if ((s = env.getProperty(categoryName+"smtp.type")) == null)
			{
				log.logMessage(categoryName+"smtp.type not found", LogLevel.ERROR);
				return null;
			}
			if (s.equals("PLAIN"))
			{
				connType = SMTPConnType.PLAIN;
			}
			else if (s.equals("SSL"))
			{
				connType = SMTPConnType.SSL;
			}
			else if (s.equals("STARTTLS"))
			{
				connType = SMTPConnType.STARTTLS;
			}
			else
			{
				log.logMessage(categoryName+"smtp.type must be one of PLAIN, SSL or STARTTLS", LogLevel.ERROR);
				return null;
			}
			if ((fromEmail = env.getProperty(categoryName+"smtp.from")) == null)
			{
				log.logMessage(categoryName+"smtp.from not found", LogLevel.ERROR);
				return null;
			}
			if ((userName = env.getProperty(categoryName+"smtp.user")) == null || (password = env.getProperty(categoryName+"smtp.password")) == null)
			{
				userName = null;
				password = null;
			}
			SMTPDirectEmailControl ctrl = new SMTPDirectEmailControl(host, port, ssl, connType, userName, password, fromEmail, log);
			return ctrl;
		}
		else if (s.equals("AWS"))
		{
			String fromAddr;
			String sRegion;
			Region region;
			String proxyHost;
			String proxySPort;
			String proxyUser;
			String proxyPassword;
			Integer proxyPort;
			if ((fromAddr = env.getProperty(categoryName+"awsmail.from")) == null)
			{
				log.logMessage(categoryName+"awsmail.from not found", LogLevel.ERROR);
				return null;
			}
			if (!StringUtil.isEmailAddress(fromAddr))
			{
				log.logMessage(categoryName+"awsmail.from not valid email", LogLevel.ERROR);
				return null;
			}
			if ((sRegion = env.getProperty(categoryName+"awsmail.region")) == null)
			{
				log.logMessage(categoryName+"awsmail.region not found", LogLevel.ERROR);
				return null;
			}
			if (sRegion.equals("us-east-2"))
			{
				region = Region.US_EAST_2;
			}
			else
			{
				log.logMessage(categoryName+"awsmail.region ("+sRegion+") not supported", LogLevel.ERROR);
				return null;
			}
			AWSEmailControl ctrl = new AWSEmailControl(fromAddr, region, log);
			proxyHost = env.getProperty(categoryName+"awsmail.proxy.host");
			proxySPort = env.getProperty(categoryName+"awsmail.proxy.port");
			proxyUser = env.getProperty(categoryName+"awsmail.proxy.user");
			proxyPassword = env.getProperty(categoryName+"awsmail.proxy.password");
			if (proxyHost != null && proxySPort != null && proxyUser != null && proxyPassword != null && (proxyPort = StringUtil.toInteger(proxySPort)) != null)
			{
				ctrl.setProxy(proxyHost, proxyPort.intValue(), proxyUser, proxyPassword);
			}
			return ctrl;
		}
		else if (s.equals("NULL"))
		{
			return new NullEmailControl();
		}
		else
		{
			return null;
		}
	}
}
