package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.inventory.di.inventoryDatabaseModule
import id.azureenterprise.cassy.inventory.di.inventoryModule
import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.BusinessDayService
import id.azureenterprise.cassy.kernel.application.CashControlService
import id.azureenterprise.cassy.kernel.application.OperationalControlService
import id.azureenterprise.cassy.kernel.application.OperationalSalesPort
import id.azureenterprise.cassy.kernel.application.ShiftService
import id.azureenterprise.cassy.kernel.application.ShiftClosingService
import id.azureenterprise.cassy.kernel.di.databaseModule
import id.azureenterprise.cassy.kernel.di.kernelModule
import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.masterdata.di.masterDataDatabaseModule
import id.azureenterprise.cassy.masterdata.di.masterDataModule
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.di.salesDatabaseModule
import id.azureenterprise.cassy.sales.di.salesModule
import id.azureenterprise.cassy.sales.di.salesPlatformModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun startDesktopKoin() {
    startKoin {
        modules(
            kernelModule,
            databaseModule,
            masterDataModule,
            masterDataDatabaseModule,
            salesModule,
            salesDatabaseModule,
            salesPlatformModule,
            inventoryModule,
            inventoryDatabaseModule,
            module {
                single<OperationalSalesPort> { DesktopOperationalSalesPort(get()) }
                single<CashierHardwarePort> { DesktopNoopCashierHardwarePort() }
                single {
                    DesktopAppController(
                        accessService = get<AccessService>(),
                        businessDayService = get<BusinessDayService>(),
                        shiftService = get<ShiftService>(),
                        cashControlService = get<CashControlService>(),
                        shiftClosingService = get<ShiftClosingService>(),
                        operationalControlService = get<OperationalControlService>(),
                        productRepository = get<ProductRepository>(),
                        productLookupUseCase = get<ProductLookupUseCase>(),
                        inventoryService = get<InventoryService>(),
                        salesService = get<SalesService>(),
                        hardwarePort = get<CashierHardwarePort>()
                    )
                }
            }
        )
    }
}

private class DesktopOperationalSalesPort(
    private val salesService: SalesService
) : OperationalSalesPort {
    override suspend fun getShiftSalesSummary(shiftId: String) = salesService.getShiftSalesSummary(shiftId)
}
