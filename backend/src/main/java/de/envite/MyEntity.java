package de.envite;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class MyEntity {
    @Id
    @GeneratedValue
    public Long id;

    public String field;

	@Override
	public String toString() {
		return "MyEntity [id=" + id + ", field=" + field + "]";
	}
}