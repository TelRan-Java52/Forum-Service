package telran.java52.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

import telran.java52.accounting.model.Role;

@Configuration
@EnableWebSecurity
public class SecurityConfiguraition {

	@Bean
	 SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.httpBasic(Customizer.withDefaults());
		http.csrf(csrf -> csrf.disable());
		http.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/account/register","/forum/posts/**")
				  .permitAll()
				.requestMatchers("/account/user/{login}/role/{role}")
				   .hasRole(Role.ADMINISTRATOR.name())
				.requestMatchers(HttpMethod.PUT,"/account/user/{login}")
				     .access(new WebExpressionAuthorizationManager("#login ==authentication.name"))
				.requestMatchers(HttpMethod.DELETE,"/account/user/{login}")
				   .access(new WebExpressionAuthorizationManager("#login ==authentication.name or hasRole('ADMINISTRATOR')"))
				.anyRequest().authenticated());
//		http.addFilter если нужен дополнительный фильтр
		return http.build();
	 }
}
