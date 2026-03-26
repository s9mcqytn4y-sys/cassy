package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.db.OutboxEvent

interface SyncReplayPort {
    val isAvailable: Boolean

    suspend fun replay(event: OutboxEvent): SyncReplayOutcome
}

sealed interface SyncReplayOutcome {
    data object Acknowledged : SyncReplayOutcome
    data class RetryableFailure(val message: String) : SyncReplayOutcome
    data class Conflict(val message: String) : SyncReplayOutcome
    data class FatalFailure(val message: String) : SyncReplayOutcome
}

object NoopSyncReplayPort : SyncReplayPort {
    override val isAvailable: Boolean = false

    override suspend fun replay(event: OutboxEvent): SyncReplayOutcome {
        return SyncReplayOutcome.RetryableFailure("Backend sync belum dikonfigurasi")
    }
}
