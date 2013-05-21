package com.lavans.lacoder2.lang;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class FileUtils{
	/**
	 * 設定保存。
	 * tmpファイルを作成して、最後にrenameします。元のファイルは.bakで保存します。
	 * @param lastname
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static boolean write(String filename, Collection<String> data) throws IOException{
		String tmpFilename = filename+".tmp";
		String bakFilename = filename+".bak";
		boolean result=false;
		File file = new File(tmpFilename);

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for(String str: data){
			out.write(str+"\n");
		}
		// 書き込み完了
		out.close();

		// bakファイルが存在するなら消す
		File bakFile = new File(bakFilename);
		if(bakFile.exists()){
			bakFile.delete();
		}
		// 旧ファイル保存
		File orgFile = new File(filename);
		orgFile.renameTo(new File(bakFilename));
		// ファイル移動
		result = file.renameTo(new File(filename));

		return result;
	}
	
	/**
	 * クラス名とメソッド名と拡張子からファイル名を作成する。
	 * クラスと同じディレクトリ上にある設定ファイル名作成する。
	 * フォーマットは"path/to/dir/classname-additional.extention"。
	 * 追加文字列を省略したときは"<クラス名>.<拡張子>"。
	 * 拡張子を省略したときは"<クラス名>.xml"。
	 * 
	 * 例)
	 * makeFileName(this)
	 *  ... "jp/co/sbisec/iris/common/di/ServiceManagerTest.xml
	 * makeFileName(this, "local")
	 *  ... "jp/co/sbisec/iris/common/di/ServiceManagerTest-local.xml
	 * makeFileName(this, "remote","json")
	 *  ... "jp/co/sbisec/iris/common/di/ServiceManagerTest-remote.json
	 * 
	 * @param clazz
	 * @param method
	 * @param extension
	 * @return
	 */
	private static final String DEFAULT_EXTENSION = "xml";
	public static String makeResourceFileName(Object obj){
		return makeResourceFileName(obj.getClass(), null, DEFAULT_EXTENSION);
	}
	public static String makeResourceFileName(Class<?> clazz){
		return makeResourceFileName(clazz, null, DEFAULT_EXTENSION);
	}
	public static String makeResourceFileName(Object obj, String additional){
		return makeResourceFileName(obj.getClass(), additional, DEFAULT_EXTENSION);
	}
	public static String makeResourceFileName(Class<?> clazz, String additional){
		return makeResourceFileName(clazz, additional, DEFAULT_EXTENSION);
	}
	public static String makeResourceFileName(Object obj, String additional, String extension){
		return makeResourceFileName(obj.getClass(), additional, extension);
	}
	public static String makeResourceFileName(Class<?> clazz, String additional, String extension){
		String filename = clazz.getName().replace(".", "/");
		if(!StringUtils.isEmpty(additional)){
			filename += "-" + additional;
		}
		return filename + "." + extension;
	}

}
