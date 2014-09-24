user_module
===========

A lightweight Java user management library

This library provides basic user management functionality needed by a typical website. This includes:

* User registration with email and/or user name
* User log in/out
* Changing passwords
* Resetting forgotten passwords
* Basic user ranking functionality

## Security

* All passwords are stored as a sha256 hash. Each password is salted with a securely generated random salt. The same salt is never used twice (a new salt is generated every time a user changes their password).

* No passwords are send by email. When a user forgets their password, a special token is generated and sent to the user who can then use it to set a new password. These tokens have time-limited validity. 

* User registration can be optionally confirmed via email.

## Usage

The user module library contains two sub-projects:

* Project `usermodule` provides the core functionality. This project can be used with any database model via the `UserDatabaseModel` interface.

* Project `usermodule.hbn` provides a default Hibernate implementation of the database model. You can use this implementation with the core project, or you can create your custom implementation.

### 1. Get the user module library

Clone this projects git repository or download the sources and import project `usermodule` and optionally `usermodule.hbn` into your IDE as Maven projects.

### 2. Include it in your Maven project

Put the following dependency code into your pom.xml file:

```
		<dependency>
			<groupId>com.jardoapps</groupId>
			<artifactId>usermodule</artifactId>
			<version>${usermodule.version}</version>
		</dependency>

		<!-- optional Hibernate database model implementation -->

		<dependency>
			<groupId>com.jardoapps</groupId>
			<artifactId>usermodule.hbn</artifactId>
			<version>${usermodule.version}</version>
		</dependency>
```

### 3. Create required interface implementations

In order to use the User Module library, you have to provide an implementation of these interfaces:

* `UserDatabaseModel` interface is used to access the user database. You can create your own implementation of the database model, or you can use the default implementation provided by class `UserDatabaseModelHbn` from project `usermodule.hbn`.

* `EmailSender` interface is used to send emails. Emails are send to confirm user registration or to restore lost passwords.

* `SessionModel` interface is used to access the websites session. The session is used to store information about logged in users.

### 4. Use class UserManager to access the user management functionality

All the core functionality is provided by class `UserManager` via its respective methods. To create an instance of this class, you have to provide implementations of the interfaces mentioned above.



