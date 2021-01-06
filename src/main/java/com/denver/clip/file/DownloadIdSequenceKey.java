/**
 * 
 */
package com.denver.clip.file;

import java.util.Objects;

/**
 * @author jucassoli
 *
 */
public class DownloadIdSequenceKey {
	
	private String downloadId;
	private long sequenceNumber;
	
	public static DownloadIdSequenceKey inst(String downloadId, long sequenceNumber) {
		return new DownloadIdSequenceKey(downloadId, sequenceNumber);
	}
	
	public DownloadIdSequenceKey(String downloadId, long sequenceNumber) {
		super();
		this.downloadId = downloadId;
		this.sequenceNumber = sequenceNumber;
	}
	
	public String getDownloadId() {
		return downloadId;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	@Override
	public int hashCode() {
		return Objects.hash(downloadId, sequenceNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DownloadIdSequenceKey other = (DownloadIdSequenceKey) obj;
		return Objects.equals(downloadId, other.downloadId) && sequenceNumber == other.sequenceNumber;
	}

	@Override
	public String toString() {
		return "downloadIdSequenceKey [downloadId=" + downloadId + ", sequenceNumber=" + sequenceNumber + "]";
	}
	
}
