package telran.java52.security;




import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;



import lombok.RequiredArgsConstructor;
import telran.java52.accounting.model.Role;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguraition {

	final CastomWebSecurity webSecurity;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.httpBasic(Customizer.withDefaults());
		http.csrf(csrf -> csrf.disable());
		//включение кукки(кукки выключены по дефолту)
//		http.sessionManagement(session ->session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
		http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/account/register", "/forum/posts/**")
				.permitAll().requestMatchers("/account/user/{login}/role/{role}").hasRole(Role.ADMINISTRATOR.name())
				.requestMatchers(HttpMethod.PUT, "/account/user/{login}")
				.access(new WebExpressionAuthorizationManager("#login ==authentication.name"))
				.requestMatchers(HttpMethod.DELETE, "/account/user/{login}")
				.access(new WebExpressionAuthorizationManager(
						"#login ==authentication.name or hasRole('ADMINISTRATOR')"))
				.requestMatchers(HttpMethod.POST, "/forum/post/{author}")
				.access(new WebExpressionAuthorizationManager("#author ==authentication.name"))
				.requestMatchers(HttpMethod.PUT, "/forum/post/{id}/comment/{author}")
				.access(new WebExpressionAuthorizationManager("#author ==authentication.name"))
				.requestMatchers(HttpMethod.PUT, "/forum/post/{id}")
				.access((authentication, context) -> new AuthorizationDecision(
						webSecurity.checkPostAuthor(context.getVariables().get("id"), authentication.get().getName())))
				.requestMatchers(HttpMethod.DELETE, "/forum/post/{id}").access((authentication, context) -> {
					boolean checkAuthor = webSecurity.checkPostAuthor(context.getVariables().get("id"),
							authentication.get().getName());
					boolean checkModerator = context.getRequest().isUserInRole(Role.MODERATOR.name());
					return new AuthorizationDecision(checkAuthor || checkModerator);

				})

				.anyRequest().authenticated());

//		http.addFilter если нужен дополнительный фильтр
		return http.build();

	}
}
