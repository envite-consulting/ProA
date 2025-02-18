package de.envite.proa.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FileServiceTest {

    private static final String TEST_FILE_NAME = "test-diagram.bpmn";
    private static final String NONEXISTENT_FILE_NAME = "does_not_exist";
    private static final File file = new File(Objects.requireNonNull(FileServiceTest.class
                    .getClassLoader().getResource(TEST_FILE_NAME)).getFile());

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void readFileToString() throws IOException {
        String expected = new String(Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream(TEST_FILE_NAME)).readAllBytes());
        String result = fileService.readFileToString(file);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void readFileToString_IOException() {
        String result = fileService.readFileToString(new File(NONEXISTENT_FILE_NAME));

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
