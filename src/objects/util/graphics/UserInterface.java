package objects.util.graphics;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.ArrayList;

public class UserInterface extends Canvas implements Runnable, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	public int WIDTH;
	public int HEIGHT;
	private int lastW;
	private int lastH;
	private Thread thread;
	private BufferedImage img;
	private boolean running = false;
	private HashMap<String, ImageSource> items; 
	private ArrayList<String> renderOrder;
	private int[] pixels;
	private JFrame frame;
	private long lastTime;
	private long rate;
	public ArrayList<InputEvent> events;
		
	public UserInterface(int width, int height) {
		rate = 1000/30;
		items = new HashMap();
		renderOrder = new ArrayList();
		events = new ArrayList();
		WIDTH = width;
		HEIGHT = height;
		lastW = width;
		lastH = height;
		img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		frame = new JFrame();
		frame.add(this);
		frame.addMouseListener(this);
		frame.addMouseMotionListener(this);
		frame.addMouseWheelListener(this);
		frame.addKeyListener(this);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setSize(WIDTH, HEIGHT);
		frame.setResizable(true);
	}

	public void nameWindow(String title) {
			frame.setTitle(title);
	}

	public void addImageSource(String key, ImageSource item) {
		items.put(key, item);
		renderOrder.add(key);
	}

	public void removeImageSource(String key) {
		items.remove(key);
		renderOrder.remove(key);
	}

	public void reorderSources(ArrayList<String> newRenderOrder) {
		renderOrder = newRenderOrder;
	}

	public void setFPS(int fps) {
		rate = (long)(1000/fps);
	}

	public void start() {
		// Starts running a thread
		if (running) {return;}
		frame.setVisible(true);
		running = true;
		thread = new Thread(this);
		lastTime = System.currentTimeMillis();
		thread.start();
	}

	public void stop() {
		// Stops running a thread
		if (!running) return;
		running = false;
		try {
			thread.join(); // join ends the thread
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	@Override
	public void run() {
		while (running) {
			while (((System.currentTimeMillis() - lastTime) <= rate)) {}
			lastTime = System.currentTimeMillis();
			render();
		}
	}
	
	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		
		WIDTH = frame.getWidth();
		HEIGHT = frame.getHeight();
		boolean changeScreen = false;

		if (lastW != WIDTH || lastH != HEIGHT) {
			for (String key: items.keySet()) {
				items.get(key).reshape(WIDTH, HEIGHT);
			}
			img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
			lastW = WIDTH;
			lastH = HEIGHT;
		}
		
		for (String key: renderOrder) {
			ImageSource item = items.get(key);
			item.render();
			item.load(pixels);
			//System.out.println("ImageSource rendered");
		}

		Graphics g = bs.getDrawGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		bs.show();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		events.add(e);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		events.add(e);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		events.add(e);
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		events.add(e);
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		events.add(e);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		events.add(e);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		events.add(e);
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		events.add(e);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		events.add(e);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		events.add(e);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		events.add(e);
	}
}

// https://docs.oracle.com/javase/tutorial/uiswing/events/mouselistener.html
// https://docs.oracle.com/javase/tutorial/uiswing/events/mousemotionlistener.html
// https://docs.oracle.com/javase/tutorial/uiswing/events/keylistener.html
// https://stackoverflow.com/questions/2941324/how-do-i-set-the-position-of-the-mouse-in-java
