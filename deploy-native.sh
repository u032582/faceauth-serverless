aws lambda delete-function --function-name faceauth-serverless-func
aws lambda create-function --function-name faceauth-serverless-func --runtime provided --role arn:aws:iam::993715192745:role/lambda-role --zip-file fileb://target/faceauth-serverless-0.0.1-native.zip --handler functionRouter --memory-size 256


