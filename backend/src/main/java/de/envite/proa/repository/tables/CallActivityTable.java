package de.envite.proa.repository.tables;

import de.envite.proa.util.SearchLabelBuilder;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CallActivityTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	private String elementId;

	private String label;

    private String searchLabel;

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