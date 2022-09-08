# Agora Governance SCORE
This repository contains SCORE (Smart Contract for ICON) examples written in Java.

## Requirements

You need to install JDK 11 or later version. Visit [OpenJDK.net](http://openjdk.java.net/) for prebuilt binaries.
Or you can install a proper OpenJDK package from your OS vendors.

In macOS:
```
$ brew tap AdoptOpenJDK/openjdk
$ brew cask install adoptopenjdk11
```

In Linux (Ubuntu 20.04):
```
$ sudo apt install openjdk-11-jdk
```

## How to Run

### 1. Build the project

```
$ ./gradlew build
```
The compiled jar bundle will be generated at `./app/build/libs/app-0.2.0.jar`.

### 2. Optimize the jar

You need to optimize your jar bundle before you deploy it to local or ICON networks.
This involves some pre-processing to ensure the actual deployment successful.

`gradle-javaee-plugin` is a Gradle plugin to automate the process of generating the optimized jar bundle.
Run the `optimizedJar` task to generate the optimized jar bundle.

```
$ ./gradlew optimizedJar
```
The output jar will be located at `./app/build/libs/app-0.2.0-optimized.jar`.

### 3. Deploy the optimized jar

#### Using `goloop` CLI command

Now you can deploy the optimized jar to ICON networks that support the Java SCORE execution environment.
Assuming you are running a local network that is listening on port 9082 for incoming requests,
you can create a deploy transaction with the optimized jar and deploy it to the local network as follows.

```
$ goloop rpc sendtx deploy ./app/build/libs/app-0.2.0-optimized.jar \
    --uri http://localhost:9082/api/v3 \
    --key_store <your_wallet_json> --key_password <password> \
    --nid 3 --step_limit=1000000 \
    --content_type application/java
```

**[Note]** The content type should be `application/java` instead of `application/zip` to differentiate it with the Python SCORE deployment.

#### Using `deployJar` extension

Starting with version `0.7.2` of `gradle-javaee-plugin`, you can also use the `deployJar` extension to specify all the information required for deployment.

```groovy
deployJar {
    endpoints {
        local {
            uri = 'http://localhost:9082/api/v3'
            nid = 3
        }
    }
    keystore = rootProject.hasProperty('keystoreName') ? "$keystoreName" : ''
    password = rootProject.hasProperty('keystorePass') ? "$keystorePass" : ''
}
```

Now you can run `deployToLocal` task as follows.

```
$ ./gradlew app:deployToLocal -PkeystoreName=<your_wallet_json> -PkeystorePass=<password>

> Task :app:deployToLocal
>>> deploy to http://localhost:9082/api/v3
>>> optimizedJar = ./app/build/libs/app-0.2.0-optimized.jar
>>> keystore = <your_wallet_json>
Succeeded to deploy: 0x699534c9f5277539e1b572420819141c7cf3e52a6904a34b2a2cdb05b95ab0a3
SCORE address: cxd6d044b01db068cded47bde12ed4f15a6da9f1d8
```

**[Note]** If you want to deploy to Lisbon testnet, use the following configuration for the endpoint and run `deployToLisbon` task.
```groovy
deployJar {
    endpoints {
        lisbon {
            uri = 'https://lisbon.net.solidwallet.io/api/v3'
            nid = 0x2
        }
        ...
    }
}
```