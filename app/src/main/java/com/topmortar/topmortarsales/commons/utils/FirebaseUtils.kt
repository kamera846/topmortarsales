package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import android.os.Build.BOARD
import android.os.Build.BRAND
import android.os.Build.FINGERPRINT
import android.os.Build.HOST
import android.os.Build.ID
import android.os.Build.MANUFACTURER
import android.os.Build.MODEL
import android.os.Build.TYPE
import android.os.Build.USER
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DISTRIBUTOR
import com.topmortar.topmortarsales.commons.FIREBASE_REFERENCE
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseUtils {

    fun getReference(dbReference: String? = FIREBASE_REFERENCE, distributorId: String): DatabaseReference {

        val dbDistributor = FIREBASE_CHILD_DISTRIBUTOR + distributorId
        return FirebaseDatabase.getInstance().getReference("$dbReference/$dbDistributor")

    }

    fun firebaseLogging(context: Context, tag: String, logMessage: String) {
        val sessionManager = SessionManager(context)
        val distributorId = sessionManager.userDistributor() ?: "0"
        val userId = sessionManager.userID() ?: "0"

        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS", Locale.getDefault())
        val dateNow = formatter.format(date)

        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val randomString =  (1..10)
            .map { chars.random() }
            .joinToString("")

        val dbDistributor = FIREBASE_CHILD_DISTRIBUTOR + (distributorId)
        val firebaseReference = FirebaseDatabase.getInstance().getReference("$FIREBASE_REFERENCE/$dbDistributor")
        val isLogActiveChild = FirebaseDatabase.getInstance().getReference("$FIREBASE_REFERENCE/isLogActive")

        isLogActiveChild.get().addOnSuccessListener {
            if (it.exists() && it.value == true) {
                val logChild = firebaseReference.child("log")
                val userLogChild = logChild.child(userId)
                val userLogTagChild = userLogChild.child(tag)
                userLogTagChild.child("$dateNow-$randomString").setValue(logMessage)
            }
        }

//        println("$tag $randomString.$dateNow: $logMessage")
    }

    fun logErr(context: Context, logMessage: String) {
        val sessionManager = SessionManager(context)
        val distributorId = sessionManager.userDistributor() ?: "0"
        val userId = sessionManager.userID() ?: "0"
        val username = sessionManager.userName() ?: "-"

        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS", Locale.getDefault())
        val dateNow = formatter.format(date)

        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val randomString =  (1..10)
            .map { chars.random() }
            .joinToString("")

        val errLogId = "$dateNow-$randomString"
        val dbDistributor = FIREBASE_CHILD_DISTRIBUTOR + distributorId
        val firebaseReference = FirebaseDatabase.getInstance().getReference("$FIREBASE_REFERENCE/$dbDistributor")
        val isLogActiveChild = FirebaseDatabase.getInstance().getReference("$FIREBASE_REFERENCE/isLogActive")

        isLogActiveChild.get().addOnSuccessListener {
            if (it.exists() && it.value == true) {
                val errLogTagChild = firebaseReference.child("error")
                val idErrLogChild = errLogTagChild.child(errLogId)
                val detailErrLogChild = idErrLogChild.child("detail")

                idErrLogChild.child("id").setValue(errLogId)
                idErrLogChild.child("messages").setValue(logMessage)
                detailErrLogChild.child("userId").setValue(userId)
                detailErrLogChild.child("username").setValue(username)
                detailErrLogChild.child("d-serial").setValue(MODEL)
                detailErrLogChild.child("d-id").setValue(ID)
                detailErrLogChild.child("d-manufacturer").setValue(MANUFACTURER)
                detailErrLogChild.child("d-brand").setValue(BRAND)
                detailErrLogChild.child("d-type").setValue(TYPE)
                detailErrLogChild.child("d-user").setValue(USER)
                detailErrLogChild.child("d-version-base").setValue("${VERSION_CODES.BASE}")
                detailErrLogChild.child("d-version-incremental").setValue(VERSION.INCREMENTAL)
                detailErrLogChild.child("d-version-sdk").setValue("${VERSION.SDK_INT}")
                detailErrLogChild.child("d-version-release").setValue(VERSION.RELEASE)
                detailErrLogChild.child("d-board").setValue(BOARD)
                detailErrLogChild.child("d-host").setValue(HOST)
                detailErrLogChild.child("d-fingerprint").setValue(FINGERPRINT)
//                println("$tag $randomString.$dateNow: $logMessage")
            }
        }
    }
}