# Web Service Penyediaan Server untuk Mahasiswa Polstat STIS

Repository ini berisikan web service mengenai layanan penyediaan server untuk kalangan mahasiswa di Polstat STIS.

## Role
-   Mahasiswa
-   Administrator

## Langkah instalasi
1. Clone project ini di direktori masing-masing
```bash
git clone https://git.stis.ac.id/uts-ppk/penyediaan-server.git
```

2. Buka project tersebut pada code editor dan lakukan build dependensi
3. Pada [application.properties](./src/main/resources/application.properties) sesuaikan JDBC Driver dengan kredensial mysql di local masing-masing.
```bash
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/ppk_uts
spring.datasource.username=root
spring.datasource.password=root
```

4. Kemudian buatlah database dengan nama ppk_uts.
5. Jalankan project, saat pertama kali dijalankan program secara otomatis membuatkan tabel serta akun administrator yang dapat digunakan untuk login ke dalam sistem.
```bash
email = unit-ti@stis.ac.id
password = unit-ti-stis
```

6. Kunjungi salah satu pranala berikut untuk mengakses dokumentasi web service yang dibuat.
	- [Dokumentasi Swagger](https://ray123fa.github.io/STIS-Server-Service-API-Documentation/)
	- [Dokumentasi Postman](https://documenter.getpostman.com/view/24405166/2sAYBPkZmh)