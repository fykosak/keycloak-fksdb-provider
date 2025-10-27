package fykos.fksdb_keycloak_user_provider;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import fykos.fksdb_keycloak_user_provider.entities.BaseGrantEntity;
import fykos.fksdb_keycloak_user_provider.entities.ContestGrantEntity;
import fykos.fksdb_keycloak_user_provider.entities.LoginEntity;
import fykos.fksdb_keycloak_user_provider.entities.OrganizerEntity;
import fykos.fksdb_keycloak_user_provider.entities.PersonEntity;
import fykos.fksdb_keycloak_user_provider.services.ContestConfigService;
import jakarta.persistence.EntityManager;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {
	protected LoginEntity loginEntity;
	protected String keycloakId;
	protected EntityManager em;
	protected ContestConfigService contestConfig;

	public UserAdapter(
			KeycloakSession session,
			RealmModel realm,
			ComponentModel model,
			LoginEntity loginEntity,
			ContestConfigService contestConfig) {
		super(session, realm, model);
		this.loginEntity = loginEntity;
		this.em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
		this.contestConfig = contestConfig;

		// generate keycloak specific id
		keycloakId = StorageId.keycloakId(model, loginEntity.getLoginId().toString());

		syncRoles();
	}

	@Override
	public String getId() {
		return keycloakId;
	}

	@Override
	public String getUsername() {
		// Keycloak username must not be null.
		// So if the user does not have a username, return email instead.
		// If email is detected on auth, Keycloak automatically searches the user by
		// email, so it should login with email as login.
		String login = loginEntity.getLogin();
		if (login != null) {
			return login;
		}
		return loginEntity.getPerson().getPersonInfo().getEmail();
	}

	@Override
	public void setUsername(String username) {
		throw new ReadOnlyException("Username is read only");
	}

	@Override
	public String getFirstName() {
		return loginEntity.getPerson().getOtherName();
	}

	@Override
	public void setFirstName(String name) {
		throw new ReadOnlyException("First name is read only");
	}

	@Override
	public String getLastName() {
		return loginEntity.getPerson().getFamilyName();
	}

	@Override
	public void setLastName(String name) {
		throw new ReadOnlyException("Last name is read only");
	}

	@Override
	public String getEmail() {
		return loginEntity.getPerson().getPersonInfo().getEmail();
	}

	@Override
	public void setEmail(String email) {
		throw new ReadOnlyException("Email is read only");
	}

	@Override
	public boolean isEmailVerified() {
		return this.getEmail() != null;
	}

	public LoginEntity getLogin() {
		return loginEntity;
	}

	public PersonEntity getPerson() {
		return loginEntity.getPerson();
	}

	private RoleModel getRealmRole(String roleName) {
		RoleModel role = realm.getRole(roleName);
		if (role == null) {
			role = realm.addRole(roleName);
		}
		return role;
	}

	private void syncRoles() {
		// roles based on data from FKSDB
		Set<String> newRoles = this.getRoles();

		// roles currently assigned to keycloak
		Set<String> assignedRoles = getRealmRoleMappingsStream().map(RoleModel::getName).collect(Collectors.toSet());

		// roles that are assigned but should not be
		Set<String> outdatedRoles = assignedRoles;
		outdatedRoles.removeAll(newRoles);

		// remove outdated roles
		for (String roleName : outdatedRoles) {
			// skip non-FKSDB roles
			if (!roleName.startsWith("fksdb-")) {
				continue;
			}
			this.deleteRoleMapping(this.getRealmRole(roleName));
		}

		// assign correct roles
		for (String roleName : newRoles) {
			this.grantRole(this.getRealmRole(roleName));
		}
	}

	/**
	 * Get roles assigned to the user based on DB data.
	 * Assigns `fksdb-<contest>` roles for active organizers of a contest and
	 * `fksdb-<contest>-<state>` roles for specific user states (active, passive)
	 *
	 */
	public Set<String> getRoles() {
		Set<String> roles = new HashSet<String>();

		for (OrganizerEntity organizer : loginEntity.getPerson().getOrganizers()) {
			String contest = contestConfig.getContestSymbol(organizer.getContestId());
			if (contest == null) {
				continue;
			}

			// Contest role for active organizer
			if (!organizer.isActive()) {
				continue;
			}

			roles.add("fksdb-" + contest);
			roles.add("fksdb-" + contest + "-state-" + organizer.getState());

			for (ContestGrantEntity contestGrant : loginEntity.getContestGrants()) {
				if (contestGrant.getContestId() == organizer.getContestId()) {
					roles.add("fksdb-" + contest + "-grant-" + contestGrant.getRole());
				}
			}
		}

		// Add base grants only if other roles are present so only if organizer is
		// active in at least one contest
		if (roles.size() > 0) {
			for (BaseGrantEntity baseGrant : loginEntity.getBaseGrants()) {
				roles.add("fksdb-grant-" + baseGrant.getRole());
			}
		}

		return roles;
	}

	@Override
	public void setSingleAttribute(String name, String value) {
		throw new ReadOnlyException("Users are read only. Use FKSDB to manage user data.");
	}

	@Override
	public void removeAttribute(String name) {
		throw new ReadOnlyException("Users are read only. Use FKSDB to manage user data.");
	}

	@Override
	public void setAttribute(String name, List<String> values) {
		throw new ReadOnlyException("Users are read only. Use FKSDB to manage user data.");
	}

	@Override
	public String getFirstAttribute(String name) {
		if (name.equals("firstName")) {
			return getFirstName();
		}

		if (name.equals("lastName")) {
			return getLastName();
		}

		if (name.equals("email")) {
			return getEmail();
		}

		if (name.equals("fksdb-id")) {
			return Integer.toString(loginEntity.getPerson().getPersonId());
		}

		for (Integer contestId : contestConfig.getContestIds()) {
			if (name.equals("fksdb-" + contestConfig.getContestSymbol(contestId) + "-email")) {
				for (OrganizerEntity organizer : loginEntity.getPerson().getOrganizers()) {
					if (organizer.getContestId() == contestId && organizer.getDomainAlias() != null) {
						return organizer.getDomainAlias() + "@" + contestConfig.getDomain(contestId);
					}
				}
			}
		}

		return super.getFirstAttribute(name);
	}

	@Override
	public Map<String, List<String>> getAttributes() {
		Map<String, List<String>> attrs = super.getAttributes();
		MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
		all.putAll(attrs);

		// Somehow the parent class provided null email which caused "Multiple values
		// found ... for protocol mapper 'email' but expected just single value"
		all.putSingle("firstName", getFirstName());
		all.putSingle("lastName", getLastName());
		all.putSingle("email", getEmail());
		all.add("fksdb-id", Integer.toString(loginEntity.getPerson().getPersonId()));

		for (OrganizerEntity organizer : loginEntity.getPerson().getOrganizers()) {
			String contest = contestConfig.getContestSymbol(organizer.getContestId());
			if (contest != null && organizer.getDomainAlias() != null) {
				all.add("fksdb-" + contest + "-email",
						organizer.getDomainAlias() + "@" + contestConfig.getDomain(organizer.getContestId()));
			}
		}

		return all;
	}

	@Override
	public Stream<String> getAttributeStream(String name) {
		if (name.equals("firstName")) {
			List<String> firstName = new LinkedList<>();
			firstName.add(getFirstName());
			return firstName.stream();
		} else if (name.equals("lastName")) {
			List<String> lastName = new LinkedList<>();
			lastName.add(getLastName());
			return lastName.stream();
		} else if (name.equals("email")) {
			List<String> email = new LinkedList<>();
			email.add(getEmail());
			return email.stream();
		} else if (name.startsWith("fksdb-")) {
			List<String> attribute = new LinkedList<>();
			String value = getFirstAttribute(name);
			if (value != null) {
				attribute.add(value);
			}
			return attribute.stream();
		} else {
			return super.getAttributeStream(name);
		}
	}
}
