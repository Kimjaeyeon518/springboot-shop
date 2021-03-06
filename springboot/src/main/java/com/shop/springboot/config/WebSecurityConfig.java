package com.shop.springboot.config;

import com.shop.springboot.handler.CustomLoginFailureHandler;
import com.shop.springboot.handler.CustomLoginSuccessHandler;
import com.shop.springboot.service.CustomUserDetailsService;
import com.shop.springboot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.sql.DataSource;

@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@EnableWebSecurity // Spring Security??? ?????????????????? ????????? ???????????????
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter { // WebSecurityConfigurerAdapter??? Spring Security??? ????????????????????? ????????? ?????? ?????? ???????????? ?????? ?????????

    private final CustomUserDetailsService customUserDetailsService;
    private final DataSource dataSource;

    @Override
    public void configure(WebSecurity web) { //  ????????? ????????? ???????????? ??????
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception { // http ?????? ?????? ??????
        http
                .csrf().disable()
                .authorizeRequests() // ????????? ?????? ?????? ????????? ??????
//                .antMatchers("/users**", "/carts**", "/profiles**", "/productOrders**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN") // USER, ADMIN??? ?????? ??????
//                .antMatchers("/admin**").hasAuthority("ROLE_ADMIN") // ADMIN??? ?????? ??????
//                .antMatchers(HttpMethod.POST, "/products").hasAuthority("ROLE_ADMIN")
//                .antMatchers(HttpMethod.PUT, "/products").hasAuthority("ROLE_ADMIN")
//                .antMatchers(HttpMethod.DELETE, "/products").hasAuthority("ROLE_ADMIN")
                .anyRequest().permitAll() // ????????? ???????????? ????????? ????????? ?????? ?????? ????????? ????????? ?????? ??????
                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                    .accessDeniedPage("/accessDenied")
                .and()
                    .formLogin() // ???????????? ?????? ??????
                    .loginProcessingUrl("/login")   // ????????? ????????? form action??? ?????? ??????
                    .successHandler(successHandler())
                    .failureHandler(failureHandler())
                .and()
                    .logout() // ??????????????? ?????? ??????
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/") // ???????????? ????????? ??????????????? ??????
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true) // ?????? ?????????
                .and()
                    .headers().frameOptions().disable()
                .and()
                    .csrf()
                    .ignoringAntMatchers("/h2-console/**")
                    .ignoringAntMatchers("/swagger-ui.html**")
                .and()
                    .sessionManagement()
                    .maximumSessions(1)
                    .expiredUrl("/duplicated-login")
                    .sessionRegistry(sessionRegistry());

        http.rememberMe()
                .key("remember-me")
                .userDetailsService(customUserDetailsService)
                .tokenRepository(getJDBCRepository())
                .tokenValiditySeconds(60*60*24);
    }

    private PersistentTokenRepository getJDBCRepository() {

        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);

        return repo;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {   // ???????????? ??? ????????? ????????? ???????????? ???
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return new CustomLoginSuccessHandler("/");
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() { return new CustomLoginFailureHandler(); }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }// Register HttpSessionEventPublisher

    @Bean
    public static ServletListenerRegistrationBean httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
    }
}