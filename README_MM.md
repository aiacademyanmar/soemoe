# DAMA APP V3 — DAMA Radio + DAMA Library

Features:
- Radio V2: online MP3 playback, a publisher-linked offline catalog, playback favorites, recent items, opt-in personal MP3 downloads.
- Library: bundled book link catalog harvested at build from DhammaDownload (`scripts/harvest_books.py`), local search and author/category filters, favorites, individually downloadable PDFs.
- PDF downloads: Not included in APK; user requests individual file downloads for personal offline reading. After download they are rendered with Android PdfRenderer (`PdfReaderActivity.java`) without internet.
- Book cover tiles are illustrative icons. Actual portraits for some monks are fetched only from Wikimedia Commons and credit/license details are in `photo_license_report.json`; failing downloads retain symbolic placeholder artwork. Never misidentify a generic icon as an actual photo.
- Source: https://www.dhammadownload.com/eBook-Myanmar.htm. The site says it has 4,000+ eBooks; this is **not a claim** that all 4,000 were indexed.
- Correct verified count is in the Github workflow `books_build_report.json` and the APK's `books_catalog.json`. No placeholders or invented 1,000 books may be used to pad the count.
- Link availability is not checked per PDF URL, and publisher sites/permissions may change.
- This package uses the previous V2 radio sources and native UI, plus Library.

Build (GitHub): Upload source to a repository branch `dama-library-v3`, then let `.github/workflows/android.yml` run. Download artifact `DAMA-APP-V3-LIBRARY-APK-CATALOG`. Use the contained .apk for Android 8.0+; test on a real device before public release. Debug signing differs from an older build and installation over an existing app may fail.