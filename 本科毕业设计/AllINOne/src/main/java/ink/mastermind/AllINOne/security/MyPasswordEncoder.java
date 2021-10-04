package ink.mastermind.AllINOne.security;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MyPasswordEncoder implements PasswordEncoder{
	public static MyPasswordEncoder m = new MyPasswordEncoder();
	
	@Override
	public String encode(CharSequence arg0) {
		return new BCryptPasswordEncoder().encode(arg0.toString());
	}

	@Override
	public boolean matches(CharSequence raw, String encode) {
		return BCrypt.checkpw(raw.toString(), encode);
	}

}
