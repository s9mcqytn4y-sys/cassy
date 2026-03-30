package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.OperationalIssue

interface OperationalHardwarePort {
    suspend fun getHardwareIssues(): List<OperationalIssue>
}

object NoopOperationalHardwarePort : OperationalHardwarePort {
    override suspend fun getHardwareIssues(): List<OperationalIssue> = emptyList()
}
