package de.envite.proa.repository;

import de.envite.proa.repository.processmodel.ProcessmodelMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessmodelMapperTest {

	@Test
	void testClassInitialization() {
		ProcessmodelMapper mapper = new ProcessmodelMapper();
		assertThat(mapper).isNotNull();
	}
}
