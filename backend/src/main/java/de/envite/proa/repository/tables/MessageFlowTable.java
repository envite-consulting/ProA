package de.envite.proa.repository.tables;

import de.envite.proa.entities.ProcessElementType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class MessageFlowTable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String bpmnId;

	private String name;

	private String description;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn( //
			name = "calling_process_id", //
			foreignKey = @ForeignKey( //
					name = "fk_calling_process", //
					foreignKeyDefinition = "FOREIGN KEY (calling_process_id) REFERENCES processmodeltable(id) ON DELETE CASCADE" //
			) //
	)
	private ProcessModelTable callingProcess;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn( //
			name = "called_process_id", //
			foreignKey = @ForeignKey( //
					name = "fk_called_process", //
					foreignKeyDefinition = "FOREIGN KEY (called_process_id) REFERENCES processmodeltable(id) ON DELETE CASCADE" //
			) //
	)
	private ProcessModelTable calledProcess;

	@Enumerated(EnumType.STRING)
	private ProcessElementType callingElementType;

	@Enumerated(EnumType.STRING)
	private ProcessElementType calledElementType;

	@ManyToOne
	private ProjectTable project;
}