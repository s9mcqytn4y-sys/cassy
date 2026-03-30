package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.OutboxRepository
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

class SyncReplayService(
    private val outboxRepository: OutboxRepository,
    private val syncVisibilityService: SyncVisibilityService,
    private val syncReplayPort: SyncReplayPort,
    private val clock: Clock
) {
    suspend fun replayPending(limit: Int = DEFAULT_BATCH_SIZE, includeFailed: Boolean = true): SyncReplayResult {
        val failedBefore = outboxRepository.getFailedEvents()
        val pendingBefore = outboxRepository.getPendingEvents()

        if (pendingBefore.isEmpty() && failedBefore.isEmpty()) {
            return SyncReplayResult.Idle
        }

        if (!syncReplayPort.isAvailable) {
            syncVisibilityService.recordSyncFailure("Backend sync belum dikonfigurasi")
            return SyncReplayResult.Unavailable(
                pendingCount = pendingBefore.size,
                failedCount = failedBefore.size
            )
        }

        var requeuedCount = 0
        if (includeFailed && failedBefore.isNotEmpty()) {
            outboxRepository.requeueFailedEvents()
            requeuedCount = failedBefore.size
        }

        val replayCandidates = outboxRepository.getPendingEvents().take(limit)
        if (replayCandidates.isEmpty()) {
            return SyncReplayResult.Idle
        }

        var processedCount = 0
        var failedCount = 0
        var conflictCount = 0
        var lastFailureMessage: String? = null

        replayCandidates.forEach { event ->
            when (val outcome = syncReplayPort.replay(event)) {
                SyncReplayOutcome.Acknowledged -> {
                    outboxRepository.markEventProcessed(event.id)
                    processedCount += 1
                }
                is SyncReplayOutcome.Conflict -> {
                    outboxRepository.markEventFailed(event.id)
                    failedCount += 1
                    conflictCount += 1
                    lastFailureMessage = outcome.message
                }
                is SyncReplayOutcome.FatalFailure -> {
                    outboxRepository.markEventFailed(event.id)
                    failedCount += 1
                    lastFailureMessage = outcome.message
                }
                is SyncReplayOutcome.RetryableFailure -> {
                    outboxRepository.markEventFailed(event.id)
                    failedCount += 1
                    lastFailureMessage = outcome.message
                }
            }
        }

        if (failedCount == 0) {
            syncVisibilityService.recordSyncSuccess()
        } else {
            syncVisibilityService.recordSyncFailure(lastFailureMessage ?: "Sebagian event sync gagal diproses")
        }

        outboxRepository.pruneProcessedEventsBefore(
            cutoffEpochMs = (clock.now() - PROCESSED_RETENTION).toEpochMilliseconds()
        )

        return SyncReplayResult.Completed(
            attemptedCount = replayCandidates.size,
            processedCount = processedCount,
            failedCount = failedCount,
            conflictCount = conflictCount,
            requeuedCount = requeuedCount,
            pendingRemaining = outboxRepository.getPendingEvents().size,
            failedRemaining = outboxRepository.getFailedEvents().size,
            message = lastFailureMessage
        )
    }

    private companion object {
        const val DEFAULT_BATCH_SIZE = 25
        val PROCESSED_RETENTION = 7.days
    }
}

sealed interface SyncReplayResult {
    data object Idle : SyncReplayResult

    data class Unavailable(
        val pendingCount: Int,
        val failedCount: Int
    ) : SyncReplayResult

    data class Completed(
        val attemptedCount: Int,
        val processedCount: Int,
        val failedCount: Int,
        val conflictCount: Int,
        val requeuedCount: Int,
        val pendingRemaining: Int,
        val failedRemaining: Int,
        val message: String?
    ) : SyncReplayResult
}
