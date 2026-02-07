# AGENTS Instructions

## Project Goals
- Maintain cross-version compatibility (Java 8, Spigot 1.8.8+).
- Keep the API stable and easy to consume for other plugins.
- Prefer safe, server-thread Bukkit usage and avoid async Bukkit calls.

## Build and Release
- Build with `./gradlew build`.
- JitPack uses `./gradlew publishToMavenLocal` (see `jitpack.yml`).

## Coding Guidelines
- Use Java 8 language features only.
- Avoid direct access to modern-only APIs unless guarded by reflection.
- Keep cooldowns and ability routing centralized.
- Log errors but do not crash the server on ability failures.

## API Changes
- Add new features in a backward-compatible way when possible.
- If a breaking change is required, update README and migration notes.
