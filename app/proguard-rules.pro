# Keep file provider and Hilt generated classes
-keep class androidx.core.content.FileProvider { *; }
-keep class dagger.hilt.** { *; }

# Commons Compress declares optional codecs. The archive UI supplies gzip, bzip2 and XZ explicitly.
-dontwarn com.github.luben.zstd.**
-dontwarn org.brotli.dec.**
