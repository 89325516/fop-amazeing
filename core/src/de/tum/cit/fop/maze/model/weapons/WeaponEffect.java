package de.tum.cit.fop.maze.model.weapons;

/**
 * Combat styling and status effects for weapons.
 */
public enum WeaponEffect {
    /** No special effect. */
    NONE,
    /** Slows or stops enemy movement. */
    FREEZE,
    /** Deals damage over time continually. */
    BURN,
    /** Deals damage over time, potentially at a slower rate than burn. */
    POISON,
    /** Reduces enemy movement speed. */
    SLOW
}
