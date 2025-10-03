# ProA

ProA is a tool that lets you manage your processes and their connections smoothly. It detects relations among the
processes and shows them in a diagram.

## Web or desktop mode

It is possible to run ProA in web or desktop mode.

Web mode includes authentication and allows multiple users to use the app.

To activate either mode please navigate to the frontend folder via `cd frontend` and run `yarn mode [web|desktop]`.

Web mode: The default user is `admin` and the default password is `admin`.

## Generating JWT keys (web mode only)

To generate the keys necessary for authentication via JWT please run the following script once:
```
./backend/generate-keys.sh
```

## Database and Fuzzy Match
ProA uses fuzzy match so that the BPMN labels can be matched with some degree of tolerance, e.g. ignoring typos.
To this end, postgres levenshtein function is used, which needs to be activated by ececuting the following:

```CREATE EXTENSION fuzzystrmatch;```

Furthermore, in Azure before executing the above statement, the extension needs to be activated via: DB >> Settings >> Server parameters >> azure.extensions >> fuzzystrmatch;


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

Important: H2 Database is not supported in native mode. When native mode is used, an external DB needs to be
configured: https://github.com/quarkusio/quarkus/issues/27021

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

### Jacoco

To create a jacoco report run:


```shell script
mvn verify
```

The report can be found under target/jacoco-report

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

## Configuring settings

Settings can be configured by clicking the settings icon in the top right corner of the app.

`Gemini API Key` is used to generate process model descriptions with AI.

Camunda Modeler `Client ID` and `Client Secret` are used to retrieve process models from the Camunda Web Modeler.

Camunda Operate `Client ID`, `Client Secret`, `Region ID` and `Cluster ID` are used to fetch active process instances.

## Code Formatting Guide (IntelliJ IDEA)

### 1. Frontend: Prettier for Vue Files

#### Setup

1. **Install Prettier** globally or in your project (if not already installed):

```sh
npm install --save-dev prettier
```

2. **Install the Prettier Plugin** in IntelliJ IDEA:
    - Go to **IntelliJ IDEA** → **Settings...** → **Plugins**
    - Search for **Prettier** and install it

#### Usage

To format Vue files using Prettier in IntelliJ IDEA:

1. **Enable Prettier**:
    - Go to **IntelliJ IDEA** → **Settings...** → **Languages & Frameworks** → **JavaScript** → **Prettier**
    - Select **Manual Prettier configuration**
    - Set the **Prettier package** to your project's `node_modules/prettier` or the globally installed version
    - Select **Run on 'Reformat Code' action** and **Run on save**
2. **Manually Format Vue Files**:
    - Right-click a file or folder in the **Project** view
    - Select **Reformat with Prettier**

#### Verification

To check formatting without modifying files, run:

```sh
npx prettier --check "src/**/*.{vue,js,ts,css,scss,json,md}"
```

## 2. Backend: Eclipse Formatter for Java Files

### Setup

1. **Import `eclipse-formatter.xml`** in IntelliJ IDEA:
    - Go to **IntelliJ IDEA** → **Settings...** → **Editor** → **Code Style** → **Java**
    - Click the gear icon next to **Scheme** → **Import Scheme**
    - Select `eclipse-formatter.xml` from the project
    - Click **Apply** and **OK**

### Usage

1. **Format on Save:**
    - Go to **IntelliJ** → **Settings** → **Tools** → **Actions on Save**
    - Check **Reformat code** and **Optimize imports**

2. **Manually Format Java Files:**
    - Open a Java file and press **Ctrl + Alt + L** (Windows/Linux) or **Cmd + Option + L** (Mac)
	
	
## License 
Shield: [![CC BY-NC-SA 4.0][cc-by-nc-sa-shield]][cc-by-nc-sa]

This work is licensed under a
[Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License][cc-by-nc-sa].

[![CC BY-NC-SA 4.0][cc-by-nc-sa-image]][cc-by-nc-sa]

[cc-by-nc-sa]: http://creativecommons.org/licenses/by-nc-sa/4.0/
[cc-by-nc-sa-image]: https://licensebuttons.net/l/by-nc-sa/4.0/88x31.png
[cc-by-nc-sa-shield]: https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg
