package org.sswr.util.web.spring;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.springframework.session.SessionRepository;
import org.sswr.util.data.StringUtil;
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

	public MQTTSessionRepository(LogTool log, String brokerHost, int port, SSLEngine ssl, TCPClientType clientType, int keepAliveS, String user, String password, String topicBase)
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
		this.sessMap = new HashMap<String, SpringSession>();
	}

	@Override
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
			this.publishAll();
		}
	}

	@Override
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
			this.publishAll();
		}
	}

	@Override
	public void onPublishMessage(String topic, byte[] buff, int buffOfst, int buffSize) {
		if (topic.endsWith("/all"))
		{
			try
			{
				List<SpringSession> sessList = new ArrayList<SpringSession>();
				String[] sarr = StringUtil.split(new String(buff, buffOfst, buffSize, StandardCharsets.UTF_8), "|");
				int i = 0;
				int j = sarr.length;
				while (i < j)
				{
					sessList.add(SpringSession.fromJSON(sarr[i]));
					i++;
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
		}
		else
		{

		}
	}

	private void publishAll()
	{
		StringBuilder sb = new StringBuilder();
		synchronized(this)
		{
			Iterator<SpringSession> it = this.sessMap.values().iterator();
			while (it.hasNext())
			{
				SpringSession sess = it.next();
				try
				{
					String json = sess.toJSON();
					if (sb.length() > 0) sb.append("|");
					sb.append(json);
				}
				catch (IOException ex)
				{
					this.log.logException(ex);
				}
			}
		}
		this.cli.publish(this.topicBase+"all", sb.toString());
	}
}
