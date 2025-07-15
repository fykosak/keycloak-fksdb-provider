package fykos.fksdb_keycloak_user_provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "base_grant")
public class BaseGrantEntity {

	@Id
	@Column(name = "base_grant_id")
	private int grantId;

	@ManyToOne
	@JoinColumn(name = "login_id")
	private LoginEntity login;

	@Column(name = "role")
	private String role;

	public LoginEntity getLogin() {
		return login;
	}

	public String getRole() {
		return role;
	}
}
