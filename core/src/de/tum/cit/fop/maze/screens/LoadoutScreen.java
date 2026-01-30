package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
 * 装备选择界面 (Loadout Screen)
 * 
 * 在进入关卡前显示，允许玩家从已购买的武器中选择最多4个带入关卡。
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

        buildUI();
    }

    private void buildUI() {
        // 主容器
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // 标题
        Label titleLabel = new Label("SELECT YOUR LOADOUT", skin, "title");
        titleLabel.setColor(Color.GOLD);
        root.add(titleLabel).padTop(30).padBottom(20).colspan(2);
        root.row();

        // 提示文字
        slotsLabel = new Label("", skin);
        updateSlotsLabel();
        root.add(slotsLabel).padBottom(20).colspan(2);
        root.row();

        // 左侧：已购买武器
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

        // 右侧：当前装备
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

        // 底部按钮
        Table buttonRow = new Table();

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LevelSelectScreen(game));
            }
        });
        buttonRow.add(backButton).width(150).height(50).pad(10);

        TextButton clearButton = new TextButton("Clear All", skin);
        clearButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                loadoutManager.clearLoadout();
                refreshUI();
            }
        });
        buttonRow.add(clearButton).width(150).height(50).pad(10);

        TextButton confirmButton = new TextButton("START MISSION", skin);
        confirmButton.getLabel().setColor(Color.GREEN);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startGame();
            }
        });
        buttonRow.add(confirmButton).width(200).height(50).pad(10);

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

        // === 第一槽位：Iron Sword（初始武器，不可移除） ===
        Table swordRow = new Table();
        swordRow.setBackground(skin.newDrawable("white", new Color(0.15f, 0.25f, 0.15f, 0.9f)));
        swordRow.pad(10);

        Label swordIcon = new Label("⚔", skin);
        swordRow.add(swordIcon).width(50).padRight(10);

        Label swordName = new Label("Iron Sword (Default)", skin);
        swordName.setColor(Color.LIGHT_GRAY);
        swordRow.add(swordName).expandX().left();

        Label lockedLabel = new Label("🔒", skin);
        lockedLabel.setColor(Color.GRAY);
        swordRow.add(lockedLabel).width(40);

        loadoutSlotsTable.add(swordRow).fillX().expandX().padBottom(8);
        loadoutSlotsTable.row();

        // 显示已选武器（不包括 Iron Sword，因为它是固定的）
        for (String weaponId : selectedIds) {
            // 获取武器信息
            ShopItem item = getShopItemById(weaponId);
            if (item != null) {
                Table weaponRow = createWeaponRow(item, false);
                loadoutSlotsTable.add(weaponRow).fillX().expandX().padBottom(8);
                loadoutSlotsTable.row();
            }
        }

        // 显示空槽位（-1 是因为 Iron Sword 占用了第一个槽位）
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
            bgColor = new Color(0.3f, 0.3f, 0.15f, 0.8f); // 已选中的显示黄色调
        }

        row.setBackground(skin.newDrawable("white", bgColor));
        row.pad(10);

        // 武器图标
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

        // 武器名称
        Label nameLabel = new Label(weapon.getName(), skin);
        nameLabel.setColor(isSelected ? Color.YELLOW : Color.WHITE);
        row.add(nameLabel).expandX().left();

        // 操作按钮
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
            // 在装备栏中 - 显示移除按钮
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
        // +1 是因为 Iron Sword 始终占用第一个槽位
        int totalEquipped = 1 + selectedCount;
        slotsLabel.setText("Equipped: " + totalEquipped + " / " + max + " weapons");

        if (totalEquipped >= max) {
            slotsLabel.setColor(Color.YELLOW);
        } else {
            slotsLabel.setColor(Color.WHITE);
        }
    }

    private void startGame() {
        // 标记这是新游戏开始，应该使用 LoadoutManager 的武器选择
        loadoutManager.setFreshStart(true);
        String path = "maps/level-" + targetLevel + ".properties";
        game.setScreen(new LoadingScreen(game, path));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
    }
}
