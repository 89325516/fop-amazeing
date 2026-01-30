package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.custom.CustomElementManager;
import de.tum.cit.fop.maze.shop.LoadoutManager;
import de.tum.cit.fop.maze.shop.ShopItem;

import java.util.List;

/**
 * Loadout Screen.
 * 
 * Displayed before entering a level, allowing the player to select up to 4
 * purchased weapons to take into the level.
 */
public class LoadoutScreen implements Screen {

    private final MazeRunnerGame game;
    private final int targetLevel;
    private Stage stage;
    private Skin skin;
    private Table availableWeaponsTable;
    private Table loadoutSlotsTable;
    private LoadoutManager loadoutManager;
    private Label slotsLabel;
    private Texture backgroundTexture;

    public LoadoutScreen(MazeRunnerGame game, int targetLevel) {
        this.game = game;
        this.targetLevel = targetLevel;
        this.loadoutManager = LoadoutManager.getInstance();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = game.getSkin();

        loadBackground();
        buildUI();
    }

    private void loadBackground() {
        try {
            backgroundTexture = new Texture(Gdx.files.internal("images/backgrounds/armor_select_bg.png"));
        } catch (Exception e) {
            Gdx.app.error("LoadoutScreen", "Failed to load background, using fallback", e);
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(0.05f, 0.05f, 0.1f, 1f);
            pm.fill();
            backgroundTexture = new Texture(pm);
            pm.dispose();
        }
    }

    private void buildUI() {
        // Main container
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Title
        Label titleLabel = new Label("SELECT YOUR LOADOUT", skin, "title");
        titleLabel.setColor(Color.GOLD);
        root.add(titleLabel).padTop(30).padBottom(20).colspan(2);
        root.row();

        // Tip text
        slotsLabel = new Label("", skin);
        updateSlotsLabel();
        root.add(slotsLabel).padBottom(20).colspan(2);
        root.row();

        // Left side: Purchased weapons
        Table leftPanel = new Table();
        leftPanel.setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.15f, 0.9f)));
        leftPanel.pad(20);

        Label availableTitle = new Label("AVAILABLE WEAPONS", skin);
        availableTitle.setFontScale(1.2f);
        availableTitle.setColor(Color.GOLD);
        leftPanel.add(availableTitle).padBottom(15);
        leftPanel.row();

        ScrollPane availableScroll = new ScrollPane(createAvailableWeaponsTable(), skin);
        availableScroll.setFadeScrollBars(false);
        leftPanel.add(availableScroll).width(350).height(400);

        root.add(leftPanel).pad(20).top();

        // Right side: Current loadout
        Table rightPanel = new Table();
        rightPanel.setBackground(skin.newDrawable("white", new Color(0.15f, 0.1f, 0.1f, 0.9f)));
        rightPanel.pad(20);

        Label loadoutTitle = new Label("YOUR LOADOUT", skin);
        loadoutTitle.setFontScale(1.2f);
        loadoutTitle.setColor(Color.CORAL);
        rightPanel.add(loadoutTitle).padBottom(15);
        rightPanel.row();

        loadoutSlotsTable = new Table();
        updateLoadoutSlots();
        rightPanel.add(loadoutSlotsTable).width(350).height(400);

        root.add(rightPanel).pad(20).top();
        root.row();

        // Footer buttons
        Table buttonRow = new Table();

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LevelSelectScreen(game));
            }
        });
        buttonRow.add(backButton).width(180).height(50).pad(10);

        TextButton clearButton = new TextButton("Clear All", skin);
        clearButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                loadoutManager.clearLoadout();
                refreshUI();
            }
        });
        buttonRow.add(clearButton).width(180).height(50).pad(10);

        TextButton confirmButton = new TextButton("START MISSION", skin);
        confirmButton.getLabel().setColor(Color.GREEN);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startGame();
            }
        });
        buttonRow.add(confirmButton).width(280).height(50).pad(10);

        root.add(buttonRow).colspan(2).padTop(20).padBottom(30);
    }

    private Table createAvailableWeaponsTable() {
        availableWeaponsTable = new Table();
        refreshAvailableWeapons();
        return availableWeaponsTable;
    }

    private void refreshAvailableWeapons() {
        availableWeaponsTable.clear();

        List<ShopItem> purchasedWeapons = loadoutManager.getPurchasedWeapons();

        if (purchasedWeapons.isEmpty()) {
            Label emptyLabel = new Label("No weapons purchased!\nVisit the shop first.", skin);
            emptyLabel.setColor(Color.GRAY);
            availableWeaponsTable.add(emptyLabel).pad(20);
            return;
        }

        for (ShopItem weapon : purchasedWeapons) {
            Table weaponRow = createWeaponRow(weapon, true);
            availableWeaponsTable.add(weaponRow).fillX().expandX().padBottom(8);
            availableWeaponsTable.row();
        }
    }

    private void updateLoadoutSlots() {
        loadoutSlotsTable.clear();

        List<String> selectedIds = loadoutManager.getSelectedWeaponIds();
        int maxSlots = loadoutManager.getMaxLoadoutSize();

        // === First Slot: Iron Sword (Starting weapon, non-removable) ===
        Table swordRow = new Table();
        swordRow.setBackground(skin.newDrawable("white", new Color(0.15f, 0.25f, 0.15f, 0.9f)));
        swordRow.pad(10);

        Label swordIcon = new Label("⚔", skin);
        swordRow.add(swordIcon).width(50).padRight(10);

        Label swordName = new Label("Iron Sword\n(Default)", skin);
        swordName.setColor(Color.LIGHT_GRAY);
        swordName.setWrap(true);
        swordRow.add(swordName).width(180).left().padRight(10);

        Label lockedLabel = new Label("🔒", skin);
        lockedLabel.setColor(Color.GRAY);
        swordRow.add(lockedLabel).width(40);

        loadoutSlotsTable.add(swordRow).fillX().expandX().padBottom(8);
        loadoutSlotsTable.row();

        // Show selected weapons (excluding Iron Sword as it's fixed)
        for (String weaponId : selectedIds) {
            // Get weapon info
            ShopItem item = getShopItemById(weaponId);
            if (item != null) {
                Table weaponRow = createWeaponRow(item, false);
                loadoutSlotsTable.add(weaponRow).fillX().expandX().padBottom(8);
                loadoutSlotsTable.row();
            }
        }

        // Show empty slots (-1 because Iron Sword occupies the first slot)
        int usedSlots = 1 + selectedIds.size(); // Iron Sword + selected weapons
        int emptySlots = maxSlots - usedSlots;
        for (int i = 0; i < emptySlots; i++) {
            Table emptySlot = new Table();
            emptySlot.setBackground(skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.5f)));
            Label emptyLabel = new Label("[ Empty Slot ]", skin);
            emptyLabel.setColor(Color.DARK_GRAY);
            emptySlot.add(emptyLabel).pad(15);
            loadoutSlotsTable.add(emptySlot).fillX().expandX().height(60).padBottom(8);
            loadoutSlotsTable.row();
        }
    }

    private Table createWeaponRow(ShopItem weapon, boolean isAvailable) {
        Table row = new Table();
        Color bgColor = isAvailable ? new Color(0.2f, 0.3f, 0.2f, 0.8f) : new Color(0.3f, 0.2f, 0.2f, 0.8f);

        boolean isSelected = loadoutManager.isWeaponSelected(weapon.getId());
        if (isAvailable && isSelected) {
            bgColor = new Color(0.3f, 0.3f, 0.15f, 0.8f); // Selected shows yellow tint
        }

        row.setBackground(skin.newDrawable("white", bgColor));
        row.pad(10);

        // Weapon icon
        Table iconCell = new Table();
        try {
            Animation<TextureRegion> idleAnim = CustomElementManager.getInstance()
                    .getAnimation(weapon.getName(), "Idle");
            if (idleAnim != null) {
                TextureRegion frame = idleAnim.getKeyFrame(0);
                Image icon = new Image(frame);
                icon.setSize(40, 40);
                iconCell.add(icon).size(40, 40);
            } else {
                Label iconLabel = new Label("⚔", skin);
                iconCell.add(iconLabel);
            }
        } catch (Exception e) {
            Label iconLabel = new Label("⚔", skin);
            iconCell.add(iconLabel);
        }
        row.add(iconCell).width(50).padRight(10);

        // Weapon name - with wrap enabled and max width
        Label nameLabel = new Label(weapon.getName(), skin);
        nameLabel.setColor(isSelected ? Color.YELLOW : Color.WHITE);
        nameLabel.setWrap(true);
        row.add(nameLabel).width(180).left().padRight(10);

        // Action buttons
        if (isAvailable) {
            if (isSelected) {
                Label selectedLabel = new Label("✓", skin);
                selectedLabel.setColor(Color.GREEN);
                row.add(selectedLabel).width(40);
            } else {
                TextButton addBtn = new TextButton("+", skin);
                addBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (loadoutManager.addWeapon(weapon.getId())) {
                            refreshUI();
                        }
                    }
                });
                row.add(addBtn).width(40).height(40);
            }
        } else {
            // In loadout bar - show remove button
            TextButton removeBtn = new TextButton("×", skin);
            removeBtn.getLabel().setColor(Color.RED);
            removeBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    loadoutManager.removeWeapon(weapon.getId());
                    refreshUI();
                }
            });
            row.add(removeBtn).width(40).height(40);
        }

        return row;
    }

    private ShopItem getShopItemById(String id) {
        List<ShopItem> allWeapons = de.tum.cit.fop.maze.shop.ShopManager
                .getItemsByCategory(ShopItem.ItemCategory.WEAPON);
        for (ShopItem item : allWeapons) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    private void refreshUI() {
        refreshAvailableWeapons();
        updateLoadoutSlots();
        updateSlotsLabel();
    }

    private void updateSlotsLabel() {
        int selectedCount = loadoutManager.getSelectedWeaponIds().size();
        int max = loadoutManager.getMaxLoadoutSize();
        // +1 because Iron Sword always occupies the first slot
        int totalEquipped = 1 + selectedCount;
        slotsLabel.setText("Equipped: " + totalEquipped + " / " + max + " weapons");

        if (totalEquipped >= max) {
            slotsLabel.setColor(Color.YELLOW);
        } else {
            slotsLabel.setColor(Color.WHITE);
        }
    }

    private void startGame() {
        // Mark this as a fresh start, should use LoadoutManager's weapon selection
        loadoutManager.setFreshStart(true);
        String path = "maps/level-" + targetLevel + ".properties";
        game.setScreen(new LoadingScreen(game, path));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background image using Cover mode
        if (backgroundTexture != null) {
            com.badlogic.gdx.graphics.g2d.SpriteBatch batch = game.getSpriteBatch();

            int screenWidth = Gdx.graphics.getBackBufferWidth();
            int screenHeight = Gdx.graphics.getBackBufferHeight();

            Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);

            batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
            batch.begin();
            batch.setColor(0.7f, 0.7f, 0.7f, 1f); // Slightly dimmed

            float texWidth = backgroundTexture.getWidth();
            float texHeight = backgroundTexture.getHeight();

            float screenRatio = (float) screenWidth / screenHeight;
            float textureRatio = texWidth / texHeight;

            float drawWidth, drawHeight;
            float drawX, drawY;

            if (screenRatio > textureRatio) {
                drawWidth = screenWidth;
                drawHeight = screenWidth / textureRatio;
                drawX = 0;
                drawY = (screenHeight - drawHeight) / 2;
            } else {
                drawHeight = screenHeight;
                drawWidth = screenHeight * textureRatio;
                drawX = (screenWidth - drawWidth) / 2;
                drawY = 0;
            }

            batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
            batch.setColor(Color.WHITE);
            batch.end();
        }

        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (stage != null)
            stage.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
    }
}
