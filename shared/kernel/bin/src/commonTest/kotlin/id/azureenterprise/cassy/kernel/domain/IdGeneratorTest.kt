package id.azureenterprise.cassy.kernel.domain

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class IdGeneratorTest {

    @Test
    fun `generator normalizes prefix and yields unique readable ids`() {
        val first = IdGenerator.nextId("Sale Event")
        val second = IdGenerator.nextId("Sale Event")

        assertTrue(first.startsWith("sale_event_"))
        assertTrue(second.startsWith("sale_event_"))
        assertNotEquals(first, second)
        assertTrue(first.substringAfter('_').length >= 21)
    }
}
