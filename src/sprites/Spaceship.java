package sprites;

import java.io.File;

import javax.swing.ImageIcon;
import app.InvadersApplication;

public class Spaceship extends Sprite2D {
	private ImageIcon bulletImage = new ImageIcon(System.getProperty("user.dir") + File.separator +
			"img" + File.separator + "bullet.png");

	public Spaceship(ImageIcon img) {
		super(img);
	}

	@Override
	public void move() {
		if (x + xSpeed < 0) {
			x = 0;
		} else if (x + xSpeed > InvadersApplication.WINDOW_SIZE.width - getSpriteWidth()) {
			x = InvadersApplication.WINDOW_SIZE.width - getSpriteWidth();
		} else {
			x += xSpeed;
		}
	}
	
	public PlayerBullet shootBullet() {
		PlayerBullet newBullet = new PlayerBullet(bulletImage);
		int x = this.x + getSpriteWidth() / 2;
		int y = this.y + getSpriteWidth() / 2 - getSpriteHeight();
		
		newBullet.setYSpeed(-15);
		newBullet.setPosition(x, y);
		return newBullet;
	}
}
