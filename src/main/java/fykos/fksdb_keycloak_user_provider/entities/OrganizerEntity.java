package fykos.fksdb_keycloak_user_provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "org")
public class OrganizerEntity {

	@Id
	@Column(name = "org_id")
	private int orgId;

	@ManyToOne
	@JoinColumn(name = "person_id")
	private PersonEntity person;

	@Column(name = "contest_id")
	private Integer contestId;
	private Integer since;
	private Integer until;

	@Column(name = "allow_wiki")
	private boolean allowWiki;
	@Column(name = "allow_pm")
	private boolean allowPM;

	public Integer getSince() {
		return since;
	}

	public Integer getUntil() {
		return until;
	}

	public Integer getContestId() {
		return contestId;
	}

	public PersonEntity getPerson() {
		return person;
	}

	public boolean getAllowWiki() {
		return allowWiki;
	}

	public boolean getAllowPM() {
		return allowPM;
	}
}
