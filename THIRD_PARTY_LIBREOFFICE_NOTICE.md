# LibreOfficeKit for Android

MyDocuVault includes the LibreOffice Android viewer UI and JNI bridge adapted from
[h4de5ing/LibreOfficeViewer v7.6.7.2](https://github.com/h4de5ing/LibreOfficeViewer/tree/v7.6.7.2).
The native engine, configuration and fonts come from the corresponding ARM64,
ARMv7, x86 and x86-64 F-Droid builds of LibreOffice Viewer 26.2.6.3. Gradle
verifies the SHA-256 of each download before extracting it. No download occurs
on a user's device. The viewer opens local documents read-only.

- [LibreOffice source and license notices for this engine](https://github.com/LibreOffice/core/tree/libreoffice-26.2.6.3)
- [F-Droid build recipes and source downloads](https://f-droid.org/packages/org.documentfoundation.libreoffice/)
- [Mozilla Public License 2.0](https://www.mozilla.org/MPL/2.0/)

LibreOffice and its bundled third-party libraries and fonts retain their
respective copyrights and licenses. The vault application's MIT license applies
to its own code.
