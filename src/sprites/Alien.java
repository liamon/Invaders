package sprites;

import javax.swing.ImageIcon;
import app.InvadersApplication;

public class Alien extends Sprite2D {
	private static int deltaX, deltaY;
	
	private short frameCount = 0; // TODO Make these static somehow?
	private ImageIcon[] alienFrames;
	
	public Alien(ImageIcon img) {
		super(img);
	}

	@Override
	public void move() {
		x += deltaX;
		y += deltaY;
		setPosition(x, y);
	}
	
	public boolean isAtScreenEdge() {
		if (x <= 0 || x >= InvadersApplication.WINDOW_SIZE.width - getSpriteWidth()) {
			return true;
		}
		return false;
	}
	
	// TODO Make these next two methods static?
	public void setAnimationFrames(ImageIcon... frames) { // With varargs, no need to pass in array
		alienFrames = new ImageIcon[frames.length];
		for (int i = 0; i < frames.length; i++) {
			alienFrames[i] = frames[i];
		}
	}
	
	public void animate(int framesOfAnimation) {
		frameCount++;
		frameCount %= framesOfAnimation;
		
		if (frameCount == 0) {
			setImageIcon(alienFrames[0]);
		} else if (frameCount == framesOfAnimation / 2) {
			setImageIcon(alienFrames[1]);
		} // I do not actually need a final empty else statement here.
	}

	public static int getDeltaX() {
		return deltaX;
	}

	public static void setDeltaX(int xSpeed) {
		deltaX = xSpeed;
	}

	public static void setDeltaY(int ySpeed) {
		deltaY = ySpeed;
	}
}
