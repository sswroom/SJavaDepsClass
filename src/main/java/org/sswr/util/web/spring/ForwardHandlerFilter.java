package org.sswr.util.web.spring;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

public class ForwardHandlerFilter extends GenericFilterBean
{
	private String scheme;
	private String host;
	private Map<String, String> cliMap;

	public ForwardHandlerFilter()
	{
		this.scheme = null;
		this.host = null;
		this.cliMap = null;
	}

	public ForwardHandlerFilter(@Nullable String scheme, @Nullable String host)
	{
		this.scheme = scheme;
		this.host = host;
		this.cliMap = null;
	}

	public void addClientMap(@Nonnull String clientAddr, @Nonnull String serverUrl)
	{
		if (this.cliMap == null)
		{
			this.cliMap = new HashMap<String, String>();
		}
		this.cliMap.put(clientAddr, serverUrl);
	}

	@Override
	public void doFilter(@Nonnull ServletRequest request, @Nonnull ServletResponse response, @Nonnull FilterChain chain) throws IOException, ServletException
	{
		if (request instanceof HttpServletRequest)
		{
			ForwardHandlerRequest req = new ForwardHandlerRequest((HttpServletRequest)request);
			if (this.scheme != null)
			{
				req.setScheme(this.scheme);
			}
			if (this.host != null)
			{
				req.setHost(this.host);
			}
			if (this.cliMap != null)
			{
				String s = this.cliMap.get(request.getRemoteAddr());
				if (s != null)
				{
					req.setServerUrl(s);
				}
			}
			request = req;
		}
		if (response instanceof HttpServletResponse)
		{
			response = new ForwardHandlerResponse((HttpServletRequest)request, (HttpServletResponse)response);
		}
		chain.doFilter(request, response);
	}	
}
