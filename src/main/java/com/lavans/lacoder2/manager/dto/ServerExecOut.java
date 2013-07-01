package com.lavans.lacoder2.manager.dto;

import java.io.Serializable;

public class ServerExecOut implements Serializable{
	private String stdout;
	private String stderr;
	public String getStdout() {
		return stdout;
	}
	public void setStdout(String stdout) {
		this.stdout = stdout;
	}
	public String getStderr() {
		return stderr;
	}
	public void setStderr(String stderr) {
		this.stderr = stderr;
	}
}
