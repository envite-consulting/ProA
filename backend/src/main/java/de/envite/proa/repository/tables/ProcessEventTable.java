package de.envite.proa.repository.tables;

import de.envite.proa.entities.process.EventType;
import jakarta.persistence.*;
import lombok.Data;

import static de.envite.proa.repository.processmodel.ProcessEventDao.buildSearchLabel;

@Entity
@Data
public class ProcessEventTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String elementId;

	private String label;

    private String searchLabel;

	@Enumerated(EnumType.STRING)
	private EventType eventType;

	@ManyToOne(fetch = FetchType.LAZY)
	private ProcessModelTable processModel;

	@ManyToOne(fetch = FetchType.LAZY)
    private ProjectVersionTable project;

    @PrePersist
    @PreUpdate
    private void generateSearchLabel() {
        this.searchLabel = buildSearchLabel(this.label);
    }
}