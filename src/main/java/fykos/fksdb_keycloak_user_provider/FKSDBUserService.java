package fykos.fksdb_keycloak_user_provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jboss.logging.Logger;

class FKSDBUserService {

	private String dbUrl;
	private String dbUsername;
	private String dbPassword;

	private static final Logger logger = Logger.getLogger(FKSDBUserService.class);

	FKSDBUserService(String dbUrl, String username, String password) {
		this.dbUrl = dbUrl;
		this.dbUsername = username;
		this.dbPassword = password;
	}

	public ResultSet getUserByUsername(String username) {
		logger.info("get user by username");
		try {
			Connection connection = DriverManager.getConnection(this.dbUrl, this.dbUsername, this.dbPassword);
			ResultSet resultSet = connection.createStatement()
					.executeQuery("SELECT * FROM login WHERE login = '" + username + "'");
			if (!resultSet.next()) {
				return null;
			}
			return resultSet;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
}
