package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.IdGenerator

class BusinessDayService(
    private val kernelRepository: KernelRepository
) {
    suspend fun isOpen(): Boolean {
        return kernelRepository.isBusinessDayOpen()
    }

    suspend fun openNewDay(): Result<String> {
        if (isOpen()) return Result.failure(Exception("Business day already open"))

        val id = IdGenerator.nextId("bd")
        kernelRepository.openBusinessDay(id)
        kernelRepository.insertAudit("audit_open_day_$id", "Business day opened: $id", "INFO")

        return Result.success(id)
    }

    suspend fun getActiveShift(terminalId: String) = kernelRepository.getActiveShift(terminalId)
}
