package com.jardoapps.usermodule;

/**
 * This class defines constants which can be set and compared with property
 * {@link User#getRank() User.rank} to provide basic access control
 * functionality. These values are just proposed, which means you can use your
 * own values or not use the property <code>User.rank</code> at all. The user
 * module functionality is not affected by this property in any way.
 * 
 * @author Jaroslav Brtiš
 * 
 */
public class UserRanks {

	public static final int DEMO_USER = 100;
	public static final int NORMAL_USER = 200;
	public static final int MODERATOR = 300;
	public static final int ADMIN = 400;
	public static final int WEBMASTER = 500;

}
