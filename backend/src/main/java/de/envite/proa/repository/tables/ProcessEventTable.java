package de.envite.proa.repository.tables;

import de.envite.proa.entities.process.EventType;
import de.envite.proa.util.SearchLabelBuilder;
import jakarta.persistence.*;
import lombok.Data;

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
        this.searchLabel = SearchLabelBuilder.buildSearchLabel(this.label);
    }
}