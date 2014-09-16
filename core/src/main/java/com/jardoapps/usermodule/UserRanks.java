/*
 * This file is part of the User Module library.
 * Copyright (C) 2014 Jaroslav Brtiš
 *
 * User Module library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * User Module library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with User Module library. If not, see <http://www.gnu.org/licenses/>.
 */

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

	private UserRanks () {

	}

}
