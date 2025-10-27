package fykos.fksdb_keycloak_user_provider;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.cache.OnUserCache;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import fykos.fksdb_keycloak_user_provider.entities.LoginEntity;
import fykos.fksdb_keycloak_user_provider.entities.PersonEntity;
import fykos.fksdb_keycloak_user_provider.services.ContestConfigService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class FKSDBUserStorageProvider implements
		UserStorageProvider,
		UserLookupProvider,
		UserQueryProvider,
		CredentialInputValidator,
		OnUserCache {

	private static final Logger logger = Logger.getLogger(FKSDBUserStorageProvider.class);
	public static final String PASSWORD_CACHE_KEY = UserAdapter.class.getName() + ".password";

	protected EntityManager em;

	protected KeycloakSession session;
	protected ComponentModel model;
	protected ContestConfigService contestConfig;

	FKSDBUserStorageProvider(KeycloakSession session, ComponentModel model, ContestConfigService contestConfig) {
		this.session = session;
		this.model = model;
		this.contestConfig = contestConfig;
		this.em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
	}

	@Override
	public void preRemove(RealmModel realm) {

	}

	@Override
	public void preRemove(RealmModel realm, GroupModel group) {

	}

	@Override
	public void preRemove(RealmModel realm, RoleModel role) {

	}

	@Override
	public void close() {

	}

	@Override
	public UserModel getUserById(RealmModel realm, String id) {
		String persistenceId = StorageId.externalId(id);
		LoginEntity entity = em.find(LoginEntity.class, persistenceId);
		if (entity == null) {
			logger.info("Could not find user by ID: " + id);
			return null;
		}
		return new UserAdapter(session, realm, model, entity, contestConfig);
	}

	@Override
	public UserModel getUserByUsername(RealmModel realm, String username) {
		logger.info("Get user by username (login): " + username);
		TypedQuery<LoginEntity> query = em.createNamedQuery("getUserByUsername", LoginEntity.class);
		query.setParameter("login", username);
		List<LoginEntity> result = query.getResultList();
		if (result.isEmpty()) {
			logger.info("Could not find user by username: " + username);
			return null;
		}

		return new UserAdapter(session, realm, model, result.get(0), contestConfig);
	}

	@Override
	public UserModel getUserByEmail(RealmModel realm, String email) {
		logger.info("Get user by email: " + email);

		// try to find user by domain alias
		String[] emailParts = email.split("@", 2);
		Integer contestId = contestConfig.getContestIdFromDomain(emailParts[1]);
		if (contestId != null) {
			TypedQuery<LoginEntity> query = em.createNamedQuery("getUserByDomainAlias",
					LoginEntity.class);
			query.setParameter("contestId", contestId);
			query.setParameter("domainAlias", emailParts[0]);
			List<LoginEntity> result = query.getResultList();
			if (!result.isEmpty()) {
				return new UserAdapter(session, realm, model, result.get(0), contestConfig);
			}
		}

		// find user by person mail
		TypedQuery<LoginEntity> query = em.createNamedQuery("getUserByEmail",
				LoginEntity.class);
		query.setParameter("email", email);
		List<LoginEntity> result = query.getResultList();
		if (!result.isEmpty()) {
			return new UserAdapter(session, realm, model, result.get(0), contestConfig);
		}

		return null;
	}

	@Override
	public void onCache(RealmModel realm, CachedUserModel user, UserModel delegate) {
		String hash = ((UserAdapter) delegate).getLogin().getHash();
		if (hash != null) {
			user.getCachedWith().put(PASSWORD_CACHE_KEY, hash);
		}
	}

	@Override
	public boolean supportsCredentialType(String credentialType) {
		return PasswordCredentialModel.TYPE.equals(credentialType);
	}

	public UserAdapter getUserAdapter(UserModel user) {
		if (user instanceof CachedUserModel) {
			return (UserAdapter) ((CachedUserModel) user).getDelegateForUpdate();
		} else {
			return (UserAdapter) user;
		}
	}

	@Override
	public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
		return supportsCredentialType(credentialType) && getUserAdapter(user).getLogin().getHash() != null;
	}

	@Override
	public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
		String inputedPassword = input.getChallengeResponse();

		if (inputedPassword == null) {
			return false;
		}

		LoginEntity login = getUserAdapter(user).getLogin();
		if (login == null || login.getHash() == null) {
			return false;
		}

		// require person to be an organizer
		PersonEntity person = login.getPerson();
		if (person == null || person.getOrganizers().size() == 0) {
			return false;
		}

		// require at least one FKSDB role to be active
		if (getUserAdapter(user).getRoles().size() == 0) {
			return false;
		}

		// hash function as per FKSDB
		String computedHash = DigestUtils.sha1Hex(login.getLoginId() + DigestUtils.md5Hex(inputedPassword));

		return computedHash.equals(login.getHash());
	}

	public String getHash(UserModel user) {
		String password = null;
		if (user instanceof CachedUserModel) {
			password = (String) ((CachedUserModel) user).getCachedWith().get(PASSWORD_CACHE_KEY);
		} else if (user instanceof UserAdapter) {
			password = ((UserAdapter) user).getLogin().getHash();
		}
		return password;
	}

	@Override
	public int getUsersCount(RealmModel realm) {
		Object count = em.createNamedQuery("getUserCount")
				.getSingleResult();
		return ((Number) count).intValue();
	}

	@Override
	public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult,
			Integer maxResults) {

		String search = params.get(UserModel.SEARCH);
		TypedQuery<LoginEntity> query = em.createNamedQuery("searchForUser", LoginEntity.class);
		String lower = search != null ? search.toLowerCase() : "";
		query.setParameter("search", "%" + lower + "%");
		if (firstResult != null) {
			query.setFirstResult(firstResult);
		}
		if (maxResults != null) {
			query.setMaxResults(maxResults);
		}
		return query.getResultStream().map(entity -> new UserAdapter(session, realm, model, entity, contestConfig));
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
