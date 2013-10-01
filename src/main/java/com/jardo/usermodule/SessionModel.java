package com.jardo.usermodule;

import com.jardo.usermodule.containers.User;

public interface SessionModel {

	User getCurrentUser();

	void setCurrentUser(User user);

}
