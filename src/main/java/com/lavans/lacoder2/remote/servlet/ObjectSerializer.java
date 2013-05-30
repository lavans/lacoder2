package com.lavans.lacoder2.remote.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.Cleanup;
import net.arnx.jsonic.util.Base64;

public class ObjectSerializer {
	public static String serialize(Object obj) {
		@Cleanup ByteArrayOutputStream bos = new ByteArrayOutputStream();
		@Cleanup ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(bos);
			os.writeObject(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] bytes = bos.toByteArray();
		return Base64.encode(bytes);
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(String str) {
		byte[] bytes = Base64.decode(str);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream is;
		try {
			is = new ObjectInputStream(bis);
			return (T)is.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
