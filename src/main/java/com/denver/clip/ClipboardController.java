/**
 * 
 */
package com.denver.clip;

import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.PushBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.denver.clip.file.FileOwnerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jucassoli
 *
 */
@Controller
public class ClipboardController {

	@Autowired
	private SimpMessagingTemplate msgTemp;
	
	@Autowired
	private UpDownFileSerivce udService;
	
	@Autowired
	private FileOwnerService ownerService;
	
	@Autowired
	private Gson gson;
	
	private Executor executor = Executors.newFixedThreadPool(10);
	
	@GetMapping("/te")
	public ResponseEntity<?> receive(final HttpServletResponse response, 
			final HttpServletRequest request) {
		
		
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/clipboard")
	public String index() {
		return "index.html";
	}
	
	@PostMapping("/receive")
	public ResponseEntity<?> receiveData(@RequestBody String data, final HttpServletResponse response, 
			final HttpServletRequest request) {
		
		System.out.println("got here: " + data);
		PushBuilder pb = request.newPushBuilder();
		Enumeration<String> ee = request.getParameterNames();
		System.out.println("--- " + ee.hasMoreElements());
		msgTemp.convertAndSend("/topic/text", "{ \"a\": 22 }");
		return ResponseEntity.ok().build();
	}
	
	@MessageMapping("/input")
	@SendTo("/topic/text")
	public String broadcastInfo(@Payload String rawMessage) {
		Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
		JsonElement ele = gson.fromJson(rawMessage, JsonElement.class);
		JsonObject clip = ele.getAsJsonObject();
		
		System.out.println("--[]---Payload:\r\n" + gsonPretty.toJson(clip));
		
		return rawMessage;
	}
	
	@MessageMapping("/download-control")
	public void processDownloadControl(@Payload String rawMessage) {
		Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
		JsonElement ele = gson.fromJson(rawMessage, JsonElement.class);
		JsonObject msg = ele.getAsJsonObject();
		
		String contentType = msg.get("contentType").getAsString();
		
		//System.out.println("--[download-control]---Msg:\r\n" + gsonPretty.toJson(msg));
		
		if(contentType.equals("data-block")) {
			
			String data = msg.get("data").getAsString();
			boolean eof = msg.get("eof").getAsBoolean();
			long sequence = msg.get("sequence").getAsLong();
			String downloadId = msg.get("downloadId").getAsString();

			if(eof) {
				System.out.println("EOF true: " + sequence);
				udService.addEndOfFileBlock(downloadId, sequence);
			} else {
				udService.addBlock(downloadId, sequence, data);
			}
		}
		
		if(contentType.equals("confirm-download")) {
			String downloadId = msg.get("downloadId").getAsString();
			String fileId = msg.get("fileId").getAsString();
			
			System.out.println("--[download-control]- downloadId:" + downloadId + ", fileId:" + fileId);
			ownerService.unlockFileDownload(downloadId, fileId);
		}
		
	}

	@GetMapping("/download/{fileId}")
	public ResponseEntity<StreamingResponseBody> callDownload(@PathVariable String fileId) {

		System.out.println("--FileId: " + fileId);
		
		String downloadId = UUID.randomUUID().toString();
		System.out.println("--downloadId: " + downloadId);
		
		ownerService.addFileUpDownInfo(downloadId, fileId);
		
		JsonObject plObject = new JsonObject();
		
		JsonObject sender = new JsonObject();
		sender.addProperty("id", "host");
		
		JsonObject data = new JsonObject();
		data.addProperty("fileId", fileId);
		data.addProperty("downloadId", downloadId);
		
		plObject.add("sender", sender);
		plObject.addProperty("contentType", "cmd:get-owner-reply-back");
		plObject.add("data", data);
		
		String payload = plObject.toString();
		
		// First confirm there is a file
		executor.execute(() -> {
			msgTemp.convertAndSend("/topic/checkFile", payload);
			System.out.println("->Sent to /topic/checkFile");
		});
		System.out.println("-> Waiting for owner to answer...");
		boolean ownerReplied = ownerService.waitForOwnerReply(downloadId, fileId);
		if(ownerReplied) {
			
			StreamingResponseBody responseBody = outputStream -> {
				udService.startSendingDownload(downloadId, outputStream);
				outputStream.close();
			};

			return ResponseEntity.ok(responseBody);

		}
		System.out.println("-> No response, no origin has this file.");
		return ResponseEntity.notFound().build();

	}

}
