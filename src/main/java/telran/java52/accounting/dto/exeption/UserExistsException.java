package telran.java52.accounting.dto.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserExistsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6956894247506119953L;

}
