# AWS Rekognition 顔認証 サーバレス参考実装

## はじめに
AWS Rekognitionを利用した顔認証のサーバレス参考実装である。以下の技術要素を利用している。
1. AWS Rekognition（顔の比較）
2. AWS Lambda（カスタムランタイム）
3. GraalVM（Javaネイティブコンパイル）

## 構成図
構成はこんな感じ。クライアントは別途作成。

![top-page](https://raw.githubusercontent.com/u032582/faceauth-serverless/master/serverside-design-diagram.jpg)

## 事前条件
1. Linuxが使えること
1. graalVM（21.3.0）がインストールされていること
2. （オプション）AWS CLIがインストールされていること

## 事前準備
### graalVM JDKのインストール
1. sdkman をインストールする
   ```
   curl -s "https://get.sdkman.io" | bash
   source "$HOME/.sdkman/bin/sdkman-init.sh"
   ```
2. graalVMをインストールする
    ```
    sdk install java 21.3.0.r11-nik
    ```
## Nativeコンパイル
1. 以下のコマンド実行
    ```
    mvn -DskipTests clean package -Pnative
    ```
1. target配下に 以下のファイルが出力されていること
   ```
   faceauth-serverless-0.0.1-native.zip
   ```
## AWSへのdeploy
1. Lambdaの実行用ロールを作成
   1. 詳細は割愛。今回のAPではRekognitionとDynamoDB のFullAccessが必要。
2. Lambda functionのデプロイ
   1. 以下のようなコマンドを実行
    ```
    aws lambda create-function --function-name faceauth-serverless-func --runtime provided --role arn:aws:iam::993715192745:role/lambda-role --zip-file fileb://target/faceauth-serverless-0.0.1-native.zip --handler functionRouter --memory-size 256
    ```
    - function-name：適当な名前
    - runtime：nativeコンパイルしたJavaを動かすので provided（Amazon Linux）を指定
    - role：Lambdaファンクション実行用のロール（別途作成しておいたもの）
    - zip-file：Lambdaカスタムランタイム用のデプロイ資材
    - handler：SpringCloudFunctionで呼び出すFunction Bean名
    - memory-size：256MB