/**
 * 
 */
package com.denver.clip.file;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author jucassoli
 *
 */
public class BlockSlot {
	
	public static final long TIMEOUT_WAITING_BLOCK_SECONDS = 20;

	private FileDataBlock block;
	private CountDownLatch blockReadyLatch;
	private long sequenceIndex;
	private volatile boolean hasBlock;
	
	public BlockSlot(long sequenceIndex) {
		super();
		this.sequenceIndex = sequenceIndex;
		blockReadyLatch = new CountDownLatch(1);
		block = null;
		hasBlock = false;
	}

	public synchronized FileDataBlock getBlock() {
		return block;
	}

	public void setBlock(FileDataBlock block) {
		synchronized (this) {
			this.block = block;
		}
		hasBlock = true;
		blockReadyLatch.countDown();
	}

	public CountDownLatch getBlockReadyLatch() {
		return blockReadyLatch;
	}

	public long getSequenceIndex() {
		return sequenceIndex;
	}
	
	public boolean hasBlock() {
		return hasBlock;
	}
	
	public FileDataBlock waitAndGetBlock() {
		try {
			blockReadyLatch.await(TIMEOUT_WAITING_BLOCK_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// Do nothing
		}
		return block;
	}
}
