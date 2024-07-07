package fykos.fksdb_keycloak_user_provider;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryMethodsProvider;

public class FKSDBUserStorageProvider implements
		UserStorageProvider,
		UserLookupProvider,
		UserQueryMethodsProvider,
		CredentialInputValidator,
		CredentialInputUpdater {

	protected KeycloakSession session;
	protected ComponentModel model;
	protected FKSDBUserService service;

	// map of loaded users in this transaction
	protected Map<String, UserModel> loadedUsers = new HashMap<>();

	private static final Logger logger = Logger.getLogger(FKSDBUserStorageProvider.class);

	public FKSDBUserStorageProvider(KeycloakSession session, ComponentModel model, FKSDBUserService service) {

		this.session = session;
		this.model = model;
		this.service = service;

		logger.info("Provider created");

	}

	@Override
	public UserModel getUserByUsername(RealmModel realm, String username) {
		logger.info("storage user by name");
		UserModel adapter = loadedUsers.get(username);
		if (adapter == null) {
			ResultSet result = this.service.getUserByUsername(username);
			if (result != null) {
				adapter = createAdapter(realm, result);
				loadedUsers.put(username, adapter);
			} else {
				logger.warn("user does not exist");
			}
		}
		return adapter;
	}

	protected UserModel createAdapter(RealmModel realm, ResultSet set) {
		logger.info("create user adapter");
		return new FKSDBUserModel(set, session, realm, model);
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
		// String password = properties.getProperty(user.getUsername());
		String password = "password"; // TODO
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

		// String password = properties.getProperty(user.getUsername());
		String password = "password"; // TODO
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

	@Override
	public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult,
			Integer maxResults) {
		System.out.println("Search:" + search);
		Predicate<String> predicate = "*".equals(search) ? username -> true : username -> username.contains(search);
		return Stream.empty(); // TODO
		// return properties.keySet().stream()
		// .map(String.class::cast)
		// .filter(predicate)
		// .skip(firstResult)
		// .map(username -> getUserByUsername(realm, username))
		// .limit(maxResults);
	}

	@Override
	public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult,
			Integer maxResults) {
		// only support searching by username
		String usernameSearchString = params.get("username");
		if (usernameSearchString != null)
			return searchForUserStream(realm, usernameSearchString, firstResult, maxResults);

		// if we are not searching by username, return all users
		return searchForUserStream(realm, "*", firstResult, maxResults);
	}

	@Override
	public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult,
			Integer maxResults) {
		return Stream.empty();
	}

	@Override
	public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
		return Stream.empty();
	}
}
