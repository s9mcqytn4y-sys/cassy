package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.kernel.domain.Shift

class ShiftService(
    private val kernelRepository: KernelRepository,
    private val accessService: AccessService
) {
    suspend fun getActiveShift(): Shift? {
        val binding = kernelRepository.getTerminalBinding() ?: return null
        return kernelRepository.getActiveShift(binding.terminalId)
    }

    suspend fun startShift(openingCash: Double): Result<Shift> {
        if (openingCash < 0) return Result.failure(IllegalArgumentException("Opening cash tidak boleh negatif"))
        val operator = accessService.requireCapability(AccessCapability.START_SHIFT).getOrElse { return Result.failure(it) }
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Terminal belum terikat"))
        val businessDay = kernelRepository.getActiveBusinessDay()
            ?: return Result.failure(IllegalStateException("Business day belum aktif"))
        if (kernelRepository.getActiveShift(binding.terminalId) != null) {
            return Result.failure(IllegalStateException("Shift aktif sudah ada di terminal ini"))
        }

        val shift = kernelRepository.openShift(
            id = IdGenerator.nextId("shift"),
            businessDayId = businessDay.id,
            terminalId = binding.terminalId,
            openingCash = openingCash,
            openedBy = operator.id
        )
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Shift ${shift.id} dimulai dengan opening cash $openingCash",
            level = "INFO"
        )
        return Result.success(shift)
    }

    suspend fun endShift(closingCash: Double): Result<Shift> {
        if (closingCash < 0) return Result.failure(IllegalArgumentException("Closing cash tidak boleh negatif"))
        val operator = accessService.requireCapability(AccessCapability.END_SHIFT).getOrElse { return Result.failure(it) }
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Terminal belum terikat"))
        val shift = kernelRepository.getActiveShift(binding.terminalId)
            ?: return Result.failure(IllegalStateException("Tidak ada shift aktif"))

        val closed = kernelRepository.closeShift(shift.id, closingCash, operator.id)
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Shift ${closed.id} ditutup dengan closing cash $closingCash",
            level = "INFO"
        )
        return Result.success(closed)
    }
}
