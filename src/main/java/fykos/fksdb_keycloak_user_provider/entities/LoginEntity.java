package fykos.fksdb_keycloak_user_provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@NamedQueries({
		@NamedQuery(name = "getUserByUsername", query = "select l from LoginEntity l where l.login = :login"),
		// @NamedQuery(name = "getUserByEmail", query = """
		// select l from LoginEntity l
		// left join person_info pi on pi.person_id = l.person_id
		// where pi.email = :email
		// """),
		@NamedQuery(name = "getUserCount", query = "select count(l) from LoginEntity l"),
		@NamedQuery(name = "getAllUsers", query = "select l from LoginEntity l"),
		@NamedQuery(name = "searchForUser", query = "select l from LoginEntity l where " +
				"lower(l.login) like :search order by l.login"),
})

@Entity
@Table(name = "login")
public class LoginEntity {

	@Id
	@Column(name = "login_id")
	private String loginId;

	private String login;
	private String hash;

	@OneToOne
	@JoinColumn(name = "person_id")
	private PersonEntity person;

	public String getLoginId() {
		return loginId;
	}

	public String getLogin() {
		return login;
	}

	public String getHash() {
		return hash;
	}

	public PersonEntity getPerson() {
		return person;
	}

	// public String getUsername() {
	// return username;
	// }

	// public void setUsername(String username) {
	// this.username = username;
	// }

	// public String getEmail() {
	// return email;
	// }

	// public void setEmail(String email) {
	// this.email = email;
	// }

	// public String getPassword() {
	// return password;
	// }

	// public void setPassword(String password) {
	// this.password = password;
	// }

	// public String getPhone() {
	// return phone;
	// }

	// public void setPhone(String phone) {
	// this.phone = phone;
	// }
}
