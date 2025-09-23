package de.envite.proa.repository.tables;

import de.envite.proa.util.SearchLabelBuilder;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DataStoreTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String label;

    private String searchLabel;

	@ManyToOne(fetch = FetchType.LAZY)
	private ProjectVersionTable project;

    @PrePersist
    @PreUpdate
    private void generateSearchLabel() {
        this.searchLabel = SearchLabelBuilder.buildSearchLabel(this.label);
    }
}