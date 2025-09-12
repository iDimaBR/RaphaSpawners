# RaphaSpawners

**RaphaSpawners** is an advanced **Minecraft plugin** that allows the creation, management, and customization of **mob spawners**. The plugin provides **dynamic holograms**, **detailed permissions**, **logs**, **top spawner rankings**, and support for **custom skulls**.

---

## Features

### 1. Spawner Panel
- Fully customizable GUI via configuration.
- Displays information such as:
  - Owner
  - Mob type
  - Quantity
  - Status (Active/Inactive)
  - Online/Offline
- Support for glowing items and custom skulls.

### 2. Permissions System
- Detailed control over what members can do:
  - Add/Remove spawners
  - Change spawner status
  - Add/Remove members
  - Manage permissions
  - Access spawner panel
- Separate GUI to manage individual member permissions.

### 3. Holograms
- Holograms above spawners showing:
  - Mob quantity
  - Owner name
  - Spawner type
  - Spawner status
- Supports **custom skulls with URL textures**.

### 4. Spawner Logs
- Records actions performed on each spawner.
- Maximum of 44 logs per spawner to maintain performance.
- GUI to view and clear logs.

### 5. Top Spawners
- Leaderboard of players with the most spawners.
- Calculates values and displays them in a fully configurable GUI.
- Customizable slots and appearance.

### 6. Entity Support
- Compatible with all living Minecraft entities.
- Supports mob stacking with metadata.
- Automatic remastering for existing entities.

---

## Configuration
- Fully customizable YAML files for:
  - Default GUI
  - Permissions panels
  - Member permissions
  - Logs
  - Top spawner leaderboard
  - Mob-specific settings (price, skull URLs, etc.)

### config.yml
```yml
MySQL:
  Host: 'localhost'
  Database: 'raphaspawners'
  Username: 'root'
  Password: ''

Generator:
  LimitStack: 200
  DistanceStack: 10
  LimitMembers: 21
  SilkTouch: false
  RandomValueSpawn: false
  Delay:
    Init: 4
  WorldBlackList:
    - 'worldexample'
  MobHead:
    Enabled: true
  ItemStack:
    Name: "&eMonster Generator"
    Lore:
      - "&7Type: &f%spawner_type%"
  Hologram:
    Height: 3
    Lines:
      - "&ex%quantity_generators% %spawner_type% Generators"
      - "&eOwner: &f%owner%"
      - "&eStatus: &f%spawner_status%"

Entities:
  LimitStack: 120
  DistanceStack: 25
  WorldBlackList:
    - 'worldexample'
```

### entities.yml
```yml
CREEPER:
  Price: 1200
  Permission: 'spawner.use'
  HeadURL: 'https://textures.minecraft.net/texture/ab5ba8ae0e263ce483e6564179fea977a0c084e22154fe555e5d2321fdbb7e03'
  Drops:
    Item-1:
      Material: DIRT
      Data: 0
      Glow: false
      Name: "&fTest"
      Lore:
        - "&7Just testing xD"
PIG:
  Price: 1200
  Permission: 'spawner.use'
  HeadURL: 'https://textures.minecraft.net/texture/ab5ba8ae0e263ce483e6564179fea977a0c084e22154fe555e5d2321fdbb7e03'
SHEEP:
  Price: 1200
  Permission: 'spawner.use'
  HeadURL: 'https://textures.minecraft.net/texture/ab5ba8ae0e263ce483e6564179fea977a0c084e22154fe555e5d2321fdbb7e03'
COW:
  Price: 1200
  Permission: 'spawner.use'
  HeadURL: 'https://textures.minecraft.net/texture/ab5ba8ae0e263ce483e6564179fea977a0c084e22154fe555e5d2321fdbb7e03'
CHICKEN:
  Price: 1200
  Permission: 'spawner.use'
  HeadURL: 'https://textures.minecraft.net/texture/ab5ba8ae0e263ce483e6564179fea977a0c084e22154fe555e5d2321fdbb7e03'
  Drops:
    Item-1:
      Material: DIRT
      Amount: 3
      Data: 0
      Looting: true
      Glow: false
      ChanceDrop: 100
      Name: "&fTest"
      Lore:
        - "&7Just testing xD"
  Rewards:
    Commands:
     - "5;give %player% dirt"
SQUID:
  Price: 1200
  Permission: 'spawner.use'
  HeadURL: 'https://textures.minecraft.net/texture/ab5ba8ae0e263ce483e6564179fea977a0c084e22154fe555e5d2321fdbb7e03'
WOLF:
  Price: 1200
  Permission: 'spawner.use'
  HeadURL: 'https://textures.minecraft.net/texture/ab5ba8ae0e263ce483e6564179fea977a0c084e22154fe555e5d2321fdbb7e03'
```

### menus.yml
```yml
Permissions:
  SizeRow: 6
  Title: "Spawner Permissions"
  MaterialWithPermission: SLIME_BALL
  MaterialNotPermission: BARRIER
  Slots:
    - 10
    - 11
    - 12
    - 13
    - 14
    - 15
    - 16
    - 19
    - 20
    - 21
    - 22
    - 23
    - 24
    - 25
    - 28
    - 29
    - 30
    - 31
    - 32
    - 33
    - 34
  ItemStack:
    Member:
      Name: "&e%member%"
      Lore:
        - "&7Left click &fto remove this player."
        - "&7Right click &fto manage this player's permissions."
    AddMember:
      Material: NETHER_STAR
      Name: "&aAdd Member"
      Lore:
        - "&7Click to add members to your generator."
PermissionsMember:
  SizeRow: 6
  Title: "%member%'s Permissions"
  WithPermission:
    Material: SLIME_BALL
    Data: 0
  NotPermission:
    Material: BARRIER
    Data: 0
  BackSlot: 49
  PermissionsType:
    Lore:
      - "&7Click to toggle this player's permissions"
      - "&7Status: &f%permission_status%"
    ADD_MOBSPAWNERS:
      Name: "&eAdd Generators"
      Slot: 10
    REMOVE_MOBSPAWNERS:
      Name: "&eRemove Generators"
      Slot: 19
    REMOVE_GENERATOR:
      Name: "&eRemove Generator"
      Slot: 12
    TURN_GENERATOR:
      Name: "&eChange Generator Status"
      Slot: 21
    ADD_MEMBER:
      Name: "&eAdd Members"
      Slot: 14
    REMOVE_MEMBER:
      Name: "&eRemove Members"
      Slot: 23
    MANAGER_PERMISSION:
      Name: "&eManage Permissions"
      Slot: 16
    ACCESS_PANEL_GENERATOR:
      Name: "&eAccess Main Panel"
      Slot: 25
Logs:
  SizeRow: 6
  Title: "Logs"

TopGenerator:
  SizeRow: 6
  Title: "Top Generators"
  Slots:
    - 11
    - 12
    - 13
    - 14
    - 15
    - 20
    - 21
    - 22
    - 23
    - 24
    - 29
    - 30
    - 31
    - 32
    - 33
  BackItemSlot: 49
  NumberOfTOPS: 15
  ItemStack:
    Name: "&e%player%"
    Lore:
      - "&7Generators: &f%quantity_generators%"
      - "&7Cost: &a$ %price_generators%"

Default:
  SizeRow: 3
  Title: "Manage Spawner"
  Items:
    Permissions:
      Material: NETHER_STAR
      Slot: 10
      Title: "&eSpawner Permissions"
      Lore:
        - ""
        - "&7Click to manage the generator's permissions"
    Owner:
      Material: SKULL_ITEM
      Slot: 11
      Title: "&eOwner"
      SkullNick: "%owner%"
      Lore:
        - ""
        - "&7Owner: &f%owner%"
        - "&7Status: &f%status%"
        - ""
        - "&7The player who places the generator"
        - "&7becomes the main owner."
    Logs:
      Material: BOOK
      Slot: 13
      Title: "&eChange Logs"
      Lore:
        - ""
        - "&7Click to view the logs"
    StoreGenerator:
      Material: MINECART
      Glow: true
      Data: 0
      Slot: 14
      Title: "&eStore Generators"
      Lore:
        - ""
        - "&7Type: &f%spawner_type%"
        - "&7Quantity: &f%spawner_quantity%"
        - ""
        - "&7Left click &7to add Generators"
        - "&7Right click &7to remove Generators"
    StatusGenerator:
      Material: MOB_SPAWNER
      Slot: 15
      Title: "&eGenerator Status"
      Lore:
        - ""
        - "&7Status: &f%spawner_status%"
        - "&7Click to toggle between &fenabled &7and &fdisabled&7."
    TopGenerator:
      Material: NAME_TAG
      Slot: 16
      Title: "&eTop Generators"
      Lore:
        - ""
        - "&7Click to see the players with"
        - "&7the most generators on the server."
```

### messages.yml
```yml
WorldBlocked: "&cGenerators are blocked in this world."
GeneratorNotFound: "&cGenerator not found."
GeneratorPlaced: "&aYou placed &fx%quantity_generators% &a%spawner_type% generators."
GeneratorBreak: "&aYou broke &fx%quantity_generators% &a%spawner_type% generators."
GeneratorSilkTouch: "&cUse a &fSilk Touch &cpickaxe to remove your generator."

Actions:
  RemoveMember: "&aYou removed the member '&f%memberName% &a' from the generator."
  ChangeStatus: "&aThe generator is now %spawner_status%&a."
  ActionCanceled: "&cThe action has been canceled."

Prompts:
  AddMember:
    - ""
    - ""
    - ""
    - ""
    - ""
    - ""
    - ""
    - "&aType the player name to add:"
    - ""
  AddGenerators:
    - ""
    - ""
    - ""
    - ""
    - ""
    - ""
    - ""
    - "&aEnter the amount of generators to store:"
    - ""
  RemoveGenerators:
    - ""
    - ""
    - ""
    - ""
    - ""
    - ""
    - ""
    - "&aEnter the amount of generators to remove:"
    - ""

Errors:
  MaxMembers: "&cYou reached the maximum number of members. (%max_members%)"
  NoPermissionTypeGenerator: "&cYou do not have permission to place generators of type &7%spawner_type%&c."
  RemoveGenerator: "&cAn error occurred while removing your generator."
  RemoveAllGenerators: "&cYou cannot remove all generators like that."
  NumberInvalid: "&cEnter a valid number or cancel."
  NoSuficientGenerators: "&cYou don't have enough generators."
  NoTakeSuficientGenerators: "&cThere aren't &f%quantity_generators% &c generators to remove."
  PlayerNotFound: "&cThis player was not found."
  NoAddMemberOwn: "&cYou cannot add yourself as a member."
  NoAddOwnerMember: "&cYou cannot add the owner as a member."
  PlayerWasMember: "&cThis player is already added as a member of this generator."
  NoHaveThePermission: "&cYou do not have the permission &7'%permission%' &cto do this."
  NoPermission: "&cYou do not have permission for this generator."
  LimitExcedeed: "&cYou reached your generator limit. (%limitstack%)"
  AmountMoreThanMax: "&cThe maximum allowed to add at once is 2304 spawners (Full Inventory)"
  AddGeneratorsMoreThanLimit: "&cYou can only add %quantity_generators%, it would exceed your limit of %limitstack%."
  DontHaveSpaceInventory: "&cYou don't have enough free inventory space to remove these generators."

Success:
  StoreGenerators: "&aStored &f%quantity_generators% &a generators."
  TakeGenerators: "&aRemoved &f%quantity_generators% &a generators."
  PlayerAddedMember: "&aPlayer &f%member% &aadded successfully."
  PlayerRemoveMember: "&aPlayer &f%member% &aremoved successfully."
  ClearLogs: "&aChange logs have been cleared."
```

---

## Dependencies
- [Spigot/Paper 1.8](https://www.spigotmc.org/)
- [HolographicDisplays](https://www.spigotmc.org/resources/holographic-displays.4924/)
- **Java 8**

---

## Installation
1. Place the plugin `.jar` in the `plugins` folder.
2. Restart or reload the server.
3. Configure with your preferences.
