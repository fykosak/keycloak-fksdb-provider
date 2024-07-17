package fykos.fksdb_keycloak_user_provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import fykos.fksdb_keycloak_user_provider.entities.LoginEntity;
import fykos.fksdb_keycloak_user_provider.entities.OrganizerEntity;
import fykos.fksdb_keycloak_user_provider.entities.PersonEntity;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {

	private static final Logger logger = Logger.getLogger(UserAdapter.class);

	protected LoginEntity loginEntity;
	protected String keycloakId;

	public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, LoginEntity loginEntity) {
		super(session, realm, model);
		this.loginEntity = loginEntity;

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
		return loginEntity.getLogin();
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
	public String getLastName() {
		return loginEntity.getPerson().getFamilyName();
	}

	@Override
	public String getEmail() {
		return loginEntity.getPerson().getPersonInfo().getEmail();
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

	private Set<String> getRoles() {
		Map<Integer, String> contestMap = new HashMap<>();
		contestMap.put(1, "fykos");
		contestMap.put(2, "vyfuk");

		Set<String> roles = new HashSet<String>();

		for (OrganizerEntity organizer : loginEntity.getPerson().getOrganizers()) {
			String contest = contestMap.get(organizer.getContestId());
			if (contest == null) {
				continue;
			}

			roles.add("fksdb-" + contest);
			if (organizer.getAllowWiki()) {
				roles.add("fksdb-" + contest + "-wiki");
			}
			if (organizer.getAllowPM()) {
				roles.add("fksdb-" + contest + "-pm");
			}
		}

		return roles;
	}
}