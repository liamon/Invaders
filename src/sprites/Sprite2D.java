package sprites;

import java.awt.*;
import javax.swing.*;

// This and its subclasses must be in their own package so InvadersApplication
// cannot access the protected members in this class.
public abstract class Sprite2D {
	protected int x, y;
	protected ImageIcon imageIcon;
	protected int xSpeed, ySpeed;

	public Sprite2D(ImageIcon img) {
		imageIcon = img;
	}

	public void paint(Graphics g) {
		g.drawImage(imageIcon.getImage(), x, y, null);
	}
	
	public abstract void move();
	
	public int getSpriteWidth() {
		return imageIcon.getIconWidth();
	}
	
	public int getSpriteHeight() {
		return imageIcon.getIconHeight();
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setImageIcon(ImageIcon img) {
		imageIcon = img;
	}
	
	public int getXSPeed() {
		return xSpeed;
	}
	
	public void setXSpeed(int xSpeed) {
		this.xSpeed = xSpeed;
	}
	
	public void setYSpeed(int ySpeed) {
		this.ySpeed = ySpeed;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
}