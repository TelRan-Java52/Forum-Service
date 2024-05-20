package telran.java52.accounting.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import telran.java52.accounting.dao.UserAccountingRepository;

import telran.java52.accounting.dto.RolesDto;
import telran.java52.accounting.dto.UserDto;
import telran.java52.accounting.dto.UserEditDto;
import telran.java52.accounting.dto.UserRegisterDto;
import telran.java52.accounting.dto.exeption.IncorrectRoleExeption;
import telran.java52.accounting.model.UserAccount;

@Service
@RequiredArgsConstructor
public class UserAccountingServiceImpl implements UserAccountService, CommandLineRunner {
	final UserAccountingRepository userAccountingRepository;
	final ModelMapper modelMapper;
	
	@Override
	public UserDto register(UserRegisterDto userRegisterDto) {
		
		if (userAccountingRepository.existsById(userRegisterDto.getLogin())) {
	        throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email " + userRegisterDto.getLogin() + " already exists");
	    }
		UserAccount user = modelMapper.map(userRegisterDto, UserAccount.class);
          String password = BCrypt.hashpw(userRegisterDto.getPassword(), BCrypt.gensalt());
		   user.setPassword(password);
          user = userAccountingRepository.save(user);
           return  modelMapper.map(user, UserDto.class);

		
	}

	@Override
	public UserDto getUser(String login) {
		UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserNotFoundException::new);
		return modelMapper.map(user, UserDto.class);
		
	}

	@Override
	public UserDto removeUser(String login) {
		UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserNotFoundException::new);
		userAccountingRepository.deleteById(login);
		return modelMapper.map(user, UserDto.class);
	}

	@Override
	public UserDto updateUser(String login, UserEditDto userEditDto) {
		UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserNotFoundException::new);
		String lastName = userEditDto.getLastName();
		if (lastName != null) {
			user.setLastName(lastName);
		}
		String firstName = userEditDto.getFirstName();
		if (firstName != null) {
			user.setFirstName(firstName);
		}
		user = userAccountingRepository.save(user);
		return modelMapper.map(user, UserDto.class);
	
	}

	@Override
	public RolesDto changeRolesList(String login, String role, boolean isAddRole) {
		UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserNotFoundException::new);
		try {
		    if (isAddRole) {
		        user.addRole(role);
		    } else {
		        user.removeRole(role);
		    }
		} catch (Exception e) {
		    throw new IncorrectRoleExeption();
		}
		userAccountingRepository.save(user);
		Set<String> roleSet = user.getRoles().stream().map(r -> r.toString()).collect(Collectors.toSet());
		return new RolesDto(login, roleSet);
	}

	@Override
	public void changePassword(String login, String newPassword) {
		UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserNotFoundException::new);
		if (user == null) {
			throw new RuntimeException("User not found");
		}
		String password = BCrypt.hashpw(newPassword, BCrypt.gensalt());
		user.setPassword(newPassword);

		userAccountingRepository.save(user);
	}

	@Override
	public void run(String... args) throws Exception {
		if (!userAccountingRepository.existsById("admin")) {
			UserAccount userAccount = new UserAccount("admin","","",password);
		//to do
		}
		
	}

	}


