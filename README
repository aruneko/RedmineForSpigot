# Redmine for Spigot
## これなに
Redmineをコマンドで操作するSpigotプラグインです。できることはまだ少なめ。

## 動作環境
- Java8
- Spigot1.9
  - 変なAPIは使ってないのでおそらく低いバージョンでも動くはず
- Redmine 3.2

## 使い方
- projectコマンド
  - /project list
    - プロジェクトの一覧を表示します
  - /project [Project ID]
    - Project IDで指定したプロジェクトの詳細を表示します
- issueコマンド
  - /issue list
    - チケットの一覧を表示します
  - /issue list [Project ID]
    - Project IDごとのチケットの一覧を表示します
  - /issue [Issue ID]
    - Issue IDで指定したチケットの詳細を表示します
  - /issue new [Project ID] [Tracker ID] [Priority ID] [Subject]
    - 新規チケットを発行します
    - Tracker IDは以下の種類があります
      - 1 : バグ 
      - 2 : 機能
      - 3 : サポート
    - Priority IDは1〜5までの段階があり、数値が大きい方が優先度が高くなります
      - 優先度2が「通常」となります
    - Subjectはスペースが混入してもコマンド末尾まで認識されます
- redmineコマンド
  - /redmine setapikey [API key]
    - APIアクセスキーをセットします

## ビルド方法
1. Scala 2.11.8を導入
1. sbtを導入
1. このリポジトリをクローンする
1. クローンしたディレクトリにcdする
1. `sbt assembly`コマンドを実行する
1. $projectroot/target/scala-2.11の中にjarファイルが完成

## 作成環境
- Scala 2.11.8 (ビルドターゲットとしてJava8を利用)
- Spigot 1.9-R0.1-SNAPSHOT
- Redmine 3.2
- IntelliJ IDEA Community Edition on Arch Linux
