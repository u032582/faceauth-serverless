aws lambda delete-function --function-name java-custom-test
aws lambda create-function --function-name java-custom-test --runtime java11 --role arn:aws:iam::993715192745:role/lambda-role --zip-file fileb://target/function-sample-0.0.1-java11.zip --handler org.springframework.cloud.function.adapter.aws.FunctionInvoker --environment "Variables={MAIN_CLASS=com.example.faceauth.FaceAuthApplication}" --timeout 30 --memory-size 1024

