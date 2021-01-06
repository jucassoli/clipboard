/**
 * 
 */
package com.denver.clip.file;

/**
 * @author jucassoli
 *
 */
public class FileDataBlock {
	
	private String downloadId;
	private long sequence;
	private String data;
	private boolean eof;
	
	public FileDataBlock(String downloadId, long sequence, String data) {
		super();
		this.downloadId = downloadId;
		this.sequence = sequence;
		this.data = data;
		this.eof = false;
	}
	
	public FileDataBlock(String downloadId, long sequence) {
		this.downloadId = downloadId;
		this.sequence = sequence;
		this.data = null;
		this.eof = true;
	}

	public String getDownloadId() {
		return downloadId;
	}
	public long getSequence() {
		return sequence;
	}
	public String getData() {
		return data;
	}

	public boolean isEof() {
		return eof;
	}

	@Override
	public String toString() {
		return "FileDataBlock [downloadId=" + downloadId + ", sequence=" + sequence + ", data=" + data + ", eof=" + eof
				+ "]";
	}
	
	
}
