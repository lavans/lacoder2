## lacoder2とは <br>
「lavans coder」の略です。Java開発を楽にするための共通クラス群＋コードジェネレーターです。
主な機能は下記です。
 * xmlファイルの読み込み(utils.Config)
 * JDBCコネクションプール機能(sqlパッケージ)
 * 超シンプルなMVCコントローラー(controllerパッケージ)
 * 設定不要のDI(diパッケージ)
 * 分散オブジェクト(remoteパッケージ)
 * Webアプリ自動生成(lacoder-generator)

## News 更新情報
 * 2011/04/28 luz2->lacoder名称統合
 * 2011/02/08 Wiki着手

## ドキュメント
 * [ポリシー](https://github.com/lavans/lacoder2/wiki/ポリシー)
 * [アーキテクチャ概要](https://github.com/lavans/lacoder2/wiki/アーキテクチャ概要)
 * [動かしてみよう](https://github.com/lavans/lacoder2/wiki/動かしてみよう)
 * [アーキテクチャ詳細](https://github.com/lavans/lacoder2/wiki/アーキテクチャ詳細)
 * [フォルダ構成](https://github.com/lavans/lacoder2/wiki/フォルダ構成)
 * [XMLReader](https://github.com/lavans/lacoder2/wiki/XMLReader)
 * [DBManager](https://github.com/lavans/lacoder2/wiki/DBManager)
 * [DIContainer](https://github.com/lavans/lacoder2/wiki/DIContainer)
 * [RemoteProcedureCall](https://github.com/lavans/lacoder2/wiki/RemoteProcedureCall)
 * [拡張子について](https://github.com/lavans/lacoder2/wiki/拡張子について)
 * [jspテンプレート](https://github.com/lavans/lacoder2/wiki/jspテンプレート)
 * [用語集](https://github.com/lavans/lacoder2/wiki/用語集)
 * [TODO](https://github.com/lavans/lacoder2/wiki/TODO)

### 設定ファイルリファレンス
 * [lacoder.xml](https://github.com/lavans/lacoder2/wiki/lacoder.xml)
 * [lacoder-generator.xml](https://github.com/lavans/lacoder2/wiki/lacoder-generator.xml)
 * [entity.xml](https://github.com/lavans/lacoder2/wiki/entity.xml)
 * [dao.xml](https://github.com/lavans/lacoder2/wiki/dao.xml)

### tagfileリファレンス
 * [checkbox.tag](https://github.com/lavans/lacoder2/wiki/checkbox.tag)
 * [errors.tag](https://github.com/lavans/lacoder2/wiki/errors.tag)
 * [pager.tag](https://github.com/lavans/lacoder2/wiki/pager.tag)
 * [paramstring.tag](https://github.com/lavans/lacoder2/wiki/paramstring.tag)
 * [radio.tag](https://github.com/lavans/lacoder2/wiki/radio.tag)
 * [select.tag](https://github.com/lavans/lacoder2/wiki/select.tag)
 * [selectArray.tag](https://github.com/lavans/lacoder2/wiki/selectArray.tag)
 * [utils.tag](https://github.com/lavans/lacoder2/wiki/utils.tag)

## メンバー
<table>
<tr>
<th>名前</th>
<th>役割</th>
</tr>
<tr>
<td>dobashi</td>
<td>リーダー</td>
</tr>
</table>


## 略歴
<table>
<tr>
<td>2000年</td>
<td>ConnectionPool, Logger, Xmlファイル読み込みを[http://lavans.jp/soft/lavansutil lavansutil]として作成。[http://sourceforge.jp/projects/lavansutil/ sourceforge.jp]に公開。</td>
</tr>
<tr>
<td>2002年</td>
<td>中小企業診断システムにてORMコード、enumクラス、jspファイルを自動生成するlamen作成開始。</td>
</tr>
<tr>
<td>2005年</td>
<td>S社証券取引システムにてlamen2作成。ブラウザから1クリックで各種ファイルを生成できるように機能拡張。</td>
</tr>
<tr>
<td>2006年</td>
<td>Loggerを削りWeb系の共通機能を足してluzに名前変更し、社内限定で利用開始。</td>
</tr>
<tr>
<td>2007年</td>
<td>コミュニティサイトシステムにてlamen3作成。</td>
</tr>
<tr>
<td>2007年</td>
<td>FXシステムにてfxmobile_lamen。SQL xmlファイル外出しなど。</td>
</tr>
<tr>
<td>2008年</td>
<td>luzのうちあまり汎用的でない機能をそぎ落としてluz2としてJSOXシステムから適用開始。</td>
</tr>
<tr>
<td>2008年</td>
<td>JSOXシステムにてcilne_lamen。自動生成や上書き対応など。</td>
</tr>
<tr>
<td>2008年</td>
<td>シンプルなRemoteProcedureCallシステムのlremoteを作成。[http://sourceforge.net/projects/lremote/ sourceforge.net]で公開。</td>
</tr>
<tr>
<td>2009年</td>
<td>lamenをlacoderに改名。struts2対応してController系の自動生成を大幅変更。自動生成や上書き対応など。</td>
</tr>
<tr>
<td>2010年</td>
<td>ログ監視システムSoberを作成。シンプルなMVC Controllerシステムを作成。サーバー=クライアント通信にlremoteを使用。</td>
</tr>
<tr>
<td>2011年</td>
<td>SoberのController,lremoteをluz2に統合。</td>
</tr>
</table>



