aws lambda delete-function --function-name faceauth-serverless-func
aws lambda create-function --function-name faceauth-serverless-func --runtime java11 --role arn:aws:iam::993715192745:role/lambda-role --zip-file fileb://target/faceauth-severless-0.0.1-aws.jar --handler org.springframework.cloud.function.adapter.aws.FunctionInvoker --memory-size 1024 --timeout 30

