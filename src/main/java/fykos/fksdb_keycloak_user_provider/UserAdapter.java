package fykos.fksdb_keycloak_user_provider;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {
	private static final Logger logger = Logger.getLogger(UserAdapter.class);
	protected FKSDBUserEntity entity;
	protected String keycloakId;

	public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, FKSDBUserEntity entity) {
		super(session, realm, model);
		this.entity = entity;
		keycloakId = StorageId.keycloakId(model, entity.getId());
	}

	public String getPassword() {
		return entity.getPassword();
	}

	public void setPassword(String password) {
		entity.setPassword(password);
	}

	@Override
	public String getUsername() {
		return entity.getUsername();
	}

	@Override
	public void setUsername(String username) {
		entity.setUsername(username);
	}

	@Override
	public void setEmail(String email) {
		entity.setEmail(email);
	}

	@Override
	public String getEmail() {
		return entity.getEmail();
	}

	@Override
	public String getId() {
		return keycloakId;
	}
}
