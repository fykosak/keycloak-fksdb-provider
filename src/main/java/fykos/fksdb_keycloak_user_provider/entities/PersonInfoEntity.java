package fykos.fksdb_keycloak_user_provider.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "person_info")
public class PersonInfoEntity {

	@Id
	@Column(name = "person_id")
	private String personId;

	private String email;

	@OneToOne
	@JoinColumn(name = "person_id")
	private PersonEntity person;

	public String getEmail() {
		return email;
	}

	public PersonEntity getPerson() {
		return person;
	}
}
