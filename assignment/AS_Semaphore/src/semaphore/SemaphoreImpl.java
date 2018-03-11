package semaphore;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class SemaphoreImpl implements Semaphore {
	private int value;
	private final Lock lock = new ReentrantLock(true);
	private final Condition notFull = lock.newCondition();

	public SemaphoreImpl(int initial) {
		if (initial < 0) throw new IllegalArgumentException();
		value = initial;
	}

	@Override
	public int available() {
		return value;
	}

	@Override
	public void acquire() {
		lock.lock();
		try {
			while(available() == 0) {
				try { notFull.await(); } catch(InterruptedException e){}
			}
			value--;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void release() {
		lock.lock();
		try {
			value++;
			notFull.signalAll();
		} finally {
			lock.unlock();
		}
	}
}
