package com.github.systeminvecklare.badger.impl.swing;

import java.awt.geom.AffineTransform;

import com.github.systeminvecklare.badger.core.pooling.IPool;
import com.github.systeminvecklare.badger.core.pooling.IPoolable;

public class PoolableAffineTransform implements IPoolable {
	private IPool<PoolableAffineTransform> pool;
	private AffineTransform transform = new AffineTransform();
	

	public PoolableAffineTransform(IPool<PoolableAffineTransform> pool) {
		this.pool = pool;
	}

	@Override
	public void free() {
		pool.free(this);
	}
	
	public PoolableAffineTransform reset()
	{
		this.transform.setToIdentity();
		return this;
	}

	@Override
	public IPool<? extends IPoolable> getPool() {
		return pool;
	}

	public AffineTransform getAffineTransform()
	{
		return transform;
	}
}
