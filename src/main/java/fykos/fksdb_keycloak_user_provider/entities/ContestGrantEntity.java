package fykos.fksdb_keycloak_user_provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "contest_grant")
public class ContestGrantEntity {

	@Id
	@Column(name = "grant_id")
	private int grantId;

	@ManyToOne
	@JoinColumn(name = "login_id")
	private LoginEntity login;

	@Column(name = "role")
	private String role;

	@Column(name = "contest_id")
	private int contestId;

	public LoginEntity getLogin() {
		return login;
	}

	public String getRole() {
		return role;
	}

	public int getContestId() {
		return contestId;
	}
}
