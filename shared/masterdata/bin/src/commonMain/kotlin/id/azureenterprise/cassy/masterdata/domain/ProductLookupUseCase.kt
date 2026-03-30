package id.azureenterprise.cassy.masterdata.domain

import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl

class ProductLookupUseCase(
    private val repository: ProductLookupRepositoryImpl,
    private val normalizer: BarcodeNormalizer
) {
    suspend fun execute(input: String): ProductLookupResult {
        if (input.isBlank()) {
            return ProductLookupResult.InvalidInput("Input cannot be empty")
        }

        val normalized = normalizer.normalize(input)

        // 1. Try barcode lookup first
        val barcodeResult = repository.findByBarcode(normalized)
        if (barcodeResult !is ProductLookupResult.NotFound) {
            return barcodeResult
        }

        // 2. Try SKU lookup
        val skuResult = repository.findBySku(normalized)
        if (skuResult !is ProductLookupResult.NotFound) {
            return skuResult
        }

        return ProductLookupResult.NotFound
    }
}
