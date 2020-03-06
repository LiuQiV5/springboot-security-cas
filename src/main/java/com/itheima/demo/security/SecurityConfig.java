package com.itheima.demo.security;

import com.itheima.demo.properties.CasProperties;
import lombok.AllArgsConstructor;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import com.itheima.demo.custom.CustomUserDetailsService;

@Configuration
@EnableWebSecurity //启用web权限
@EnableGlobalMethodSecurity(prePostEnabled = true) //启用方法验证
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final CasProperties casProperties;

	private final CustomUserDetailsService customUserDetailsService;
	/**定义认证用户信息获取来源，密码校验规则等*/
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		super.configure(auth);
		auth.authenticationProvider(casAuthenticationProvider());
	}
	
	/**定义安全策略*/
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()//配置安全策略
			//.antMatchers("/","/hello").permitAll()//定义/请求不需要验证
			.anyRequest().authenticated()//其余的所有请求都需要验证
			.and()
		.logout()
			.permitAll()//定义logout不需要验证
			.and()
		.formLogin();//使用form表单登录

		/**
		 * 执行顺序为
		 * LogoutFilter-->SingleSignOutFilter-->CasAuthenticationFilter-->ExceptionTranslationFilter
		 */
		http
                .exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint())
			    .and()
			    .addFilter(casAuthenticationFilter())
			    .addFilterBefore(casLogoutFilter(), LogoutFilter.class)
			    .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);
		http.csrf().disable();
	}
	
	/**  认证的入口，即跳转至服务端的cas地址
	 *   浏览器访问不可直接填客户端的login请求,若如此则会返回Error页面，无法被此入口拦截
	 * */
	@Bean
	public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
		CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
		casAuthenticationEntryPoint.setLoginUrl(casProperties.getCasServerLoginUrl());
		casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
		return casAuthenticationEntryPoint;
	}
	
	/**指定service相关信息*/
	@Bean
	public ServiceProperties serviceProperties() {
		ServiceProperties serviceProperties = new ServiceProperties();
		// 设置回调的service路径
		serviceProperties.setService(casProperties.getAppServerUrl() + casProperties.getAppLoginUrl());
		// 对所有的未拥有ticket的访问均需要验证
		serviceProperties.setAuthenticateAllArtifacts(true);
		return serviceProperties;
	}
	
	/**CAS认证过滤器*/
	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		casAuthenticationFilter.setFilterProcessesUrl(casProperties.getAppLoginUrl());
		return casAuthenticationFilter;
	}


	/**    cas 认证 Provider
	 *    TicketValidator、AuthenticationUserDetailService属性必须设置;
	 *    serviceProperties属性主要应用于ticketValidator用于去cas服务端检验ticket
	 * */
	@Bean
	public CasAuthenticationProvider casAuthenticationProvider() {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setAuthenticationUserDetailsService(customUserDetailsService);
		//应用于ticketValidator用于去cas服务端检验ticket
		casAuthenticationProvider.setServiceProperties(serviceProperties());
		casAuthenticationProvider.setTicketValidator(cas30ServiceTicketValidator());
		casAuthenticationProvider.setKey("casAuthenticationProviderKey");
		return casAuthenticationProvider;
	}

	/**
	 *  配置ticket校验器
	 */
	@Bean
	public Cas30ServiceTicketValidator cas30ServiceTicketValidator() {
		//配置上服务端的校验ticket地址
		return new Cas30ServiceTicketValidator(casProperties.getCasServerUrl());
	}
	
	/** 单点登出过滤器
	 *  单点注销，接受cas服务端发出的注销session请求
	 */
	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
		SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
		// 设置cas服务端路径前缀，应用于front channel的注销请求
		singleSignOutFilter.setCasServerUrlPrefix(casProperties.getCasServerUrl());
		singleSignOutFilter.setIgnoreInitConfiguration(true);
		return singleSignOutFilter;
	}
	
	/** 请求单点退出过滤器
	 *  请求/logout，转发至cas服务端进行注销
	 * */
	@Bean
	public LogoutFilter casLogoutFilter() {
		LogoutFilter logoutFilter = new LogoutFilter(casProperties.getCasServerLogoutUrl(), new SecurityContextLogoutHandler());
		logoutFilter.setFilterProcessesUrl(casProperties.getAppLogoutUrl());
		return logoutFilter;
	}
}
