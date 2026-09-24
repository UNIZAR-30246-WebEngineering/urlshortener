package es.unizar.urlshortener

import org.jmolecules.archunit.JMoleculesArchitectureRules
import org.jmolecules.archunit.JMoleculesArchitectureRules.VerificationDepth
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.core.VerificationOptions
import org.springframework.modulith.docs.Documenter

class ModularityTests {
    private val modules = ApplicationModules.of(Application::class.java)

    @Test
    fun `verifies module structure and hexagonal stereotypes`() {
        val hexagonal = JMoleculesArchitectureRules.ensureHexagonal(VerificationDepth.SEMI_STRICT)
        modules.verify(VerificationOptions.defaults().withAdditionalVerifications(hexagonal))
    }

    @Test
    fun `writes module documentation`() {
        Documenter(modules).writeDocumentation()
    }
}
