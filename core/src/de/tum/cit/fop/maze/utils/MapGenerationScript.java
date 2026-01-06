package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import de.tum.cit.fop.maze.config.RandomMapConfig;
import de.tum.cit.fop.maze.model.DamageType;

public class MapGenerationScript {

    public void generateMaps() {
        Gdx.app.log("MapGenScript", "Starting Map Generation for 20 Levels...");

        MapGenerator generator = new MapGenerator();
        String[] themes = { "Grassland", "Desert", "Ice", "Jungle", "Space" };
        DamageType[] damageTypes = { DamageType.PHYSICAL, DamageType.PHYSICAL, DamageType.MAGICAL, DamageType.PHYSICAL,
                DamageType.MAGICAL };
        boolean[] shields = { false, true, false, true, true };

        int levelCounter = 1;

        for (int t = 0; t < themes.length; t++) {
            String theme = themes[t];
            DamageType dmg = damageTypes[t];
            boolean shield = shields[t];

            for (int i = 0; i < 4; i++) {
                int size = 50 + (i * 50); // 50, 100, 150, 200
                int difficulty = i + 1; // 1 to 4 based on level progression

                RandomMapConfig config = new RandomMapConfig()
                        .setSize(size, size)
                        .setTheme(theme)
                        .setDamageType(dmg)
                        .setEnemyShieldEnabled(shield)
                        .setDifficulty(difficulty);

                String filename = "maps/level-" + levelCounter + ".properties";
                Gdx.app.log("MapGenScript", "Generating " + filename + " [" + theme + " " + size + "x" + size + "]");

                generator.generateAndSave(filename, config);
                levelCounter++;
            }
        }

        Gdx.app.log("MapGenScript", "Generation Complete!");
    }
}
