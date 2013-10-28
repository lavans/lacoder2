package com.lavans.lacoder2.manager.dto;

import java.util.ArrayList;
import java.util.List;

public class CacheInitOut {
	private List<String> messages;

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	
	/**
	 * メッセージ追加
	 * @param message
	 */
	public void addMessage(String message){
		if(messages==null){
			messages = new ArrayList<>();
		}
		messages.add(message);
	}
}
