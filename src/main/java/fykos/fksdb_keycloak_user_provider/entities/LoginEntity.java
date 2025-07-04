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
		@NamedQuery(name = "getUserByEmail", query = "select l from LoginEntity l where l.person.personInfo.email = :email"),
		@NamedQuery(name = "getUserCount", query = "select count(l) from LoginEntity l"),
		@NamedQuery(name = "getAllUsers", query = "select l from LoginEntity l"),
		@NamedQuery(name = "searchForUser", query = """
				select l from LoginEntity l where
				lower(l.login) like :search
				or l.person.otherName like :search
				or l.person.otherName like :search
				or l.person.familyName like :search
				order by l.person.familyName, l.person.otherName
				"""),
})

@Entity
@Table(name = "login")
public class LoginEntity {

	@Id
	@Column(name = "login_id")
	private Integer loginId;

	private String login;
	private String hash;

	@OneToOne
	@JoinColumn(name = "person_id")
	private PersonEntity person;

	public Integer getLoginId() {
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
}
