package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.*
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock

class FakeKernelRepository : id.azureenterprise.cassy.kernel.data.KernelRepository(
    database = null,
    ioDispatcher = kotlin.coroutines.EmptyCoroutineContext,
    clock = Clock.System
) {
    private var terminalBinding: TerminalBinding? = null
    private val operators = mutableMapOf<String, OperatorAccount>()
    private var activeSession: AccessSession? = null
    private var activeBusinessDay: BusinessDay? = null
    private val shifts = mutableMapOf<String, Shift>()
    val audits = mutableListOf<String>()
    val events = mutableListOf<String>()

    override suspend fun isBusinessDayOpen(): Boolean = activeBusinessDay?.status == "OPEN"
    override suspend fun getTerminalBinding(): TerminalBinding? = terminalBinding
    override suspend fun upsertTerminalBinding(binding: TerminalBinding) { terminalBinding = binding }
    override suspend fun listActiveOperators(): List<OperatorAccount> = operators.values.filter { it.isActive }
    override suspend fun getOperatorById(id: String): OperatorAccount? = operators[id]
    override suspend fun upsertOperator(operator: OperatorAccount) { operators[operator.id] = operator }
    override suspend fun updateOperatorAccessState(operatorId: String, failedAttempts: Int, lockedUntil: Instant?, lastLoginAt: Instant?) {
        operators[operatorId]?.let {
            operators[operatorId] = it.copy(failedAttempts = failedAttempts, lockedUntil = lockedUntil, lastLoginAt = lastLoginAt)
        }
    }
    override suspend fun getActiveAccessSession(): AccessSession? = activeSession
    override suspend fun createAccessSession(session: AccessSession) { activeSession = session }
    override suspend fun clearActiveAccessSessions() { activeSession = null }
    override suspend fun getActiveBusinessDay(): BusinessDay? = activeBusinessDay
    override suspend fun openBusinessDay(id: String): BusinessDay {
        val day = BusinessDay(id, Clock.System.now(), null, "OPEN")
        activeBusinessDay = day
        return day
    }
    override suspend fun closeBusinessDay(id: String): BusinessDay {
        val day = activeBusinessDay?.copy(closedAt = Clock.System.now(), status = "CLOSED") ?: error("No active day")
        activeBusinessDay = null
        return day
    }
    override suspend fun getActiveShift(terminalId: String): Shift? = shifts.values.find { it.terminalId == terminalId && it.status == "OPEN" }
    override suspend fun openShift(id: String, businessDayId: String, terminalId: String, openingCash: Double, openedBy: String): Shift {
        val shift = Shift(id, businessDayId, terminalId, Clock.System.now(), openingCash, null, 0.0, openedBy, null, "OPEN")
        shifts[id] = shift
        return shift
    }
    override suspend fun closeShift(id: String, closingCash: Double, closedBy: String): Shift {
        val shift = shifts[id]?.copy(closedAt = Clock.System.now(), closingCash = closingCash, closedBy = closedBy, status = "CLOSED") ?: error("No shift")
        shifts[id] = shift
        return shift
    }
    override suspend fun insertAudit(id: String, message: String, level: String) { audits.add(message) }
    override suspend fun insertEvent(id: String, type: String, payload: String) {
        events.add("$type|$payload")
    }
}
