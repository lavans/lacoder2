package com.lavans.lacoder2.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.lang.StringUtils;

@Scope(Type.PROTOTYPE)
public class CommandExecutor {
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

	/** デフォルトのコマンドタイムアウト */
//	private static final int TIMEOUT=3;

	/** 出力先：標準出力 */
	private String stdout="";
	/** 出力先：標準エラー */
	private String stderr="";

	/** 実行中のプロセス */
	private Process proc = null;

	/** 文字コード Windowsで実行する場合など、UTF-8から変更したいときに利用する。 */
	private String charaset="UTF-8";
	public void setCharaset(String value){
		charaset=value;
	}
	/**
	 * コマンド実行。
	 * デフォルトの3秒タイムアウトで実行する。
	 *
	 * @param command
	 * @return
	 */
	public boolean exec(String command){
		return exec(command.split("\\s"), 0);
	}
	public boolean exec(String command, int timeout){
		return exec(command.split("\\s"), timeout);
	}
	public boolean exec(String commandArray[]){
		return exec(commandArray, 0);
	}

	/**
	 * コマンド実行。
	 * タイムアウト指定有り。
	 *
	 * @param command
	 * @param timeout タイムアウト(秒)
	 * @return
	 */
	public boolean exec(String commandArray[], int timeout){
		logger.info(StringUtils.join(commandArray, " ")+" (timeout:"+ timeout +")");
		boolean result = false;
		Timer timer = new Timer();

		try {
			proc = Runtime.getRuntime().exec(commandArray);
			// チェック開始する前に、タイムアウト用Threadを作成。
			if(timeout>0){
				timer.schedule(new TimerTask(){
					@Override
					public void run(){
						stopExec();
					}
				},timeout*1000);
			}

			// 標準入力読出
			StreamReader stdoutReader = new StreamReader(proc.getInputStream());
			stdoutReader.start();
			StreamReader stderrReader = new StreamReader(proc.getErrorStream());
			stderrReader.start();

			try {
				stdoutReader.join();
				stderrReader.join();
			} catch (InterruptedException e1) {
			}

			// 終了コードを取得する前に終了待機
			try{
				proc.waitFor();
			}catch(java.lang.InterruptedException e){
			}

			stdout = stdoutReader.getResult();
			stderr = stderrReader.getResult();

			if(proc.exitValue()==0){
				result = true;
			}else{
				logger.info("exit:"+proc.exitValue());
			}
			proc.destroy();
			Runtime.getRuntime().gc();
		}catch (IOException e) {
			logger.error("コマンド実行エラー", e);
			// IOExceptionはfalseを返せばよい。
			try{
				proc.destroy();
				Runtime.getRuntime().gc();
			}catch (Exception e2) {
			}
			proc=null;
//		}catch (InterruptedException e) {
//			// waitFor()による待機が割り込まれた場合
//			// そのようなコーディングはしていないのでここにくることはない。
//			logger.debug(e.getMessage());
//			proc.destroy();
		}finally{
			timer.cancel();
			proc = null;	// CheckThreadからコールバックされたときのためにnullをセット。
		}
		if(!result){
			logger.debug("execute error["+ StringUtils.join(commandArray, " ") +"] stdout["+ stdout +"] stderr["+ stderr +"]");
		}
		return result;
	}

	/**
	 * タイムアウトが起きたときに強制的にチェック処理を終了させる。
	 *
	 */
	protected void stopExec(){
		if(proc!=null){
			logger.debug("destroy proc");
			proc.destroy();
			Runtime.getRuntime().gc();
		}
	}

	/**
	 * @return stdout
	 */
	public String getStdout(){
		return stdout;
	}

	/**
	 * @return stderr
	 */
	public String getStderr(){
		return stderr;
	}

	/**
	 * 別スレッドで標準出力/標準エラー出力を読み出すためのクラス
	 * @author Yuki
	 *
	 */
	protected class StreamReader extends Thread{
		private final InputStream in;
		private String result="";
		private Map<String,String> mdcMap;
		@SuppressWarnings("unchecked")
		public StreamReader(InputStream in){
			this.in = in;
			mdcMap = MDC.getCopyOfContextMap();
		}
		@Override
		public void run(){
			MDC.setContextMap(mdcMap);

			String line = null;
			BufferedReader is=null;
			try {
				is = new BufferedReader(new InputStreamReader(in,charaset));
			} catch (UnsupportedEncodingException e1) {
				logger.error("", e1);
			}
			try {
				while(true){
					line=is.readLine();
					if(line==null){
						break;
					}
					result += line+"\n";
					logger.debug("stream result:"+ line);
				}
				is.close();
			} catch (IOException e) {
				logger.error("read error", e);
			}finally{
			}

			MDC.clear();
		}
		public String getResult(){
			return result;
		}
	}

}
