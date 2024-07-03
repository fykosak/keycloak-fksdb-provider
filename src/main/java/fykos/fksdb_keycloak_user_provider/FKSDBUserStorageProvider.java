package fykos.fksdb_keycloak_user_provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.user.UserLookupProvider;

public class FKSDBUserStorageProvider implements
		UserStorageProvider,
		UserLookupProvider,
		CredentialInputValidator,
		CredentialInputUpdater {

	protected KeycloakSession session;
	protected Properties properties;
	protected ComponentModel model;
	// map of loaded users in this transaction
	protected Map<String, UserModel> loadedUsers = new HashMap<>();

	public FKSDBUserStorageProvider(KeycloakSession session, ComponentModel model, Properties properties) {
		this.session = session;
		this.model = model;
		this.properties = properties;
	}

	@Override
	public UserModel getUserByUsername(RealmModel realm, String username) {
		UserModel adapter = loadedUsers.get(username);
		if (adapter == null) {
			String password = properties.getProperty(username);
			if (password != null) {
				adapter = createAdapter(realm, username);
				loadedUsers.put(username, adapter);
			}
		}
		return adapter;
	}

	protected UserModel createAdapter(RealmModel realm, String username) {
		return new AbstractUserAdapter(session, realm, model) {
			@Override
			public String getUsername() {
				return username;
			}

			@Override
			public SubjectCredentialManager credentialManager() {
				throw new IOException();
			}
		};
	}

	@Override
	public void close() {

	}

	@Override
	public UserModel getUserById(RealmModel realm, String id) {
		StorageId storageId = new StorageId(id);
		String username = storageId.getExternalId();
		return getUserByUsername(realm, username);
	}

	@Override
	public UserModel getUserByEmail(RealmModel realm, String email) {
		return null;
	}

	@Override
	public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
		String password = properties.getProperty(user.getUsername());
		return credentialType.equals(PasswordCredentialModel.TYPE) && password != null;
	}

	@Override
	public boolean supportsCredentialType(String credentialType) {
		return credentialType.equals(PasswordCredentialModel.TYPE);
	}

	@Override
	public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
		if (!supportsCredentialType(input.getType()))
			return false;

		String password = properties.getProperty(user.getUsername());
		if (password == null)
			return false;
		return password.equals(input.getChallengeResponse());
	}

	@Override
	public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
		if (input.getType().equals(PasswordCredentialModel.TYPE))
			throw new ReadOnlyException("user is read only for this update");

		return false;
	}

	@Override
	public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {

	}

	@Override
	public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
		return Stream.empty();
	}
}
