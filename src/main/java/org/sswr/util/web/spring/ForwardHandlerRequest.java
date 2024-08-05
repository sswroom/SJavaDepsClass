package org.sswr.util.web.spring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import org.sswr.util.data.StringUtil;

public class ForwardHandlerRequest implements HttpServletRequest {
	private HttpServletRequest parent;
	private String fwdHost;
	private String fwdPort;
	private String fwdProto;
	private String fwdFor;
	
	public ForwardHandlerRequest(HttpServletRequest parent)
	{
		this.parent = parent;
		this.fwdHost = this.parent.getHeader("X-Forwarded-Host");
		this.fwdPort = this.parent.getHeader("X-Forwarded-Port");
		this.fwdProto = this.parent.getHeader("X-Forwarded-Proto");
		this.fwdFor = this.parent.getHeader("X-Forwarded-For");
		if (this.fwdHost == null)
		{
			this.fwdHost = null;
		}
	}

	public void setScheme(String scheme)
	{
		this.fwdProto = scheme;
	}

	public void setHost(String host)
	{
		this.fwdHost = host;
	}

	public void setServerUrl(String url)
	{
		int i;
		Integer iPort;
		if (url.startsWith("http://"))
		{
			this.fwdProto = "http";
			this.fwdHost = url.substring(7);
			i = this.fwdHost.lastIndexOf(':');
			iPort = null;
			if (i >= 0)
			{
				iPort = StringUtil.toInteger(this.fwdHost.substring(i + 1));
			}
			if (iPort == null)
			{
				this.fwdPort = "80";
			}
			else
			{
				this.fwdPort = iPort.toString();
			}
		}
		else if (url.startsWith("https://"))
		{
			this.fwdProto = "https";
			this.fwdHost = url.substring(8);
			i = this.fwdHost.lastIndexOf(':');
			iPort = null;
			if (i >= 0)
			{
				iPort = StringUtil.toInteger(this.fwdHost.substring(i + 1));
			}
			if (iPort == null)
			{
				this.fwdPort = "443";
			}
			else
			{
				this.fwdPort = iPort.toString();
			}
		}
	}

	@Override
	public Object getAttribute(String name) {
		return this.parent.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return this.parent.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding() {
		return this.parent.getCharacterEncoding();
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		this.parent.setCharacterEncoding(env);
	}

	@Override
	public int getContentLength() {
		return this.parent.getContentLength();
	}

	@Override
	public long getContentLengthLong() {
		return this.parent.getContentLengthLong();
	}

	@Override
	public String getContentType() {
		return this.parent.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return this.parent.getInputStream();
	}

	@Override
	public String getParameter(String name) {
		return this.parent.getParameter(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return this.parent.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		return this.parent.getParameterValues(name);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return this.parent.getParameterMap();
	}

	@Override
	public String getProtocol() {
		return this.parent.getProtocol();
	}

	@Override
	public String getScheme() {
		if (this.fwdProto != null)
		{
			return this.fwdProto;
		}
		return this.parent.getScheme();
	}

	@Override
	public String getServerName() {
		if (this.fwdHost != null)
		{
			int i = this.fwdHost.lastIndexOf(':');
			if (i >= 0)
			{
				return this.fwdHost.substring(0, i);
			}
			else
			{
				return this.fwdHost;
			}
		}
		return this.parent.getServerName();
	}

	@Override
	public int getServerPort() {
		if (this.fwdHost != null)
		{
			int i = this.fwdHost.lastIndexOf(':');
			if (i >= 0)
			{
				Integer iPort = StringUtil.toInteger(this.fwdHost.substring(i + 1));
				if (iPort != null)
				{
					return iPort.intValue();
				}
			}
		}
		if (this.fwdPort != null)
		{
			Integer iPort = StringUtil.toInteger(this.fwdPort);
			if (iPort != null)
			{
				return iPort.intValue();
			}
		}
		return this.parent.getServerPort();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return this.parent.getReader();
	}

	@Override
	public String getRemoteAddr() {
		if (this.fwdFor != null)
		{
			String forList[] = StringUtil.split(this.fwdFor, ",");
			return forList[0];
		}
		return this.parent.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		if (this.fwdFor != null)
		{
			return this.getRemoteAddr();
		}
		return this.parent.getRemoteHost();
	}

	@Override
	public void setAttribute(String name, Object o) {
		this.parent.setAttribute(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		this.parent.removeAttribute(name);
	}

	@Override
	public Locale getLocale() {
		return this.parent.getLocale();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return this.parent.getLocales();
	}

	@Override
	public boolean isSecure() {
		if (this.fwdProto != null)
		{
			return this.fwdProto.equals("https");
		}
		return this.parent.isSecure();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return this.parent.getRequestDispatcher(path);
	}

	@Override
	public int getRemotePort() {
		return this.parent.getRemotePort();
	}

	@Override
	public String getLocalName() {
		return this.parent.getLocalName();
	}

	@Override
	public String getLocalAddr() {
		return this.parent.getLocalAddr();
	}

	@Override
	public int getLocalPort() {
		return this.parent.getLocalPort();
	}

	@Override
	public ServletContext getServletContext() {
		return this.parent.getServletContext();
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return this.parent.startAsync();
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		return this.parent.startAsync(servletRequest, servletResponse);
	}

	@Override
	public boolean isAsyncStarted() {
		return this.parent.isAsyncStarted();
	}

	@Override
	public boolean isAsyncSupported() {
		return this.parent.isAsyncSupported();
	}

	@Override
	public AsyncContext getAsyncContext() {
		return this.parent.getAsyncContext();
	}

	@Override
	public DispatcherType getDispatcherType() {
		return this.parent.getDispatcherType();
	}

	@Override
	public String getAuthType() {
		return this.parent.getAuthType();
	}

	@Override
	public Cookie[] getCookies() {
		return this.parent.getCookies();
	}

	@Override
	public long getDateHeader(String name) {
		return this.parent.getDateHeader(name);
	}

	@Override
	public String getHeader(String name) {
		return this.parent.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return this.parent.getHeaders(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return this.parent.getHeaderNames();
	}

	@Override
	public int getIntHeader(String name) {
		return this.parent.getIntHeader(name);
	}

	@Override
	public String getMethod() {
		return this.parent.getMethod();
	}

	@Override
	public String getPathInfo() {
		return this.parent.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return this.parent.getPathTranslated();
	}

	@Override
	public String getContextPath() {
		return this.parent.getContextPath();
	}

	@Override
	public String getQueryString() {
		return this.parent.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		return this.parent.getRemoteUser();
	}

	@Override
	public boolean isUserInRole(String role) {
		return this.parent.isUserInRole(role);
	}

	@Override
	public Principal getUserPrincipal() {
		return this.parent.getUserPrincipal();
	}

	@Override
	public String getRequestedSessionId() {
		return this.parent.getRequestedSessionId();
	}

	@Override
	public String getRequestURI() {
		return this.parent.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return this.parent.getRequestURL();
	}

	@Override
	public String getServletPath() {
		return this.parent.getServletPath();
	}

	@Override
	public HttpSession getSession(boolean create) {
		return this.parent.getSession(create);
	}

	@Override
	public HttpSession getSession() {
		return this.parent.getSession();
	}

	@Override
	public String changeSessionId() {
		return this.parent.changeSessionId();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return this.parent.isRequestedSessionIdValid();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return this.parent.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return this.parent.isRequestedSessionIdFromURL();
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return this.parent.authenticate(response);
	}

	@Override
	public void login(String username, String password) throws ServletException {
		this.parent.login(username, password);
	}

	@Override
	public void logout() throws ServletException {
		this.parent.logout();
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return this.parent.getParts();
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		return this.parent.getPart(name);
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		return this.parent.upgrade(handlerClass);
	}

	@Override
	public String getProtocolRequestId() {
		return this.parent.getProtocolRequestId();
	}

	@Override
	public String getRequestId() {
		return this.parent.getRequestId();
	}

	@Override
	public ServletConnection getServletConnection() {
		return this.parent.getServletConnection();
	}
}
