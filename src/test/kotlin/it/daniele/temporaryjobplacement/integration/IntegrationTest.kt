package it.daniele.temporaryjobplacement.integration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.jdbc.datasource.init.ScriptUtils
import java.sql.DriverManager

@SpringBootTest
@ContextConfiguration(initializers = [IntegrationTest.Initializer::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class IntegrationTest {
    companion object {
        private val db = PostgreSQLContainer("postgres:latest")
    }

    @AfterEach
    fun clear(){
        val connection = DriverManager.getConnection(db.jdbcUrl, db.username, db.password)
        connection.use { c ->
            ScriptUtils.executeSqlScript(c, ClassPathResource("clean.sql"))
        }
    }

    @BeforeEach
    fun initialize(){
        val connection = DriverManager.getConnection(db.jdbcUrl, db.username, db.password)
        connection.use { c ->
            ScriptUtils.executeSqlScript(c, ClassPathResource("data.sql"))
        }
    }

    internal class Initializer: ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            db
                .withInitScript("./schema.sql")
                .start()
            TestPropertyValues.of(
                "spring.datasource.url=${db.jdbcUrl}",
                "spring.datasource.username=${db.username}",
                "spring.datasource.password=${db.password}",
                "spring.datasource.driver-class-name=${db.driverClassName}"
            ).applyTo(applicationContext.environment)

        }
    }
}