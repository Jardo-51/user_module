package com.jardoapps.usermodule;

import com.jardoapps.usermodule.containers.User;

/**
 * This interface is used by {@link UserManager} to store data (like the
 * currently logged in user) in a server-side session.
 * 
 * @author Jaroslav Brtiš
 */
public interface SessionModel {

	/**
	 * Returns user who is currently logged in.
	 * 
	 * @return user who is currently logged in or null, if no user is logged in
	 * @see UserManager#logIn(String, String, String)
	 * @see UserManager#logOut()
	 */
	User getCurrentUser();

	/**
	 * Sets the user is currently logged in. If the user parameter is null, that
	 * means that no user is currently logged in.
	 * 
	 * @param user
	 *            user who is currently logged in, can be null
	 * @see UserManager#logIn(String, String, String)
	 * @see UserManager#logOut()
	 */
	void setCurrentUser(User user);

}
