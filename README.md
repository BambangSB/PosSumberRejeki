# 🏪 Sumber Rejeki POS (Point of Sale)

<p align="center">
  <b>Aplikasi Kasir & Manajemen Toko Sembako / Retail Berbasis Android (100% Offline-First)</b>
  <br />
  <i>Dibangun dengan Kotlin Modern, Jetpack Compose Material 3, Clean Architecture, dan Sistem Lisensi Kriptografi Offline.</i>
</p>

---

## 📖 Tentang Aplikasi (About The App)

**Sumber Rejeki POS** adalah solusi aplikasi kasir digital (*Point of Sale*) terintegrasi yang dirancang khusus untuk toko kelontong, pedagang sembako, minimarket mandiri, dan UMKM. 

Aplikasi ini mengusung prinsip **100% Offline-First**, artinya seluruh operasional kasir harian—mulai dari pemindaian barcode dengan kamera, kalkulasi keranjang, pencetakan struk via thermal printer Bluetooth, hingga pelaporan laba/rugi dan validasi lisensi—berjalan tanpa memerlukan koneksi internet maupun server cloud eksternal.

---

## ✨ Fitur Utama (Key Features)

### 🔐 1. Otentikasi & Manajemen Multi-User (RBAC)
- **Role Pengguna**:
  - **Admin (Owner)**: Akses penuh ke katalog, harga beli modal, laporan keuangan, manajemen staf, aktivasi lisensi, dan pencadangan database.
  - **Kasir (Staff)**: Akses terfokus pada transaksi kasir, pemindai barcode, dan pencetakan struk.
- **Login PIN Cepat**: 6-digit PIN yang aman dengan algoritma *Salted SHA-256 Hash*.
- **Onboarding Admin Pertama Kali**: Pengaturan akun pemilik otomatis saat database baru diinisialisasi.

### 📦 2. Manajemen Produk & Inventori Cerdas
- **Informasi Lengkap Produk**: Barcode/SKU, Nama Barang, Kategori, Harga Modal (*Cost Price*), Harga Jual (*Sell Price*), dan Jumlah Stok.
- **Pre-populated Catalog**: Otomatis memuat data katalog sembako/grocery populer Indonesia saat instalasi pertama (Beras, Minyak Goreng, Gula, Kopi, Indomie, Susu, Sabun, dll).
- **Filter & Pencarian Real-Time**: Pencarian cepat berdasarkan nama/barcode dan filter chip berdasarkan kategori barang.

### 🛒 3. Point of Sale (POS) & Transaksi Kasir
- **Keranjang Belanja Persisten**: Data keranjang disimpan langsung ke Room SQLite (`CartDraftEntity`), mencegah kehilangan transaksi belanja saat aplikasi tidak sengaja tertutup atau baterai habis.
- **Multi-Metode Pembayaran**:
  - 💵 Tunai (Cash)
  - 📱 QRIS
  - 🏦 Transfer Bank
  - 💳 Kartu Debit
- **Kalkulator Kembalian Otomatis**: Tombol nominal cepat (*Uang Pas, Rp10.000, Rp20.000, Rp50.000, Rp100.000*).
- **Transaksi ACID Atomik**: Pemotongan stok barang dan penyimpanan riwayat penjualan dieksekusi dalam satu transaksi database tunggal untuk menjaga integritas data.

### 📷 4. Pemindai Barcode Real-Time (CameraX + ML Kit)
- Pemindaian cepat memanfaatkan kamera perangkat dengan Google ML Kit Barcode Scanning.
- *Interactive Bottom Sheet*:
  - Jika barcode sudah terdaftar: Langsung atur kuantitas dan masukkan ke keranjang belanja.
  - Jika barcode belum terdaftar: Tersedia tombol pintas instan untuk mendaftarkan barang baru dengan barcode terisi otomatis.

### 🖨️ 5. Pencetakan Struk Thermal Bluetooth (ESC/POS)
- Integrasi langsung dengan printer thermal mini Bluetooth (ukuran kertas standar 58mm dan 80mm).
- Format struk profesional: Header toko, no invoice unik, tanggal & waktu, rincian barang, subtotal, metode pembayaran, uang tunai, kembalian, dan ucapan terima kasih.
- Opsi cetak langsung saat pembayaran berhasil atau cetak ulang dari menu riwayat transaksi.

### 📈 6. Riwayat Transaksi & Laporan Keuangan
- **Riwayat Penjualan**: Filter transaksi per rentang tanggal dan status.
- **Analisis Finansial Mendalam**:
  - **Omzet Penjualan (*Gross Revenue*)**
  - **Laba / Keuntungan Bersih (*Net Profit*)**: Dihitung otomatis dari selisih harga jual dan harga modal per item yang terjual.
  - **Total Transaksi & Nilai Rata-rata Keranjang (*Average Order Value / AOV*)**.
  - **Produk Terlaris (*Top Selling Products*)**.

### 🛡️ 7. Sistem Lisensi Offline Kriptografi RSA-256
- Validasi lisensi perangkat secara **100% Offline** menggunakan verifikasi *Digital Signature* (RSA 2048 + SHA256withRSA).
- Lisensi terikat secara kriptografis dengan **Installation ID** unik perangkat.
- **Tingkatan Lisensi (Tiers)**:
  - `FREE`: Cocok untuk toko perorangan skala kecil.
  - `PREMIUM`: Membuka kuota staf kasir tak terbatas dan fitur pelaporan lanjutan.

### 💾 8. Backup & Restore Database Lokal
- Fitur ekspor dan impor file database SQLite `.db` secara mandiri untuk proteksi dan kemudahan migrasi perangkat.

---

## 🏗️ Arsitektur & Tech Stack

Aplikasi ini dirancang mengikuti panduan arsitektur resmi Android (*Modern Android Development / MAD*):

```
                       ┌───────────────────────────────┐
                       │   Jetpack Compose UI (M3)    │
                       └──────────────┬────────────────┘
                                      │
                       ┌──────────────▼────────────────┐
                       │          ViewModels           │
                       └──────────────┬────────────────┘
                                      │
                       ┌──────────────▼────────────────┐
                       │       Domain Use Cases        │
                       └──────────────┬────────────────┘
                                      │
                       ┌──────────────▼────────────────┐
                       │     Repository Interfaces     │
                       └──────────────┬────────────────┘
                                      │
                 ┌────────────────────┴────────────────────┐
                 │                                         │
    ┌────────────▼────────────┐               ┌────────────▼────────────┐
    │   Room Database (SQLite)│               │ DataStore & Peripherals │
    └─────────────────────────┘               └─────────────────────────┘
```

| Komponen | Spesifikasi & Library |
| :--- | :--- |
| **Bahasa Pemrograman** | Kotlin 1.9+ / Target Java 17 |
| **UI Toolkit** | Jetpack Compose (BOM 2024.06.00) + Material Design 3 |
| **Navigasi** | Jetpack Navigation Compose 2.7.7 |
| **Arsitektur** | Clean Architecture (Data, Domain, Presentation) + MVVM |
| **Dependency Injection** | AppContainer Pattern (Manual DI yang ringan & bebas boilerplate) |
| **Database Lokal** | Room Database 2.6.1 |
| **Penyimpanan Preferensi** | Jetpack DataStore Preferences 1.1.1 |
| **Scanner Kamera** | AndroidX CameraX 1.3.4 + Google ML Kit Barcode Scanning 17.3.0 |
| **Pencetakan Bluetooth** | ESC/POS Command Protocol Engine |
| **Keamanan** | RSA-256 Asymmetric Cryptography, SHA-256 + Salt Hasher |
| **Asinkron & Reaktif** | Kotlin Coroutines & StateFlow |

---

## 📂 Struktur Direktori Proyek

```
app/src/main/java/com/sumberrejeki/pos/
├── AppContainer.kt                    # Dependency Injection Container
├── MainActivity.kt                    # Single Activity Host
├── PosApplication.kt                  # Base Application
├── data/
│   ├── local/                         # Room Database, DAOs, Entities
│   │   ├── dao/                       # UserDao, ProductDao, CartDraftDao, OrderDao
│   │   └── entity/                    # UserEntity, ProductEntity, OrderEntity, dll.
│   ├── preferences/                   # DataStore Preferences (Sesi & Lisensi)
│   └── repository/                    # Implementasi Repository
├── domain/
│   ├── model/                         # Data Models murni (Product, Order, User, dll.)
│   ├── repository/                    # Kontrak Repository (Interfaces)
│   └── usecase/                       # Business Logic / Use Cases
│       ├── auth/                      # Login, Register, Staff Use Cases
│       ├── cart/                      # Cart Draft Use Cases
│       ├── checkout/                  # Process Checkout & Change Calculator
│       ├── product/                   # CRUD & Search Product Use Cases
│       ├── report/                    # Financial & Dashboard Metrics
│       └── license/                   # RSA Verification & License Status
├── presentation/
│   ├── auth/                          # Layar Login PIN & Register Admin
│   ├── checkout/                      # Layar Konfirmasi Pembayaran & Sukses
│   ├── components/                    # Komponen UI Reusable (PIN Pad, TopBar, Dialog, dll.)
│   ├── history/                       # Layar Riwayat & Laporan Keuangan
│   ├── home/                          # Dashboard & Ringkasan Metrik
│   ├── navigation/                    # Rute & NavHost Compose
│   ├── product/                       # Katalog Produk & Form Tambah/Edit
│   ├── profile/                       # Profil, Manajemen Staf & Lisensi
│   ├── scan/                          # Kamera Pemindai Barcode
│   └── theme/                         # Tema Warna Hijau Toko, Tipografi, Shape
└── utils/
    ├── backup/                        # SQLite Backup & Restore Manager
    ├── printer/                       # ESC/POS Thermal Bluetooth Driver & Formatter
    └── security/                      # RSA Helper, Key Verifier & PIN Hasher
```

---

## 🚀 Panduan Memulai (Getting Started)

### Prasyarat:
- **Android Studio** (Ladybug / Koala atau versi lebih baru)
- **JDK**: Java 17
- **Perangkat / Emulator**: Android 7.0 (API Level 24) ke atas

### Langkah Instalasi:
1. Clone repositori ini:
   ```bash
   git clone https://github.com/BambangSB/PosSumberRejeki.git
   cd PosSumberRejeki
   ```
2. Buka folder proyek di Android Studio.
3. Tunggu hingga proses **Gradle Sync** selesai secara otomatis.
4. Hubungkan perangkat Android via USB Debugging atau jalankan Android Emulator.
5. Klik **Run** (`Shift + F10`) untuk mengompilasi dan menginstal aplikasi.

---

## 👨‍💻 Pengembang & Kontribusi

Dikembangkan oleh **[Bambang Setyabudi](https://github.com/BambangSB)**.

Kontribusi, *pull request*, dan saran perbaikan fitur sangat dipersilakan! Silakan buat *issue* terlebih dahulu untuk mendiskusikan perubahan yang ingin Anda lakukan.

---

## 📄 Lisensi
Hak Cipta © 2026 **Sumber Rejeki POS**. Seluruh hak cipta dilindungi.
