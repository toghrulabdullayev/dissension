package com.nihat.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {

  // Virtual resolution — everything is drawn in this space
  static final float WIDTH = 800f;
  static final float HEIGHT = 600f;

  // Paddle settings
  static final float PADDLE_WIDTH = 12f;
  static final float PADDLE_HEIGHT = 80f;
  static final float PADDLE_SPEED = 320f;
  static final float PADDLE_MARGIN = 20f;

  // Ball settings
  static final float BALL_SIZE = 12f;
  static final float BALL_INITIAL_SPEED = 300f;
  static final float BALL_SPEED_INCREMENT = 20f;

  // Game state
  enum GameState {
    MENU, PLAYING, PAUSED, GAME_OVER
  }

  enum GameMode {
    VS_AI, LOCAL_COOP
  }

  GameState state = GameState.MENU;
  GameMode mode = GameMode.VS_AI;

  // Paddles & ball
  Rectangle leftPaddle, rightPaddle;
  float leftPaddleSpeed = 0, rightPaddleSpeed = 0;
  float ballX, ballY, ballVX, ballVY;

  // Scores
  int leftScore = 0, rightScore = 0;
  static final int WINNING_SCORE = 7;

  // Rendering
  OrthographicCamera camera;
  FitViewport viewport;
  ShapeRenderer shapeRenderer;
  SpriteBatch batch;
  BitmapFont font, bigFont;
  GlyphLayout layout;

  // Misc
  float aiReactionTimer = 0f;
  float aiTargetY = 0f;
  String winnerText = "";
  float flashTimer = 0f;

  @Override
  public void create() {
    camera = new OrthographicCamera();
    viewport = new FitViewport(WIDTH, HEIGHT, camera);
    camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0);
    camera.update();

    shapeRenderer = new ShapeRenderer();
    batch = new SpriteBatch();
    font = new BitmapFont();
    font.getData().setScale(1.5f);
    bigFont = new BitmapFont();
    bigFont.getData().setScale(3f);
    layout = new GlyphLayout();

    initPaddles();
    resetBall(true);
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height);
    camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0);
    camera.update();
  }

  void initPaddles() {
    leftPaddle = new Rectangle(PADDLE_MARGIN, HEIGHT / 2f - PADDLE_HEIGHT / 2f, PADDLE_WIDTH, PADDLE_HEIGHT);
    rightPaddle = new Rectangle(WIDTH - PADDLE_MARGIN - PADDLE_WIDTH, HEIGHT / 2f - PADDLE_HEIGHT / 2f, PADDLE_WIDTH,
        PADDLE_HEIGHT);
  }

  void resetBall(boolean toRight) {
    ballX = WIDTH / 2f;
    ballY = HEIGHT / 2f;
    float angle = MathUtils.random(-30f, 30f) * MathUtils.degreesToRadians;
    float speed = BALL_INITIAL_SPEED;
    ballVX = (toRight ? 1 : -1) * speed * MathUtils.cos(angle);
    ballVY = speed * MathUtils.sin(angle);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    viewport.apply();
    shapeRenderer.setProjectionMatrix(camera.combined);
    batch.setProjectionMatrix(camera.combined);

    float delta = Gdx.graphics.getDeltaTime();
    flashTimer -= delta;

    switch (state) {
      case MENU:
        updateMenu();
        renderMenu();
        break;
      case PLAYING:
        updateGame(delta);
        renderGame();
        break;
      case PAUSED:
        renderGame();
        renderPause();
        break;
      case GAME_OVER:
        renderGame();
        renderGameOver();
        break;
    }
  }

  // ─── MENU ────────────────────────────────────────────────────────────────

  void updateMenu() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
      mode = GameMode.VS_AI;
      startGame();
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
      mode = GameMode.LOCAL_COOP;
      startGame();
    }
  }

  void startGame() {
    leftScore = 0;
    rightScore = 0;
    initPaddles();
    resetBall(true);
    state = GameState.PLAYING;
  }

  void renderMenu() {
    batch.begin();
    drawCentered(bigFont, "PONG", HEIGHT * 0.72f);
    drawCentered(font, "Press  1  -  vs AI", HEIGHT * 0.50f);
    drawCentered(font, "Press  2  -  Local Co-op", HEIGHT * 0.40f);
    drawCentered(font, "Left: W/S     Right: UP/DOWN", HEIGHT * 0.26f);
    drawCentered(font, "First to 7 wins!", HEIGHT * 0.18f);
    batch.end();
  }

  // ─── GAME UPDATE ─────────────────────────────────────────────────────────

  void updateGame(float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      state = GameState.PAUSED;
      return;
    }
    handleInput();
    if (mode == GameMode.VS_AI)
      updateAI(delta);
    movePaddles(delta);
    moveBall(delta);
    checkScoring();
  }

  void handleInput() {
    leftPaddleSpeed = 0;
    if (Gdx.input.isKeyPressed(Input.Keys.W))
      leftPaddleSpeed = PADDLE_SPEED;
    if (Gdx.input.isKeyPressed(Input.Keys.S))
      leftPaddleSpeed = -PADDLE_SPEED;

    if (mode == GameMode.LOCAL_COOP) {
      rightPaddleSpeed = 0;
      if (Gdx.input.isKeyPressed(Input.Keys.UP))
        rightPaddleSpeed = PADDLE_SPEED;
      if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
        rightPaddleSpeed = -PADDLE_SPEED;
    }
  }

  void updateAI(float delta) {
    aiReactionTimer -= delta;
    if (aiReactionTimer <= 0) {
      aiReactionTimer = 0.05f;
      float error = MathUtils.random(-30f, 30f);
      aiTargetY = ballY + error - PADDLE_HEIGHT / 2f;
    }
    float center = rightPaddle.y + PADDLE_HEIGHT / 2f;
    float target = aiTargetY + PADDLE_HEIGHT / 2f;
    if (center < target - 4)
      rightPaddleSpeed = PADDLE_SPEED * 0.85f;
    else if (center > target + 4)
      rightPaddleSpeed = -PADDLE_SPEED * 0.85f;
    else
      rightPaddleSpeed = 0;
  }

  void movePaddles(float delta) {
    leftPaddle.y = MathUtils.clamp(leftPaddle.y + leftPaddleSpeed * delta, 0, HEIGHT - PADDLE_HEIGHT);
    rightPaddle.y = MathUtils.clamp(rightPaddle.y + rightPaddleSpeed * delta, 0, HEIGHT - PADDLE_HEIGHT);
  }

  void moveBall(float delta) {
    ballX += ballVX * delta;
    ballY += ballVY * delta;

    if (ballY <= 0) {
      ballY = 0;
      ballVY = Math.abs(ballVY);
    }
    if (ballY >= HEIGHT - BALL_SIZE) {
      ballY = HEIGHT - BALL_SIZE;
      ballVY = -Math.abs(ballVY);
    }

    // Left paddle collision
    if (ballVX < 0
        && ballX <= leftPaddle.x + leftPaddle.width
        && ballX >= leftPaddle.x
        && ballY + BALL_SIZE >= leftPaddle.y
        && ballY <= leftPaddle.y + leftPaddle.height) {
      ballX = leftPaddle.x + leftPaddle.width;
      reflectBallOff(leftPaddle);
    }

    // Right paddle collision
    if (ballVX > 0
        && ballX + BALL_SIZE >= rightPaddle.x
        && ballX + BALL_SIZE <= rightPaddle.x + rightPaddle.width + 4
        && ballY + BALL_SIZE >= rightPaddle.y
        && ballY <= rightPaddle.y + rightPaddle.height) {
      ballX = rightPaddle.x - BALL_SIZE;
      reflectBallOff(rightPaddle);
    }
  }

  void reflectBallOff(Rectangle paddle) {
    float relHit = ((ballY + BALL_SIZE / 2f) - (paddle.y + PADDLE_HEIGHT / 2f)) / (PADDLE_HEIGHT / 2f);
    relHit = MathUtils.clamp(relHit, -1f, 1f);
    float bounceAngle = relHit * 60f * MathUtils.degreesToRadians;
    float speed = (float) Math.sqrt(ballVX * ballVX + ballVY * ballVY) + BALL_SPEED_INCREMENT;
    speed = Math.min(speed, 700f);
    int dir = ballVX < 0 ? 1 : -1;
    ballVX = dir * speed * MathUtils.cos(bounceAngle);
    ballVY = speed * MathUtils.sin(bounceAngle);
    flashTimer = 0.08f;
  }

  void checkScoring() {
    if (ballX < 0) {
      rightScore++;
      flashTimer = 0.15f;
      checkWin();
      resetBall(true);
    } else if (ballX > WIDTH) {
      leftScore++;
      flashTimer = 0.15f;
      checkWin();
      resetBall(false);
    }
  }

  void checkWin() {
    if (leftScore >= WINNING_SCORE) {
      winnerText = mode == GameMode.VS_AI ? "YOU WIN!" : "LEFT PLAYER WINS!";
      state = GameState.GAME_OVER;
    }
    if (rightScore >= WINNING_SCORE) {
      winnerText = mode == GameMode.VS_AI ? "AI WINS!" : "RIGHT PLAYER WINS!";
      state = GameState.GAME_OVER;
    }
  }

  // ─── RENDER GAME ─────────────────────────────────────────────────────────

  void renderGame() {
    boolean flash = flashTimer > 0;

    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    // Dashed center line
    shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
    int dashes = 20;
    float dashH = HEIGHT / (dashes * 2f);
    for (int i = 0; i < dashes; i++)
      shapeRenderer.rect(WIDTH / 2f - 2, i * dashH * 2, 4, dashH);

    // Paddles
    shapeRenderer.setColor(flash ? Color.CYAN : Color.WHITE);
    shapeRenderer.rect(leftPaddle.x, leftPaddle.y, leftPaddle.width, leftPaddle.height);
    shapeRenderer.rect(rightPaddle.x, rightPaddle.y, rightPaddle.width, rightPaddle.height);

    // Ball
    shapeRenderer.setColor(flash ? Color.YELLOW : Color.WHITE);
    shapeRenderer.rect(ballX, ballY, BALL_SIZE, BALL_SIZE);

    shapeRenderer.end();

    // HUD
    batch.begin();
    drawCentered(bigFont, leftScore + "   " + rightScore, HEIGHT - 50f);
    font.setColor(0.4f, 0.4f, 0.4f, 1f);
    drawCentered(font, mode == GameMode.LOCAL_COOP
        ? "W/S  vs  UP/DOWN      ESC = pause"
        : "W/S to move      ESC = pause", 18f);
    font.setColor(Color.WHITE);
    batch.end();
  }

  void renderPause() {
    batch.begin();
    drawCentered(bigFont, "PAUSED", HEIGHT / 2f + 40f);
    drawCentered(font, "Press ESC to resume", HEIGHT / 2f - 20f);
    drawCentered(font, "Press M for menu", HEIGHT / 2f - 55f);
    batch.end();
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
      state = GameState.PLAYING;
    if (Gdx.input.isKeyJustPressed(Input.Keys.M))
      state = GameState.MENU;
  }

  void renderGameOver() {
    batch.begin();
    drawCentered(bigFont, winnerText, HEIGHT / 2f + 60f);
    drawCentered(font, "Press R to play again", HEIGHT / 2f - 10f);
    drawCentered(font, "Press M for menu", HEIGHT / 2f - 45f);
    batch.end();
    if (Gdx.input.isKeyJustPressed(Input.Keys.R))
      startGame();
    if (Gdx.input.isKeyJustPressed(Input.Keys.M))
      state = GameState.MENU;
  }

  // ─── HELPERS ─────────────────────────────────────────────────────────────

  void drawCentered(BitmapFont f, String text, float y) {
    layout.setText(f, text);
    f.draw(batch, text, (WIDTH - layout.width) / 2f, y);
  }

  @Override
  public void dispose() {
    shapeRenderer.dispose();
    batch.dispose();
    font.dispose();
    bigFont.dispose();
  }
}
