package com.example.gestionnairedechantiers.entities

import com.google.firebase.firestore.Exclude

data class User(
    var uid: String,
    var email: String,
    var userData: Personnel?,
)