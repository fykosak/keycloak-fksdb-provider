package fykos.fksdb_keycloak_user_provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

public class FKSDBUserStorageProvider implements
		UserStorageProvider,
		UserLookupProvider,
		UserQueryMethodsProvider,
		CredentialInputValidator,
		CredentialInputUpdater {

	protected KeycloakSession session;
	protected ComponentModel model;
	protected FKSDBUserService service;

	protected EntityManager em;

	// map of loaded users in this transaction
	protected Map<String, UserModel> loadedUsers = new HashMap<>();

	private static final Logger logger = Logger.getLogger(FKSDBUserStorageProvider.class);

	public FKSDBUserStorageProvider(KeycloakSession session, ComponentModel model) {

		this.session = session;
		this.model = model;
		this.em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();

		logger.info("Provider created");

	}

	@Override
	public void close() {

	}

	@Override
	public UserModel getUserById(RealmModel realm, String id) {
		logger.info("Get user by ID: " + id);
		String persistenceId = StorageId.externalId(id);
		FKSDBUserEntity entity = em.find(FKSDBUserEntity.class, persistenceId);
		if (entity == null) {
			logger.info("Could not find user by ID: " + id);
			return null;
		}
		return new UserAdapter(session, realm, model, entity);
	}

	@Override
	public UserModel getUserByUsername(RealmModel realm, String username) {
		logger.info("Get user by username(login): " + username);
		TypedQuery<FKSDBUserEntity> query = em.createNamedQuery("getUserByUsername", FKSDBUserEntity.class);
		query.setParameter("login", username);
		List<FKSDBUserEntity> result = query.getResultList();
		if (result.isEmpty()) {
			logger.info("Could not find user by username: " + username);
			return null;
		}

		return new UserAdapter(session, realm, model, result.get(0));
	}

	@Override
	public UserModel getUserByEmail(RealmModel realm, String email) {
		logger.info("Get user by email: " + email);
		TypedQuery<FKSDBUserEntity> query = em.createNamedQuery("getUserByEmail", FKSDBUserEntity.class);
		query.setParameter("email", email);
		List<FKSDBUserEntity> result = query.getResultList();
		if (result.isEmpty()) {
			logger.info("Could not find user by email: " + email);
			return null;
		}

		return new UserAdapter(session, realm, model, result.get(0));
	}

	@Override
	public boolean supportsCredentialType(String credentialType) {
		return credentialType.equals(PasswordCredentialModel.TYPE);
	}

	@Override
	public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
		if (input.getType().equals(PasswordCredentialModel.TYPE))
			throw new ReadOnlyException("user is read only for this update");

		return false;
	}

	public UserAdapter getUserAdapter(UserModel user) {
		return (UserAdapter) user;
	}

	@Override
	public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
	}

	@Override
	public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
		if (getUserAdapter(user).getPassword() != null) {
			Set<String> set = new HashSet<>();
			set.add(PasswordCredentialModel.TYPE);
			return set.stream();
		} else {
			return Stream.empty();
		}
	}

	public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
		return supportsCredentialType(credentialType) && getUserAdapter(user).getPassword() != null;
	}

	@Override
	public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
		return true;
		// TODO
		// FKSDBUserModel userModel = (FKSDBUserModel) user;
		// String inputedPassword = input.getChallengeResponse();
		// String userHash = userModel.getHash();
		// if (inputedPassword == null || userHash == null) {
		// return false;
		// }

		// logger.info("UserHash: " + userHash);
		// logger.info("Inputed password: " + inputedPassword);

		// return userHash.equals(inputedPassword);
	}

	@Override
	public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult,
			Integer maxResults) {

		String search = params.get(UserModel.SEARCH);
		TypedQuery<FKSDBUserEntity> query = em.createNamedQuery("searchForUser", FKSDBUserEntity.class);
		String lower = search != null ? search.toLowerCase() : "";
		query.setParameter("search", "%" + lower + "%");
		if (firstResult != null) {
			query.setFirstResult(firstResult);
		}
		if (maxResults != null) {
			query.setMaxResults(maxResults);
		}
		return query.getResultStream().map(entity -> new UserAdapter(session, realm, model, entity));
	}

	@Override
	public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult,
			Integer maxResults) {
		return Stream.empty(); // TODO
	}

	@Override
	public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
		return Stream.empty(); // TODO
	}
}
