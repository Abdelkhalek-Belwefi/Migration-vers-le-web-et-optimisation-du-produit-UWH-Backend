package com.example.pfe.stock.entity;

/**
 * Statuts possibles d'une ligne de stock.
 * Correspond aux couleurs décrites dans le manuel L‑mobile (page 71) :
 * - DISPONIBLE  (vert)
 * - RESERVE     (jaune)
 * - BLOQUE      (rouge)
 * - QUALITE     (orange)
 */
public enum StockStatut {
    DISPONIBLE,
    RESERVE,
    BLOQUE,
    QUALITE
}