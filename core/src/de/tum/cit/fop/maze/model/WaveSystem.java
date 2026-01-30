package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.config.EndlessModeConfig;

/**
 * Wave System.
 * 
 * Manages time pressure and difficulty progression in Endless Mode.
 * 
 * Mechanism:
 * - Wave stages based on survival time
 * - Each wave has diffent enemy spawn rates and health multipliers
 * - Boss enemies start appearing after 12 minutes
 * 
 * Follows Single Responsibility Principle: Handles wave logic only.
 */
public class WaveSystem {

    /** Current survival time (seconds). */
    private float survivalTime;

    /** Current wave index (0-5). */
    private int currentWaveIndex;

    /** Next enemy spawn time. */
    private float nextSpawnTime;

    /** Next boss spawn time. */
    private float nextBossTime;

    /** Listener: Callback when wave changes. */
    private WaveListener listener;

    /**
     * Wave change listener interface.
     */
    public interface WaveListener {
        /** Called when wave changes. */
        void onWaveChanged(int newWave, float spawnInterval, float healthMultiplier);

        /** Called when enemy spawn is required. */
        void onSpawnEnemy();

        /** Called when boss spawn is required. */
        void onSpawnBoss();
    }

    public WaveSystem() {
        reset();
    }

    /**
     * Set wave listener.
     */
    public void setListener(WaveListener listener) {
        this.listener = listener;
    }

    /**
     * Update per frame.
     * 
     * @param delta Frame delta time (seconds)
     */
    public void update(float delta) {
        survivalTime += delta;

        // Check for wave change
        int newWaveIndex = EndlessModeConfig.getWaveIndex(survivalTime);
        if (newWaveIndex != currentWaveIndex) {
            currentWaveIndex = newWaveIndex;

            if (listener != null) {
                listener.onWaveChanged(
                        currentWaveIndex,
                        getSpawnInterval(),
                        getEnemyHealthMultiplier());
            }
        }

        // Check enemy spawn (no spawn during safe period)
        if (survivalTime >= EndlessModeConfig.SAFE_PERIOD_DURATION &&
                survivalTime >= nextSpawnTime) {
            nextSpawnTime = survivalTime + getSpawnInterval();

            if (listener != null) {
                listener.onSpawnEnemy();
            }
        }

        // Check Boss spawn (every 2 minutes after 12 minutes)
        if (survivalTime >= EndlessModeConfig.FIRST_BOSS_TIME &&
                survivalTime >= nextBossTime) {
            nextBossTime = survivalTime + EndlessModeConfig.BOSS_SPAWN_INTERVAL;

            if (listener != null) {
                listener.onSpawnBoss();
            }
        }
    }

    /**
     * Get current enemy spawn interval (seconds).
     */
    public float getSpawnInterval() {
        return EndlessModeConfig.WAVE_SPAWN_INTERVALS[currentWaveIndex];
    }

    /**
     * Get current enemy health multiplier.
     */
    public float getEnemyHealthMultiplier() {
        return EndlessModeConfig.WAVE_HEALTH_MULTIPLIERS[currentWaveIndex];
    }

    /**
     * Get current wave index (0-5).
     */
    public int getCurrentWave() {
        return currentWaveIndex;
    }

    /**
     * Get current wave display name (Wave 1-6).
     */
    public String getWaveName() {
        return "Wave " + (currentWaveIndex + 1);
    }

    /**
     * Get start time of next wave (seconds).
     * 
     * @return Next wave start time, or -1 if final wave.
     */
    public float getNextWaveTime() {
        if (currentWaveIndex >= EndlessModeConfig.WAVE_TIME_THRESHOLDS.length - 1) {
            return -1;
        }
        return EndlessModeConfig.WAVE_TIME_THRESHOLDS[currentWaveIndex + 1];
    }

    /**
     * Get time remaining until next wave (seconds).
     */
    public float getTimeToNextWave() {
        float nextTime = getNextWaveTime();
        if (nextTime < 0) {
            return -1;
        }
        return Math.max(0, nextTime - survivalTime);
    }

    /**
     * Is currently in Boss Spawn Phase?
     */
    public boolean isBossPhase() {
        return survivalTime >= EndlessModeConfig.FIRST_BOSS_TIME;
    }

    /**
     * Get time remaining until next boss (seconds).
     */
    public float getTimeToNextBoss() {
        if (!isBossPhase()) {
            return EndlessModeConfig.FIRST_BOSS_TIME - survivalTime;
        }
        return Math.max(0, nextBossTime - survivalTime);
    }

    /**
     * Get formatted survival time (MM:SS).
     */
    public String getFormattedTime() {
        int minutes = (int) (survivalTime / 60);
        int seconds = (int) (survivalTime % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Force reset.
     */
    public void reset() {
        survivalTime = 0;
        currentWaveIndex = 0;
        nextSpawnTime = EndlessModeConfig.WAVE_SPAWN_INTERVALS[0];
        nextBossTime = EndlessModeConfig.FIRST_BOSS_TIME;
    }

    // ========== Getters ==========

    public float getSurvivalTime() {
        return survivalTime;
    }

    // ========== Setters (for save restore) ==========

    public void setSurvivalTime(float time) {
        this.survivalTime = time;
        this.currentWaveIndex = EndlessModeConfig.getWaveIndex(time);
        this.nextSpawnTime = time + getSpawnInterval();

        // Calculate next boss time
        if (time >= EndlessModeConfig.FIRST_BOSS_TIME) {
            float timeSinceFirstBoss = time - EndlessModeConfig.FIRST_BOSS_TIME;
            int bossesSpawned = (int) (timeSinceFirstBoss / EndlessModeConfig.BOSS_SPAWN_INTERVAL);
            this.nextBossTime = EndlessModeConfig.FIRST_BOSS_TIME +
                    (bossesSpawned + 1) * EndlessModeConfig.BOSS_SPAWN_INTERVAL;
        } else {
            this.nextBossTime = EndlessModeConfig.FIRST_BOSS_TIME;
        }
    }
}
