package id.azureenterprise.cassy.kernel.domain

import kotlin.math.max
import kotlin.random.Random
import kotlinx.datetime.Clock

/**
 * Time-ordered ID generator for local-first entities.
 * Format: <prefix>_<base36-timestamp><base36-sequence><hex-random>
 *
 * The goal is operational traceability and sortable IDs without depending on opaque UUID blobs.
 */
object IdGenerator {
    private var lastTimestamp = 0L
    private var sequence = 0

    fun nextId(prefix: String = ""): String {
        val normalizedPrefix = normalizePrefix(prefix)
        val now = Clock.System.now().toEpochMilliseconds()
        val safeTimestamp = max(now, lastTimestamp)
        val nextSequence = if (safeTimestamp == lastTimestamp) {
            (sequence + 1) % MAX_SEQUENCE
        } else {
            0
        }
        lastTimestamp = safeTimestamp
        sequence = nextSequence

        val timestampPart = safeTimestamp.toString(36).padStart(10, '0')
        val sequencePart = nextSequence.toString(36).padStart(3, '0')
        val randomPart = Random.nextInt().toUInt().toString(16).padStart(8, '0')
        return "${normalizedPrefix}_${timestampPart}${sequencePart}${randomPart}"
    }

    private fun normalizePrefix(prefix: String): String {
        val sanitized = prefix
            .trim()
            .lowercase()
            .map { char -> if (char.isLetterOrDigit()) char else '_' }
            .joinToString("")
            .trim('_')
        return sanitized.ifEmpty { "id" }
    }

    private const val MAX_SEQUENCE = 36 * 36 * 36
}
