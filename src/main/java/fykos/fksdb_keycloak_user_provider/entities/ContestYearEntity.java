package fykos.fksdb_keycloak_user_provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@NamedQueries({
		@NamedQuery(name = "getContestYear", query = "select cy from ContestYearEntity cy where cy.contestId = :contestId and cy.academicYear = :academicYear"),
})

@Entity
@Table(name = "contest_year")
public class ContestYearEntity {
	@Id
	@Column(name = "contest_id")
	private int contestId;

	@Column(name = "year")
	private int contestYear;

	@Column(name = "ac_year")
	private int academicYear;

	public int getContestId() {
		return contestId;
	}

	public int getContestYear() {
		return contestYear;
	}

	public int getAcademicYear() {
		return academicYear;
	}
}
