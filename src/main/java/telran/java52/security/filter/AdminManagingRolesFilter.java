
package telran.java52.security.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.PassThroughSourceExtractor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import telran.java52.accounting.dao.UserAccountingRepository;
import telran.java52.accounting.dto.exeption.AccessDeniedException;
import telran.java52.accounting.model.Role;
import telran.java52.accounting.model.UserAccount;
import telran.java52.accounting.service.UserNotFoundException;
import telran.java52.security.model.User;

@Component
@RequiredArgsConstructor
@Order(20)
public class AdminManagingRolesFilter implements Filter {



	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		String method = request.getMethod();
		String path = request.getServletPath();

		if (checkEndpoint(method, path)) {

			User login = (User) request.getUserPrincipal();
		
									
				if (!isAdmin(login.getRoles())) {
					response.sendError(403);
					return;
				}

			
			
		}

					chain.doFilter(request, response);
	}

	private boolean checkEndpoint(String method, String path) {
		
		Pattern accountManagementPattern = Pattern
				.compile("(?i)^/account/user/[^/]+/role/(ADMINISTRATOR|MODERATOR|USER)$");

				return (HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method))
				&& accountManagementPattern.matcher(path).matches();
	}

	
	private boolean isAdmin(Set<String> roles) {
		return roles.contains(Role.ADMINISTRATOR.toString());
	}

}
