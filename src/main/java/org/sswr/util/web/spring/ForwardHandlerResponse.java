package org.sswr.util.web.spring;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForwardHandlerResponse implements HttpServletResponse {

	private HttpServletRequest req;
	private HttpServletResponse resp;

	public ForwardHandlerResponse(HttpServletRequest req, HttpServletResponse resp)
	{
		this.req = req;
		this.resp = resp;
	}

	private String filterLocation(String location)
	{
//		System.out.println("Redirect location: "+location);
		if (location.startsWith("https://"))
		{
			return location;
		}
		else if (location.startsWith("http://"))
		{
			return location;
		}
		else if (location.startsWith("/") || location.equals(""))
		{
			String scheme = this.req.getScheme();
			String url = this.req.getScheme()+"://"+this.req.getServerName();
			int port = this.req.getServerPort();
			if (port == 443 && scheme.equals("https"))
			{

			}
			else if (port == 80 && scheme.equals("http"))
			{

			}
			else
			{
				url = url + ":" + port;
			}
			return url + location;
		}
		else
		{
			return location;
		}
	}

	@Override
	public String getCharacterEncoding() {
		return this.resp.getCharacterEncoding();
	}

	@Override
	public String getContentType() {
		return this.resp.getContentType();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return this.resp.getOutputStream();
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return this.resp.getWriter();
	}

	@Override
	public void setCharacterEncoding(String charset) {
		this.resp.setCharacterEncoding(charset);
	}

	@Override
	public void setContentLength(int len) {
		this.resp.setContentLength(len);
	}

	@Override
	public void setContentLengthLong(long len) {
		this.resp.setContentLengthLong(len);
	}

	@Override
	public void setContentType(String type) {
		this.resp.setContentType(type);
	}

	@Override
	public void setBufferSize(int size) {
		this.resp.setBufferSize(size);
	}

	@Override
	public int getBufferSize() {
		return this.resp.getBufferSize();
	}

	@Override
	public void flushBuffer() throws IOException {
		this.resp.flushBuffer();
	}

	@Override
	public void resetBuffer() {
		this.resp.resetBuffer();
	}

	@Override
	public boolean isCommitted() {
		return this.resp.isCommitted();
	}

	@Override
	public void reset() {
		this.resp.reset();
	}

	@Override
	public void setLocale(Locale loc) {
		this.resp.setLocale(loc);
	}

	@Override
	public Locale getLocale() {
		return this.resp.getLocale();
	}

	@Override
	public void addCookie(Cookie cookie) {
		this.resp.addCookie(cookie);
	}

	@Override
	public boolean containsHeader(String name) {
		return this.resp.containsHeader(name);
	}

	@Override
	public String encodeURL(String url) {
		return this.resp.encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(String url) {
		return this.resp.encodeRedirectURL(url);
	}

	@Override
	@SuppressWarnings("deprecation")
	public String encodeUrl(String url) {
		return this.resp.encodeUrl(url);
	}

	@Override
	@SuppressWarnings("deprecation")
	public String encodeRedirectUrl(String url) {
		return this.resp.encodeRedirectUrl(url);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		this.resp.sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		this.resp.sendError(sc);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		this.resp.sendRedirect(filterLocation(location));
	}

	@Override
	public void setDateHeader(String name, long date) {
		this.resp.setDateHeader(name, date);
	}

	@Override
	public void addDateHeader(String name, long date) {
		this.resp.addDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value) {
		if (name.equalsIgnoreCase("Location"))
		{
			this.resp.setHeader(name, filterLocation(value));
		}
		else
		{
			this.resp.setHeader(name, value);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		if (name.equalsIgnoreCase("Location"))
		{
			this.resp.addHeader(name, filterLocation(value));
		}
		else
		{
			this.resp.addHeader(name, value);
		}
	}

	@Override
	public void setIntHeader(String name, int value) {
		this.resp.setIntHeader(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		this.resp.addIntHeader(name, value);
	}

	@Override
	public void setStatus(int sc) {
		this.resp.setStatus(sc);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setStatus(int sc, String sm) {
		this.resp.setStatus(sc, sm);
	}

	@Override
	public int getStatus() {
		return this.resp.getStatus();
	}

	@Override
	public String getHeader(String name) {
		return this.resp.getHeader(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return this.resp.getHeaders(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return this.resp.getHeaderNames();
	}
}
