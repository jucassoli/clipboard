package com.denver.clip.file;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FileUpDownInfo {

	public static long TIMEOUT_SECONDS = 25;
	
	private String fileId;
	private String downloadId;
	private CountDownLatch ownerReply;
	
	public FileUpDownInfo(String downloadId, String fileId) {
		super();
		this.fileId = fileId;
		this.downloadId = downloadId;
		ownerReply = new CountDownLatch(1);
	}
	
	public boolean waitForOwnerReply() throws InterruptedException {
		System.out.println("Start waiting for the owner of [downloadId:" + downloadId + "] to respond. Wait: " + TIMEOUT_SECONDS + " seconds");
		return ownerReply.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}
	
	public void setOwnerReplyOk() {
		ownerReply.countDown();
	}

	public String getFileId() {
		return fileId;
	}

	public String getDownloadId() {
		return downloadId;
	}

	public CountDownLatch getOwnerReply() {
		return ownerReply;
	}

	@Override
	public String toString() {
		return "FileUpDownInfo [fileId=" + fileId + ", downloadId=" + downloadId + ", ownerReply=" + ownerReply + "]";
	}
	
	
}
