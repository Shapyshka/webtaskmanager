package com.example.task.config;


import com.example.task.controllers.maincontroller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

@Configuration
@EnableWebSecurity
public class websecurityconfig extends WebSecurityConfigurerAdapter {

    private final String testlink = "api.ok654.org";


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                authorizeRequests()
                .antMatchers("/").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
      //          .logoutUrl("/logout")
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutSuccessHandler(new LogoutSuccessHandler() {

                    @Override
                    public void onLogoutSuccess(HttpServletRequest request,
                                                HttpServletResponse response, Authentication authentication)
                            throws IOException, ServletException {
                        URL url = new URL("https://"+testlink+"/logout");

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");

                        conn.setUseCaches(true);

                        conn.setRequestProperty("Cookie",
                                maincontroller.cocok);

                        conn.connect();
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                conn.getInputStream(),"utf-8"));
                        String inputLine;
                        StringBuffer response2 = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response2.append(inputLine);
                        }
                        in.close();
                        maincontroller.cocok="";
                        //response.sendRedirect(request.getContextPath());
                        response.sendRedirect("/oauth2/authorization/google");
                    }
                })
                .permitAll()
                .and()
                .csrf().disable()
                .cors().disable();
    }


}

