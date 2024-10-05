package fykos.fksdb_keycloak_user_provider.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import fykos.fksdb_keycloak_user_provider.entities.ContestYearEntity;
import org.jboss.logging.Logger;

public class ContestYearService {

	private final EntityManager em;

	private static final Logger logger = Logger.getLogger(ContestYearService.class);

	public ContestYearService(EntityManager em) {
		this.em = em;
	}

	/**
	 * Get the current ContestYearEntity based on contestId and academicYear
	 *
	 * @param contestId    - The ID of the contest
	 * @param academicYear - The academic year to filter
	 * @return ContestYearEntity or null if not found
	 */
	public ContestYearEntity getCurrentContestYear(int contestId, int academicYear) {
		TypedQuery<ContestYearEntity> query = em.createNamedQuery("getContestYear", ContestYearEntity.class)
				.setParameter("contestId", contestId)
				.setParameter("academicYear", academicYear);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			logger.info("Could not find contest year for contest " + contestId + " and academic year " + academicYear);
			return null;
		}
	}
}
