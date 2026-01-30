package de.tum.cit.fop.maze.model;

/**
 * Enumeration of available player skills.
 * Define skill names, descriptions, and point costs.
 */
public enum Skill {
    SPEED_BOOST_1("Speed Up I", "Increases movement speed by 10%.", 100),
    SPEED_BOOST_2("Speed Up II", "Increases movement speed by 20%.", 200),
    DAMAGE_UP_1("Damage Up I", "Increases attack range and damage potential.", 150),
    HEALTH_UP_1("Health Up I", "Increases max lives by 1.", 150),
    HEALTH_UP_2("Health Up II", "Increases max lives by 2.", 300);

    private final String displayName;
    private final String description;
    private final int cost;

    Skill(String displayName, String description, int cost) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }
}
