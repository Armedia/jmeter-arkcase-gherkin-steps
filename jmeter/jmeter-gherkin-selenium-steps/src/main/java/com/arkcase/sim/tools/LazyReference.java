package com.arkcase.sim.tools;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class LazyReference<T> {
	private static final Object NONE = new Object();

	protected static final class ConstructionException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ConstructionException() {
			super();
		}

		public ConstructionException(String message) {
			super(message);
		}

		public ConstructionException(Throwable cause) {
			super(cause);
		}

		public ConstructionException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock = this.rwLock.readLock();
	private final Lock writeLock = this.rwLock.writeLock();

	private final Supplier<T> supplier;

	@SuppressWarnings("unchecked")
	private volatile T value = (T) LazyReference.NONE;

	public LazyReference(Supplier<T> supplier) {
		this.supplier = Objects.requireNonNull(supplier, "Must provide a non-null Supplier instance");
	}

	public final void reset() {
		this.writeLock.lock();
		try {
			if (this.value != LazyReference.NONE) {
				@SuppressWarnings("unchecked")
				T v = (T) LazyReference.NONE;
				this.value = v;
			}
		} finally {
			this.writeLock.unlock();
		}
	}

	protected T construct() {
		return this.supplier.get();
	}

	public final T get() {
		this.readLock.lock();
		try {
			T current = this.value;
			if (current == LazyReference.NONE) {
				this.readLock.unlock();
				this.writeLock.lock();
				try {
					current = this.value;
					if (current == LazyReference.NONE) {
						boolean ok = false;
						try {
							current = construct();
							this.value = current;
							ok = true;
						} catch (ConstructionException e) {
							// Construction failed, the reference should be reset
							Throwable cause = e.getCause();
							if (cause != e) {
								throw new RuntimeException(e.getMessage(), cause);
							} else {
								throw new RuntimeException(e.getMessage());
							}
						} finally {
							if (!ok) {
								reset();
							}
						}
					}
				} finally {
					this.readLock.lock();
					this.writeLock.unlock();
				}
			}
			return current;
		} finally {
			this.readLock.unlock();
		}
	}
}