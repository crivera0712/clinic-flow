package com.clinicflow.clinic_flow.demo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DemoSeedMigrationTest {

    @Test
    void expandedDemoMigrationContainsExpectedSandboxSeed() throws IOException {
        String sql = readMigration("db/migration/V24__expand_demo_clinic_seed.sql");

        assertAll(
                () -> assertTrue(sql.contains("demo_display")),
                () -> assertTrue(sql.contains("'DISPLAY'")),
                () -> assertTrue(sql.contains("Jill Valentine")),
                () -> assertTrue(sql.contains("Claire Redfield")),
                () -> assertTrue(sql.contains("Rebecca Chambers")),
                () -> assertTrue(sql.contains("Carlos Oliveira")),
                () -> assertTrue(sql.contains("Sheva Alomar")),
                () -> assertTrue(sql.contains("Helena Harper")),
                () -> assertTrue(sql.contains("Kennedy, L")),
                () -> assertTrue(sql.contains("Redfield, C")),
                () -> assertTrue(sql.contains("Wong, A")),
                () -> assertTrue(sql.contains("Burton, B")),
                () -> assertTrue(sql.contains("Birkin, S")),
                () -> assertTrue(sql.contains("Winters, E")),
                () -> assertTrue(sql.contains("Winters, M")),
                () -> assertTrue(sql.contains("Winters, R")),
                () -> assertTrue(sql.contains("Burton, M")),
                () -> assertTrue(sql.contains("Graham, A")),
                () -> assertTrue(sql.contains("Hunnigan, I")),
                () -> assertTrue(sql.contains("Luciani, P")),
                () -> assertTrue(sql.contains("'LOW_BACK', 'Low Back'")),
                () -> assertTrue(sql.contains("'CHECKED_IN'")),
                () -> assertTrue(sql.contains("'SCHEDULED'")),
                () -> assertTrue(sql.contains("'IN_SESSION'")),
                () -> assertTrue(sql.contains("'FINISHED'")),
                () -> assertTrue(sql.contains("'EVALUATION'")),
                () -> assertTrue(sql.contains("'FOLLOW_UP'")),
                () -> assertTrue(sql.contains("'REASSESSMENT'")),
                () -> assertTrue(sql.contains("TIME '08:00'")),
                () -> assertTrue(sql.contains("TIME '11:30'")),
                () -> assertTrue(sql.contains("TIME '13:30'")));
    }

    private String readMigration(String path) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(inputStream, "Migration file should exist: " + path);
        try (inputStream) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
