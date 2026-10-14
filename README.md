# BLE Radar

Aplikasi Android berbasis Kotlin dan Jetpack Compose untuk memindai advertising Bluetooth Low Energy (BLE), memantau RSSI, memperkirakan kedekatan satu target, dan menyimpan riwayat perangkat di ponsel.

## Prasyarat

- Android Studio dengan Android SDK Platform 35 dan JDK 21. Gradle Wrapper proyek memakai Gradle 8.14.5; jangan pilih JDK 25 sebagai Gradle JDK.
- Perangkat fisik Android 8.0 (API 26) atau lebih baru yang mendukung BLE. Pemindaian BLE nyata memerlukan perangkat fisik.
- Bluetooth dan layanan lokasi ponsel aktif. Berikan izin **Perangkat di sekitar** dan **Lokasi** saat aplikasi memintanya.

Jika Android Studio tidak mengenali nama JDK `zulu-21` yang tersimpan di konfigurasi proyek, pilih instalasi JDK 21 pada **Settings/Preferences > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**, lalu sinkronkan proyek.

## Kompilasi dan menjalankan

1. Buka direktori `ble-radar` sebagai proyek di Android Studio dan tunggu Gradle Sync selesai.
2. Pilih konfigurasi `app` dan perangkat Android fisik, lalu jalankan dari Android Studio.
3. Untuk membuat APK debug dari terminal, pastikan `JAVA_HOME` mengarah ke JDK 21, lalu jalankan:

   ```bash
   ./gradlew :app:assembleDebug
   ```

   Di Windows gunakan `gradlew.bat :app:assembleDebug`. APK hasil kompilasi berada di `app/build/outputs/apk/debug/app-debug.apk`.

Perintah di atas membuat APK debug untuk evaluasi. APK yang sudah ada di direktori `build` bisa berasal dari versi source sebelumnya; kompilasi ulang sebelum mengirim artefak.

## Cara menggunakan

1. Di tab **Pindai**, tekan **Mulai pemindaian** dan izinkan akses yang diminta Android.
2. Daftar menampilkan perangkat BLE yang baru terdeteksi, diurutkan dari RSSI terkuat. Gunakan pencarian nama/alamat MAC atau filter batas minimum RSSI.
3. Ketuk kartu perangkat untuk melihat radar, estimasi jarak, RSSI mentah, kestabilan sampel, dan waktu terakhir terlihat.
4. Tekan **Hentikan pemindaian** untuk berhenti. Tab **Riwayat** menyimpan perangkat yang pernah terdeteksi beserta waktu terakhir dan RSSI terakhir.

## Arsitektur dan teknologi

| Modul | Tanggung jawab |
| --- | --- |
| `:app` | Compose UI, `TrackerViewModel`, izin, lifecycle, dan konfigurasi Hilt |
| `:core:model` | Model pembacaan perangkat dan pemetaan zona RSSI/jarak |
| `:core:ble` | Adapter Android BLE dan aliran hasil scan melalui `callbackFlow` |
| `:core:storage` | Entity, DAO, dan database Room untuk riwayat |

Pola **MVVM** memisahkan UI Compose dari state dan logika pemindaian di `TrackerViewModel`. Hilt menyediakan `BleScanner` dan database Room. `MainActivity` menangani izin, status Bluetooth/lokasi, dan lifecycle. `TrackerViewModel` mengurutkan perangkat berdasarkan RSSI, mempertahankan state saat rotasi, serta menyimpan pembacaan ke Room. UI membaca state dengan `collectAsStateWithLifecycle`.

Scan aktif hanya saat aplikasi di foreground. Saat aplikasi masuk background, koleksi `callbackFlow` dibatalkan dan `stopScan` dipanggil. Jika pengguna belum menekan **Hentikan pemindaian**, scan dilanjutkan ketika aplikasi kembali ke foreground. Radar menandai target hilang jika tidak terdeteksi selama 10 detik saat scan aktif.

## Disclosure penggunaan alat bantu

OpenAI Codex digunakan untuk membantu penyusunan struktur proyek, implementasi Kotlin/Compose, perbaikan UI, peninjauan kode, dan penulisan dokumentasi ini. Perubahan UI terakhir diperiksa dengan kompilasi langsung Kotlin 2.0 dan plugin Compose tanpa error. Kompilasi Gradle dan pengujian visual pada perangkat **belum diulang setelah perubahan UI dan dokumentasi terakhir**; lakukan `:app:assembleDebug` dan pemeriksaan di perangkat fisik sebelum menyerahkan APK.

## Izin dan data lokal

- Aplikasi meminta `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, dan `ACCESS_FINE_LOCATION` sesuai kebutuhan Android untuk memindai BLE. Aplikasi juga memeriksa apakah layanan lokasi perangkat aktif.
- Data yang dipakai dari advertising adalah nama jika tersedia, alamat yang diberikan Android, dan RSSI. Riwayat Room menyimpan alamat, nama, RSSI terakhir, dan waktu terakhir terlihat di ponsel.
- Aplikasi tidak membuat koneksi GATT, tidak membaca isi perangkat, dan tidak mengirim riwayat ke server. Proyek tidak mendeklarasikan izin internet.

## Asumsi dan keterbatasan

- Estimasi jarak merupakan pemetaan kasar dari rentang RSSI pada brief. Dinding, orientasi, interferensi, dan perangkat keras dapat mengubah nilainya; angka tersebut bukan pengukuran jarak presisi.
- Banyak perangkat BLE tidak mengiklankan nama. Jika nama advertising dan cache Android sama-sama kosong, aplikasi menampilkan **Perangkat tanpa nama**.
- Perangkat yang merotasi alamat BLE dapat muncul sebagai beberapa entri riwayat. Alamat yang terlihat tidak selalu merupakan identitas fisik permanen.
- Status pada radar adalah status *deteksi advertising*, bukan status koneksi Bluetooth. Riwayat saat ini belum memiliki tombol hapus.
- RSSI ekstrem atau tidak wajar dari perangkat/OS dapat memengaruhi kategori kedekatan; aplikasi belum menyaring outlier tersebut.
