package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.BusinessDay
import id.azureenterprise.cassy.kernel.domain.IdGenerator

class BusinessDayService(
    private val kernelRepository: KernelRepository,
    private val accessService: AccessService
) {
    suspend fun isOpen(): Boolean = kernelRepository.isBusinessDayOpen()

    suspend fun getActiveBusinessDay(): BusinessDay? = kernelRepository.getActiveBusinessDay()

    suspend fun openNewDay(): Result<BusinessDay> {
        accessService.requireCapability(AccessCapability.OPEN_DAY).getOrElse { return Result.failure(it) }
        if (isOpen()) return Result.failure(IllegalStateException("Business day sudah terbuka"))

        val opened = kernelRepository.openBusinessDay(IdGenerator.nextId("bd"))
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Business day ${opened.id} dibuka",
            level = "INFO"
        )
        return Result.success(opened)
    }

    suspend fun closeCurrentDay(): Result<BusinessDay> {
        accessService.requireCapability(AccessCapability.CLOSE_DAY).getOrElse { return Result.failure(it) }
        val activeDay = kernelRepository.getActiveBusinessDay()
            ?: return Result.failure(IllegalStateException("Tidak ada business day aktif"))
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Terminal belum terikat"))
        if (kernelRepository.getActiveShift(binding.terminalId) != null) {
            return Result.failure(IllegalStateException("Shift aktif harus ditutup sebelum close day"))
        }

        val closed = kernelRepository.closeBusinessDay(activeDay.id)
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Business day ${closed.id} ditutup",
            level = "INFO"
        )
        return Result.success(closed)
    }
}
