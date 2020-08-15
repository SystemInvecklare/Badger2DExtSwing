package com.github.systeminvecklare.badger.impl.swing;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.IInputHandler;

public class SwingInputListener implements KeyListener, MouseListener, MouseMotionListener, ComponentListener {
	private IInputHandler handler;
	private boolean[] keyCache = new boolean[65535];
	private int componentHeight;
	
	public SwingInputListener(IInputHandler handler) {
		this.handler = handler;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		synchronized (handler) {
			handler.registerPointerDown(e.getX(), componentHeight-e.getY(), 0, e.getButton());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		synchronized (handler) {
			handler.registerPointerUp(e.getX(), componentHeight-e.getY(), 0, e.getButton());
		}
	}
	

	@Override
	public void mouseDragged(MouseEvent e) {
		synchronized (handler) {
			handler.registerPointerDragged(e.getX(), componentHeight-e.getY(), 0);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}


	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if(!keyCache[key])
		{
			keyCache[key] = true;
			synchronized (handler) {
				handler.registerKeyDown(key);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		synchronized (handler) {
			handler.registerKeyUp(key);
		}
		keyCache[key] = false;
	}
	

	@Override
	public void componentResized(ComponentEvent e) {
		componentHeight = e.getComponent().getHeight();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	public void connect(Frame frame, Component panel) {
		frame.addKeyListener(this);
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
		this.componentHeight = panel.getHeight();
		panel.addComponentListener(this);
	}
}
