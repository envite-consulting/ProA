package de.envite.proa.repository.tables;

import de.envite.proa.entities.ProcessElementType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class ProcessConnectionTable {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	private ProcessModelTable callingProcess;

	@ManyToOne(fetch = FetchType.EAGER)
	private ProcessModelTable calledProcess;

	@Enumerated(EnumType.STRING)
	private ProcessElementType callingElementType;

	@Enumerated(EnumType.STRING)
	private ProcessElementType calledElementType;

	private String callingElement;

	private String calledElement;

	@Override
	public String toString() {
		return "ProcessConnectionTable [id=" + id + ", callingProcess=" + callingProcess.getName() + ", calledProcess="
				+ calledProcess.getName() + ", callingElementType=" + callingElementType + ", calledElementType="
				+ calledElementType + ", callingElement=" + callingElement + ", calledElement=" + calledElement + "]";
	}
}