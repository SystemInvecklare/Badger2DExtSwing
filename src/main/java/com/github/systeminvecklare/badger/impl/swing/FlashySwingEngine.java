package com.github.systeminvecklare.badger.impl.swing;

import com.github.systeminvecklare.badger.core.graphics.components.layer.ILayerDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.layer.Layer;
import com.github.systeminvecklare.badger.core.graphics.components.layer.LayerDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.IMovieClipDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.MovieClip;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.MovieClipDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.scene.ISceneDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.scene.Scene;
import com.github.systeminvecklare.badger.core.graphics.components.scene.SceneDelegate;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.IFlashyEngine;
import com.github.systeminvecklare.badger.core.pooling.FlashyPoolManager;
import com.github.systeminvecklare.badger.core.pooling.IPoolManager;
import com.github.systeminvecklare.badger.core.pooling.SimplePool;
import com.github.systeminvecklare.badger.core.util.ISmartList;

public class FlashySwingEngine implements IFlashyEngine {
	private FlashyPoolManager poolManager;
	

	public FlashySwingEngine() {
		this.poolManager = new FlashyPoolManager();
		this.poolManager.registerPool(PoolableAffineTransform.class, new SimplePool<PoolableAffineTransform>(3,30) {
			@Override
			public PoolableAffineTransform newObject() {
				return new PoolableAffineTransform(this);
			}
			
			@Override
			public PoolableAffineTransform obtain() {
				return super.obtain().reset();
			}
		});
	}

	@Override
	public IPoolManager getPoolManager() {
		return poolManager;
	}

	@Override
	public ISceneDelegate newSceneDelegate(Scene wrapper) {
		return new SceneDelegate(wrapper);
	}

	@Override
	public ILayerDelegate newLayerDelegate(Layer wrapper) {
		return new LayerDelegate(wrapper);
	}

	@Override
	public IMovieClipDelegate newMovieClipDelegate(MovieClip wrapper) {
		return new MovieClipDelegate(wrapper);
	}

	@Override
	public <T> ISmartList<T> newSmartList() {
		return new SynchronizedSmartList<T>();
	}
}
