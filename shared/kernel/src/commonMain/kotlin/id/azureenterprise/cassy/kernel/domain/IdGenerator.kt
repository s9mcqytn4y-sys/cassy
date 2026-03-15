package id.azureenterprise.cassy.kernel.domain

import kotlin.random.Random
import kotlinx.datetime.Clock

/**
 * UUIDv7 based ID Generator (Simplified for KMP context).
 * In a production scenario, use a proper UUIDv7 library to ensure time-ordered monotonicity.
 */
object IdGenerator {
    fun nextId(prefix: String = ""): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextLong(0, Long.MAX_VALUE).toString(16)
        val id = "$timestamp-$random"
        return if (prefix.isEmpty()) id else "${prefix}_$id"
    }
}
