package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class Main extends Canvas implements Runnable {

	private static final long serialVersionUID = -5277914939514560578L;
	public static final int TICKS_PER_SECOND = 60;
	public static final double NANOSECONDS_PER_TICK = 1000000000 / TICKS_PER_SECOND;
	public static final Color BACKGROUND_COLOR = Color.DARK_GRAY;
	public static final int HEIGHT = 1024, WIDTH = 1024;
	
	public static final double ROBOT_WIDTH = 50;
	
	private Thread thread;
	private boolean running = false;
	
	public static List<Obstacle> obstacles;
	public static Spline baseSpline, actualSpline;
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Spline-based path planning");
		Main main = new Main();
		Dimension d = new Dimension(WIDTH, HEIGHT);
		frame.setSize(d);
		frame.setPreferredSize(d);
		frame.setMaximumSize(d);
		frame.setMinimumSize(d);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.requestFocus();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.add(main);
		main.start();
	}
	
	private synchronized void start() {
		if(!running) {
			thread = new Thread(this);
			thread.start();
			running = true;
		}
	}
	
	private synchronized void stop() {
		if(running) {
			try {
				thread.join();
			} catch (InterruptedException e) { e.printStackTrace(); }
			running = false;
		}
	}
	
	public void init() {
		this.createBufferStrategy(3);
		
		GridMap.initializeMap();
		
		obstacles = new ArrayList<Obstacle>();
		obstacles.add(new Obstacle(new Vector(500, 500), 15));
		
		baseSpline = new Spline(new Vector(400, 800), new Vector(650, 200), new Vector(0, -100), new Vector(0, -100), false);
		actualSpline = new Spline(new Vector(400, 800), new Vector(650, 200), new Vector(0, -100), new Vector(0, -100), true);
	}
	
	public void run() {
		init();
		long now, delta = 0, lastTime = System.nanoTime();
		while(running) {
			now = System.nanoTime();
			delta += now - lastTime;
			lastTime = now;
			if(delta >= NANOSECONDS_PER_TICK) {
				tick();
				delta -= NANOSECONDS_PER_TICK;
			}
			render();
		}
		stop();
	}
	
	public void tick() {
		
	}
	
	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		Graphics g = bs.getDrawGraphics();
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		GridMap.render(g);
		for(Obstacle o : obstacles) o.render(g);
		
		baseSpline.render(g);
		actualSpline.render(g);
		
		g.dispose();
		bs.show();
	}
}