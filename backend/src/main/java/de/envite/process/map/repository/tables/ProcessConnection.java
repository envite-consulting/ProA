package de.envite.process.map.repository.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class ProcessConnection {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private ProcessModel callingProcess;

	@ManyToOne
	private ProcessModel calledProcess;

	private String callingElementType;

	private String calledElementType;

	private String callingElement;

	private String calledElement;
}
