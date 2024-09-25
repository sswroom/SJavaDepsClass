package org.sswr.util.web.spring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.springframework.session.Session;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;
import org.sswr.util.data.JSONBase;
import org.sswr.util.data.JSONBuilder;
import org.sswr.util.data.JSONBuilder.ObjectType;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textbinenc.Base64Enc.B64Charset;
import org.sswr.util.data.JSONObject;

import jakarta.annotation.Nonnull;

public class SpringSession implements Session {
	private String id;
	private Instant createTime;
	private Instant accessTime;
	private Duration maxInactiveInterval;
	private int cnt;
	private HashMap<String, Object> attrMap;
	private boolean updated;

	public SpringSession()
	{
		this.createTime = Instant.now();
		this.accessTime = this.createTime;
		this.maxInactiveInterval = Duration.ofMinutes(15);
		this.cnt = 0;
		this.attrMap = new HashMap<String, Object>();
		this.changeSessionId();
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String changeSessionId() {
		this.cnt++;
		this.id = this.createTime.getEpochSecond()+"-"+this.createTime.getNano()+"-"+cnt;
		this.updated = true;
		return this.id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String attributeName) {
		return (T)this.attrMap.get(attributeName);
	}

	@Override
	public Set<String> getAttributeNames() {
		return this.attrMap.keySet();
	}

	@Override
	public void setAttribute(String attributeName, Object attributeValue) {
		this.attrMap.put(attributeName, attributeValue);
		this.updated = true;
	}

	@Override
	public void removeAttribute(String attributeName) {
		this.attrMap.remove(attributeName);
		this.updated = true;
	}

	@Override
	public Instant getCreationTime() {
		return this.createTime;
	}

	@Override
	public void setLastAccessedTime(Instant lastAccessedTime) {
		this.accessTime = lastAccessedTime;
		this.updated = true;
	}

	@Override
	public Instant getLastAccessedTime() {
		return this.accessTime;
	}

	@Override
	public void setMaxInactiveInterval(Duration interval) {
		this.maxInactiveInterval = interval;
		this.updated = true;
	}

	@Override
	public Duration getMaxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	@Override
	public boolean isExpired() {
		return Instant.now().isAfter(this.accessTime.plus(maxInactiveInterval));
	}

	void setCnt(int cnt)
	{
		this.cnt = cnt;
	}

	int getCnt()
	{
		return this.cnt;
	}

	void setId(@Nonnull String id)
	{
		this.id = id;
	}

	void setCreateTime(@Nonnull Instant createTime)
	{
		this.createTime = createTime;
	}

	public boolean isUpdated()
	{
		return this.updated;
	}

	public void setUpdated(boolean updated)
	{
		this.updated = updated;
	}

	@Nonnull
	public String toJSON() throws IOException
	{
		JSONBuilder builder = new JSONBuilder(ObjectType.OT_OBJECT);
		builder.objectAddStr("id", this.getId());
		builder.objectAddInt64("createTime", this.getCreationTime().getEpochSecond());
		builder.objectAddInt32("createTimeNano", this.getCreationTime().getNano());
		builder.objectAddInt64("accessTime", this.getLastAccessedTime().getEpochSecond());
		builder.objectAddInt32("accessTimeNano", this.getLastAccessedTime().getNano());
		builder.objectAddInt64("maxInactiveInterval", this.getMaxInactiveInterval().toMillis());
		builder.objectAddInt32("cnt", this.getCnt());
		builder.objectBeginObject("attributes");
		Set<String> names = this.getAttributeNames();
		Iterator<String> it = names.iterator();
		Deflater deflate = new Deflater(Deflater.BEST_COMPRESSION);
		Base64Enc b64 = new Base64Enc(B64Charset.NORMAL, true);
		while (it.hasNext())
		{
			String name = it.next();
			Object o = this.getAttribute(name);
			if (o instanceof Serializable)
			{
				Serializable obj = (Serializable)o;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(obj);
				oos.flush();
				oos.close();
				byte[] input = baos.toByteArray();
				byte[] output = new byte[input.length + 5];
				deflate.setInput(input);
				deflate.finish();
				int compSize = deflate.deflate(output, 4, output.length - 4);
				deflate.end();
				ByteTool.writeInt32(output, 0, input.length);
				builder.objectAddStr(name, b64.encodeBin(output, 0, compSize + 4));
			}
			else
			{
				System.out.println("Name = "+name+", Class = "+o.getClass().toString()+", "+DataTools.toObjectString(o));
				throw new IOException("Object type not supported: Name = "+name+", Class = "+o.getClass().toString());
			}
		}
		builder.objectEnd();
		return builder.toString();
	}

	@Nonnull
	public static SpringSession fromJSON(@Nonnull String sessStr) throws IOException, ClassNotFoundException, DataFormatException
	{
		String s;
		JSONBase json = JSONBase.parseJSONStr(sessStr);
		if (json == null)
		{
			System.out.println(sessStr);
			throw new IllegalArgumentException("sessStr is not in valid format");
		}
		SpringSession sess = new SpringSession();
		long lval;
		int ival;
		if ((s = json.getValueString("id")) == null) throw new IOException("id not found");
		sess.setId(s);
		if ((lval = json.getValueAsInt64("createTime")) == 0) throw new IOException("createTime not found");
		sess.setCreateTime(Instant.ofEpochSecond(lval, json.getValueAsInt64("createTimeNano")));
		if ((lval = json.getValueAsInt64("accessTime")) == 0) throw new IOException("accessTime not found");
		sess.setLastAccessedTime(Instant.ofEpochSecond(lval, json.getValueAsInt64("accessTimeNano")));
		if ((ival = json.getValueAsInt32("cnt")) == 0) throw new IOException("cnt not found");
		sess.setCnt(ival);
		JSONObject attr = json.getValueObject("attributes");
		if (attr == null) throw new IOException("attributes not found");
		Set<String> names = attr.getObjectNames();
		Base64Enc b64 = new Base64Enc(B64Charset.NORMAL, true);
		Iterator<String> it = names.iterator();
		while (it.hasNext())
		{
			String name = it.next();
			String value = attr.getObjectString(name);
			if (value == null) throw new IOException("attribute["+name+"] not found");
			byte[] buff = b64.decodeBin(value);
			int len = ByteTool.readInt32(buff, 0);
			byte[] outBuff = new byte[len];
			Inflater inflater = new Inflater();
			inflater.setInput(buff, 4, buff.length - 4);
			inflater.inflate(outBuff);
			inflater.end();
			ByteArrayInputStream bais = new ByteArrayInputStream(outBuff);
			ObjectInputStream ois = new ObjectInputStream(bais);
			sess.setAttribute(name, ois.readObject());
		}
		sess.setUpdated(false);
		return sess;
	}
}
