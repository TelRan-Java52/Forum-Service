package telran.java52.security.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;

import javax.management.loading.PrivateClassLoader;

import org.apache.catalina.connector.Response;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import telran.java52.accounting.dao.UserAccountingRepository;
import telran.java52.accounting.model.UserAccount;


@Component
@RequiredArgsConstructor
@Order(10)
public class AuthenticationFilter implements Filter {
	
	final UserAccountingRepository userAccountingRepository;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;// Преобразование общего ServletRequest в HttpServletRequest
		HttpServletResponse response = (HttpServletResponse) resp;// Преобразование общего ServletResponse в HttpServletResponse
		// Проверка, нужно ли применять фильтр для данного запроса
		if (checkEndpoint(request.getMethod(), request.getServletPath())) {
			try {
				// Извлечение учетных данных из заголовка Authorization
				String[] credentials = getCredentials(request.getHeader("Authorization"));
				UserAccount userAccount = userAccountingRepository.findById(credentials[0])// Поиск пользователя по логину, если не найден - выбрасывается исключение
						.orElseThrow(RuntimeException::new);
				// Проверка пароля пользователя с использованием BCrypt
				if (!BCrypt.checkpw(credentials[1], userAccount.getPassword())) {
					throw new RuntimeException();
				}
				request = new WrappedRequest(request, userAccount.getLogin());// Создание обертки для запроса с информацией о пользователе
			} catch (Exception e) {
				response.sendError(401);
				return;
			} 
		}
		chain.doFilter(request, response);// Передача запроса следующему фильтру в цепочке
	}

	private boolean checkEndpoint(String method, String path) {
		return !(HttpMethod.POST.matches(method) && path.matches("/account/register"));
	}// Проверка, если метод POST и путь /account/register, фильтр не применяется

	private String[] getCredentials(String header) {
		String token = header.split(" ")[1];// Извлечение токена из заголовка Authorization
		String decode = new String(Base64.getDecoder().decode(token));// Декодирование токена из Base64
		return decode.split(":");// Разделение декодированного токена на логин и пароль
	}
	
	// Класс обертка для HttpServletRequest
	private class WrappedRequest extends HttpServletRequestWrapper {
		private String login;

		public WrappedRequest(HttpServletRequest request, String login) {
			super(request);// Вызов конструктора родительского класса
			this.login = login;// Сохранение логина пользователя
		}

		@Override
		public Principal getUserPrincipal() {
			return () -> login;// Возврат Principal с логином пользователя
		}

	}

}