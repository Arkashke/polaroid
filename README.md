# Polaroid Client

Modifications for Minecraft 1.12.2+ client.

## Build

```bash
./gradlew build
```

JAR will be in `build/libs/polaroid.jar`.

## Requirements

- JDK 17+
- Gradle 7.x+

## Project Structure

```
polaroid-client/
├── src/
│   ├── polaroid/           # Client code
│   │   └── client/
│   │       ├── command/    # Command system
│   │       ├── commands/   # Commands implementation
│   │       ├── config/    # Configuration
│   │       ├── events/    # Event system
│   │       ├── icons/     # Module icons
│   │       ├── managment/ # Management
│   │       ├── modules/   # Modules (combat, movement, etc.)
│   │       ├── proxy/     # Proxy system
│   │       ├── scripts/   # Scripting
│   │       ├── ui/        # UI components
│   │       └── utils/     # Utilities
│   ├── com/                # Third-party libraries
│   │   ├── jagrosh/        # Discord IPC
│   │   ├── jhlabs/         # Image processing
│   │   └── mojang/         # Mojang auth
│   ├── net/                # Network & protocol
│   └── assets/             # Minecraft assets
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Modules

- **Combat** - Aura, AutoPotion, Criticals, TriggerBot, etc.
- **Movement** - Speed, Fly, Scaffold, Jesus, etc.
- **Visuals** - HUD, ESP, Chams, etc.
- **Misc** - AutoGG, AutoReconnect, etc.

## License

Proprietary
