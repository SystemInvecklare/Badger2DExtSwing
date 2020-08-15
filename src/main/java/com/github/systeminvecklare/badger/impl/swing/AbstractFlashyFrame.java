package com.github.systeminvecklare.badger.impl.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.scene.IScene;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.ApplicationContext;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.IApplicationContext;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.ISceneManager;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.SceneManager;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.GameLoop;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.GameLoopHooksAdapter;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.IGameLoop;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.IGameLoopHooks;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.FlashyInputHandler;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.IInputHandler;

public abstract class AbstractFlashyFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private BufferedImage canvas;
	private BufferedImage offCanvas;
	private boolean threadPaused = false;
	private IGameLoop gameLoop;
	private Collection<IScene> trashCan = new ArrayList<IScene>();
	private IApplicationContext applicationContext = new ApplicationContext();
	private IScene currentScene;
	private final float step;
	private double lastFrameWidth;
	private double lastFrameHeight;
	private JPanel panel;
	private boolean windowResized = false;

	public AbstractFlashyFrame(String title, int width, int height, int fps) throws HeadlessException {
		super(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.offCanvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.step = 1f/fps;
		this.panel = new JPanel(true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				synchronized (offCanvas) {
					g.drawImage(offCanvas, 0, 0, null);
				}
			}
		};
		panel.setPreferredSize(new Dimension(width, height));
		this.add(panel);
		
		configureSceneManager();
		
		IInputHandler inputHandler = new FlashyInputHandler(null);
		IGameLoopHooks hooks = new GameLoopHooksAdapter() {
			@Override
			public void onAfterDraw() {
				synchronized (offCanvas) {
					Graphics2D offg2d = offCanvas.createGraphics();
					offg2d.drawImage(canvas, 0, 0, null);
					offg2d.dispose();
				}
				AbstractFlashyFrame.this.repaint();
			}
			
			@Override
			public void onBeforeDraw() {
				if(windowResized)
				{
					windowResized = false;
					synchronized (offCanvas) {
						double newFrameWidth = AbstractFlashyFrame.this.getSize().getWidth();
						double newFrameHeight = AbstractFlashyFrame.this.getSize().getHeight();
						int dWidth = (int) (newFrameWidth-lastFrameWidth);
						int dHeight= (int) (newFrameHeight-lastFrameHeight);
						
						lastFrameWidth = newFrameWidth;
						lastFrameHeight = newFrameHeight;
						
						int width = canvas.getWidth()+dWidth;
						int height = canvas.getHeight()+dHeight;
						width = width > 1 ? width : 1;
						height = height > 1 ? height : 1;
						canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
						BufferedImage newOffCanvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
						Graphics2D g2d = newOffCanvas.createGraphics();
						g2d.drawImage(offCanvas, 0, 0, null);
						g2d.dispose();
						offCanvas = newOffCanvas;
					}
				}
			}
		};
		gameLoop = new GameLoop(inputHandler, applicationContext, hooks) {
			private SwingDrawCycle swingDrawCycle = new SwingDrawCycle();
			private Graphics2D g2d;
			
			@Override
			protected IDrawCycle newDrawCycle() {
				this.g2d = canvas.createGraphics();
				g2d.setBackground(Color.WHITE);
				g2d.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
				
				//Turn on Antialiasing
		        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				//Flip y-axis
				g2d.scale(1, -1);
				g2d.translate(0, -canvas.getHeight());
				return swingDrawCycle.reset(this.g2d);
			}
			
			@Override
			protected IScene getCurrentScene() {
				return currentScene;
			}
			
			@Override
			protected void closeDrawCycle() {
				g2d.dispose();
				g2d = null;
			}
		};
		
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized = true;
			}
		});
		
		new SwingInputListener(inputHandler).connect(this, panel);
	}
	
	protected abstract IScene getInitialScene();
	
	@Override
	public void pack() {
		super.pack();
		this.lastFrameWidth = this.getSize().getWidth();
		this.lastFrameHeight = this.getSize().getHeight();
	}
	
	
	private void configureSceneManager() {
		SceneManager.set(new ISceneManager() {
			
			@Override
			public void skipQueuedUpdates() {
				gameLoop.skipQueuedUpdates();
			}
			
			@Override
			public void sendToTrashCan(IScene sceneToBeDisposed) {
				synchronized (trashCan) {
					trashCan.add(sceneToBeDisposed);
				}
			}
			
			@Override
			public float getWidth() {
				return canvas.getWidth();
			}
			
			@Override
			public float getStep() {
				return step;
			}
			
			@Override
			public float getHeight() {
				return canvas.getHeight();
			}
			
			@Override
			public IApplicationContext getApplicationContext() {
				return applicationContext;
			}
			
			@Override
			public void emptyTrashCan() {
				synchronized (trashCan) {
					for(IScene doomedScene : trashCan)
					{
						doomedScene.dispose();
					}
					trashCan.clear();
				}
			}
			
			@Override
			public void changeScene(IScene newScene) {
				currentScene = newScene;
			}
		});
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		currentScene = getInitialScene();
		currentScene.init();
		if(b)
		{
			new Thread(new Runnable() {
				private long lastRun = -1;
				@Override
				public void run() {
					while(true)
					{
						if(!threadPaused)
						{
							if(lastRun == -1)
							{
								lastRun = System.currentTimeMillis();
							}
							else
							{
								long now = System.currentTimeMillis();
								gameLoop.execute((now-lastRun)/1000f);
								lastRun = now;
							}
						}
						else
						{
							lastRun = System.currentTimeMillis();
						}
					}
				}
			}).start();
		}
	}
}