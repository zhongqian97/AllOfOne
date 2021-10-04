package ink.mastermind.AllINOne.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import ink.mastermind.AllINOne.pojo.Json;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.service.UserService;

/**
 * 自定义Spring Security认证处理类的时候
 * 我们需要继承自WebSecurityConfigurerAdapter来完成，相关配置重写对应 方法即可。 
 * */
@Configuration
public class AppSecurityConfigurer extends WebSecurityConfigurerAdapter{

	// 依赖注入用户服务类
	@Autowired
    private UserService userService;
	
	// 依赖注入加密接口
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	// 依赖注入用户认证接口
	@Autowired
    private AuthenticationProvider authenticationProvider;
	
	// 依赖注入认证处理成功类，验证用户成功后处理不同用户跳转到不同的页面
	@Autowired
	AppAuthenticationSuccessHandler appAuthenticationSuccessHandler;
	
	/*
	 *  BCryptPasswordEncoder是Spring Security提供的PasswordEncoder接口是实现类
	 *  用来创建密码的加密程序，避免明文存储密码到数据库
	 */
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	 
	// DaoAuthenticationProvider是Spring Security提供AuthenticationProvider的实现
	@Bean
    public AuthenticationProvider authenticationProvider() {
		System.out.println("AuthenticationProvider authenticationProvider");
		// 创建DaoAuthenticationProvider对象
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // 不要隐藏"用户未找到"的异常
        provider.setHideUserNotFoundExceptions(false);
        // 通过重写configure方法添加自定义的认证方式。
        provider.setUserDetailsService(userService);
        // 设置密码加密程序认证
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
	@Bean
	CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
	    CustomAuthenticationFilter filter = new CustomAuthenticationFilter();
	    filter.setAuthenticationSuccessHandler(new AuthenticationSuccessHandler() {
	        @Override
	        public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication authentication) throws IOException, ServletException {
	            resp.setContentType("application/json;charset=utf-8");
	            PrintWriter out = resp.getWriter();
	            User user = userService.getUserByUsername(authentication.getName());
	            user.setTime(new Long(new Date().getTime()));
	            userService.save(user);
	            Json json = Json.getJson().setAndPush(200, "登录成功",
	            		user.getRole());
	            out.write(new ObjectMapper().writeValueAsString(json));
	            out.flush();
	            out.close();
	        }
	    });
	    filter.setAuthenticationFailureHandler(new AuthenticationFailureHandler() {
	        @Override
	        public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse resp, AuthenticationException e) throws IOException, ServletException {
	            resp.setContentType("application/json;charset=utf-8");
	            PrintWriter out = resp.getWriter();
	            Json json = Json.getJson().setAndPush(500, "登录失败", null);
	            out.write(new ObjectMapper().writeValueAsString(json));
	            out.flush();
	            out.close();
	        }
	    });
	    filter.setAuthenticationManager(authenticationManagerBean());
	    return filter;
	}

	
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	System.out.println("AppSecurityConfigurer configure auth......");
    	// 设置认证方式。
    	auth.authenticationProvider(authenticationProvider);
    }

    /**
     * 设置了登录页面，而且登录页面任何人都可以访问，然后设置了登录失败地址，也设置了注销请求，注销请求也是任何人都可以访问的。 
     * permitAll表示该请求任何人都可以访问，.anyRequest().authenticated(),表示其他的请求都必须要有权限认证。
     * */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	System.out.println("AppSecurityConfigurer configure http......");
    	http.csrf().disable();
    	http.headers().frameOptions().sameOrigin();
    	http.authorizeRequests()
    	// spring-security 5.0 之后需要过滤静态资源
    	.antMatchers("/register.html","/register",
    			"/index.html","/login","/",
    			"/findPassword.html","/findPassword","/checkEmail",
    			"/json/**","/layui/**","/images/**","/css/**","/js/**").permitAll() 
	  	.antMatchers("/page/**").hasAnyRole("USER","ADMIN")
	  	.antMatchers("/admin/**").hasAnyRole("ADMIN")
	  	.anyRequest().authenticated()
	  	.and()
	  	.formLogin().loginPage("/index.html").successHandler(appAuthenticationSuccessHandler)
	  	.usernameParameter("userName").passwordParameter("password")
	  	.and()
	  	.logout().permitAll()
	  	.and()
	  	.exceptionHandling().accessDeniedPage("/index.html");
    	http.addFilterAt(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    }
}
