package fykos.fksdb_keycloak_user_provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "person")
public class PersonEntity {

	@Id
	@Column(name = "person_id")
	private int personId;

	@Column(name = "family_name")
	private String familyName;

	@Column(name = "other_name")
	private String otherName;

	@OneToOne(mappedBy = "person")
	private LoginEntity login;

	@OneToOne(mappedBy = "person")
	private PersonInfoEntity personInfo;

	public int getPersonId() {
		return personId;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getOtherName() {
		return otherName;
	}

	public String getName() {
		return otherName + " " + familyName;
	}

	public LoginEntity getLogin() {
		return login;
	}

	public PersonInfoEntity getPersonInfo() {
		return personInfo;
	}
}
