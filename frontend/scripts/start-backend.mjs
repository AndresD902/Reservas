import { spawn } from 'node:child_process'
import { existsSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const scriptDirectory = dirname(fileURLToPath(import.meta.url))
const projectRoot = resolve(scriptDirectory, '..', '..')
const isWindows = process.platform === 'win32'
const wrapper = resolve(projectRoot, isWindows ? 'gradlew.bat' : 'gradlew')

if (!existsSync(wrapper)) {
  console.error(`No se encontró el wrapper de Gradle en ${wrapper}`)
  process.exit(1)
}

const child = spawn(wrapper, ['bootRun', '--console=plain'], {
  cwd: projectRoot,
  env: {
    ...process.env,
    SPRING_PROFILES_ACTIVE: process.env.SPRING_PROFILES_ACTIVE ?? 'local',
  },
  shell: isWindows,
  stdio: 'inherit',
})

function shutdown() {
  if (!child.killed) {
    child.kill()
  }
}

process.on('SIGINT', shutdown)
process.on('SIGTERM', shutdown)
process.on('exit', shutdown)

child.on('exit', (code) => {
  if (code !== null && code !== 0) {
    process.exit(code)
  }
})
