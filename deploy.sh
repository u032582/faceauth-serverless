aws lambda delete-function --function-name java-custom-test
aws lambda create-function --function-name java-custom-test --runtime provided --role arn:aws:iam::993715192745:role/lambda-role --zip-file fileb://target/function-sample-0.0.1-native.zip --handler event --environment "Variables={MAIN_CLASS=com.example.faceauth.FaceAuthApplication}" --memory-size 256


