package fykos.fksdb_keycloak_user_provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizer")
public class OrganizerEntity {

	@Id
	@Column(name = "organizer_id")
	private int organizerId;

	@ManyToOne
	@JoinColumn(name = "person_id")
	private PersonEntity person;

	@Column(name = "contest_id")
	private Integer contestId;
	private Integer since;
	private Integer until;

	@Column(name = "domain_alias")
	private String domainAlias;
	@Column(name = "tex_signature")
	private String texSignature;

	private String state;

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

	public String getDomainAlias() {
		return domainAlias;
	}

	public String getTexSignature() {
		return texSignature;
	}

	public String getState() {
		return state;
	}
}
