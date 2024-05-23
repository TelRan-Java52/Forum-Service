package telran.java52.security.filter;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

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

@Component
@RequiredArgsConstructor
@Order(30)
public class RequestManagementFilter implements Filter {

	final UserAccountingRepository userAccountingRepository;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		String method = request.getMethod();
		String path = request.getServletPath();
		
		if (checkEndpoint(method, path)) {
			try {

				String userName = path.substring(path.lastIndexOf('/') + 1);
				String login = request.getUserPrincipal().getName();

				UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserNotFoundException::new);

				if (HttpMethod.PUT.matches(method) && !isOwner(login, userName)) {
					throw new AccessDeniedException();
				}
				if (HttpMethod.DELETE.matches(method)
						&& !(isOwner(login, userName) || isAdmin(user.getRoles()))) {
					throw new AccessDeniedException();
				}
			} catch (UserNotFoundException e) {
				
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
				return;
			} catch (AccessDeniedException e) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
				return;

			} catch (Exception e) {
			
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
				return;
			}
		}

		
		chain.doFilter(request, response);
	}

	private boolean checkEndpoint(String method, String path) {

		Pattern accountManagementPattern = Pattern.compile("^/account/user/[^/]+$");

	
		return (HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method))
				&& accountManagementPattern.matcher(path).matches();
	}

	private boolean isOwner(String login, String userName) {
		return login.equalsIgnoreCase(userName);
	}

	private boolean isAdmin(Set<Role> roles) {
		return roles.contains(Role.ADMINISTRATOR);
	}
}