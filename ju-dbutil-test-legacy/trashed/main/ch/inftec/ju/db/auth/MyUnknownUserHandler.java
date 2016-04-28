package ch.inftec.ju.db.auth;

import java.util.Arrays;
import java.util.List;

public class MyUnknownUserHandler implements UnknownUserHandler {

	@Override
	public NewUserInfo handleUser(String userName) {
		return new NewUserInfo() {
			@Override
			public String getPassword() {
				return "newPwd";
			}
			
			@Override
			public List<String> getAuthorities() {
				return Arrays.asList("NEW_ROLE");
			}
		};
	}

}
