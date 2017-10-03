package org.dlearn.helsinki.skeleton;

import javax.servlet.http.HttpServletRequest;
import org.dlearn.helsinki.skeleton.database.Database;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    private final Database db = new Database();

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
            .dataSource(db)
            .usersByUsernameQuery("select * ("
                        + "select username, password, true from students"
                        + "union"
                        + "select username, password, true from teachers"
                    + ") where username=?")
            .authoritiesByUsernameQuery("select * ("
                        + "select username, 'ROLE_STUDENT' from students"
                        + "union"
                        + "select username, 'ROLE_TEACHER' from teachers"
                    + ") where username=?")
            .passwordEncoder(new BCryptPasswordEncoder(16))
            .and()
            .inMemoryAuthentication()
            .withUser("teacher").password("password").roles("TEACHER")
            .and()
            .withUser("student").password("password").roles("STUDENT");
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().configurationSource((HttpServletRequest request) -> {
                CorsConfiguration cors = new CorsConfiguration();
                cors.applyPermitDefaultValues();
                return cors;
            })
            .and()
            .authorizeRequests()
            .antMatchers("/webapi").authenticated()
            .antMatchers("/webapi/students/**").hasRole("TEACHER")
            .antMatchers("/webapi/teachers/**").hasRole("TEACHER")
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}