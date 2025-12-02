package org.example.server.entity;

import java.io.Serializable;

/**
 * Interface de base pour toutes les entités de l'application
 * Définit les méthodes communes que toutes les entités doivent implémenter
 */
public interface Entity extends Serializable {
    
    /**
     * Retourne l'identifiant unique de l'entité
     * @return l'ID de l'entité, ou null si l'entité n'a pas encore été persistée
     */
    Integer getId();
    
    /**
     * Définit l'identifiant unique de l'entité
     * @param id l'ID à assigner à l'entité
     */
    void setId(Integer id);
    
    /**
     * Vérifie si l'entité a déjà été persistée en base de données
     * @return true si l'entité a un ID (donc existe en base), false sinon
     */
    default boolean isPersisted() {
        return getId() != null && getId() > 0;
    }
    
    /**
     * Vérifie si l'entité est nouvelle (pas encore en base)
     * @return true si l'entité n'a pas encore d'ID
     */
    default boolean isNew() {
        return !isPersisted();
    }
}
