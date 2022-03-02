aws lambda delete-function --function-name java-custom-test
aws lambda create-function --function-name java-custom-test --runtime java11 --role arn:aws:iam::993715192745:role/lambda-role --zip-file fileb://target/function-sample-0.0.1-aws.jar --handler org.springframework.cloud.function.adapter.aws.FunctionInvoker --memory-size 1024 --timeout 30

