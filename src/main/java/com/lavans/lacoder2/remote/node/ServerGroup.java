package com.lavans.lacoder2.remote.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.lavans.lacoder2.util.Config;

/**
 * NodeGroup.
 * static
 * @author mdobashi
 *
 */
public class ServerGroup implements Comparable<ServerGroup>{
	/** logger */
	private static final Logger logger = LoggerFactory.getLogger(ServerGroup.class);

	private static final String SERVER_GROUP="server-group";
	private static final String SERVER_NODE="server-node";
	private static final String SELF_NODE="self-node";
	private static final String ATTRIBUTE_NAME="name";
	private static final String ATTRIBUTE_GROUP_SELECTOR="selector";
	private static final String ATTRIBUTE_GROUP_CLASS="class";
	private static final String ATTRIBUTE_NODE_URI="uri";
	private static final String ATTRIBUTE_NODE_ONLINE="online";

	/** config reader */
	private static Map<String, ServerGroup> groupMap = new ConcurrentSkipListMap<>();

	static {
		load(Config.CONFIG_FILE);
	}

	/**
	 * init.
	 */
	public static void clear(){
		groupMap.clear();
	}

	/**
	 * すべてのServerGroup一覧を返します。
	 * 設定ファイルの記述順に並んでいます。
	 *
	 * @return
	 */
	public static List<ServerGroup> getAll(){
		List<ServerGroup> list = new ArrayList<>(groupMap.values());
//		Collections.sort(list);
		return list;
	}


	/**
	 * load
	 * Read node setting from lremote.xml.
	 * This method adds ServerNodes
	 */
	public static void load(String configFile){
		Config config = Config.getInstance(configFile);
		// self name
		String selfName = config.getNodeValue(SELF_NODE);
		// get node group
		NodeList groupList  = config.getNodeList(SERVER_GROUP);
		for(int i=0; i<groupList.getLength(); i++){
			Element groupNode = (Element)groupList.item(i);
			ServerGroup group = new ServerGroup();
			group.index=i;
			group.name = groupNode.getAttribute(ATTRIBUTE_NAME);
			group.selector = groupNode.getAttribute(ATTRIBUTE_GROUP_SELECTOR);
			group.localClass = groupNode.getAttribute(ATTRIBUTE_GROUP_CLASS);
			groupMap.put(group.getName(), group);
			logger.info("["+ group.getName() + "]");
			// get node
			NodeList nodeList = config.getNodeList(SERVER_NODE, groupNode);
			for(int j=0; j<nodeList.getLength(); j++){
				Element node = (Element)nodeList.item(j);
				// create Remote node
				ServerNode serverNode = createNode(node, j);
				serverNode.setSelf(serverNode.getName().equals(selfName));
				group.nodeList.add(serverNode);
			}
		}
	}

	private static ServerNode createNode(Element node, int index){
		ServerNode serverNode = new ServerNode(
				node.getAttribute(ATTRIBUTE_NAME),
				node.getAttribute(ATTRIBUTE_NODE_URI),
				index);
		// online="false"の時だけオフラインにする。それ以外はオンライン
		if(node.getAttribute(ATTRIBUTE_NODE_ONLINE).equals("false")){
			serverNode.setOffline();
		}
		logger.info("  "+ serverNode.toString());
		return serverNode;
	}

	/**
	 * 状態保存。
	 * onlineステータスの保存を行う。nodeの定義は変更せず、設定ファイルとメモリ内容が
	 * 一致している前提で処理を行う。
	 */
	private static final String XQUERY_NODE =
			"server-group[@name='$group']/server-node[@name='$node']";
	public static void save(){
		// TODO lacoder2.xmlが消えてしまうことがある。要検証。
		Config config = Config.getInstance(Config.CONFIG_FILE);
		for(ServerGroup group: groupMap.values()){
			for(ServerNode node: group.nodeList){
				String query = XQUERY_NODE.replace("$group", group.name).replace("$node", node.getName());
				Element xmlNode = (Element)config.getNode(query);
				xmlNode.setAttribute("online", String.valueOf(node.isOnline()));
			}
		}
		config.save();
	}
	/**
	 *
	 */
	public static ServerGroup getInstance(String name){
		return groupMap.get(name);
	}

	private int index;
	private String name;
	private String selector;
	private boolean isSync=true;
	private String localClass;
//	private SortedSet<ServerNode> onlineList = new ConcurrentSkipListSet<>(); //new ServerNodeComparator()
//	private SortedSet<ServerNode> errorList = new ConcurrentSkipListSet<>();
	private SortedSet<ServerNode> nodeList = new ConcurrentSkipListSet<>();
	public String getSelector(){
		return selector;
	}
	public boolean isSync(){
		return isSync;
	}

	/**
	 * ローカル接続かどうかを返す。
	 * @return
	 */
	public boolean isLocal(){
		return selector.equalsIgnoreCase("local");
	}

	/**
	 * ローカル接続のときのクラス名を返す
	 * 同一パッケージの中でlocal/remoteを切り替えられるようにするため、
	 * server定義の中にlocalの実クラス名を指定できるようにしたが、
	 * この機能は使わない事になったので消すかも。
	 *
	 * @return
	 */
	public String getLocalClass(){
		return localClass;
	}

	/**
	 * 指定されたノード名がServerGroupの中にあるかどうかを判定します。
	 *
	 * @param サーバーノード名
	 * @return 見つかればtrue.見つからなければfalse.
	 */
	public boolean contains(String localName){
		return find(name)!=null;
	}

	/**
	 * サーバーノードを名前から探します。
	 *
	 * @param name ノード名
	 * @return サーバーノード。見つからない場合はnull。
	 */
	public ServerNode find(String name){
		for(ServerNode node: nodeList){
			if(name.equals(node.getName())){
				return node;
			}
		}
		return null;
	}

	/**
	 * Clone RemoteNodeGroup.
	 * nodeList must be deep copied.
	 *
	 */
//	public Object clone() throws CloneNotSupportedException {
//		ServerGroup dst = (ServerGroup)super.clone();
//		dst.nodeList.addAll(this.nodeList);
//		dst.errorList.addAll(this.errorList);
//		return dst;
//	}

	/**
	 * @return groupName
	 */
	public String getName() {
		return name;
	}
	/**
	 * ServerGroup名をセットします。
	 *
	 * @param groupName
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 * @return
	 */
	public List<ServerNode> getNodeList() {
		List<ServerNode> list = new ArrayList<>(nodeList);
		return list;
	}

	/**
	 * オンラインになっているサーバーノード一覧を返します。
	 * 返却されるリストはコピーなので変更しても他への影響は有りません。
	 * リスト内にあるサーバーノードはコピーではなく本体なので、
	 * setOffline()等を呼び出すと他へも反映されます。
	 *
	 * @return connectionList
	 */
	public List<ServerNode> getOnlineList() {
		List<ServerNode> list = new ArrayList<>();
		for(ServerNode node: nodeList){
			if(node.isOnline()){
				list.add(node);
			}
		}

		// onlineが0でも接続先が一つの場合は常にそれを返す
		// 設定ファイルで自動オフライン機能のオフ
		if(list.isEmpty() && nodeList.size()==1){
			list.add(nodeList.first());
		}
		return list;
	}

	/**
	 * 文字列表記を返します。
	 * 名前:セレクタ:同期/非同期
	 *
	 */
	@Override
	public String toString(){
		return ServerGroup.class.getSimpleName()+":"+name+":"+selector+":"+(isSync?"sync":"async");
	}

	/**
	 * 比較
	 * @author sbisec
	 *
	 */
//	private static class ServerNodeComparator implements Comparator<ServerNode>{
//		@Override
//		public int compare(ServerNode o1, ServerNode o2) {
//			return Integer.compare(o1.getIndex(), o2.getIndex());
//		}
//	}

	/**
	 * 順序決定用比較メソッド
	 * 読み込み順に並べます。
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(ServerGroup o) {
		return new Integer(index).compareTo(o.index);
	}
}
