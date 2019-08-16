package readm.paper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

/**
 * @Author: Readm
 * @Date: 2019/8/16 18:05
 * @Version 1.0
 */


@Configuration
public class SecurityConfig   extends WebSecurityConfigurerAdapter {


    public void configure(WebSecurity web) throws Exception {

        web.ignoring().mvcMatchers("**/**");

    }



}
