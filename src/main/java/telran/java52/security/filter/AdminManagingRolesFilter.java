package telran.java52.security.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import telran.java52.accounting.dao.UserAccountingRepository;
import telran.java52.accounting.model.Role;
import telran.java52.accounting.model.UserAccount;
import telran.java52.accounting.service.UserNotFoundException;

@Component
@Order(20)
public class AdminManagingRolesFilter implements Filter {
	
	final UserAccountingRepository userAccountingRepository;
	 
	@Autowired
	public AdminManagingRolesFilter(UserAccountingRepository userAccountingRepository) {
		super();
		this.userAccountingRepository = userAccountingRepository;
	}
	
    @Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // Получаем имя пользователя из запроса
        String login = request.getUserPrincipal().getName();

        // Пытаемся найти пользователя в базе данных
        UserAccount user = null;
        try {
            user = userAccountingRepository.findById(login)
                    .orElseThrow(UserNotFoundException::new);
        } catch (UserNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        // Проверяем роли пользователя
        Set<Role> roles = user.getRoles();
        if (!isAdmin(roles)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
            return;
        }

        // Если пользователь администратор, продолжаем выполнение цепочки фильтров
        chain.doFilter(request, response);
    }

    // Проверка, является ли пользователь администратором
    private boolean isAdmin(Set<Role> roles) {
        return roles.contains(Role.ADMINISTRATOR);
    }

	}


