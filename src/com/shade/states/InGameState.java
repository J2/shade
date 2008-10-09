package com.shade.states;

import java.awt.Font;
import java.io.InputStream;
import java.util.LinkedList;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.*;
import org.newdawn.slick.util.ResourceLoader;

import com.shade.base.Entity;
import com.shade.controls.*;
import com.shade.crash.*;
import com.shade.entities.*;
import com.shade.entities.util.MushroomFactory;
import com.shade.light.InfiniteLight;
import com.shade.light.LightMask;
import com.shade.light.LightSource;
import com.shade.shadows.*;
import com.shade.shadows.ShadowEntity.ShadowIntensity;

public class InGameState extends BasicGameState {

    public static final int ID = 1;
    private static final float SUN_START_ANGLE = 2.5f;
    private static final float SUN_START_DEPTH = 10f;
    private static final float SUN_ANGLE_INCREMENT = 0.001f;

    private enum Status {
        NOT_STARTED, RUNNING, PAUSED, GAME_OVER
    };

    private Status currentStatus;

    private ShadowLevel level;
    private MeterControl meter;
    private CounterControl counter;

    private Image backgroundSprite, trimSprite;
    private Image counterSprite;
    private TrueTypeFont counterFont;

    private Player player;

    private int timer, totalTimer;

    private int numMoles;
    
    LightMask l;
    @Override
    public int getID() {
        return ID;
    }

    public void init(GameContainer container, StateBasedGame game)
            throws SlickException {
        level = new ShadowLevel(new Grid(8, 6, 200), SUN_START_ANGLE,
                SUN_START_DEPTH, SUN_ANGLE_INCREMENT);
        currentStatus = Status.NOT_STARTED;
        initSprites();
        initFonts();
        l = new LightMask(container.getWidth(),container.getHeight());

    }

    private void initFonts() throws SlickException {
        try {
            InputStream oi = ResourceLoader
                    .getResourceAsStream("states/ingame/jekyll.ttf");
            Font jekyll = Font.createFont(Font.TRUETYPE_FONT, oi);
            counterFont = new TrueTypeFont(jekyll.deriveFont(36f), true);
        } catch (Exception e) {
            throw new SlickException("Failed to load font.", e);
        }
    }

    private void initSprites() throws SlickException {
        backgroundSprite = new Image("states/ingame/background.png");
        trimSprite = new Image("states/ingame/trim.png");
        counterSprite = new Image("states/ingame/counter.png");
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game)
            throws SlickException {
        currentStatus = Status.RUNNING;
        totalTimer = 0;
        timer = 0;

        level.clear();
        meter = new MeterControl(20, 456, 100, 100);
        counter = new CounterControl(60, 520, counterSprite, counterFont);
        numMoles = 0;

        initObstacles();
        initBasket();
        initPlayer();
    }

    private void initShrooms(GameContainer container) throws SlickException {
        for (int i = 0; i < 5; i++) {
            Vector2f p = level.randomPoint(container);
            level.add(MushroomFactory.makeMushroom(p.x, p.y));
        }
    }

    private void initObstacles() throws SlickException {
        LinkedList<ShadowCaster> casters = new LinkedList<ShadowCaster>();
        casters.add(new Block(55, 355, 125, 125, 16));
        casters.add(new Block(224, 424, 56, 56, 6));
        casters.add(new Block(324, 424, 56, 56, 6));
        casters.add(new Block(75, 225, 56, 56, 6));
        casters.add(new Block(545, 330, 80, 80, 10));
        casters.add(new Block(445, 460, 80, 80, 10));
        // domes
        casters.add(new Dome(288, 165, 32, 7));
        casters.add(new Dome(180, 95, 44, 10));
        casters.add(new Dome(300, 65, 25, 6));
        casters.add(new Dome(710, 80, 28, 6));
        casters.add(new Dome(600, 100, 40, 9));
        casters.add(new Dome(680, 220, 60, 13));
        // fences
        casters.add(new Fence(225, 225, 11, 120, 5));
        casters.add(new Fence(390, 140, 120, 11, 5));
        casters.add(new Fence(715, 368, 11, 120, 5));
        casters.add(new Fence(50, 50, 11, 120, 5));
        // shrubs
        // casters.add(new Shrub(300, 300));
        
        //gargoyles
        Gargoyle g1 = new Gargoyle(500,300);
        Gargoyle g2 = new Gargoyle(500,200);
        Gargoyle g3 = new Gargoyle(500,400);
    	casters.add(g1);
    	casters.add(g2);
    	casters.add(g3);
    	level.add((Entity)g1);
    	level.add((Entity)g2);
    	level.add((Entity)g3);
    	
    	LightSource light = new InfiniteLight();
    	l.add(light);

        for (ShadowCaster c : casters) {
            level.add(c);
            l.add(c);
        }
    }

    private void initBasket() throws SlickException {
        Basket b = new Basket(400, 250, 65, 40);
        b.add(counter);
        b.add(meter);
        level.add(b);
    }

    private void initPlayer() throws SlickException {
        player = new Player(400, 350, 18);
        level.add(player);
    }

    public void render(GameContainer container, StateBasedGame game, Graphics g)
            throws SlickException {
        backgroundSprite.draw();
        l.render(g);
        level.render(game, g);
		trimSprite.draw();
		GL11.glDisable(GL11.GL_BLEND);
        
        meter.render(game, g);
        counter.render(game, g);
        if (currentStatus == Status.GAME_OVER) {
            counterFont.drawString(320, 300, "Game Over");
        }
    }

    public void update(GameContainer container, StateBasedGame game, int delta)
            throws SlickException {
    	l.update();
        if (currentStatus == Status.RUNNING) {
            level.update(game, delta);
            timer += delta;
            totalTimer += delta;

            // first run
            if (totalTimer == delta) {
                initShrooms(container);
            }

            // Randomly plant mushrooms
            if (Math.random() > .9965 || timer % 5000 + delta > 5000) {
                Vector2f p = level.randomPoint(container);
                level.add(MushroomFactory.makeMushroom(p.x, p.y));
            }

            if (counter.value >= 5 && numMoles < 1) {
                level.add(new Mole(4000));
                numMoles++;
            }
            if (counter.value > 25 && numMoles < 2) {
                level.add(new Mole(5000));
                numMoles++;
            }
            if (counter.value > 50 && numMoles < 3) {
                level.add(new Mole(6000));
                numMoles++;
            }

            meter.update(game, delta);
            counter.update(game, delta);

            if (player.hasIntensity(ShadowIntensity.UNSHADOWED)) {
               meter.decrement(0.05);
            }
            
            if(player.isStunned()){
            	meter.decrement(0.5);
            }

            // Check for lose condition
            if (meter.isEmpty()) {
                currentStatus = Status.GAME_OVER;
            }
        }

        // check whether to restart
        if (container.getInput().isKeyPressed(Input.KEY_R)) {
            enter(container, game);
        }
    }

}
