const fs = require("fs");
const path = require("path");

const mode = process.argv[2];

if (!mode || (mode !== "web" && mode !== "desktop")) {
  console.error("Please provide \"web\" or \"desktop\" as an argument.");
  process.exit(1);
}

const envPath = path.join(__dirname, ".env");

if (!fs.existsSync(envPath)) {
  fs.writeFileSync(envPath, "");
}

const propFiles = ["application.properties", "application-dev.properties"];
const propPaths = propFiles.map((file) => path.join(__dirname, "..", "backend", "src", "main", "resources", file));

let envContent = fs.readFileSync(envPath, "utf8");

const envSearchValue = /VITE_APP_MODE=.*/g;
const envReplaceValue = `VITE_APP_MODE="${mode}"`;

const updatedContent = envContent.match(envSearchValue)
  ? envContent.replace(envSearchValue, envReplaceValue)
  : (envContent.trim() === "" ? `${envReplaceValue}\n` : envContent.trim() + `\n\n${envReplaceValue}\n`);

fs.writeFileSync(envPath, updatedContent);

const propSearchValue = /app.mode=.*/g;
const propReplaceValue = `app.mode=${mode}`;

for (const propPath of propPaths) {
  const propContent = fs.readFileSync(propPath, "utf8");
  const updatedPropContent = propContent.match(propSearchValue)
    ? propContent.replace(propSearchValue, propReplaceValue)
    : propContent.trim() + `\n\n${propReplaceValue}\n`;

  fs.writeFileSync(propPath, updatedPropContent);
}

console.log(`Successfully set mode to ${mode}`);
