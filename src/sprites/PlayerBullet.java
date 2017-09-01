package sprites;

import javax.swing.ImageIcon;

public class PlayerBullet extends Sprite2D {

	public PlayerBullet(ImageIcon img) {
		super(img);
	}

	@Override
	public void move() {
		y += ySpeed;
	}

}
