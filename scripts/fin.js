import { spawn } from "child_process";

async function runScript(scriptName) {
  return new Promise((resolve, reject) => {
    console.log(`\n>>> Running ${scriptName}...`);
    const child = spawn("powershell", ["-ExecutionPolicy", "Bypass", "-File", scriptName], {
      stdio: "inherit",
    });

    child.on("close", (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`${scriptName} failed with exit code ${code}`));
      }
    });
  });
}

async function main() {
  try {
    await runScript("build_web.ps1");
    await runScript("build_release.ps1");
    console.log("\n>>> All tasks completed successfully.");
  } catch (error) {
    console.error(`\n>>> Error: ${error.message}`);
    process.exit(1);
  }
}

main();
