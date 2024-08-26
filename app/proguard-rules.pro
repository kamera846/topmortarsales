# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.topmortar.** { *; }
-keep interface com.topmortar.** { *; }
-keepattributes SourceFile,LineNumberTable
-keep class com.google.firebase.crashlytics.** { *; }
-dontoptimize
-dontwarn org.bouncycastle.asn1.ASN1Encodable
-dontwarn org.bouncycastle.asn1.ASN1InputStream
-dontwarn org.bouncycastle.asn1.ASN1Integer
-dontwarn org.bouncycastle.asn1.ASN1ObjectIdentifier
-dontwarn org.bouncycastle.asn1.ASN1OctetString
-dontwarn org.bouncycastle.asn1.ASN1Primitive
-dontwarn org.bouncycastle.asn1.ASN1Set
-dontwarn org.bouncycastle.asn1.DEROctetString
-dontwarn org.bouncycastle.asn1.DEROutputStream
-dontwarn org.bouncycastle.asn1.DERSet
-dontwarn org.bouncycastle.asn1.cms.ContentInfo
-dontwarn org.bouncycastle.asn1.cms.EncryptedContentInfo
-dontwarn org.bouncycastle.asn1.cms.EnvelopedData
-dontwarn org.bouncycastle.asn1.cms.IssuerAndSerialNumber
-dontwarn org.bouncycastle.asn1.cms.KeyTransRecipientInfo
-dontwarn org.bouncycastle.asn1.cms.OriginatorInfo
-dontwarn org.bouncycastle.asn1.cms.RecipientIdentifier
-dontwarn org.bouncycastle.asn1.cms.RecipientInfo
-dontwarn org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
-dontwarn org.bouncycastle.asn1.x500.X500Name
-dontwarn org.bouncycastle.asn1.x509.AlgorithmIdentifier
-dontwarn org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
-dontwarn org.bouncycastle.asn1.x509.TBSCertificateStructure
-dontwarn org.bouncycastle.crypto.BlockCipher
-dontwarn org.bouncycastle.crypto.CipherParameters
-dontwarn org.bouncycastle.crypto.engines.AESFastEngine
-dontwarn org.bouncycastle.crypto.modes.CBCBlockCipher
-dontwarn org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
-dontwarn org.bouncycastle.crypto.params.KeyParameter
-dontwarn org.bouncycastle.crypto.params.ParametersWithIV
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE