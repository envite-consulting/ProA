const fs = require("fs");
const path = require("path");

const mode = process.argv[2];

if (!mode || (mode !== "web" && mode !== "desktop")) {
  console.error("Please provide \"web\" or \"desktop\" as an argument.");
  process.exit(1);
}

const envPath = path.join(__dirname, ".env");

if (!fs.existsSync(envPath)) {
  console.error(".env file not found!");
  process.exit(1);
}

const propFiles = ["application.properties", "application-dev.properties"]
const propPaths = propFiles.map(file => path.join(__dirname, "..", "backend", "src", "main", "resources", file));

propPaths.forEach(propPath => {
  if (!fs.existsSync(propPath)) {
    console.error(`${propPath} file not found!`);
    process.exit(1);
  }
});

let envContent = fs.readFileSync(envPath, "utf8");

const envSearchValue = /VITE_DESKTOP_OR_WEB=.*/g;
const envReplaceValue = `VITE_DESKTOP_OR_WEB="${mode}"`;

const updatedContent = envContent.match(envSearchValue)
  ? envContent.replace(envSearchValue, envReplaceValue)
  : envContent.trim() + `\n\n${envReplaceValue}\n`;

fs.writeFileSync(envPath, updatedContent);

const propSearchValue = /app.mode=.*/g;
const propReplaceValue = `app.mode=${mode}`;

propPaths.forEach(propPath => {
  const propContent = fs.readFileSync(propPath, "utf8");
  const updatedContent = propContent.match(propSearchValue)
    ? propContent.replace(propSearchValue, propReplaceValue)
    : propContent.trim() + `\n\n${propReplaceValue}\n`;

  fs.writeFileSync(propPath, updatedContent);
});

console.log(`Successfully set mode to ${mode}`);
