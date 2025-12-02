# hello-agent

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/hello-agent-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- Camel LangChain4j Tokenizer ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/langchain4j-tokenizer.html)): LangChain4j Tokenizer
- Camel langChain4j Chat ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/langchain4j-chat.html)): LangChain4j Chat component
- Camel LangChain4j Tools ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/langchain4j-tools.html)): LangChain4j Tools and Function Calling Features
- Camel LangChain4j Agent ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/langchain4j-agent.html)): LangChain4j Agent component
- Camel LangChain4j Web Search ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/langchain4j-web-search.html)): LangChain4j Web Search Engine
