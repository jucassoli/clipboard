/**
 * 
 */
package com.denver.clip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.denver.clip.file.BlockSlot;
import com.denver.clip.file.FileDataBlock;
import com.denver.clip.file.DownloadIdSequenceKey;

/**
 * @author jucassoli
 *
 */
@Service
public class UpDownFileSerivce {
	
	public static final long QUEUE_WAIT_TIMEOUT_SEC = 25;
	
	private ConcurrentHashMap<DownloadIdSequenceKey, BlockSlot> downloadMap;
	
	private Decoder decoder = Base64.getDecoder();
	
	public UpDownFileSerivce() {
		downloadMap = new ConcurrentHashMap<DownloadIdSequenceKey, BlockSlot>();
	}

	private BlockSlot getBlockSlot(String downloadId, long sequenceNumber) {
		DownloadIdSequenceKey key = DownloadIdSequenceKey.inst(downloadId, sequenceNumber);
		if(!downloadMap.contains(key)) {
			BlockSlot q = downloadMap.putIfAbsent(key, new BlockSlot(sequenceNumber));
			return (q==null)? downloadMap.get(key) : q;
		} else {
			return downloadMap.get(key);
		}
	}
	
	public void addEndOfFileBlock(String downloadId, long sequence) {
		BlockSlot slot = getBlockSlot(downloadId, sequence);
		slot.setBlock(new FileDataBlock(downloadId, sequence));
	}
	
	public void addBlock(String downloadId, long sequence, String data) {
		BlockSlot slot = getBlockSlot(downloadId, sequence);
		slot.setBlock(new FileDataBlock(downloadId, sequence, data));
	}
	
	public void startSendingDownload(String downloadId, OutputStream outputStream) {

		long totalSize = 0;
		
		long nextSequenceIndex = 0;
		boolean keepIterating = true;
		
		while(keepIterating) {
			BlockSlot slot = getBlockSlot(downloadId, nextSequenceIndex);
			FileDataBlock block = slot.waitAndGetBlock();
			if(block == null) {
				// Timed out waiting
				System.out.println("Timeout waiting for nextSequenceIndex:" + nextSequenceIndex);
				keepIterating = false;
			} else if(block.isEof()){
				System.out.println("EOF Found at index:" + nextSequenceIndex);
				keepIterating = false;
			} else {
				byte[] data = decoder.decode(block.getData());
				try {
					totalSize += data.length;
					
					outputStream.write(data);
					outputStream.flush();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				nextSequenceIndex++;
			}
		}
		
		try {
			System.out.println("Download end.: " + totalSize);
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    }

}
