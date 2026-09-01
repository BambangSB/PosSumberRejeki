# 🏪 Sumber Rejeki POS (Point of Sale)

A modern, fast, and 100% offline-first Android Point of Sale (POS) application built for retail, grocery stores (*sembako*), and SMEs (UMKM).

Built natively with **Kotlin**, **Jetpack Compose (Material 3)**, and **Clean Architecture**.

---

## ✨ Fitur Utama (Key Features)

- 🔐 **Autentikasi Multi-User & RBAC**:
  - Role Admin (Owner) dan Kasir (Staff).
  - Login cepat menggunakan 6-Digit PIN terenkripsi (PBKDF2/Salted SHA-256).
  - Manajemen staf kasir dengan kontrol akses.

- 📦 **Manajemen Katalog & Inventori**:
  - CRUD Produk lengkap: Barcode, Nama, Kategori, Harga Modal (*Cost Price*), Harga Jual (*Sell Price*), dan Stok.
  - Pencarian instan dan filter kategori.
  - Pre-populated data produk sembako populer saat instalasi pertama.

- 🛒 **Point of Sale (POS) & Transaksi Cepat**:
  - Keranjang belanja draft tersimpan persisten di Room SQLite (tidak hilang jika aplikasi tertutup).
  - Multi-metode pembayaran: Tunai (Cash), QRIS, Transfer Bank, dan Debit.
  - Kalkulator kembalian otomatis & quick cash chips (Uang pas, Rp10.000, Rp20.000, Rp50.000, Rp100.000).
  - Transaksi ACID (pemotongan stok dan pencatatan pesanan secara atomik).

- 📷 **Pemindai Barcode Cepat**:
  - Integrasi CameraX dan Google ML Kit Barcode Scanning.
  - Tambah kuantitas produk instan ke keranjang atau tambah produk baru otomatis jika barcode belum terdaftar.

- 🖨️ **Cetak Struk Bluetooth Thermal**:
  - Dukungan printer thermal Bluetooth (ESC/POS 58mm / 80mm).
  - Format struk lengkap dan rapi (Header, No Invoice, Items, Subtotal, Tunai, Kembalian, Footer).
  - Cetak struk langsung setelah pembayaran dan cetak ulang dari riwayat.

- 📈 **Riwayat & Laporan Keuangan**:
  - Riwayat transaksi harian dengan filter status dan rentang tanggal.
  - Laporan omzet penjualan (*Gross Revenue*), laba bersih (*Net Profit*), jumlah transaksi, dan produk terlaris.

- 🛡️ **Sistem Lisensi Offline (RSA-256 Signature)**:
  - Validasi lisensi 100% offline menggunakan enkripsi asimetris RSA.
  - Lisensi terikat secara kriptografis dengan Unique Installation ID perangkat.
  - Tier FREE dan PREMIUM.

- 💾 **Backup & Restore Database**:
  - Ekspor dan impor database SQLite secara lokal untuk keamanan data toko.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 1.9+ (JVM 17)
- **UI Framework**: Jetpack Compose, Material 3, Navigation Compose
- **Architecture**: Clean Architecture (Data, Domain, Presentation) + MVVM
- **Local Storage**: Room Database 2.6.1 (SQLite), Jetpack DataStore Preferences
- **Barcode Scanner**: AndroidX CameraX 1.3.4 + Google ML Kit Barcode Scanning
- **Thermal Printer**: Bluetooth ESC/POS Commands
- **Security**: RSA-256 Digital Signatures (SHA256withRSA), SHA-256 + Salt PIN Hashing
- **Asynchronous**: Kotlin Coroutines & Flow (StateFlow / SharedFlow)

---

## 📂 Struktur Proyek

```
app/src/main/java/com/sumberrejeki/pos/
├── AppContainer.kt          # Manual Dependency Injection Container
├── MainActivity.kt          # Single Activity entry point
├── PosApplication.kt        # Application class & container initialization
├── data/
│   ├── local/               # Room Database, DAOs, Entities
│   ├── preferences/         # DataStore User Session & License prefs
│   └── repository/          # Repository Implementations
├── domain/
│   ├── model/               # Domain Models (Product, Order, User, etc.)
│   ├── repository/          # Repository Interfaces
│   └── usecase/             # Business Logic Use Cases
├── presentation/
│   ├── auth/                # Login PIN, Register Admin
│   ├── checkout/            # Cart checkout, payment, receipt preview
│   ├── components/          # Reusable Compose UI components
│   ├── history/             # Transaction history & Financial reports
│   ├── home/                # Dashboard overview & Quick actions
│   ├── navigation/          # NavHost & Route definitions
│   ├── product/             # Product catalog & Add/Edit form
│   ├── profile/             # Settings, Staff management, License upgrade
│   ├── scan/                # CameraX Barcode Scanner
│   └── theme/               # Material 3 Color, Type, Shape, Theme
└── utils/
    ├── backup/              # SQLite Database backup/restore
    ├── printer/             # ESC/POS Bluetooth printer engine & formatter
    └── security/            # RSA key helper & PIN hasher
```

---

## 🚀 Cara Menjalankan (Getting Started)

1. Clone repositori ini:
   ```bash
   git clone <URL_REPOSITORY>
   cd sumberrejeki
   ```
2. Buka project di **Android Studio** (Koala / Ladybug atau versi lebih baru).
3. Pastikan JDK diatur ke **Java 17**.
4. Hubungkan perangkat Android fisik atau gunakan Android Virtual Device (AVD).
5. Build & Run (`Shift + F10`).

---

## 📄 Lisensi
Hak Cipta © 2026 Sumber Rejeki POS.
