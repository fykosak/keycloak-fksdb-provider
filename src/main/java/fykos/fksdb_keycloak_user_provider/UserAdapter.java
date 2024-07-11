package fykos.fksdb_keycloak_user_provider;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import fykos.fksdb_keycloak_user_provider.entities.LoginEntity;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {

	private static final Logger logger = Logger.getLogger(UserAdapter.class);

	protected LoginEntity loginEntity;
	protected String keycloakId;

	public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, LoginEntity loginEntity) {
		super(session, realm, model);
		this.loginEntity = loginEntity;
		keycloakId = StorageId.keycloakId(model, loginEntity.getLoginId());
	}

	public String getHash() {
		return loginEntity.getHash();
	}

	@Override
	public String getUsername() {
		return loginEntity.getLogin();
	}

	@Override
	public void setUsername(String username) {
		throw new ReadOnlyException("Username is read only");
	}

	//  @Override
	//  public String getEmail() {
	// return entity.getEmail();
	//  }

	//  @Override
	//  public void setEmail(String email) {
	// throw new ReadOnlyException("Email is read only");
	//  }

	@Override
	public String getId() {
		return keycloakId;
	}
}
