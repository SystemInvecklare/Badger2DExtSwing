package com.github.systeminvecklare.badger.impl.swing;

import com.github.systeminvecklare.badger.core.util.ISmartList;
import com.github.systeminvecklare.badger.core.util.QuickArray;

public class SynchronizedSmartList<T> implements ISmartList<T> {
	private QuickArray<QueuedUpdate> updates = null;
	private QueuedUpdate latestUpdate = null;
	private QuickArray<T> array = null;
	private Object updateLock = new Object();
	
	@SuppressWarnings("rawtypes")
	private static QuickArray STATIC_NULL_ARRAY = new QuickArray()
	{
		@Override
		public void add(Object object) {
			throw new UnsupportedOperationException();
		}
		@Override
		public void add(Object[] objects) {
			throw new UnsupportedOperationException();
		}
		@Override
		public void addAllFrom(QuickArray other) {
			throw new UnsupportedOperationException();
		}
		@Override
		public void clear() {
		}
		
		@Override
		public Object get(int index)
		{
			return null;
		}
		@Override
		public void removeAllIn(QuickArray other) {
			throw new UnsupportedOperationException();
		}
		public int getSize() {
			return 0;	
		}
	};
	
	@Override
	public void addToBirthList(T object)
	{
		addToUpdate(true, object);
	}
	
	private void addToUpdate(boolean birth, T object) {
		if(updates == null)
		{
			updates = new QuickArray<QueuedUpdate>();
		}
		synchronized (updateLock) {
			if(latestUpdate != null)
			{
				if(latestUpdate.birth != birth)
				{
					latestUpdate = new QueuedUpdate(birth);
					updates.add(latestUpdate);
				}
			}
			else
			{
				latestUpdate = new QueuedUpdate(birth);
				updates.add(latestUpdate);
			}
			latestUpdate.list.add(object);
		}
	}

	@Override
	public void addToDeathList(T object)
	{
		addToUpdate(false, object);
	}
	
	private void update()
	{
		if(updates != null)
		{
			if(array == null)
			{
				array = new QuickArray<T>();
			}
			
			for(int i = 0; i < updates.getSize(); ++i)
			{
				QueuedUpdate update = updates.get(i);
				if(update.birth)
				{
					array.addAllFrom(update.list);
				}
				else
				{
					array.removeAllIn(update.list);
				}
				update.list.clear();
			}
			updates.clear();
			synchronized (updateLock) {
				latestUpdate = null;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public QuickArray<T> getUpdatedArray() {
		update();
		if(array == null)
		{
			return STATIC_NULL_ARRAY;
		}
		return array;
	}

	@Override
	public void clear() {
		if(updates != null)
		{
			updates.clear();
		}
		if(array != null)
		{
			array.clear();
		}
	}
	
	private class QueuedUpdate {
		public QuickArray<T> list = new QuickArray<T>();
		public boolean birth;

		public QueuedUpdate(boolean birth) {
			this.birth = birth;
		};
	}
}
