package org.sswr.util.web.spring;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class LogHttpResponse implements HttpServletResponse {

	private HttpServletResponse parent;

	public LogHttpResponse(HttpServletResponse parent)
	{
		this.parent = parent;
	}

	@Override
	public String getCharacterEncoding() {
		return this.parent.getCharacterEncoding();
	}

	@Override
	public String getContentType() {
		return this.parent.getContentType();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return this.parent.getOutputStream();
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return this.parent.getWriter();
	}

	@Override
	public void setCharacterEncoding(String charset) {
		this.parent.setCharacterEncoding(charset);
	}

	@Override
	public void setContentLength(int len) {
		this.parent.setContentLength(len);
	}

	@Override
	public void setContentLengthLong(long len) {
		this.parent.setContentLengthLong(len);
	}

	@Override
	public void setContentType(String type) {
		this.parent.setContentType(type);
	}

	@Override
	public void setBufferSize(int size) {
		this.parent.setBufferSize(size);
	}

	@Override
	public int getBufferSize() {
		return this.parent.getBufferSize();
	}

	@Override
	public void flushBuffer() throws IOException {
		this.parent.flushBuffer();
	}

	@Override
	public void resetBuffer() {
		this.parent.resetBuffer();
	}

	@Override
	public boolean isCommitted() {
		return this.parent.isCommitted();
	}

	@Override
	public void reset() {
		this.parent.reset();
	}

	@Override
	public void setLocale(Locale loc) {
		this.parent.setLocale(loc);
	}

	@Override
	public Locale getLocale() {
		return this.parent.getLocale();
	}

	@Override
	public void addCookie(Cookie cookie) {
		this.parent.addCookie(cookie);
	}

	@Override
	public boolean containsHeader(String name) {
		return this.parent.containsHeader(name);
	}

	@Override
	public String encodeURL(String url) {
		return this.parent.encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(String url) {
		return this.parent.encodeRedirectURL(url);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		this.parent.sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		this.parent.sendError(sc);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		if (location.startsWith("https://"))
		{
			System.out.println(location);
			this.parent.sendRedirect(location);
		}
		else
		{
			this.parent.sendRedirect(location);
		}
	}

	@Override
	public void setDateHeader(String name, long date) {
		this.parent.setDateHeader(name, date);
	}

	@Override
	public void addDateHeader(String name, long date) {
		this.parent.addDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value) {
		if (name.equalsIgnoreCase("Location"))
		{
			this.parent.setHeader(name, value);
		}
		else
		{
			this.parent.setHeader(name, value);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		if (name.equalsIgnoreCase("Location"))
		{
			this.parent.addHeader(name, value);
		}
		else
		{
			this.parent.addHeader(name, value);
		}
	}

	@Override
	public void setIntHeader(String name, int value) {
		this.parent.setIntHeader(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		this.parent.addIntHeader(name, value);
	}

	@Override
	public void setStatus(int sc) {
		this.parent.setStatus(sc);
	}

	@Override
	public int getStatus() {
		return this.parent.getStatus();
	}

	@Override
	public String getHeader(String name) {
		return this.parent.getHeader(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return this.parent.getHeaders(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return this.parent.getHeaderNames();
	}

	@Override
	public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {
		if (location.startsWith("https://"))
		{
			System.out.println(location);
			this.parent.sendRedirect(location, sc, clearBuffer);
		}
		else
		{
			this.parent.sendRedirect(location, sc, clearBuffer);
		}
	}
}
