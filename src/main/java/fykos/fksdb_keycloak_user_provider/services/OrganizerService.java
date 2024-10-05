package fykos.fksdb_keycloak_user_provider.services;

import fykos.fksdb_keycloak_user_provider.entities.ContestYearEntity;
import fykos.fksdb_keycloak_user_provider.entities.OrganizerEntity;

import java.util.Calendar;

public class OrganizerService {

	private final ContestYearService contestYearService;

	public OrganizerService(ContestYearService contestYearService) {
		this.contestYearService = contestYearService;
	}

	public boolean isOrganizerActive(OrganizerEntity organizer) {

		// Calculate the appropriate academicYear
		Calendar c = Calendar.getInstance();
		int currentYear = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int academicYear = (month >= Calendar.SEPTEMBER) ? currentYear : currentYear - 1;

		// Use the ContestYearService to get the current contest year
		ContestYearEntity contestYear = contestYearService.getCurrentContestYear(organizer.getContestId(),
				academicYear);

		// Logic to check if the organizer is active (customize as needed)
		if (contestYear != null && organizer.getSince() <= contestYear.getContestYear()
				&& (organizer.getUntil() == null || organizer.getUntil() >= contestYear.getContestYear())) {
			return true;
		}
		return false;
	}
}
