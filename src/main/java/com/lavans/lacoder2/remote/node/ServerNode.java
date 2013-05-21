/* $Id: RemoteNode.java 509 2012-09-20 14:43:25Z dobashi $
 * created: 2005/08/03
 */
package com.lavans.lacoder2.remote.node;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 *
 * @author dobashi
 */
public class ServerNode implements Comparable<ServerNode>{
	/** サーバーノード名 */
	private String name;
	private String url;
	private int index;
	private boolean isOnline = true;

	public ServerNode() {
	}

	/**
	 * Constructor.
	 *
	 * @param name. ServerNode name.
	 * @param url. connection url.
	 * @param index. Sort index.
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public ServerNode(String name, String url, int index) {
		this.name = name;
		this.url = url;
		this.index = index;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public void setOnline() {
		setOnline(true);
	}

	public void setOffline(){
		setOnline(false);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUri(String uri) {
		this.url = uri;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	private boolean isSelf = false;


	@Override
	public String toString(){
		return name+"["+url+"]";
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return url
	 */
	public String getUri() {
		return url;
	}

	public boolean isSelf() {
		return isSelf;
	}
	
	public int getIndex(){
		return index;
	}

	/**
	 * package scope.
	 * @param isSelf
	 */
	void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}

	/**
	 * 比較メソッド。
	 * index昇順でソートします。
	 * 
	 */
	@Override
	public int compareTo(ServerNode o) {
		return new Integer(index).compareTo(o.index);
	}
}
