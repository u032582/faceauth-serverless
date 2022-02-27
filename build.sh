docker run -it --rm -v ${PWD}:/app -v ~/.m2/repository:/root/.m2/repository native-build  ./mvnw -X -ntp package -Pnative-image

