package com.github.systeminvecklare.badger.impl.swing;

import java.awt.geom.AffineTransform;

import com.github.systeminvecklare.badger.core.graphics.components.transform.IReadableTransform;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.math.IReadableVector;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;

public class SwingUtil {
	public static AffineTransform convertToAffine(IReadableTransform transform, EasyPooler ep)
	{
		AffineTransform rotationTransform = ep.obtain(PoolableAffineTransform.class).getAffineTransform();
		AffineTransform shearScaleTransform = ep.obtain(PoolableAffineTransform.class).getAffineTransform();
		rotationTransform.setToRotation(transform.getRotation().getTheta());
		IReadableVector scale = transform.getScale();
		shearScaleTransform.setTransform(scale.getX(), transform.getShear()*scale.getY(), 0, scale.getY(), 0, 0);
			
		rotationTransform.concatenate(shearScaleTransform);
		IReadablePosition position = transform.getPosition();
		rotationTransform.translate(position.getX(), position.getY());
		return rotationTransform;
	}
}
