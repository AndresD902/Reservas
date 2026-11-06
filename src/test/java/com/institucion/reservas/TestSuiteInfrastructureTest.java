package com.institucion.reservas;

import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.repository.UsuarioRepository;
import com.institucion.reservas.support.DatabaseCleaner;
import com.institucion.reservas.support.UsuarioTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TestSuiteInfrastructureTest {

    private static final String DOCUMENTO_COMPARTIDO = "91000001";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        databaseCleaner.clean();
    }

    @Test
    void flywayAppliesMigrationsOnTheTestDatabase() {
        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE", Integer.class);
        Integer createdTables = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE LOWER(table_name) IN ('usuarios', 'recursos', 'reservas')",
                Integer.class);

        assertThat(appliedMigrations).isPositive();
        assertThat(createdTables).isEqualTo(3);
    }

    @Test
    void firstTestCanCreateDataWithSharedDocument() {
        assertThat(usuarioRepository.findByDocumento(DOCUMENTO_COMPARTIDO)).isEmpty();

        UsuarioEntity usuario = UsuarioTestBuilder.aUsuario()
                .withDocumento(DOCUMENTO_COMPARTIDO)
                .persist(usuarioRepository);

        assertThat(usuario.getId()).isNotNull();
    }

    @Test
    void secondTestCanCreateDataWithSharedDocument() {
        assertThat(usuarioRepository.findByDocumento(DOCUMENTO_COMPARTIDO)).isEmpty();

        UsuarioEntity usuario = UsuarioTestBuilder.aUsuario()
                .withDocumento(DOCUMENTO_COMPARTIDO)
                .persist(usuarioRepository);

        assertThat(usuario.getId()).isNotNull();
    }
}
