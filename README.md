# ProA

ProA is a tool that lets you manage your processes and their connections smoothly. It detects relations among the processes and shows them in a diagram.

## The Entire Application

In order to build an uber jar, which also contains the frontend, run the following in the root:

```mvn clean package```

This will build the frontend and will copy the built frontend sources to the quarkus app.


## Backend

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

### Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.

The produced jar is an uber jar due to the configuration in the application.properties.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.


### Creating a native executable

Important: H2 Database is not supported in native mode. When native mode is used, an external DB needs to be configured: https://github.com/quarkusio/quarkus/issues/27021

### Using GraalVM
Install GraalVM version 21 from https://github.com/graalvm/graalvm-ce-builds/releases

For Windows Users:
Install Visual Studio Code: https://www.graalvm.org/latest/docs/getting-started/windows/

You can create a native executable using: 
```shell script
./mvnw package -Dnative
```
For Windows Users: Execute the above statement using the x64 Native Tools Command Prompt .

#### Using Docker

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

This will produce a Linux executable.

You can then execute your native executable with: `./target/pro-a-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Frontend

### Project setup

```
# yarn
yarn

# npm
npm install

# pnpm
pnpm install
```

### Compiles and hot-reloads for development

```
# yarn
yarn dev

# npm
npm run dev

# pnpm
pnpm dev
```

### Compiles and minifies for production

```
# yarn
yarn build

# npm
npm run build

# pnpm
pnpm build
```

### Lints and fixes files

```
# yarn
yarn lint

# npm
npm run lint

# pnpm
pnpm lint
```
