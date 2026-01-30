package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.shop.ShopItem;
import de.tum.cit.fop.maze.shop.ShopManager;
import de.tum.cit.fop.maze.utils.AudioManager;
import de.tum.cit.fop.maze.utils.DialogFactory;
import de.tum.cit.fop.maze.utils.GameLogger;
import de.tum.cit.fop.maze.utils.UIUtils;

import java.util.List;

/**
 * Shop Screen.
 *
 * Displays purchasable weapons and armor, allowing players to buy them with
 * coins.
 */
public class ShopScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final Skin skin;

    private Label coinLabel;
    private Table itemsTable;
    private ScrollPane scrollPane;
    private Texture backgroundTexture;

    public ShopScreen(MazeRunnerGame game) {
        this.game = game;
        this.skin = game.getSkin();
        // Use FitViewport to ensure consistent display across all screen sizes
        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080), game.getSpriteBatch());

        // Load background
        try {
            backgroundTexture = new Texture(Gdx.files.internal("shop_bg.jpg"));
        } catch (Exception e) {
            backgroundTexture = null;
        }

        setupUI();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Title
        Label title = new Label("SHOP", skin, "title");
        title.setAlignment(Align.center);
        root.add(title).padTop(30).padBottom(20).row();

        // Coin display
        coinLabel = new Label("Coins: " + ShopManager.getPlayerCoins(), skin);
        coinLabel.setColor(Color.GOLD);
        root.add(coinLabel).padBottom(20).row();

        // Category tabs
        HorizontalGroup tabs = new HorizontalGroup();
        tabs.space(20);

        TextButton weaponsTab = new TextButton("Weapons", skin);
        TextButton armorTab = new TextButton("Armor", skin);
        TextButton allTab = new TextButton("All", skin);

        weaponsTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showCategory(ShopItem.ItemCategory.WEAPON);
                AudioManager.getInstance().playSound("select");
            }
        });

        armorTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showCategory(ShopItem.ItemCategory.ARMOR);
                AudioManager.getInstance().playSound("select");
            }
        });

        allTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showAllItems();
                AudioManager.getInstance().playSound("select");
            }
        });

        tabs.addActor(allTab);
        tabs.addActor(weaponsTab);
        tabs.addActor(armorTab);
        root.add(tabs).padBottom(20).row();

        // Scrollable items container
        itemsTable = new Table();
        scrollPane = new ScrollPane(itemsTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // Auto-focus scroll on hover so user doesn't need to click
        UIUtils.enableHoverScrollFocus(scrollPane, stage);

        // Use percentage width for responsive layout (60% of screen width)
        root.add(scrollPane).width(Value.percentWidth(0.6f, root)).height(Value.percentHeight(0.6f, root)).padBottom(20)
                .row();

        // Back button
        TextButton backBtn = new TextButton("Back to Menu", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameLogger.debug("ShopScreen", "Back to Menu clicked");
                AudioManager.getInstance().playSound("select");
                game.setScreen(new MenuScreen(game));
            }
        });
        root.add(backBtn).padBottom(30);

        // Show all items by default
        showAllItems();
    }

    private void showAllItems() {
        populateItems(ShopManager.getAvailableItems());
    }

    private void showCategory(ShopItem.ItemCategory category) {
        populateItems(ShopManager.getItemsByCategory(category));
    }

    private void populateItems(List<ShopItem> items) {
        itemsTable.clear();

        for (ShopItem item : items) {
            Table itemRow = createItemRow(item);
            itemsTable.add(itemRow).growX().padBottom(10).row();
        }
    }

    private Table createItemRow(ShopItem item) {
        Table row = new Table();
        row.pad(10);
        row.setBackground(skin.newDrawable("white", new Color(0.2f, 0.2f, 0.3f, 0.8f)));

        // Item icon with solid black background using Stack layout
        Stack iconStack = new Stack();
        iconStack.setSize(50, 50);

        // Black background layer
        Image bgImage = new Image(skin.newDrawable("white", Color.BLACK));
        iconStack.add(bgImage);

        // Load item icon texture
        String iconPath = "images/items/shop/" + item.getTextureKey() + ".png";
        try {
            Texture iconTexture = new Texture(Gdx.files.internal(iconPath));
            iconTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Image iconImage = new Image(iconTexture);
            // Center the icon
            Table iconWrapper = new Table();
            iconWrapper.add(iconImage).size(46, 46).center();
            iconStack.add(iconWrapper);
        } catch (Exception e) {
            // If icon missing, show text placeholder
            Label iconLabel = new Label(item.getCategory() == ShopItem.ItemCategory.WEAPON ? "⚔" : "🛡", skin);
            Table iconWrapper = new Table();
            iconWrapper.add(iconLabel).size(40, 40).center();
            iconStack.add(iconWrapper);
        }
        row.add(iconStack).size(50, 50).padRight(15);

        // Item info
        Table infoTable = new Table();
        infoTable.left();

        Label nameLabel = new Label(item.getName(), skin);
        nameLabel.setColor(item.isPurchased() ? Color.GREEN : Color.WHITE);
        infoTable.add(nameLabel).left().row();

        Label descLabel = new Label(item.getDescription(), skin);
        descLabel.setFontScale(0.8f);
        descLabel.setColor(Color.LIGHT_GRAY);
        descLabel.setWrap(true); // Allow text wrapping
        infoTable.add(descLabel).left().growX(); // growX is important for wrapping

        row.add(infoTable).expandX().fillX().left();

        // Price and button
        Table priceTable = new Table();

        if (item.isPurchased()) {
            Label ownedLabel = new Label("OWNED", skin);
            ownedLabel.setColor(Color.GREEN);
            priceTable.add(ownedLabel);
        } else {
            Label priceLabel = new Label(item.getPrice() + " coins", skin);
            priceLabel.setColor(Color.GOLD);
            priceTable.add(priceLabel).padBottom(5).row();

            TextButton buyBtn = new TextButton("Buy", skin);
            boolean canAfford = ShopManager.getPlayerCoins() >= item.getPrice();
            buyBtn.setColor(canAfford ? Color.WHITE : Color.DARK_GRAY);

            // === Fix: Always add listener, give feedback if funds insufficient ===
            final ShopItem itemToBuy = item;
            final int itemPrice = item.getPrice();
            buyBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    int currentCoins = ShopManager.getPlayerCoins();
                    if (currentCoins >= itemPrice) {
                        if (ShopManager.purchaseItem(itemToBuy.getId())) {
                            AudioManager.getInstance().playSound("collect");
                            refreshUI();
                        }
                    } else {
                        // Insufficient funds feedback
                        AudioManager.getInstance().playSound("select");
                        showInsufficientFundsDialog(itemPrice, currentCoins);
                    }
                }
            });
            priceTable.add(buyBtn);
        }

        row.add(priceTable).right().padLeft(15);

        return row;
    }

    private void refreshUI() {
        coinLabel.setText("Coins: " + ShopManager.getPlayerCoins());
        showAllItems();
    }

    /**
     * Shows insufficient funds dialog.
     */
    private void showInsufficientFundsDialog(int itemPrice, int currentCoins) {
        DialogFactory.showInsufficientFundsDialog(stage, skin, itemPrice, currentCoins);
    }

    @Override
    public void show() {
        GameLogger.info("ShopScreen", "Showing Shop Screen");
        Gdx.input.setInputProcessor(stage);
        // 🔊 Global button sound
        de.tum.cit.fop.maze.utils.UIUtils.enableMenuButtonSound(stage);
        // Initial scroll focus
        if (scrollPane != null) {
            stage.setScrollFocus(scrollPane);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background using Cover mode (same as MenuScreen)
        if (backgroundTexture != null) {
            SpriteBatch batch = game.getSpriteBatch();

            // Get actual screen size - use backbuffer size to ensure correctness
            int screenWidth = Gdx.graphics.getBackBufferWidth();
            int screenHeight = Gdx.graphics.getBackBufferHeight();

            // Reset GL Viewport to full screen
            Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);

            // Set projection matrix to screen pixel coordinate system
            batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
            batch.begin();
            batch.setColor(0.4f, 0.4f, 0.4f, 1f); // Dim

            // Background texture original size
            float texWidth = backgroundTexture.getWidth();
            float texHeight = backgroundTexture.getHeight();

            // Calculate scale ratio for Cover mode
            float screenRatio = (float) screenWidth / screenHeight;
            float textureRatio = texWidth / texHeight;

            float drawWidth, drawHeight;
            float drawX, drawY;

            if (screenRatio > textureRatio) {
                // Screen is wider, fit to width, height might overflow
                drawWidth = screenWidth;
                drawHeight = screenWidth / textureRatio;
                drawX = 0;
                drawY = (screenHeight - drawHeight) / 2; // Center vertically
            } else {
                // Screen is taller, fit to height, width might overflow
                drawHeight = screenHeight;
                drawWidth = screenHeight * textureRatio;
                drawX = (screenWidth - drawWidth) / 2; // Center horizontally
                drawY = 0;
            }

            batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }

        // Restore Stage's Viewport (this will reset the correct glViewport)
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
        stage.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
    }
}
