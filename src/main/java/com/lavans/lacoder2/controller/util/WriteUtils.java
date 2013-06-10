package com.lavans.lacoder2.controller.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * HTTP read/write utilities.
 * パッケージとかクラス名とか要検討
 *
 * @author sbisec
 *
 */
public class WriteUtils {
	/**
	 * Write Json data to response with "application/json" and "UTF-8".
	 * @param response
	 * @param data
	 */
	public void writeJson(HttpServletResponse response, String data){
		// application/json or  text/javascript
		response.setContentType("application/json; charset=UTF-8;");
		response.setCharacterEncoding("UTF-8");

		PrintWriter writer=null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			new RuntimeException(e);
		}
		writer.write(data);
		writer.flush();
		writer.close();
	}

	/**
	 * Write Image data to response with "image/gif".
	 * @param response
	 * @param img
	 */
	public void writeImage(HttpServletResponse response, BufferedImage img) {
		// image/gif

		response.setContentType("image/gif");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			ImageIO.write(img, "gif", out);
			out.flush();
			out.close();
		} catch (IOException e) {
			new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param response
	 * @param document DOMの内容
	 * @author k-tei
	 */
	public void writeXml(HttpServletResponse response, Document document) {
		PrintWriter out = null;
		try {
			//文字コードとMIMEタイプを指定する
			response.setContentType("text/xml; charset=Shift_JIS");
			//出力ストリームを取得する
			out = response.getWriter();

			//DOMの内容をクライアントに出力する
			TransformerFactory tfactory = TransformerFactory.newInstance();
			Transformer transformer = tfactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "Shift_JIS");
			transformer.transform(new DOMSource(document), new StreamResult(out));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Write Json data to response with "application/json" and "UTF-8".
	 * @param response
	 * @param data
	 */
//	private static final String DEFAULT_CHARSET="UTF-8";
//	public void writeHtml(HttpServletResponse response, String data){
//		writeHtml(response, data, DEFAULT_CHARSET);
//	}
	public void writeHtml(HttpServletResponse response, String data, String charset){
		// application/json or  text/javascript
		response.setContentType("text/html; charset="+ charset+";");
		response.setCharacterEncoding(charset);

		PrintWriter writer=null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			new RuntimeException(e);
		}
		writer.write(data);
		writer.flush();
		writer.close();
	}

	/**
	 * Write Json data to response with "application/json" and "UTF-8".
	 * @param response
	 * @param data
	 */
	public void writeBytes(HttpServletResponse response, byte data[]){
		// application/json or  text/javascript
		response.setContentType("application/octet-stream");
		response.setCharacterEncoding("UTF-8");

		try(OutputStream out = response.getOutputStream()) {
			out.write(data);
			out.flush();
		} catch (IOException e) {
			new RuntimeException(e);
		}
	}


}
