package com.topmortar.topmortarsales.commons.utils

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DISTRIBUTOR
import com.topmortar.topmortarsales.commons.FIREBASE_REFERENCE

class FirebaseUtils {

    fun getReference(dbReference: String? = FIREBASE_REFERENCE, distributorId: String): DatabaseReference {

        val dbDistributor = FIREBASE_CHILD_DISTRIBUTOR + distributorId
        return FirebaseDatabase.getInstance().getReference("$dbReference/$dbDistributor")

    }
}