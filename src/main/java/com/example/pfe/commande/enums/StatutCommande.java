package com.example.pfe.commande.enums;

public enum StatutCommande {
    EN_ATTENTE,
    EN_PREPARATION,  // NOUVEAU : pour les transferts en cours de préparation
    VALIDEE,
    EXPEDIEE,
    ANNULEE
}