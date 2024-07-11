package fykos.fksdb_keycloak_user_provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "person")
public class PersonEntity {

	@Id
	@Column(name = "person_id")
	private String personId;

	@Column(name = "family_name")
	private String familyName;

	@Column(name = "other_name")
	private String otherName;

	public String getPersonId() {
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
}
