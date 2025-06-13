package fykos.fksdb_keycloak_user_provider.services;

import fykos.fksdb_keycloak_user_provider.entities.OrganizerEntity;

public class OrganizerService {
	public boolean isOrganizerActive(OrganizerEntity organizer) {
		if (organizer.getState() == "active" || organizer.getState() == "passive") {
			return true;
		}
		return false;
	}
}
