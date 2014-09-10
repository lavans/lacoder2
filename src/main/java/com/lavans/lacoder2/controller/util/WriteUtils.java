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
	 * 任意のテキストを出力する
	 * @param response
	 * @param data
	 * @param contentType
	 * @param encoding
	 */
	public void writeText(HttpServletResponse response, String data, String contentType, String encoding){
		response.setContentType(contentType);
		response.setCharacterEncoding(encoding);

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

	public void writeHtml(HttpServletResponse response, String data, String charset){
		writeText(response, data, "text/html; charset="+ charset+";", charset);
	}
	/**
	 * Write Json data to response with "application/json" and "UTF-8".
	 * @param response
	 * @param data
	 */
	public void writeJson(HttpServletResponse response, String data){
		writeText(response, data, "application/json; charset=UTF-8;", "UTF-8");
	}

	/**
	 * Write Json data to response with "application/json" and "UTF-8".
	 * @param response
	 * @param data
	 */
	public void writeJsonp(HttpServletResponse response, String data){
		writeText(response, data, "application/javascript; charset=UTF-8;", "UTF-8");
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
	public void writeXml(HttpServletResponse response, Document document, String charset) {
		PrintWriter out = null;
		try {
			//文字コードとMIMEタイプを指定する
			response.setContentType("text/xml; charset="+ charset);
			//出力ストリームを取得する
			out = response.getWriter();

			//DOMの内容をクライアントに出力する
			TransformerFactory tfactory = TransformerFactory.newInstance();
			Transformer transformer = tfactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, charset);
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
	 * Write byte data to response with contentType.
	 * @param response
	 * @param data
	 */
	public void writeBytes(HttpServletResponse response, String contentType, byte data[]){
		response.setContentType(contentType);

		try(OutputStream out = response.getOutputStream()) {
			out.write(data);
			out.flush();
		} catch (IOException e) {
			new RuntimeException(e);
		}
	}


}
