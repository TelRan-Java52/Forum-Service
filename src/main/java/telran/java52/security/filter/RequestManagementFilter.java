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
import telran.java52.accounting.model.Role;
import telran.java52.accounting.model.UserAccount;
import telran.java52.accounting.service.UserNotFoundException;

@Component
@RequiredArgsConstructor
@Order(30)
public class RequestManagementFilter implements Filter{
	
	final UserAccountingRepository userAccountingRepository;
	
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		
		    HttpServletRequest request = (HttpServletRequest) req;
	        HttpServletResponse response = (HttpServletResponse) resp;

	        // Получение информации о методе запроса и пути
	        String method = request.getMethod();
	        String path = request.getServletPath();

	        // Проверка, соответствует ли путь шаблону управления аккаунтами
	        if (checkEndpoint(method, path)) {
	            try {
	                // Извлечение имени пользователя из запроса
	                String username = request.getUserPrincipal().getName();

	                // Получение пользователя из базы данных по имени пользователя
	                UserAccount user = userAccountingRepository.findById(username)
	                        .orElseThrow(UserNotFoundException::new);

	                // Проверка, имеет ли пользователь права на выполнение операции
	                if (!isAdmin(user.getRoles())) {
	                    // Если пользователь не является администратором, возвращаем ошибку доступа
	                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
	                    return;
	                }

	            } catch (UserNotFoundException e) {
	                // Если пользователь не найден, возвращаем ошибку
	                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
	                return;
	            } catch (Exception e) {
	                // В случае других ошибок возвращаем ошибку сервера
	                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
	                return;
	            }
	        }

	        // Передача запроса следующему фильтру в цепочке
	        chain.doFilter(request, response);
	    }

	    private boolean checkEndpoint(String method, String path) {
	    	// Паттерн для проверки пути на соответствие шаблону управления аккаунтами
	        Pattern accountManagementPattern = Pattern.compile("^/account/user/[^/]+$");

	        // Проверка метода запроса (PUT или DELETE) и соответствия пути шаблону управления аккаунтами
	        return (HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method))
	                && accountManagementPattern.matcher(path).matches();
	}

		// Проверка, является ли пользователь администратором
	    private boolean isAdmin(Set<Role> roles) {
	        return roles.contains(Role.ADMINISTRATOR);
	    }}