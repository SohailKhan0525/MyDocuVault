# LibreOfficeKit Android integration

This repository vendors the Android LibreOfficeKit bridge from
[h4de5ing/LibreOfficeViewer](https://github.com/h4de5ing/LibreOfficeViewer),
tag `v7.6.7.2`, to render `.xls`, `.xlsx`, `.doc`, `.docx`, `.ppt`, and
`.pptx` files on-device.

The LibreOffice source is distributed under the Mozilla Public License 2.0.
The bundled native engine and assets retain their upstream licensing and
copyright notices in the upstream project.

The 170 MB arm64 native engine is downloaded during Gradle builds from the
upstream release and verified against its SHA-256 checksum. It is intentionally
not stored in this Git repository.
