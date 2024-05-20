package telran.java52.accounting.dto.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncorrectRoleExeption extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7450020975780126026L;

	

}
