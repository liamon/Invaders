package app;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import sprites.*;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// This must be in a separate package so it cannot access Sprite2D's protected members.
@SuppressWarnings("serial")
public class InvadersApplication extends JFrame implements Runnable, KeyListener {
	public static final Dimension WINDOW_SIZE = new Dimension(720, 850);
	private static final int NUM_ALIENS = 4 * 7;
	private static final int VERTICAL_OFFSET = 25;
	private static int MAX_ALIEN_Y; // Can't be static as must be set in initializeAliens.
	
	// This boolean fixes the problem I had where paint would throw an exception
	// the very first time it was called but still work fine afterward.
	private boolean isReadyToPaint = false;
	private List<Alien> aliens = new ArrayList<Alien>(NUM_ALIENS);
	private Spaceship playerShip;
	private BufferStrategy strategy;
	private Graphics bufferedGraphics;
	
	// Originally this was a Queue interface with ArrayDeque implementation.
	// This was because I was only dealing with bullets going off screen
	// and not any form of collision detection.
	private List<PlayerBullet> bullets = new ArrayList<PlayerBullet>();
	private ImageIcon[] alienFrames = new ImageIcon[2];
	private int alienXSpeed = 5;
	
	private static long score = 0;
	private static long highScore = 0;
	private boolean isGameRunning;
	
	// Using File.separator makes this portable so it can
	// run on Windows, Mac and Linux without changing it.
	private static final String imageDirectory = System.getProperty("user.dir") +
			File.separator + "img" + File.separator;
	
	public static void main(String[] args) {
		new InvadersApplication();
	}
	
	public InvadersApplication() {
		setTitle("Space Invaders!");
		setBounds(0, 0, WINDOW_SIZE.width, WINDOW_SIZE.height);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		ImageIcon playerSprite = new ImageIcon(imageDirectory + "player_ship.png");
		playerShip = new Spaceship(playerSprite);
		
		// If you subtract the full icon width of the sprite it will go too far to the left.
		playerShip.setPosition(
			WINDOW_SIZE.width / 2 - playerSprite.getIconWidth() / 2,
			WINDOW_SIZE.height - 100
		);
		
		initializeAliens(alienXSpeed);
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		bufferedGraphics = strategy.getDrawGraphics();
		
		addKeyListener(this);
		Thread t = new Thread(this);
		t.start();
		isGameRunning = true;
		isReadyToPaint = true;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(20); // 50 fps
			} catch (InterruptedException ie) { ; }
			
			if (isGameRunning) {
				for (Alien alien : aliens) {
					if (alien.isAtScreenEdge()) {
						if (Alien.getDeltaX() != 0) {
							moveAliensDown();
						} else {
							alienHorizontalMotion();
						}
						break;
					}
				}
				for (Alien alien : aliens) {
					alien.move();
				}
				playerShip.move();
				moveBullets();
				
				bulletCollisionDetection();
				if (playerCollisionDetection() == true) {
					isGameRunning = false;
				}
				
				if (aliens.size() == 0) {
					startNewWave();
				}
			}
			repaint();
		}
	}
	
	private void startNewWave() {
		alienXSpeed += 5;
		initializeAliens(alienXSpeed);
	}
	
	private void moveAliensDown() {
		// For loop prevents aliens going below a certain height.
		for (Alien alien : aliens) {
			if (alien.getY() >= MAX_ALIEN_Y) {
				alienHorizontalMotion();
				return;
			}
		}
		Alien.setDeltaX(0);
		Alien.setDeltaY(25);
	}
	
	public void alienHorizontalMotion() {
		for (Alien alien : aliens) {
			if (alien.getX() >= WINDOW_SIZE.width - alien.getSpriteWidth()) { // Right side of screen
				Alien.setDeltaX(-alienXSpeed);
			} else { // Left side of screen
				Alien.setDeltaX(alienXSpeed);
			}
			Alien.setDeltaY(0);
		}
	}
	
	private void moveBullets() {
		boolean isBulletOffScreen = false;
		for (PlayerBullet bullet : bullets) {
			bullet.move();
			// If the bullet is entirely off screen
			if (bullets.get(0).getY() < 0 - bullets.get(0).getSpriteHeight() &&
					!isBulletOffScreen) { // If we know bullet is off screen, no need to check.
				isBulletOffScreen = true;
			}
		}
		// Removing outside the for loop prevents a ConcurrentModificationException.
		if (isBulletOffScreen) {
			bullets.remove(0);
		}
	}
	
	private void bulletCollisionDetection() {
		short alienIndex = 0, bulletIndex;
		Deque<Short> spritesToRemoveStack = new ArrayDeque<Short>();
		
		for (Alien a : aliens) {
			bulletIndex = 0;
			for (PlayerBullet b : bullets) {
				
				if (overlap (a, b)) {
					spritesToRemoveStack.push(alienIndex);
					spritesToRemoveStack.push(bulletIndex);
				}
				bulletIndex++;
			}
			alienIndex++;
		}
		
		// Removing it out here prevents a ConcurrentModificationException.
		while (spritesToRemoveStack.size() > 0) {
			bulletIndex = spritesToRemoveStack.pop();
			alienIndex = spritesToRemoveStack.pop();
			
			// If this was a List instead of a Deque (being used as a Stack),
			// I would have had to decrement every remaining element
			// in the list by one.
			bullets.remove(bulletIndex);
			aliens.remove(alienIndex);
			
			score += 10;
			if (score > highScore) {
				highScore += 10;
			}
		}
	}
	
	// I factored out this method before uploading this to GitHub.
	private boolean overlap (Sprite2D sprite1, Sprite2D sprite2) {
		return (sprite1.getX() >= sprite2.getX() && sprite1.getX() <= sprite2.getX() + sprite2.getSpriteWidth() ||
				sprite2.getX() >= sprite1.getX() && sprite2.getX() <= sprite1.getX() + sprite1.getSpriteWidth()) &&
				(sprite1.getY() >= sprite2.getY() && sprite1.getY() <= sprite2.getY() + sprite2.getSpriteHeight() ||
				sprite2.getY() >= sprite1.getY() && sprite2.getY() <= sprite1.getY() + sprite1.getSpriteHeight());
	}
	
	private boolean playerCollisionDetection() {
		boolean isPlayerHit = false;
		for (Alien a : aliens) {
			if (a.getY() + a.getSpriteHeight() < playerShip.getY()) {
				continue; // if the alien is too far away from the ship.
			}
			
			// Unlike in bulletCollisionDetection, we already know that there is
			// an overlap in the y-axis because of the continue in the last line.
			if (a.getX() >= playerShip.getX() && a.getX() <= playerShip.getX() + playerShip.getSpriteWidth() ||
					playerShip.getX() >= a.getX() && playerShip.getX() <= a.getX() + a.getSpriteWidth()) {
				isPlayerHit = true;
				break;
			}
		}
		return isPlayerHit;
	}

	public void paint(Graphics g) {
		if (!isReadyToPaint) {
			return;
		}

		g = bufferedGraphics;
		g.setColor(Color.BLACK);
		// This paints a white square background on top of the last frame.
		// This stops the sprites from "smearing".
		g.fillRect(0, 0, WINDOW_SIZE.width, WINDOW_SIZE.height);
		g.setColor(Color.WHITE);
		
		// Originally I tried using a JLabel for the text, but it did not work.
		if (isGameRunning) {
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
			// x and y values found through trial and error.
			g.drawString("Score: " + score + " High Score: " + highScore, 275, VERTICAL_OFFSET + 20);
			
			for (Alien alien : aliens) {
				alien.animate(50); // Aliens will change every half-second (50 / 2 == 25 frames).
				alien.paint(g);
			}
			playerShip.paint(g);
			for (PlayerBullet bullet : bullets) {
				bullet.paint(g);
			}
			
		} else {
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
			g.drawString("GAME OVER", 225, 300);
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
			g.drawString("Press any key to retry.", 270, 350);
		}
		strategy.show();

	}
	
	@Override
	public void keyPressed(KeyEvent ke) {
		int keyCode = ke.getKeyCode();
		if (isGameRunning) {
			switch (keyCode) {
			case KeyEvent.VK_LEFT:
				playerShip.setXSpeed(-10);
				break;
			case KeyEvent.VK_RIGHT:
				playerShip.setXSpeed(10);
				break;
			case KeyEvent.VK_SPACE:
				bullets.add(playerShip.shootBullet());
			}
		} else {
			isGameRunning = true; // Player can press any key to start new game.
			startNewGame();
		}
	}
	
	private void startNewGame() { // This resets any variables needing to be reset.
		score = 0;
		// W/o this line there are still aliens from the last game, which causes errors.
		aliens = new ArrayList<Alien>(NUM_ALIENS);
		alienXSpeed = 5;
		initializeAliens(alienXSpeed);
	}
	
	@Override
	public void keyReleased(KeyEvent ke) {
		playerShip.setXSpeed(0);
		// Do not need move() here as there is no need to stop the ship from
		// going off the screen. This is because it is now stationary.
	}
	
	@Override
	public void keyTyped(KeyEvent ke) { }
	
	private void initializeAliens(int xSpeed) {
		int x, y, row, column;
		final int ROW_SPACING = 8, COLUMN_SPACING = 8;
		
		alienFrames[0] = new ImageIcon(imageDirectory + "alien_ship_1.png");
		alienFrames[1] = new ImageIcon(imageDirectory + "alien_ship_2.png");
		MAX_ALIEN_Y = WINDOW_SIZE.height - 100 - alienFrames[0].getIconHeight();
		
		for (int i = 0; i < NUM_ALIENS; i++) {
			aliens.add(new Alien(alienFrames[0]));
			row = i % NUM_ALIENS % 4 ;
			column = i % NUM_ALIENS / 4; // Fixed from incorrect % 7.
			
			// In these two lines, all alien images should have same dimensions
			// so it shouldn't matter which one we use to get the measurements.
			x = column * (alienFrames[0].getIconWidth() + COLUMN_SPACING);
			y = row * (alienFrames[0].getIconHeight() + ROW_SPACING) + VERTICAL_OFFSET;
			aliens.get(i).setPosition(x, y);
			aliens.get(i).setAnimationFrames(alienFrames);
		}
		Alien.setDeltaX(xSpeed);
		Alien.setDeltaY(0);
	}
}