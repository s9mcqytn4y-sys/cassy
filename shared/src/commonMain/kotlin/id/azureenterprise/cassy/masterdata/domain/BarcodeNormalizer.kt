package id.azureenterprise.cassy.masterdata.domain

/**
 * Ensures barcodes are consistently formatted before lookup.
 * Trims whitespace and handles common prefix/suffix issues if any.
 */
class BarcodeNormalizer {
    fun normalize(rawBarcode: String): String {
        return rawBarcode.trim()
    }
}
