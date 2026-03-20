package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.data.OutboxRepository
import id.azureenterprise.cassy.kernel.domain.SyncLevel
import id.azureenterprise.cassy.kernel.domain.SyncStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * R5 Hardening: Provides hooks to update sync metadata and track visibility.
 * This service acts as the 'writer' for sync health, while ReportingQueryFacade is the 'reader'.
 */
class SyncVisibilityService(
    private val kernelRepository: KernelRepository,
    private val outboxRepository: OutboxRepository,
    private val clock: Clock
) {
    companion object {
        const val LAST_SYNC_SUCCESS_KEY = "sync.last_success_timestamp"
        const val LAST_SYNC_ERROR_KEY = "sync.last_error_message"
    }

    /**
     * Called by the sync engine (implementation pending) when a batch is successfully acknowledged by HQ.
     */
    suspend fun recordSyncSuccess() {
        val now = clock.now().toEpochMilliseconds()
        kernelRepository.upsertMetadata(LAST_SYNC_SUCCESS_KEY, now.toString())
        kernelRepository.upsertMetadata(LAST_SYNC_ERROR_KEY, "") // Clear last error

        kernelRepository.insertAudit(
            id = "audit_sync_success_${now}",
            message = "Sync metadata updated: success at ${clock.now()}",
            level = "INFO"
        )
    }

    /**
     * Called by the sync engine when a terminal failure occurs.
     */
    suspend fun recordSyncFailure(message: String) {
        val now = clock.now().toEpochMilliseconds()
        kernelRepository.upsertMetadata(LAST_SYNC_ERROR_KEY, message)

        kernelRepository.insertAudit(
            id = "audit_sync_failure_${now}",
            message = "Sync failure recorded: $message",
            level = "WARN"
        )
    }

    /**
     * For R5 Visibility Lite: Provides a quick check if sync is 'Active'.
     * 'Active' means the outbox is not growing indefinitely and the last success was recent.
     */
    suspend fun isSyncHealthy(): Boolean {
        val pending = outboxRepository.getPendingEvents().size
        val lastSuccess = kernelRepository.getMetadata(LAST_SYNC_SUCCESS_KEY)?.toLongOrNull()
            ?: return pending == 0 // If never synced, healthy only if nothing to sync

        val ageMs = clock.now().toEpochMilliseconds() - lastSuccess
        return pending < 100 && ageMs < 3600_000 // Healthy if < 100 pending and synced in last hour
    }
}
