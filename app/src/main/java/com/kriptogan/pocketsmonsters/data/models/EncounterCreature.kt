package com.kriptogan.pocketsmonsters.data.models

data class EncounterCreature(
    val id: String = "",
    val name: String = "",
    val initiative: String = "",
    val ac: String = "",
    val maxHp: String = "",
    val currentHp: String = ""
) {
    fun updateName(newName: String): EncounterCreature {
        return copy(name = newName.trim())
    }
    
    fun updateInitiative(newInitiative: String): EncounterCreature {
        return copy(initiative = newInitiative.trim())
    }
    
    fun updateAc(newAc: String): EncounterCreature {
        return copy(ac = newAc.trim())
    }
    
    fun updateMaxHp(newMaxHp: String): EncounterCreature {
        return copy(maxHp = newMaxHp.trim())
    }
    
    fun updateCurrentHp(newCurrentHp: String): EncounterCreature {
        return copy(currentHp = newCurrentHp.trim())
    }
}
