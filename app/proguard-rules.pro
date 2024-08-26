# Pertahankan tipe generik pada class yang digunakan oleh Retrofit
-keepattributes Signature

# Jangan obfuscate model data yang digunakan oleh Gson/Retrofit
-keep class com.topmortar.** { *; }
-keep interface com.topmortar.** { *; }

# Pertahankan semua field yang dianotasi dengan @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Pertahankan semua class dan method di dalam package com.topmortar
-keep class com.topmortar.** { *; }
-keepclassmembers class com.topmortar.** { *; }

# Pertahankan kelas-kelas Retrofit
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Pertahankan class dan method dari google
-keep class com.google.** { *; }
-keepclassmembers class com.google.** { *; }

# Pertahankan class dan method untuk handling API response
-keepclassmembers class * {
    public <init>(...);
}

# Pertahankan semua kelas di package org.bouncycastle
-keep class org.bouncycastle.** { *; }

# Pertahankan semua kelas di package org.conscrypt
-keep class org.conscrypt.** { *; }

# Pertahankan semua kelas di package org.openjsse
-keep class org.openjsse.** { *; }

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
-dontwarn org.bouncycastle.cert.X509CertificateHolder
-dontwarn org.bouncycastle.cms.CMSEnvelopedData
-dontwarn org.bouncycastle.cms.Recipient
-dontwarn org.bouncycastle.cms.RecipientId
-dontwarn org.bouncycastle.cms.RecipientInformation
-dontwarn org.bouncycastle.cms.RecipientInformationStore
-dontwarn org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
-dontwarn org.bouncycastle.cms.jcajce.JceKeyTransRecipient
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