package de.envite.process.map.repository.tables;

import jakarta.persistence.Entity;
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

	private String callingElementType;

	private String calledElementType;

	private String callingElement;

	private String calledElement;

	@Override
	public String toString() {
		return "ProcessConnectionTable [id=" + id + ", callingProcess=" + callingProcess.getName() + ", calledProcess="
				+ calledProcess.getName() + ", callingElementType=" + callingElementType + ", calledElementType="
				+ calledElementType + ", callingElement=" + callingElement + ", calledElement=" + calledElement + "]";
	}
}