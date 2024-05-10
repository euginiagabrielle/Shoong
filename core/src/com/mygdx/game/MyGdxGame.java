package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import static com.badlogic.gdx.math.MathUtils.random;

public class MyGdxGame extends ApplicationAdapter {

    private String gameState;

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    final int MAP_WIDTH = 1080; //Lebar map
    final int MAP_HEIGHT = 1920; //tinggi map
    boolean gameOverLose;
    private boolean gameOverWin;
    private final int foodPos = MAP_HEIGHT - 300; //start position makanan
    private static final int totalFood = 7; //banyak makanan per baris
    private static final int totalRow = 6; //jumlah row makanan
    private static final float foodSize = 64;
    private static final int foodDistance = 60; //jarak antar makanan
    private ArrayList<Food> foods = new ArrayList<>();
    private ArrayList<rottenApple> rottenApples = new ArrayList<>();
    public GameOverLose gameOverLoseScreen;
    public GameOverWin gameOverWinScreen;
    public MainMenu mainMenuScreen;
    private GameScreen gameScreen;

    public MyGdxGame() {
        // inisialisasi objek-objek lainnya
        gameScreen = new GameScreen(this);
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }


    Player player;
    SpriteBatch batch;
    OrthographicCamera cam;
    Viewport viewport;
    Texture map;
    Texture heart;
    int playerHealth;
    int heartWidth = 89;
    int heartHeight = 89;

    private float rottenAppleTime;

    BitmapFont timeFont;
    private static final float timer = 45f;
    private float timerCount = 45f;
    BitmapFont scoreFont;
    private int score = 0;
    private boolean saveTime;
    private int lastTime;
    int highScore;
    public Screen currentScreen;


    private Music mainMusic;
    private Music mainMenuMusic;
    private Sound arrowSfx;
    private Sound arrowHitSfx;
    private Sound bunnyDeathSfx;
    private Sound rottenAppleDropSfx;
    private boolean arrowSfxPlayed = false;
    private float arrowSfxCooldown = 0;


    @Override
    public void create() {
        // Deklarasi viewport & camera
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        cam = new OrthographicCamera();
        viewport = new FitViewport((float) (MAP_WIDTH), MAP_HEIGHT, cam);
        cam.update();

        // Gambar map
        map = new Texture("bg.png");
        map.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);


		// Deklarasi musik Game
		mainMusic = Gdx.audio.newMusic(Gdx.files.internal("Main_music.mp3"));
		mainMusic.setVolume(0.2f);
		mainMusic.setLooping(true);
		mainMusic.play();

		// Deklarasi Musik MainMenu
		mainMenuMusic = Gdx.audio.newMusic(Gdx.files.internal("MainMenu_music.mp3"));
		mainMenuMusic.setVolume(0.5f);
		mainMenuMusic.setLooping(true);
		mainMenuMusic.play();



		// Deklarasi sound effect
		arrowSfx  = Gdx.audio.newSound(Gdx.files.internal("arrow_shot.mp3"));
		arrowHitSfx  = Gdx.audio.newSound(Gdx.files.internal("arrow_fruit_impact.mp3"));
		bunnyDeathSfx = Gdx.audio.newSound(Gdx.files.internal("bunny_death.mp3"));
		rottenAppleDropSfx  = Gdx.audio.newSound(Gdx.files.internal("rotten_apple_drop.mp3"));


		// Deklarasi kelinci
		batch = new SpriteBatch();
		player = new Player("rabbit.png", MAP_WIDTH, MAP_HEIGHT, arrowSfx);

        // Deklarasi makanan
        int foodRowWidth = (int) (totalFood * (foodSize + foodDistance) - foodDistance); // Total lebar semua monster dalam satu baris
        int startPos = (int) ((MAP_WIDTH - foodRowWidth) / 1.5 / 2); // Menghitung posisi x awal agar makanan berada di tengah

        //menyusun makanan agar muncul dari kiri ke kanan lalu atas ke bawah
        for (int i = 0; i < totalRow; i++) { //loop baris
            for (int j = 0; j < totalFood; j++) { //loop kolom
                float posX = startPos + j * (foodSize + foodDistance);
                float posY = foodPos - i * (foodSize + foodDistance);

                //jika index baris 0, maka memunculkan strong monster
                if (i == 0) {
                    foods.add(new StrongFood(posX, posY));
                }

                //jika index baris 1 atau 2 muncul monster medium
                else if (i == 1 || i == 2) {
                    foods.add(new MediumFood(posX, posY));
                }

                //jika index baris 3 atau 4 atau 5, muncul monster strong
                else {
                    foods.add(new WeakFood(posX, posY));
                }
            }
        }

        //Deklarasi rotten apple
        rottenAppleTime = 1.5f;
        Random random = new Random();
        for (int i = 0; i < 4; i++) { //maksimal memunculkan 4 apel
            float posX = foodSize / 2f + random.nextFloat() * (MAP_WIDTH - foodSize * 2); // perlu dikurangi dengan 2 kali ukuran apel untuk mencegah muncul di luar layar
            float posY = MAP_HEIGHT - foodSize / 2f - random.nextFloat() * (MAP_HEIGHT - foodSize * 2);  // agar apel muncul dari atas tapi tidak melebihi MAP_HEIGHT
            rottenApples.add(new rottenApple("rottenApple.png", posX, posY, rottenAppleTime));
        }

        // Score font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixeled.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 40;
        scoreFont = generator.generateFont(parameter);

        // Timer
        timeFont = generator.generateFont(parameter);
        generator.dispose();
        saveTime = true;

        // Membaca file high score
        try {
            BufferedReader br = new BufferedReader(new FileReader("saveScore.txt"));
            highScore = Integer.parseInt(br.readLine());
            br.close();
        } catch (Exception e) {

        }

        // Deklarasi heart. Satu heart mewakili 20 health
        heart = new Texture("heart.png");
        playerHealth = player.getHealth() / 20;

        setGameState("START"); // Set the initial game state to "START"
        setScreen(new MainMenu(this));

        mainMenuScreen = new MainMenu(this); // Sets the current screen to MainMenu
        gameOverLoseScreen = new GameOverLose(this);
        gameOverWinScreen = new GameOverWin(this);

        setScreen(mainMenuScreen); // Activates the screen

    }

    public void setScreen(Screen screen) {
        // Dispose the current screen and set the new screen
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        currentScreen = screen;
        currentScreen.show();
        currentScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (gameState.equals("LOSE")) {
        }
        if (gameState.equals("WIN")) {
        }

    }

    @Override
    public void render() {
        // Check the current screen and update music accordingly
        if (currentScreen instanceof GameScreen) {
            // If the current screen is the GameScreen, play the mainMusic
            if (!mainMusic.isPlaying()) {
                mainMusic.play();
            }
        } else {
            // If the current screen is not the GameScreen, stop the mainMusic
            if (mainMusic.isPlaying()) {
                mainMusic.stop();
            }
        }

        // Check the current screen and update music accordingly
        if (currentScreen instanceof MainMenu) {
            // If the current screen is the mainMenuScreen, play the mainMenuMusic
            if (!mainMenuMusic.isPlaying()) {
                mainMenuMusic.play();
            }
        } else {
            // If the current screen is not the mainMenuScreen, stop the mainMenuMusic
            if (mainMenuMusic.isPlaying()) {
                mainMenuMusic.stop();
            }
        }


        // cek kondisi game over dan update gameState
        if (gameState.equals("PLAYING")) {

            // kode render untuk game

            // Untuk memberi bg warna
            ScreenUtils.clear(1, 1, 1, 1);

            cam.update();
            batch.setProjectionMatrix(cam.combined);

            //player
            batch.begin();
            batch.draw(map, 0, 0, MAP_WIDTH, MAP_HEIGHT);
            player.handleMovement();
            player.getSprite().setPosition((player.getPosX() / 2f)+25, player.getPosY() / 2f);
            player.getSprite().draw(batch);

            //panah
            float deltaTime = Gdx.graphics.getDeltaTime();
            arrowSfxCooldown -= deltaTime;
            if (!arrowSfxPlayed && player.getLasers().size() > 0 && arrowSfxCooldown <= 0) {
                arrowSfx.play(0.5f);
                arrowSfxPlayed = true;
                arrowSfxCooldown = 0.0000000000000000000001f; // Set the cooldown duration
            } else if (player.getLasers().size() == 0) {
                arrowSfxPlayed = false;
            }

            for (Arrow Arrow : player.getLasers()) {
                Arrow.handleMovement(); //mengatur pergerakan laser
                Sprite laserSprite = Arrow.getSprite();
                laserSprite.setPosition(Arrow.getPosX(), Arrow.getPosY()-200); //mengatur posisi laser
                laserSprite.draw(batch);
            }

            // Draw makanan
            for (int i = 0; i < foods.size(); i++) {
                Food food = foods.get(i);
                food.getSprite().setPosition(food.getPosX(), food.getPosY());
                food.getSprite().draw(batch);

                // Cek apakah panah dan makanan berada pada posisi yang sama
                for (int j = 0; j < player.getLasers().size(); j++) {
                    Arrow Arrow = player.getLasers().get(j);

                    //Jika makanan dan panah berada pada posisi yang sama, maka health makanan dihilangkan
                    if (Arrow.getSprite().getBoundingRectangle().overlaps(food.getSprite().getBoundingRectangle())) {
                        food.setHealth(food.getHealth() - Arrow.getDamage());
                        player.getLasers().remove(j);
                        j--;

                        //remove makanan jika healthnya 0
                        if (food.getHealth() <= 0) {
                            if (food instanceof WeakFood) {
                                score += 5;
                            } else if (food instanceof MediumFood) {
                                score += 10;
                            } else {
                                score += 15;
                            }
                            foods.remove(i);
							arrowHitSfx.play(0.5f); //tiap kali makanannya diremove bakal bunyi
							i--;
                            break;
                        }
                    }
                }
            }

            rottenAppleTime += Gdx.graphics.getDeltaTime();

            //draw tiap rotten apple
            for (rottenApple rottenApple : rottenApples) {
                rottenApple.handleMovement();
                Sprite rottenAppleSprite = rottenApple.getSprite();
                rottenAppleSprite.setPosition(rottenApple.getPosX(), rottenApple.getPosY());
                rottenAppleSprite.draw(batch);

                // Menghilangkan rotten apple jika mencapai batas bawah layar
                if (rottenApple.getPosY() < 255) {
                    rottenApple.setPosition(random.nextFloat() * MAP_WIDTH, MAP_HEIGHT + random.nextFloat() * MAP_HEIGHT);
                }

                // Cek apakah rotten apple dan player berada pada posisi yang sama
                if (rottenAppleSprite.getBoundingRectangle().overlaps(player.getSprite().getBoundingRectangle())) {
					rottenAppleDropSfx.play(0.2f); // sfx tiap kali player kena apple
					player.setHealth(player.getHealth() - 20);  // Kurangi health player
                    playerHealth = player.getHealth() / 20;  // Update jumlah heart
                    rottenApple.setPosition(random.nextFloat() * MAP_WIDTH, MAP_HEIGHT + random.nextFloat() * MAP_HEIGHT);  // Reset posisi meteor
                }
            }

            // Countdown
            GlyphLayout timeLayout = new GlyphLayout(timeFont, "Time: ");
            timeFont.draw(batch, timeLayout, MAP_WIDTH - 850 - timeLayout.width, 30 + timeLayout.height);
            if (timerCount > 0) {
                GlyphLayout timerLayout = new GlyphLayout(timeFont, String.valueOf((int) timerCount));
                timeFont.draw(batch, timerLayout, MAP_WIDTH - 680 - timeLayout.width, 30 + timeLayout.height);
                timerCount -= Gdx.graphics.getDeltaTime();
            } else {
                GlyphLayout timerLayout = new GlyphLayout(timeFont, String.valueOf(0));
                timeFont.draw(batch, timerLayout, MAP_WIDTH - 680 - timeLayout.width, 30 + timeLayout.height);
            }

            // Score
            GlyphLayout scoreLabelLayout = new GlyphLayout(scoreFont, "Score: " + score);
            scoreFont.draw(batch, scoreLabelLayout, MAP_WIDTH - 140 - scoreLabelLayout.width, 30 + scoreLabelLayout.height);

            GlyphLayout highScoreLabelLayout1 = new GlyphLayout(scoreFont, "High score: ");
            scoreFont.draw(batch, highScoreLabelLayout1, MAP_WIDTH - 630 - highScoreLabelLayout1.width, 1800 + highScoreLabelLayout1.height);
            GlyphLayout highScoreLabelLayout2 = new GlyphLayout(scoreFont, String.valueOf(highScore));
            scoreFont.draw(batch, highScoreLabelLayout2, MAP_WIDTH - 500 - highScoreLabelLayout2.width, 1800 + highScoreLabelLayout2.height);

            if (foods.size() == 0 && saveTime) {
                saveTime = false;
                lastTime = (int) timerCount;
                score += (timer - lastTime) * 2;
                System.out.println(score);
                if (score >= highScore) {
                    highScore = score;

                    try {
                        BufferedWriter bw = new BufferedWriter(new FileWriter("saveScore.txt"));
                        bw.write("" + highScore);
                        bw.close();
                    } catch (Exception e) {

                    }
                }
            }

            // Draw hearts
            for (int i = 0; i < playerHealth; i++) {
                batch.draw(heart, 40 + i * (heartWidth + 3), 40 + timeLayout.height, heartWidth, heartHeight);
            }

            batch.end();
            if (playerHealth <= 0 || timerCount <= 0 && foods.size() > 0) {
                gameState = "LOSE";
                setScreen(gameOverLoseScreen);
            } else if (playerHealth > 0 && foods.size() == 0 && timerCount > 0) {
                gameState = "WIN";
                setScreen(gameOverWinScreen);
            }

        }
        currentScreen.render(Gdx.graphics.getDeltaTime());
    }

    public void resetGame() {
        // Stop all music and sound effects
        mainMusic.stop();
        mainMenuMusic.stop();
        arrowSfx.stop();
        arrowHitSfx.stop();
        bunnyDeathSfx.stop();
        rottenAppleDropSfx.stop();

        // Reset player
        player = new Player("rabbit.png", MAP_WIDTH, MAP_HEIGHT, arrowSfx);
        playerHealth = player.getHealth() / 20;

        // Foods
        foods.clear();
        int foodRowWidth = (int) (totalFood * (foodSize + foodDistance) - foodDistance); // Total lebar semua monster dalam satu baris
        int startPos = (int) ((MAP_WIDTH - foodRowWidth) / 1.5 / 2); // Menghitung posisi x awal agar makanan berada di tengah
        for (int i = 0; i < totalRow; i++) { //loop baris
            for (int j = 0; j < totalFood; j++) { //loop kolom
                float posX = startPos + j * (foodSize + foodDistance);
                float posY = foodPos - i * (foodSize + foodDistance);

                //jika index baris 0, maka memunculkan strong monster
                if (i == 0) {
                    foods.add(new StrongFood(posX, posY));
                }

                //jika index baris 1 atau 2 muncul monster medium
                else if (i == 1 || i == 2) {
                    foods.add(new MediumFood(posX, posY));
                }

                //jika index baris 3 atau 4 atau 5, muncul monster strong
                else {
                    foods.add(new WeakFood(posX, posY));
                }
            }
        }

        // Rotten Apples
        rottenApples.clear();
        rottenAppleTime = 1.5f;
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            float posX = foodSize / 2f + random.nextFloat() * (MAP_WIDTH - foodSize);
            float posY = MAP_HEIGHT + random.nextFloat() * MAP_HEIGHT;
            rottenApples.add(new rottenApple("rottenApple.png", posX, posY, rottenAppleTime));
        }

        // Reset score and timer
        score = 0;
        saveTime = true;
        timerCount = timer;  // Assuming `timer` is the initial timer value

        // Reset game state
        gameState = "START";

        // Reset current screen to MainMenu
        setScreen(mainMenuScreen);
    }



    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        cam.position.set(MAP_WIDTH / 2f, MAP_HEIGHT / 2f, 0);
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }
}
