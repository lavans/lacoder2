package com.lavans.lacoder2.manager.dto;

import java.util.HashMap;
import java.util.Map;

public class AccessSetStatusOut {
	private Map<String, String> messages = new HashMap<>();

	public Map<String, String> getMessages() {
		return messages;
	}

	public void setMessages(Map<String, String> messages) {
		this.messages = messages;
	}
	
}
