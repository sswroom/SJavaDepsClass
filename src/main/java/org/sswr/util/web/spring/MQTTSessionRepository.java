package org.sswr.util.web.spring;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.session.SessionRepository;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.net.MQTTPublishMessageHdlr;
import org.sswr.util.net.MQTTStaticClient;
import org.sswr.util.net.SSLEngine;
import org.sswr.util.net.TCPClientType;

public class MQTTSessionRepository implements SessionRepository<SpringSession>, MQTTPublishMessageHdlr {
	private HashMap<String, SpringSession> sessMap;
	private MQTTStaticClient cli;
	private String topicBase;
	private LogTool log;

	public MQTTSessionRepository(@Nonnull LogTool log, @Nonnull String brokerHost, int port, @Nullable SSLEngine ssl, @Nonnull TCPClientType clientType, int keepAliveS, @Nullable String user, @Nullable String password, @Nonnull String topicBase)
	{
		this.log = log;
		if (topicBase.endsWith("/"))
		{
			this.topicBase = topicBase;
		}
		else
		{
			this.topicBase = topicBase+"/";
		}
		this.cli = new MQTTStaticClient(brokerHost, port, ssl, clientType, keepAliveS, user, password, true);
		this.cli.subscribe(this.topicBase+"+", this);
		this.cli.publish(this.topicBase+"cmd", "getAll");
		this.sessMap = new HashMap<String, SpringSession>();
	}

	@Override
	@Nonnull 
	public SpringSession createSession() {
		return new SpringSession();
	}

	@Override
	public void save(SpringSession session) {
		synchronized (this)
		{
			this.sessMap.put(session.getId(), session);
		}
		if (session.isUpdated())
		{
			session.setUpdated(false);
			this.publishUpdate(session);
		}
	}

	@Override
	@Nullable
	public SpringSession findById(String id) {
		synchronized (this)
		{
			return this.sessMap.get(id);
		}
	}

	@Override
	public void deleteById(String id) {
		synchronized (this)
		{
			this.sessMap.remove(id);
			this.publishDelete(id);
		}
	}

	@Override
	public void onPublishMessage(@Nonnull String topic, @Nonnull byte[] buff, int buffOfst, int buffSize) {
		if (topic.endsWith("/update"))
		{
			try
			{
				SpringSession session = SpringSession.fromJSON(new String(buff, buffOfst, buffSize, StandardCharsets.UTF_8));
				synchronized (this)
				{
					this.sessMap.put(session.getId(), session);
				}
			}
			catch (IOException ex)
			{
				log.logException(ex);
			}
			catch (ClassNotFoundException ex)
			{
				log.logException(ex);
			}
			catch (DataFormatException ex)
			{
				log.logException(ex);
			}
		}
		else if (topic.endsWith("/del"))
		{
			String id = new String(buff, buffOfst, buffSize, StandardCharsets.UTF_8);
			synchronized (this)
			{
				this.sessMap.remove(id);
			}
		}
		else if (topic.endsWith("/cmd"))
		{
			String val = new String(buff, buffOfst, buffSize, StandardCharsets.UTF_8);
			if (val.equals("getAll"))
			{
				synchronized (this)
				{
					Iterator<SpringSession> it = this.sessMap.values().iterator();
					while (it.hasNext())
					{
						this.publishUpdate(it.next());
					}
				}
	
			}
		}
	}

	private void publishUpdate(@Nonnull SpringSession session)
	{
		try
		{
			String json = session.toJSON();
			if (json.length() > 65000)
			{
				this.log.logMessage("Session is too large to boardcast: len = "+json.length(), LogLevel.ERROR);
			}
			else
			{
				this.cli.publish(this.topicBase+"update", json);
			}
		}
		catch (IOException ex)
		{
			this.log.logException(ex);
		}
	}

	private void publishDelete(@Nonnull String id)
	{
		this.cli.publish(this.topicBase+"del", id);
	}
}
