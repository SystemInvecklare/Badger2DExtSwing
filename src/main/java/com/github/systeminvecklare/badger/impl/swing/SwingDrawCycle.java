package com.github.systeminvecklare.badger.impl.swing;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.shader.IShader;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;

public class SwingDrawCycle implements IDrawCycle {
	private ITransform transform = FlashyEngine.get().getPoolManager().getPool(ITransform.class).obtain();
	private Graphics2D g2d;
	private AffineTransform originalTransform;
	

	@Override
	public ITransform getTransform() {
		return transform;
	}
	
	public SwingDrawCycle reset(Graphics2D g2d)
	{
		this.g2d = g2d;
		this.originalTransform = g2d.getTransform();
		transform.setToIdentity();
		return this;
	}
	
	public void updateGraphicsTransform()
	{
		g2d.setTransform(originalTransform);
		EasyPooler ep = EasyPooler.obtainFresh();
		try
		{
			g2d.transform(SwingUtil.convertToAffine(transform, ep));
		}
		finally
		{
			ep.freeAllAndSelf();
		}
	}

	@Override
	public void setShader(IShader shader) {
		//TODO We could define a SwingShader here though and use it
	}

	public Graphics2D getGraphics2d() {
		return g2d;
	}

	public static Graphics2D updateAndGetG2d(IDrawCycle drawCycle) {
		((SwingDrawCycle) drawCycle).updateGraphicsTransform();
		return ((SwingDrawCycle) drawCycle).getGraphics2d();
	}
}
