import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;

public class Main {

	public static int scale = 100;
	public static RGB[][] values = new RGB[scale * scale][6];
	public static boolean[] obstacles = new boolean[scale * scale];
	
	public static class RGB {
		public float r, g, b;
		public RGB(float r, float g, float b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public int GetColorCode() {
			if (r < 0) r = 0;
			if (r > 1) r = 1;
			if (g < 0) g = 0;
			if (g > 1) g = 1;
			if (b < 0) b = 0;
			if (b > 1) b = 1;
			return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
		}
		
		public void Print() {
			System.out.println(r + ", " + g + ", " + b);
		}
	}
	
	public enum Direction {
		UP(0), DOWN(1), LEFT(2), RIGHT(3);

		public int val;
		Direction(int i) {
			this.val = i;
		}
	}
	
	public static void AddValue(int x, int y, RGB rgb, Direction direction) {
		if (x < 0 || y < 0 || x >= scale || y >= scale) return;
		values[x + y * scale][direction.val] = rgb;
	}
	
	public static void PropogateLight(RGB center, RGB next, float lose) {
		if (next.r < center.r * lose) {
			next.r = (center.r * lose);
		}
		if (next.g < center.g * lose) {
			next.g = (center.g * lose);
		}
		if (next.b < center.b * lose) {
			next.b = (center.b * lose);
		}
	}
	
	public static void MixLight(RGB A, RGB B, float mix) {
		float r = A.r + (B.r - A.r) * mix;
		float g = A.g + (B.g - A.g) * mix; 
		float b = A.b + (B.b - A.b) * mix;
		A.r = r;
		A.g = g;
		A.b = b;
	}
	
	public static float Mix(float A, float B, float mix) {
		return A + (B - A) * mix;
	}
	
	public static RGB GetValue(int x, int y, Direction direction) {
		if (x < 0 || y < 0 || x >= scale || y >= scale) return new RGB(0, 0, 0);
		return values[x + y * scale][direction.val];
	}
	
	public static boolean GetObstacle(int x, int y) {
		if (x < 0 || y < 0 || x >= scale || y >= scale) return true;
		return obstacles[x + y * scale];
	}
	
	public static Random random = new Random();
	public static void UpdateLights(int x, int y, Direction direction) {
		if (x < 0 || y < 0 || x >= scale || y >= scale) return;
		RGB center = GetValue(x, y, direction);
		center.r *= 0.95f;
		center.g *= 0.95f;
		center.b *= 0.95f;
		
		float normal = 0.98f;
		float side = 0.9f;
		float offs = (float)Math.cos(timer / 1000.0f);
		int offs_sign = (int)Math.signum(offs);
		
		int updateTick = (timer / 2) & 1;
		boolean flag = updateTick == 0;
		
		if (GetObstacle(x, y)) return;
		
		switch (direction) {
		case UP:
		{
			float r_count = 0;
			float g_count = 0;
			float b_count = 0;
			float r = 0;
			float g = 0;
			float b = 0;
			if (!GetObstacle(x, y - 1)) {
				RGB other = GetValue(x, y - 1, direction);
				MixLight(center, other, 0.75f);
			} else {
				RGB other = GetValue(x, y, Direction.DOWN);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			
			if (!GetObstacle(x - 1, y) && !GetObstacle(x - 1, y - 1)) {
				RGB other = GetValue(x - 1, y - 1, direction);
				RGB middle = GetValue(x - 1, y, direction);
				if (other.r > center.r && middle.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g && middle.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b && middle.b > center.b) {
					b += other.b;
					b_count++;
				}
			} else {
				RGB other = GetValue(x, y, Direction.RIGHT);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (!GetObstacle(x + 1, y) && !GetObstacle(x + 1, y - 1)) {
				RGB other = GetValue(x + 1, y - 1, direction);
				RGB middle = GetValue(x + 1, y, direction);
				if (other.r > center.r && middle.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g && middle.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b && middle.b > center.b) {
					b += other.b;
					b_count++;
				}
			} else {
				RGB other = GetValue(x, y, Direction.LEFT);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (r_count > 0) {
				r /= r_count;
				center.r = Mix(center.r, r, 0.25f);
			}
			if (g_count > 0) {
				g /= g_count;
				center.g = Mix(center.g, g, 0.25f);
			}
			if (b_count > 0) {
				b /= b_count;
				center.b = Mix(center.b, b, 0.25f);
			}
			
			break;
		}
		case DOWN:
		{
			float r_count = 0;
			float g_count = 0;
			float b_count = 0;
			float r = 0;
			float g = 0;
			float b = 0;
			
			if (!GetObstacle(x, y + 1)) {
				RGB other = GetValue(x, y + 1, direction);
				MixLight(center, other, (1.0f - Math.abs(offs)) * 0.75f);
			} else {
				RGB other = GetValue(x, y, Direction.UP);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (!GetObstacle(x + offs_sign, y + 1) && !GetObstacle(x + offs_sign, y)) {
				RGB other = GetValue(x + offs_sign, y + 1, direction);
				MixLight(center, other, Math.abs(offs) * 0.75f);
			}
			
			/*
			if (!GetObstacle(x - 1, y) && !GetObstacle(x - 1, y + 1)) {
				RGB other = GetValue(x - 1, y + 1, direction);
				RGB middle = GetValue(x - 1, y, direction);
				if (other.r > center.r && middle.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g && middle.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b && middle.b > center.b) {
					b += other.b;
					b_count++;
				}
			} else {
				RGB other = GetValue(x, y, Direction.RIGHT);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (!GetObstacle(x + 1, y) && !GetObstacle(x + 1, y + 1)) {
				RGB other = GetValue(x + 1, y + 1, direction);
				RGB middle = GetValue(x + 1, y, direction);
				if (other.r > center.r && middle.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g && middle.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b && middle.b > center.b) {
					b += other.b;
					b_count++;
				}
			} else {
				RGB other = GetValue(x, y, Direction.LEFT);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			 */
			
			if (r_count > 0) {
				r /= r_count;
				center.r = Mix(center.r, r, 0.25f);
			}
			if (g_count > 0) {
				g /= g_count;
				center.g = Mix(center.g, g, 0.25f);
			}
			if (b_count > 0) {
				b /= b_count;
				center.b = Mix(center.b, b, 0.25f);
			}
			
			break;
		}
		case LEFT:
		{
			float r_count = 0;
			float g_count = 0;
			float b_count = 0;
			float r = 0;
			float g = 0;
			float b = 0;
			if (!GetObstacle(x + 1, y)) {
				RGB other = GetValue(x + 1, y, direction);
				MixLight(center, other, 0.75f);
			} else {
				RGB other = GetValue(x, y, Direction.RIGHT);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (!GetObstacle(x, y - 1) && !GetObstacle(x + 1, y - 1)) {
				RGB other = GetValue(x + 1, y - 1, direction);
				RGB middle = GetValue(x, y - 1, direction);
				if (other.r > center.r && middle.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g && middle.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b && middle.b > center.b) {
					b += other.b;
					b_count++;
				}
			} else {
				RGB other = GetValue(x, y, Direction.UP);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (!GetObstacle(x, y + 1) && !GetObstacle(x + 1, y + 1)) {
				RGB other = GetValue(x + 1, y + 1, direction);
				RGB middle = GetValue(x, y + 1, direction);
				if (other.r > center.r && middle.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g && middle.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b && middle.b > center.b) {
					b += other.b;
					b_count++;
				}
			} else {
				RGB other = GetValue(x, y, Direction.DOWN);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (r_count > 0) {
				r /= r_count;
				center.r = Mix(center.r, r, 0.25f);
			}
			if (g_count > 0) {
				g /= g_count;
				center.g = Mix(center.g, g, 0.25f);
			}
			if (b_count > 0) {
				b /= b_count;
				center.b = Mix(center.b, b, 0.25f);
			}
			
			break;
		}
		case RIGHT:
		{
			float r_count = 0;
			float g_count = 0;
			float b_count = 0;
			float r = 0;
			float g = 0;
			float b = 0;
			
			if (!GetObstacle(x - 1, y)) {
				RGB other = GetValue(x - 1, y, direction);
				MixLight(center, other, 0.75f);
			} else {
				RGB other = GetValue(x, y, Direction.LEFT);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (!GetObstacle(x, y - 1) && !GetObstacle(x - 1, y - 1)) {
				RGB other = GetValue(x - 1, y - 1, direction);
				RGB middle = GetValue(x, y - 1, direction);
				if (other.r > center.r && middle.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g && middle.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b && middle.b > center.b) {
					b += other.b;
					b_count++;
				}
			} else {
				RGB other = GetValue(x, y, Direction.UP);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (!GetObstacle(x, y + 1) && !GetObstacle(x - 1, y + 1)) {
				RGB other = GetValue(x - 1, y + 1, direction);
				RGB middle = GetValue(x, y + 1, direction);
				if (other.r > center.r && middle.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g && middle.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b && middle.b > center.b) {
					b += other.b;
					b_count++;
				}
			} else {
				RGB other = GetValue(x, y, Direction.DOWN);
				
				if (other.r > center.r) {
					r += other.r;
					r_count++;
				}
				if (other.g > center.g) {
					g += other.g;
					g_count++;
				}
				if (other.b > center.b) {
					b += other.b;
					b_count++;
				}
			}
			
			if (r_count > 0) {
				r /= r_count;
				center.r = Mix(center.r, r, 0.25f);
			}
			if (g_count > 0) {
				g /= g_count;
				center.g = Mix(center.g, g, 0.25f);
			}
			if (b_count > 0) {
				b /= b_count;
				center.b = Mix(center.b, b, 0.25f);
			}
			
			break;
		}
		}
	}
	
	public static RGB GetLight(int x, int y) {
		
		RGB value = new RGB(0, 0, 0);
		for (int i = 0; i < Direction.values().length; i++) {
			RGB test = GetValue(x, y, Direction.values()[i]);
			if (value.r < test.r) value.r = test.r; 
			if (value.g < test.g) value.g = test.g;
			if (value.b < test.b) value.b = test.b;
		}
		return value;
	}
	
	public static float Curve(float value) {
		return (float)Math.sqrt(1 - value * value);
	}

	
	public static int timer = 0;
	

	static int mouse_x = 0;
	static int mouse_y = 0;
	
	static boolean left_pressed;
	static boolean right_pressed;
	static boolean middle_pressed;
	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("test");
		frame.setSize(500, 500);
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setLocationRelativeTo(null);

		frame.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				float uv_x = e.getX() / (float)frame.getWidth();
				float uv_y = e.getY() / (float)frame.getHeight();
				if (uv_x < 0) uv_x = 0;
				if (uv_x > 1) uv_x = 1;
				if (uv_y < 0) uv_y = 0;
				if (uv_y > 1) uv_y = 1;
				
				int px = (int)(uv_x * scale);
				int py = (int)((1.0f - uv_y) * (scale - 1));
				
				mouse_x = px;
				mouse_y = py;
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				float uv_x = e.getX() / (float)frame.getWidth();
				float uv_y = e.getY() / (float)frame.getHeight();
				if (uv_x < 0) uv_x = 0;
				if (uv_x > 1) uv_x = 1;
				if (uv_y < 0) uv_y = 0;
				if (uv_y > 1) uv_y = 1;
				
				int px = (int)(uv_x * scale);
				int py = (int)((1.0f - uv_y) * (scale - 1));
				
				mouse_x = px;
				mouse_y = py;
			}
			
		});
		
		frame.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				
				if (e.getButton() == 1) {
					left_pressed = true;
				} else if (e.getButton() == 3) {
					right_pressed = true;
				} else if (e.getButton() == 2) {
					middle_pressed = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == 1) {
					left_pressed = false;
				} else if (e.getButton() == 3) {
					right_pressed = false;
				} else if (e.getButton() == 2) {
					middle_pressed = false;
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				
			}
			
		});
		
		values = new RGB[scale * scale][6];
		obstacles = new boolean[scale * scale];
		for (int x = 0; x < scale; x++) {
			for (int y = 0; y < scale; y++) {
				for (int i = 0; i < 6; i++) {
					values[x + y * scale][i] = new RGB(0, 0, 0);
				}
				if (x == 8 || y == 8 || x == (scale - 8) || y == scale - 14) {
					obstacles[x + y * scale] = true;
				}
				if (x == scale / 2 && y <= 8) {
					obstacles[x + y * scale] = true;
				}
				
			}
		}
		
		BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
		while (true) {
			Update(frame, image);
		}
	}
	
	public static void Update(JFrame frame, BufferedImage image) {
		

		if (left_pressed) {
			obstacles[mouse_x + mouse_y * scale] = true;
		} else if (right_pressed) {
			AddValue(mouse_x, mouse_y, new RGB(0, 1, 0), Direction.DOWN);
		} else if (middle_pressed) {
			obstacles[mouse_x + mouse_y * scale] = false;
		}
		
		for (int px = 0; px < scale; px++) {
			for (int py = 0; py < scale; py++) {
				if (px == 0) {
					AddValue(px, py, new RGB(1, 0, 0), Direction.RIGHT);
				}
				if (px == scale - 1) {
					AddValue(px, py, new RGB(0, 0, 1), Direction.LEFT);
				}
				if (py == scale - 1) {
					AddValue(px, py, new RGB(0.7f, 0.7f, 2), Direction.DOWN);
				}
				if (py == 0) {
					AddValue(px, py, new RGB(0, 1, 0), Direction.UP);
				}
				
				if (((px + timer) & 1) == (py & 1)) {
					UpdateLights(px, py, Direction.UP);
					UpdateLights(px, py, Direction.DOWN);
					UpdateLights(px, py, Direction.LEFT);
					UpdateLights(px, py, Direction.RIGHT);
				}
			}
		}
		
		for (int x = 0; x < 500; x++) {
			for (int y = 0; y < 500; y++) {
				
				float uv_x = x / 500.0f;
				float uv_y = y / 500.0f;
				
				int px = (int)(uv_x * scale);
				int py = (int)((1.0f - uv_y) * (scale - 1));
				
				if (obstacles[px + py * scale]) {
					image.setRGB(x, y, 0xffcc00);
				} else {
					RGB light = GetLight(px, py);
					image.setRGB(x, y, GetLight(px, py).GetColorCode());
				}
				
				
				//color | (color << 8) | (color << 16)
				
			}
		}
		timer++;
		Graphics g = frame.getGraphics();
		g.setColor(Color.black);
		g.drawImage(image, 0, 0, 500, 500, null);
		g.dispose();
	}

}
