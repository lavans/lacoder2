package com.lavans.lacoder2.remote.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.Cleanup;
import net.arnx.jsonic.util.Base64;

import org.slf4j.Logger;

import com.lavans.lacoder2.lang.LogUtils;

public class ObjectSerializer {
	private static final Logger logger = LogUtils.getLogger();

	public static String serializeString(Object obj) {
		return Base64.encode(serialize(obj));
	}
	public static byte[] serialize(Object obj) {
		try {
			@Cleanup ByteArrayOutputStream bos = new ByteArrayOutputStream();
			@Cleanup ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(obj);
			return bos.toByteArray();
		}catch(NotSerializableException e){
			logger.error(obj.getClass().getSimpleName()+":"+obj.toString(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T deserialize(String str) {
		return deserialize(Base64.decode(str));
	}
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte bytes[]) {
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
