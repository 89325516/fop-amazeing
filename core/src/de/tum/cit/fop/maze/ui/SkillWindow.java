package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.model.Skill;

/**
 * Skill Tree Window.
 * <p>
 * Displays the skill tree where players can view and unlock skills using Skill
 * Points (SP).
 * Extends Scene2D Window to provide a draggable and modal dialog.
 */
public class SkillWindow extends Window {

    private final Player player;
    private final Skin skin;
    private final Label pointsLabel;

    /**
     * Creates the Skill Window.
     *
     * @param skin   The skin for UI elements.
     * @param player The player instance.
     */
    public SkillWindow(Skin skin, Player player) {
        super("Skill Tree", skin);
        this.skin = skin;
        this.player = player;

        setModal(true);
        setMovable(true);
        setResizable(false);
        getTitleLabel().setAlignment(com.badlogic.gdx.utils.Align.center);

        Table content = new Table();
        content.pad(20);

        pointsLabel = new Label("Skill Points: " + player.getSkillPoints(), skin);
        add(pointsLabel).padBottom(20).row();

        // Add skills
        for (Skill skill : Skill.values()) {
            addSkillRow(content, skill);
        }

        ScrollPane scroll = new ScrollPane(content, skin);
        scroll.setFadeScrollBars(false);
        add(scroll).grow().width(500).height(300).row();

        TextButton closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setVisible(false);
            }
        });
        add(closeBtn).padTop(10).padBottom(10);

        pack();
    }

    /**
     * Adds a row for a specific skill, including name, description, and unlock
     * button.
     *
     * @param table The table to add the row to.
     * @param skill The skill to display.
     */
    private void addSkillRow(Table table, final Skill skill) {
        table.add(new Label(skill.getDisplayName(), skin)).left().width(150);

        Label descLabel = new Label(skill.getDescription(), skin);
        descLabel.setWrap(true);
        table.add(descLabel).left().width(250).padLeft(10);

        final TextButton buyBtn = new TextButton("Unlock (" + skill.getCost() + ")", skin);
        buyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player.unlockSkill(skill)) {
                    updateUI();
                }
            }
        });

        table.add(buyBtn).width(100).padLeft(10).row();

        // Separator
        // table.add(new Image(skin.newDrawable("white", 0.5f, 0.5f, 0.5f,
        // 1))).colspan(3).growX().height(1).pad(5).row();
    }

    /**
     * Updates the UI to reflect current skill points and potential unlock status
     * changes.
     */
    public void updateUI() {
        pointsLabel.setText("Skill Points: " + player.getSkillPoints());

        // We might want to disable buttons if already unlocked, but rebuilding the
        // table is complex.
        // For now, the unlockSkill method returns false if already unlocked.
        // A full refresh would be better but this is a starter implementation.
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updateUI();
            setPosition(getStage().getWidth() / 2 - getWidth() / 2, getStage().getHeight() / 2 - getHeight() / 2);
        }
    }
}
