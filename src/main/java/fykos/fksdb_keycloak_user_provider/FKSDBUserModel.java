package fykos.fksdb_keycloak_user_provider;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.adapter.AbstractUserAdapter;

public class FKSDBUserModel extends AbstractUserAdapter {

	private String login;
	private String hash;

	private static final Logger logger = Logger.getLogger(FKSDBUserService.class);

	FKSDBUserModel(ResultSet set, KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel) {
		super(session, realm, storageProviderModel);
		try {
			this.login = set.getString("login");
			this.hash = set.getString("hash");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getUsername() {
		return login;
	}

	public String getHash() {
		return hash;
	}

	@Override
	public SubjectCredentialManager credentialManager() {
		return new UserCredentialManager(session, realm, this);
	}
}
