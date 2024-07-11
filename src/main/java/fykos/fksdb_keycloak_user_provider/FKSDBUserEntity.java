package fykos.fksdb_keycloak_user_provider;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
		@NamedQuery(name = "getUserByUsername", query = "select l from login l where l.login = :login"),
		@NamedQuery(name = "getUserByEmail", query = """
					select l from login l
					left join person_info pi on pi.person_id = l.person_id
					where pi.email = :email
				"""),
		@NamedQuery(name = "getUserCount", query = "select count(l) from login l"),
		@NamedQuery(name = "getAllUsers", query = "select l from login l")
})
@Entity
public class FKSDBUserEntity {
	@Id
	private String id;

	private String username;
	private String email;
	private String password;
	private String phone;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}
