package com.daeng.nyang.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;


import com.daeng.nyang.jwt.JwtAuthenticationEntryPoint;
import com.daeng.nyang.jwt.JwtRequestFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
   
   @Autowired
    private JwtRequestFilter jwtRequestFilter;

   @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
   
   final String corsOrigin = "*";

   @Override
   protected void configure(HttpSecurity http) throws Exception {
      http
      .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class).	
      // security에서 제공하는 UsernamePasswordAuthenticationFilter 이전에 내가 만든 jwtRequestFilter 먼저 체인에 걸어둠
      httpBasic().disable().   // 기본적으로 제공되는 페이지 비활성화
      csrf().disable(). // 요청위조 방지 비활성화
      cors().and().	// 아래 만든 CorsConfigurationSource로 cors 이용
      formLogin().disable(). // security에서 제공하는 formLogin 비활성화
      sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS). // 시큐리티의 세션 생성 X 사용 X
      and().authorizeRequests().	// 요청 시
         requestMatchers(CorsUtils::isPreFlightRequest).permitAll(). // CORS 허가 후
         antMatchers("/newuser/login","/newuser/signup").anonymous().// /newuser/login, /newuser/signup 으로 시작하는 url 요청 시 인증절차 없이 컨트롤러 진입
         antMatchers("/user").hasAnyRole("ADMIN","USER"). // /user 로 시작하는 url 요청 시 ADMIN, USER 중 하나의 권한을 가지고 있어야 컨트롤러 진입 가능 
         antMatchers("/admin").hasRole("ADMIN"). // /admin 으로 시작한느 url 요청 시 ADMIN 권한을 가진 자만이 컨트롤러 진입 가능
         antMatchers("/newuser/**").permitAll(). // /newuser/ 로 시작하는 모든 요청은 인증이 안되도 컨트롤러 입장 가능
//         antMatchers("/newuser").permitAll().
      and().authorizeRequests(). // 그 외
         anyRequest(). // 어떤 요청이라도
         authenticated(). // 인증된 사용자만이 접근 허용
      and().exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint); // exception 발생 시 내가 만든 jwtAuthenticationEntryPoint로 exception 전달하여 처리
      
   }
   
   
   public CorsConfigurationSource corsConfigurationSource(String corsOrigin) {
	    CorsConfiguration configuration = new CorsConfiguration();
	    configuration.addAllowedOrigin("*");
//	    configuration.setAllowedOrigins(Arrays.asList(corsOrigin));
	    configuration.setAllowedMethods(Arrays.asList("GET","POST","HEAD","OPTIONS","PUT","PATCH","DELETE"));
	    configuration.setMaxAge(10L);
	    configuration.setAllowCredentials(true);
	    configuration.setAllowedHeaders(Arrays.asList("Accept","Access-Control-Request-Method","Access-Control-Request-Headers",
	      "Accept-Language","Authorization","Content-Type","Request-Name","Request-Surname","Origin","X-Request-AppVersion",
	      "X-Request-OsVersion", "X-Request-Device", "X-Requested-With"));
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", configuration);
	    
	    
	    
		/*
		 * configuration.addAllowedOrigin("*"); configuration.addAllowedMethod("*");
		 * configuration.addAllowedHeader("*"); UrlBasedCorsConfigurationSource source =
		 * new UrlBasedCorsConfigurationSource();
		 * source.registerCorsConfiguration("/**", configuration);
		 */
	    return source;
	}
   


//   @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        // configure AuthenticationManager so that it knows from where to load
//        // user for matching credentials
//        // Use BCryptPasswordEncoder
//        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
//    }

   @Bean
   @Override
   public AuthenticationManager authenticationManagerBean() throws Exception {
      return super.authenticationManagerBean();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }

   @Override
   public void configure(WebSecurity web) throws Exception {
      
   }
   
//   @Bean
//   public MultipartResolver multipartResolver() {
//       CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
//       multipartResolver.setMaxUploadSize(2000000000);
//       return multipartResolver;
//   }
}