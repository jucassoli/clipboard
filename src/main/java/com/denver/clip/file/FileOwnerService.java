/**
 * 
 */
package com.denver.clip.file;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * @author jucassoli
 *
 */
@Service
public class FileOwnerService {
	
	private Map<String, FileUpDownInfo> fileUpDownInfoMap;

	public FileOwnerService() {
		fileUpDownInfoMap = new HashMap<String, FileUpDownInfo>();
	}
	
	public boolean waitForOwnerReply(String downloadId, String fileId) {
		FileUpDownInfo fileInfo = getFileUpDown(downloadId, fileId);
		if (fileInfo != null) {
			try {
				boolean result = fileInfo.waitForOwnerReply();
				fileUpDownInfoMap.remove(buildDownFileKey(downloadId, fileId));
				
				return result;
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		return false;
	}
	
	public String buildDownFileKey(String downloadId, String fileId) {
		return downloadId + "." + fileId;
	}
	
	public FileUpDownInfo getFileUpDown(String downloadId, String fileId) {
		return fileUpDownInfoMap.get(buildDownFileKey(downloadId, fileId));
	}
	
	public void addFileUpDownInfo(String downloadId, String fileId) {
		FileUpDownInfo fileUpDownInfo = new FileUpDownInfo(downloadId, fileId);
		fileUpDownInfoMap.put(buildDownFileKey(downloadId, fileId), fileUpDownInfo);
	}

	public void unlockFileDownload(String downloadId, String fileId) {
		FileUpDownInfo fileInfo = getFileUpDown(downloadId, fileId);
		if (fileInfo != null) {
			fileInfo.setOwnerReplyOk();
		} else {
			System.out.println("ERROR- could not find [downloadId: " + downloadId + "], fileId: " + fileId);
		}
	}
}
