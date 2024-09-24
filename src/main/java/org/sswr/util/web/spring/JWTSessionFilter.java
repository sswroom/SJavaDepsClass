package org.sswr.util.web.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.sswr.util.basic.ThreadVar;
import org.sswr.util.web.HttpUtil;
import org.sswr.util.web.JWTSession;
import org.sswr.util.web.JWTSessionManager;

public class JWTSessionFilter extends GenericFilterBean
{
	private List<String> ignorePaths;
	private JWTSessionManager sessMgr;
	public JWTSessionFilter(@Nonnull JWTSessionManager sessMgr)
	{
		super();
		this.sessMgr = sessMgr;
		this.ignorePaths = new ArrayList<String>();
	}

	public void ignorePath(@Nonnull String path)
	{
		this.ignorePaths.add(path);
	}

	@Nullable
	public JWTSession getSession(@Nonnull ServletRequest request)
	{
		if (request instanceof HttpServletRequest)
		{
			HttpServletRequest req = (HttpServletRequest)request;
			String path = req.getRequestURI();
			int i = this.ignorePaths.size();
			while (i-- > 0)
			{
				if (path.startsWith(this.ignorePaths.get(i)))
				{
					return null;
				}
			}
			JWTSession sess = null;
			String tokenHdr = req.getHeader("X-Token");
			if (tokenHdr != null)
			{
				sess = this.sessMgr.getSession(tokenHdr);
			}
			if (sess == null)
			{
				Map<String, Object> params = HttpUtil.parseParams(req, null);
				Object token;
				token = params.get("Token");
				if (token == null)
				{
					token = params.get("token");
				}
				if (token != null)
				{
					sess = this.sessMgr.getSession(token.toString());
				}
			}
			return sess;
		}
		return null;	
	}

	@Override
	public void doFilter(@Nonnull ServletRequest request, @Nonnull ServletResponse response, @Nonnull FilterChain chain) throws IOException, ServletException
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
