package id.azureenterprise.cassy.desktop

object DesktopLabels {
    object Dashboard {
        const val title = "Ringkasan operasional"
        const val subtitle = "Cek kesiapan kerja, lihat kendala utama, lalu pilih langkah berikutnya."
        const val issuesEmpty = "Tidak ada kendala utama saat ini. Terminal siap dipakai sesuai langkah berikutnya."
    }

    object Cashier {
        const val title = "Kasir"
        const val subtitle = "Scan barang, periksa keranjang, lalu selesaikan pembayaran."
        const val unavailableTitle = "Kasir belum siap"
        const val unavailableDetail = "Kasir belum siap dipakai."
        const val emptyBasket = "Belum ada barang di keranjang. Scan barcode atau cari SKU untuk mulai transaksi."
        const val finalizationTitle = "Selesaikan transaksi"
        const val finalizationIntro = "Setelah keranjang dicek, tambahkan member bila ada lalu lanjutkan ke pembayaran."
        const val reviewFirst = "Periksa keranjang dulu sebelum pembayaran dibuka."
        const val reviewCta = "Periksa keranjang"
        const val reviewedCta = "Keranjang sudah dicek"
        const val memberTitle = "Member"
        const val memberHelper = "Opsional. Isi bila pelanggan punya member."
        const val memberSkipped = "Langkah member dilewati untuk transaksi ini."
        const val memberNumberHelper = "Kosongkan bila tidak ada."
        const val memberNameHelper = "Opsional untuk pencatatan lokal."
        const val donationTitle = "Donasi"
        const val donationHelper = "Opsional. Tawarkan hanya bila memang dipakai di toko ini."
        const val donationAmountHelper = "Catatan lokal. Tidak mengubah total transaksi inti."
        const val receiptTitle = "Struk"
        const val receiptReady = "Struk siap dicetak."
        const val receiptPending = "Struk belum tersedia."
        const val receiptPreviewPending = "Preview lengkap akan muncul setelah transaksi final selesai."
        const val paymentCashHelper = "Masukkan nominal yang dibayar pelanggan."
        const val paymentCta = "Selesaikan pembayaran"
        const val clearSale = "Kosongkan"
        const val printLastReceipt = "Cetak struk terakhir"
        const val reprintReceipt = "Cetak ulang struk"
        const val paymentReady = "Siap diselesaikan"
        const val paymentWaiting = "Menunggu nominal pembayaran"

        fun milestoneHint(step: CashierMilestone): String = when (step) {
            CashierMilestone.ScanBarang -> "Mulai dengan scan barang atau cari SKU bila barcode tidak terbaca."
            CashierMilestone.ReviewKeranjang -> "Periksa jumlah, subtotal, dan total sebelum lanjut."
            CashierMilestone.Member -> "Tambahkan member bila ada, atau lewati langkah ini."
            CashierMilestone.Donasi -> "Tawarkan donasi bila pelanggan ingin berpartisipasi."
            CashierMilestone.Pembayaran -> "Terima pembayaran, cek kembalian, lalu selesaikan transaksi."
            CashierMilestone.Selesai -> "Transaksi selesai. Cetak struk atau mulai transaksi berikutnya."
        }

        fun scanLaneHint(step: CashierMilestone): String = when (step) {
            CashierMilestone.ScanBarang -> "Scan barang untuk mulai transaksi."
            CashierMilestone.ReviewKeranjang -> "Periksa jumlah dan total sebelum lanjut."
            CashierMilestone.Member -> "Tambahkan member bila ada, atau lewati."
            CashierMilestone.Donasi -> "Input nominal donasi bila ada."
            CashierMilestone.Pembayaran -> "Terima pembayaran and cek kembalian."
            CashierMilestone.Selesai -> "Transaksi selesai. Siap mulai transaksi berikutnya."
        }
    }

    object Inventory {
        const val title = "Inventori"
        const val subtitle = "Kondisi stok saat ini dan daftar produk dipisah agar layar tetap ringkas dan operator tidak salah konteks."
    }

    object Operations {
        const val title = "Operasional"
        const val subtitle = "Tugas operasional berat dipisah per area agar cepat dipahami dan aman dipakai harian."
    }
}
