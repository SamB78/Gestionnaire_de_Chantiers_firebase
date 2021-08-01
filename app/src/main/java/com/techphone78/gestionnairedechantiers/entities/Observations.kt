package com.techphone78.gestionnairedechantiers.entities

data class Observations(
    var securiteRespectPortEPI: Boolean = false,
    var securiteBalisage: Boolean = false,
    var environnementProprete: Boolean = false,
    var environnementNonPollution: Boolean = false,
    var propreteVehicule: Boolean = false,
    var entretienMateriel: Boolean = false,
    var renduCarnetDeBord: Boolean = false,
    var renduBonCarburant: Boolean = false,
    var renduBonDecharge: Boolean = false,
    var feuillesInterimaires: Boolean = false,
    var bonDeCommande: Boolean = false
) {
    fun sendNumberOfTrueChamps(): Int {
        var number = 0
        if (securiteRespectPortEPI) number += 1
        if (securiteBalisage) number += 1
        if (environnementProprete) number += 1
        if (environnementNonPollution) number += 1
        if (propreteVehicule) number += 1
        if (entretienMateriel) number += 1
        if (renduCarnetDeBord) number += 1
        if (renduBonCarburant) number += 1
        if (renduBonDecharge) number += 1
        if (feuillesInterimaires) number += 1
        if (bonDeCommande) number += 1
        return number
    }

    fun sendNumberOfFalseChamps(): Int {
        return 11 - sendNumberOfTrueChamps()

    }
}