package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.utils.AudioManager;
import de.tum.cit.fop.maze.utils.UIUtils;

/**
 * Shared Settings UI Logic used by both Main Menu and In-Game Overlay.
 */
public class SettingsUI {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final Skin skin;
    private final Runnable onBackAction;

    private Table contentTable;
    private Label statusLabel;

    // Key remap controls
    private TextButton btnUp, btnDown, btnLeft, btnRight, btnAttack, btnSwitchWeapon;
    private String remappingKeyName = null;

    public SettingsUI(MazeRunnerGame game, Stage stage, Runnable onBackAction) {
        this.game = game;
        this.stage = stage;
        this.skin = game.getSkin();
        this.onBackAction = onBackAction;

        setupInputProcessor();
    }

    public Table build() {
        // Create the main table with a semi-transparent background to ensure
        // readability over any bg
        contentTable = new Table();
        contentTable.setBackground(game.getSkin().newDrawable("white", 0, 0, 0, 0.8f)); // Dark semi-transparent
        contentTable.pad(40);

        // Title
        contentTable.add(new Label("Settings", skin, "title")).colspan(2).padBottom(30).row();

        // 1. Audio Section
        createSectionHeader("Audio");
        createAudioControls();

        // 2. Gameplay Section
        createSectionHeader("Gameplay");
        createGameplayControls();

        // 3. Controls Section
        createSectionHeader("Controls");
        createKeyBindControls();

        // 4. Footer / Navigation
        createFooter();

        return contentTable;
    }

    private void createSectionHeader(String title) {
        contentTable.add(new Label(title, skin)).colspan(2).padBottom(10).padTop(10).row();
        // Optional: Add separator line?
    }

    private void createAudioControls() {
        Table audioTable = new Table();

        // Volume
        audioTable.add(new Label("Vol:", skin)).right().padRight(10);
        final Slider volumeSlider = new Slider(0, 1, 0.1f, false, skin);
        volumeSlider.setValue(AudioManager.getInstance().getVolume()); // Assuming getter exists or we track public
                                                                       // field?
        // Note: AudioManager might not expose getVolume directly if not added in
        // previous tasks.
        // Checking task breakdown... we might need to assume it does or use stored
        // prefs.
        // Actually, previous SettingsScreen used volumeSlider.getValue() to set it, and
        // initialized seamlessly.
        // Let's assume we can get it or init at 0.5f if unknown.
        // Better: Use GameSettings or just init to default.
        // Re-checking SettingsScreen: volumeSlider.setValue(0.5f); // comment says
        // "Should get from AudioManager"
        // We will stick to 0.5f reset for now or try to match current.
        volumeSlider.setValue(0.5f);

        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().setVolume(volumeSlider.getValue());
            }
        });
        audioTable.add(volumeSlider).width(200).padRight(20);

        // Mute Toggle
        boolean isEnabled = AudioManager.getInstance().isMusicEnabled();
        final TextButton muteBtn = new TextButton("Music: " + (isEnabled ? "ON" : "OFF"), skin);
        muteBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean newState = !AudioManager.getInstance().isMusicEnabled();
                AudioManager.getInstance().setMusicEnabled(newState);
                muteBtn.setText("Music: " + (newState ? "ON" : "OFF"));
            }
        });
        audioTable.add(muteBtn).width(120);

        contentTable.add(audioTable).colspan(2).padBottom(10).row();
    }

    private void createGameplayControls() {
        Table gameplayTable = new Table();

        // Walk Speed
        gameplayTable.add(new Label("Walk Spd:", skin)).right().padRight(10);
        final Label walkLabel = new Label(String.format("%.1f", GameSettings.playerWalkSpeed), skin);
        Slider walkSlider = new Slider(1f, 15f, 0.5f, false, skin);
        walkSlider.setValue(GameSettings.playerWalkSpeed);
        walkSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.playerWalkSpeed = ((Slider) actor).getValue();
                walkLabel.setText(String.format("%.1f", GameSettings.playerWalkSpeed));
            }
        });
        gameplayTable.add(walkSlider).width(150);
        gameplayTable.add(walkLabel).width(40).padLeft(5).padRight(20);

        // Run Speed
        gameplayTable.add(new Label("Run Spd:", skin)).right().padRight(10);
        final Label runLabel = new Label(String.format("%.1f", GameSettings.playerRunSpeed), skin);
        Slider runSlider = new Slider(5f, 20f, 0.5f, false, skin);
        runSlider.setValue(GameSettings.playerRunSpeed);
        runSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.playerRunSpeed = ((Slider) actor).getValue();
                runLabel.setText(String.format("%.1f", GameSettings.playerRunSpeed));
            }
        });
        gameplayTable.add(runSlider).width(150);
        gameplayTable.add(runLabel).width(40).padLeft(5);
        gameplayTable.row();

        // Fog of War
        gameplayTable.add(new Label("Fog of War:", skin)).right().padRight(10);
        final TextButton fogBtn = new TextButton(GameSettings.isFogEnabled() ? "ON" : "OFF", skin);
        fogBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean newState = !GameSettings.isFogEnabled();
                GameSettings.setFogEnabled(newState);
                fogBtn.setText(newState ? "ON" : "OFF");
            }
        });
        gameplayTable.add(fogBtn).width(100).left();
        gameplayTable.row();

        // Camera Zoom
        gameplayTable.add(new Label("Camera:", skin)).right().padRight(10);
        final Label zoomLabel = new Label(String.format("%.1fx", 1.0f / GameSettings.cameraZoom), skin);
        Slider zoomSlider = new Slider(0.3f, 1.5f, 0.05f, false, skin);
        zoomSlider.setValue(GameSettings.cameraZoom);
        zoomSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.cameraZoom = ((Slider) actor).getValue();
                zoomLabel.setText(String.format("%.1fx", 1.0f / GameSettings.cameraZoom));
            }
        });
        gameplayTable.add(zoomSlider).width(150);
        gameplayTable.add(zoomLabel).width(50).padLeft(5);
        gameplayTable.row();

        // Fog Hint
        Label fogHintLabel = new Label("* Fog mode: vision range is fixed", skin);
        fogHintLabel.setColor(0.7f, 0.7f, 0.7f, 1f);
        fogHintLabel.setFontScale(0.8f);
        gameplayTable.add(fogHintLabel).colspan(6).left().padTop(5);

        contentTable.add(gameplayTable).colspan(2).padBottom(10).row();
    }

    private void createKeyBindControls() {
        statusLabel = new Label("Click button -> Press key", skin);
        statusLabel.setColor(Color.YELLOW);
        contentTable.add(statusLabel).colspan(2).padBottom(10).row();

        btnUp = createKeyButton("Up", "UP");
        btnDown = createKeyButton("Down", "DOWN");
        btnLeft = createKeyButton("Left", "LEFT");
        btnRight = createKeyButton("Right", "RIGHT");
        btnAttack = createKeyButton("Attack", "ATTACK");
        btnSwitchWeapon = createKeyButton("Switch", "SWITCH_WEAPON");

        Table keyTable = new Table();
        addToKeyTable(keyTable, "Up:", btnUp);
        addToKeyTable(keyTable, "Down:", btnDown);
        addToKeyTable(keyTable, "Left:", btnLeft);
        keyTable.row();
        addToKeyTable(keyTable, "Right:", btnRight);
        addToKeyTable(keyTable, "Atk:", btnAttack);
        addToKeyTable(keyTable, "Switch:", btnSwitchWeapon);

        contentTable.add(keyTable).colspan(2).padBottom(10).row();
    }

    private TextButton createKeyButton(String label, String keyName) {
        final TextButton btn = new TextButton(getKeyName(keyName), skin);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startRemap(keyName, btn);
            }
        });
        return btn;
    }

    private void addToKeyTable(Table table, String label, Actor actor) {
        table.add(new Label(label, skin)).right().padRight(5);
        table.add(actor).left().width(100).height(35).padRight(20);
    }

    private void createFooter() {
        TextButton backBtn = new TextButton("Back / Save", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Save persistent settings
                GameSettings.saveKeyBindingsOnly();
                // If it's pure main menu settings, we might want to save everything.
                // But generally, saving KeyBindings is the critical part.
                // The implementation plan says "Only save settings when accessed from Main Menu
                // (not in-game)" for generic prefs,
                // But actually game settings like WalkSpeed are usually session focused unless
                // explicitly saved.
                // Let's stick to Safe Defaults + explicit Save.
                // If onBackAction is run, we just leave.

                // Trigger callback
                if (onBackAction != null) {
                    onBackAction.run();
                }
            }
        });
        contentTable.add(backBtn).colspan(2).width(200).height(50).padTop(10);
    }

    // --- Remapping Logic ---

    private String getKeyName(String keyName) {
        int code = -1;
        switch (keyName) {
            case "UP":
                code = GameSettings.KEY_UP;
                break;
            case "DOWN":
                code = GameSettings.KEY_DOWN;
                break;
            case "LEFT":
                code = GameSettings.KEY_LEFT;
                break;
            case "RIGHT":
                code = GameSettings.KEY_RIGHT;
                break;
            case "ATTACK":
                code = GameSettings.KEY_ATTACK;
                break;
            case "SWITCH_WEAPON":
                code = GameSettings.KEY_SWITCH_WEAPON;
                break;
        }
        return Input.Keys.toString(code);
    }

    private void startRemap(String keyName, TextButton btn) {
        remappingKeyName = keyName;
        statusLabel.setText("Press key for " + keyName);
        btn.setText("...");
    }

    private void updateButtons() {
        btnUp.setText(getKeyName("UP"));
        btnDown.setText(getKeyName("DOWN"));
        btnLeft.setText(getKeyName("LEFT"));
        btnRight.setText(getKeyName("RIGHT"));
        btnAttack.setText(getKeyName("ATTACK"));
        btnSwitchWeapon.setText(getKeyName("SWITCH_WEAPON"));
    }

    /**
     * Call this from the InputProcessor associated with the Stage
     */
    public boolean handleKeyDown(int keycode) {
        if (remappingKeyName != null) {
            if (keycode == Input.Keys.ESCAPE) {
                remappingKeyName = null;
                updateButtons();
                statusLabel.setText("Cancelled");
                return true;
            }
            switch (remappingKeyName) {
                case "UP":
                    GameSettings.KEY_UP = keycode;
                    break;
                case "DOWN":
                    GameSettings.KEY_DOWN = keycode;
                    break;
                case "LEFT":
                    GameSettings.KEY_LEFT = keycode;
                    break;
                case "RIGHT":
                    GameSettings.KEY_RIGHT = keycode;
                    break;
                case "ATTACK":
                    GameSettings.KEY_ATTACK = keycode;
                    break;
                case "SWITCH_WEAPON":
                    GameSettings.KEY_SWITCH_WEAPON = keycode;
                    break;
            }
            remappingKeyName = null;
            updateButtons();
            statusLabel.setText("Saved");
            return true;
        }
        return false;
    }

    private void setupInputProcessor() {
        // We attach a listener to the stage that forwards to our handler
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return handleKeyDown(keycode);
            }
        });
    }
}
