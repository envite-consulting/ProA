package de.envite.proa.repository;

import de.envite.proa.repository.processmodel.ProcessmodelMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessModelMapperTest {

    @Test
    void testClassInitialization() {
        ProcessmodelMapper mapper = new ProcessmodelMapper();
        assertThat(mapper).isNotNull();
    }
}
