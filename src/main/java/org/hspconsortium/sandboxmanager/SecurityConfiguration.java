//package org.hspconsortium.sandboxmanager;
//
//import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.web.bind.annotation.RestController;
//
//@EnableWebSecurity
//@Configuration
//@EnableOAuth2Sso
//@RestController
//class SecurityConfiguration extends WebSecurityConfigurerAdapter {
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
////                .httpBasic()
////                .and()
//                .authorizeRequests()
//                .antMatchers(HttpMethod.POST, "/launchScenario").hasRole("ADMIN")
//                .antMatchers(HttpMethod.PUT, "/launchScenario").hasRole("ADMIN")
//                .antMatchers(HttpMethod.DELETE, "/launchScenario").hasRole("ADMIN")
//                .antMatchers(HttpMethod.GET, "/launchScenarios/**").hasRole("ADMIN").and()
//                .csrf().disable();
////        http.authorizeRequests().anyRequest().fullyAuthenticated().and().
////                httpBasic().and().
//
////                csrf().disable();
//    }
//
//}

