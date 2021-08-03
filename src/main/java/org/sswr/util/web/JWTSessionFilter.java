package org.sswr.util.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.sswr.util.basic.ThreadVar;


public class JWTSessionFilter extends GenericFilterBean
{
	private JWTSessionManager sessMgr;
	public JWTSessionFilter(JWTSessionManager sessMgr)
	{
		super();
		this.sessMgr = sessMgr;
	}

	public JWTSession getSession(ServletRequest request)
	{
		if (request instanceof HttpServletRequest)
		{
			HttpServletRequest req = (HttpServletRequest)request;
			JWTSession sess = null;
			String tokenHdr = req.getHeader("X-Token");
			if (tokenHdr != null)
			{
				sess = this.sessMgr.getSession(tokenHdr);
			}
			if (sess == null)
			{
				Map<String, Object> params = HttpUtil.parseParams(req, null);
				Object token;;
				if ((token = params.get("Token")) != null || (token = params.get("token")) != null)
				{
					sess = this.sessMgr.getSession(token.toString());
				}
			}
			return sess;
		}
		return null;	
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		String userName = null;
		if (authentication == null || !authentication.getClass().equals(JWTSessionAuthentication.class))
		{
			JWTSession sess = getSession(request);
			if (sess != null)
			{
				userName = sess.getUserName();
				securityContext.setAuthentication(new JWTSessionAuthentication(sess));
			}
		}
		ThreadVar.set("User", userName);
		chain.doFilter(request, response);
	}
}
