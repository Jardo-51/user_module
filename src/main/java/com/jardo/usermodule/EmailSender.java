package com.jardo.usermodule;

public interface EmailSender {

	boolean sendLostPasswordEmail(String email, String tokenKey);

	boolean sendRegistrationEmail(String email, String userName, int userId, String registrationControlCode);

}
