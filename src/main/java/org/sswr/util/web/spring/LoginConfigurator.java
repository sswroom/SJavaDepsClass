package org.sswr.util.web.spring;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface LoginConfigurator
{
	public void configure(HttpSecurity http) throws Exception;
}
