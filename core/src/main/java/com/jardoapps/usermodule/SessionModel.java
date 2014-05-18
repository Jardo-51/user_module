package com.jardoapps.usermodule;

import com.jardoapps.usermodule.containers.User;

public interface SessionModel {

	User getCurrentUser();

	void setCurrentUser(User user);

}
