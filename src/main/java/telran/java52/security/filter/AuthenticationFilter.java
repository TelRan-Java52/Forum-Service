package telran.java52.security.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;

import javax.management.loading.PrivateClassLoader;

import org.apache.catalina.connector.Response;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.core.annotation.Order;
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

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

//		System.out.println("request:");
//		System.out.println("Path "+request.getServletPath());
//		System.out.println("Method "+request.getMethod());
//		
//		System.out.println("дотуп к заголовкам");
//		System.out.println("Authorization: "+request.getHeader("Authorization"));

		if (checkEndpoint(request.getMethod(),request.getServletPath())) {
			try {
				String[] credential = getCredentials(request.getHeader("Authorization"));
				// пароль который при входе на сайт прилетел
				// System.out.println("login " + credential[0] + ", password " + credential[1]);

				UserAccount userAccount = userAccountingRepository.findById(credential[0])
						.orElseThrow(RuntimeException::new);
				// найти пользователя в базе для проверки пароля

				if (!BCrypt.checkpw(credential[1], userAccount.getPassword())) {
					// если не совпадают пароли то дальше не пускаем
					throw new RuntimeException(); // бросаем ошибку
				}
				request = new WrappedRequest(request, userAccount.getLogin());
				// сделать нормальный Principal с логином и паролем
			} catch (Exception e) {
				response.sendError(401); // все возможные ошибки ловим тут и пробрасываем 401-ю "Unauthorized"
				return; // и выходим
			} 
		}
		chain.doFilter(request, response); // если все было ок то пробросить данные дальше
	}

	private boolean checkEndpoint(String method, String servletPath) {
		//to do
		return false;
	}

	private String[] getCredentials(String header) {
		String token = header.split(" ")[1]; // закодированый логин и пароль без "Base "
		String decode = new String(Base64.getDecoder().decode(token)); // раскодировать в "login:password"
		return decode.split(":"); // сделать массив из логина и пароля, [0] - логин, [1] - пароль
	}

	private class WrappedRequest extends HttpServletRequestWrapper {
		// класс для того чтобы сделать нормальный Principal
		private String login;

		public WrappedRequest(HttpServletRequest request, String login) {
			super(request);
			this.login = login;
		}

		@Override
		public Principal getUserPrincipal() {
		    return () -> login;
		}
	}
}