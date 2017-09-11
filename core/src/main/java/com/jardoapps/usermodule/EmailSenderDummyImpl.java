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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dummy {@link EmailSender} implementation which only logs emails (does not send emails).
 *
 * @author Jaroslav Brtiš
 */
public class EmailSenderDummyImpl implements EmailSender {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderDummyImpl.class);

	@Override
	public boolean sendLostPasswordEmail(String email, String tokenKey) {
		LOGGER.info("Lost password email. Email: '{}', Token key: '{}'", email, tokenKey);
		return true;
	}

	@Override
	public boolean sendManualRegistrationEmail(String email, String userName, int userId, String registrationControlCode, User registrator) {
		LOGGER.info("Manual user registration email. Email: '{}', User name: '{}', User id: {}, Registration code: : '{}', Registrator name: '{}'", email, userName, userId,
				registrationControlCode, registrator.getName());
		return true;
	}

	@Override
	public boolean sendRegistrationEmail(String email, String userName, int userId, String registrationControlCode) {
		LOGGER.info("User registration email. Email: '{}', User name: '{}', User id: {}, Registration code: : '{}'", email, userName, userId, registrationControlCode);
		return true;
	}

}
