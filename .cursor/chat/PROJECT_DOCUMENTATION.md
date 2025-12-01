# Pockets & Monsters - Project Documentation

## 📋 Project Overview

**Pockets & Monsters** is an Android application that combines Pokémon data with Dungeons & Dragons (D&D) mechanics. The app serves as a comprehensive companion tool for managing Pokémon in a D&D-style tabletop RPG format, featuring offline data support, party management, encounter tracking, and utility tools.

### Key Characteristics
- **Platform**: Android (Kotlin)
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Data Source**: PokéAPI (with offline fallback)
- **Target SDK**: 36 (Android 15)
- **Min SDK**: 24 (Android 7.0)
- **Package**: `com.kriptogan.pocketsmonsters`

---

## 🎯 Core Concept

The application converts traditional Pokémon game mechanics into D&D 5th Edition rules:
- Pokémon stats are converted to D&D ability scores
- Moves are organized by D&D spell slot tiers (1st-9th level)
- Experience follows D&D progression tables (levels 1-20)
- Energy slots follow Wizard spell slot progression
- Natures provide proficiency bonuses and penalties
- Status effects and conditions are tracked per round

---

## 🏗️ Architecture

### Project Structure

```
app/src/main/java/com/kriptogan/pocketsmonsters/
├── MainActivity.kt                    # Entry point, fullscreen immersive mode
├── data/
│   ├── api/
│   │   └── PokeApiService.kt          # Retrofit API service for PokéAPI
│   ├── converter/
│   │   └── DnDConverter.kt            # Converts Pokémon stats to D&D format
│   ├── encounter/
│   │   └── EncounterManager.kt        # Manages encounter creatures
│   ├── inventory/
│   │   └── InventoryManager.kt        # Manages inventory items
│   ├── local/
│   │   └── LocalStorage.kt            # SharedPreferences data persistence
│   ├── models/
│   │   ├── Pokemon.kt                  # Base Pokémon data model
│   │   ├── PartyPokemon.kt            # Party Pokémon with D&D stats
│   │   ├── Nature.kt                   # Nature system for stat bonuses
│   │   ├── Stat.kt                     # Stat definitions
│   │   ├── Type.kt                     # Type information
│   │   └── ...                         # Other data models
│   ├── network/
│   │   └── NetworkModule.kt           # Retrofit/OkHttp configuration
│   ├── offline/
│   │   └── OfflineDataLoader.kt       # Loads data from assets
│   ├── party/
│   │   └── PartyManager.kt             # Party management and persistence
│   └── repository/
│       └── PokemonRepository.kt       # Data access layer
├── ui/
│   ├── components/                    # Reusable UI components
│   │   ├── PokedexContainer.kt        # Main container with Pokedex design
│   │   ├── PokemonCard.kt             # Pokémon display card
│   │   ├── PartyPokemonDetailScreen.kt # Detailed party Pokémon view
│   │   └── ...                         # Other components
│   ├── screens/                       # Main application screens
│   │   ├── PokedexScreen.kt           # Pokémon search and browse
│   │   ├── MyPartyScreen.kt           # Party management
│   │   ├── UtilitiesScreen.kt         # Utility tools menu
│   │   ├── WeaknessesScreen.kt        # Type effectiveness chart
│   │   ├── NaturesScreen.kt           # Nature information
│   │   ├── EnergySlotsScreen.kt       # Energy slot reference
│   │   ├── DiceRollingScreen.kt       # Dice rolling utility
│   │   ├── InventoryScreen.kt         # Inventory management
│   │   └── EncounterScreen.kt         # Encounter tracking
│   ├── theme/                         # Material Design 3 theming
│   └── viewmodel/
│       ├── PokemonViewModel.kt        # Main state management
│       └── PartyViewModel.kt          # Party state management
└── ...
```

### Data Flow

1. **Data Loading Priority**:
   - Offline assets (bundled in APK) → Primary source
   - Local storage (SharedPreferences) → Fallback
   - PokéAPI → Last resort

2. **State Management**:
   - ViewModels hold UI state
   - Repository pattern for data access
   - Local persistence via SharedPreferences

3. **Pokémon Conversion**:
   - Base Pokémon data loaded from assets
   - DnDConverter transforms stats to D&D format
   - PartyPokemon adds runtime state (level, HP, moves, etc.)

---

## 📱 Main Features

### 1. Pokédex Screen
- **Search & Browse**: Search through all Pokémon (1000+)
- **Grid/List View**: Toggle between display modes
- **Pokémon Details**: View stats, types, moves, abilities
- **Add to Party**: Convert Pokémon to party member
- **Swipe Navigation**: Navigate between Pokémon in detail view

### 2. My Party Screen
- **Party Management**: Up to 6 Pokémon per party
- **Pokémon Details**: 
  - Current HP/Max HP
  - D&D stats (Attack, Defense, Sp.Atk, Sp.Def, Speed, HP)
  - Level and experience
  - Proficiency bonus
  - Nature and bonuses/penalties
  - Available moves by tier
  - Prepared moves (4 moves + free moves)
  - Energy slots (1st-9th level)
  - Status effects and conditions
  - Evolution information
- **Level Management**: Click level to add/remove experience
- **Move Management**: Prepare moves with energy costs
- **Status Effects**: Add effects/conditions with duration
- **Round Tracking**: Pass rounds to decrement effect durations

### 3. Utilities Screen
- **Type Effectiveness Chart**: View weaknesses/resistances
- **Natures Reference**: All 25 natures with descriptions
- **Energy Slots Reference**: Spell slot progression table
- **Dice Rolling**: D&D dice rolling utility

### 4. Inventory Screen
- **Item Management**: Track items and quantities
- **Add/Edit Items**: Manage inventory items

### 5. Encounter Screen
- **Encounter Tracking**: Manage encounter creatures
- **Combat Management**: Track HP, status, etc.

---

## 🎲 D&D Conversion System

### Stat Conversion Formula

**Standard Stats** (Attack, Defense, Sp.Atk, Sp.Def, Speed):
```
D&D Stat = floor((Base Stat ÷ 10) + 5)
```

**HP Stat**:
```
D&D HP = floor(Base HP ÷ 3)
```

**Modifiers**:
```
Modifier = floor((Stat - 10) ÷ 2)
```

### Movement Speed Calculation

Complex formula based on:
- Speed stat
- Weight categories:
  - < 10 kg: +1
  - 10-49.9 kg: +0
  - 50-149.9 kg: -1
  - 150-299.9 kg: -2
  - ≥ 300 kg: -3
- Final movement = (adjusted score × 2.5) rounded to nearest 5

### Armor Class (AC)
```
AC = 10 + Speed Modifier + Proficiency Bonus (if Speed is proficient)
```

### Initiative
```
Initiative = Speed Modifier + Proficiency Bonus (if Speed is proficient)
```

### Hit Dice
Based on HP ranges:
- ≤ 50 HP: d6
- 51-80 HP: d8
- 81-120 HP: d10
- > 120 HP: d12

### Move Tiers
Pokémon moves are organized by D&D spell slot tiers:
- **Tier 1-9**: Corresponds to 1st-9th level spell slots
- Conversion: `D&D Level = ceil(Pokémon Level ÷ 5)`
- Moves learned at Pokémon levels 1-5 → D&D level 1
- Moves learned at Pokémon levels 6-10 → D&D level 2
- etc.

### Energy Slots
Follows Wizard spell slot progression:
- Level 1: 2 × 1st level slots
- Level 2: 3 × 1st level slots
- Level 3: 4 × 1st level, 2 × 2nd level
- ...continues to level 20 with 9th level slots

### Experience & Leveling
- **D&D Experience Table**: Levels 1-20
- **Proficiency Bonus**: 
  - Levels 1-4: +2
  - Levels 5-8: +3
  - Levels 9-12: +4
  - Levels 13-16: +5
  - Levels 17-20: +6

---

## 📊 Data Models

### Pokemon (Base Data)
```kotlin
data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val stats: List<Stat>,
    val types: List<TypeSlot>,
    val baseExperience: Int,
    val abilities: List<AbilitySlot>,
    val levelUpMoves: List<LevelUpMove>,
    val spritePath: String
)
```

### PartyPokemon (Runtime State)
```kotlin
data class PartyPokemon(
    val id: Int,
    val name: String,
    val basePokemon: Pokemon,
    val level: Int = 1,
    val currentHP: Int,
    val maxHP: Int,
    val actualSize: Int,              # Base ±5 variation
    val actualWeight: Int,             # Base ±5 variation
    val availableMoves: List<LevelUpMove>,
    val currentMoveSet: List<String>,  # Selected 4 moves
    val movesData: List<MoveData>,
    val convertedDnDStats: Map<String, Int>,
    val currentDnDStats: Map<String, Int>,
    val weaknesses: List<String>,
    val resistances: List<String>,
    val currentStatusEffects: List<StatusEffect>?,
    val nature: Nature,
    val currentExp: Int,
    val expToLevelUp: Int,
    val proficiency: Int,
    val movementSpeed: Int,
    val energySlots: List<Int>,        # Max slots per tier
    val currentEnergySlots: List<Int>, # Current available slots
    val evolution: EvolutionDetails?
)
```

### Nature System
- 25 different natures
- Each nature can increase one stat (+proficiency bonus)
- Each nature can decrease one stat (-2 penalty)
- Neutral natures (Hardy) have no bonuses/penalties

### Status Effects
- Property-based effects (Attack, Defense, Speed, etc.)
- Condition-based effects (Poison, Paralysis, etc.)
- Duration tracking (number of rounds)
- Automatic cleanup when duration reaches 0

---

## 💾 Data Storage

### Offline Assets
Located in `app/src/main/assets/`:
- `pokemons.json`: Complete Pokémon data (1000+ entries)
- `moves_database.json`: All move data with tiers, power, accuracy, etc.
- `evolution_summary.json`: Evolution chain data
- `moves_by_range.json`: Moves organized by level ranges
- `front_images/`: Pokémon sprite images (PNG format)
- `sprites/`: Additional sprite assets

### Local Storage (SharedPreferences)
- **Party Data**: Serialized PartyPokemon list
- **Inventory**: Item list with quantities
- **Encounters**: Encounter creature data

### Data Loading Priority
1. **Offline Assets** (bundled in APK) - Primary
2. **Local Storage** (SharedPreferences) - Fallback
3. **PokéAPI** (network) - Last resort

---

## 🛠️ Development Tools & Scripts

### Data Management Scripts
Located in `scripts/` directory:

1. **download_pokemon_data.py**: Downloads all Pokémon data from PokéAPI
2. **download_moves_data.py**: Downloads move data
3. **download_front_images.py**: Downloads Pokémon sprites
4. **add_evolution_data.py**: Adds evolution chain data
5. **fix_evolution_data.py**: Fixes evolution data issues
6. **clean_pokemon_data.py**: Cleans and validates Pokémon data
7. **restructure_pokemon_moves.py**: Organizes moves by tiers
8. **sort_pokemon_moves.py**: Sorts moves by level
9. **extract_moves_by_level_range.py**: Extracts moves by level ranges
10. **fix_sprite_paths.py**: Fixes sprite path references
11. **remove_urls.py**: Removes URL references from data
12. **separate_pokemon_data.py**: Separates data into multiple files
13. **analyze_version_coverage.py**: Analyzes version coverage

### Running Scripts
```bash
# Python scripts
py scripts/download_pokemon_data.py

# Kotlin script
kotlin scripts/download_pokemon_data.kt
```

---

## 🎨 UI/UX Design

### Theme
- **Material Design 3**: Modern Material You theming
- **Color Scheme**: Crystal blue gradient background
- **Pokedex Design**: Custom top/bottom Pokedex frame components
- **Fullscreen Mode**: Immersive fullscreen experience
- **Portrait Only**: Locked to portrait orientation

### Navigation
- **Bottom Navigation**: Custom navigation bar
- **Main Tabs**:
  - Pokédex
  - My Party
  - Utilities
  - Inventory
  - Encounter

### Layout Direction
- **Forced LTR**: Even on RTL systems (Hebrew), layout is left-to-right
- Ensures consistent UI across all languages

---

## 🔧 Dependencies

### Core Android
- `androidx.core:core-ktx:1.17.0`
- `androidx.lifecycle:lifecycle-runtime-ktx:2.9.2`
- `androidx.activity:activity-compose:1.10.1`

### Jetpack Compose
- `androidx.compose.bom:2024.09.00`
- `androidx.compose.ui:ui`
- `androidx.compose.material3:material3`
- `androidx.compose.ui:ui-tooling-preview`

### Networking
- `com.squareup.retrofit2:retrofit:2.9.0`
- `com.squareup.retrofit2:converter-gson:2.9.0`
- `com.squareup.okhttp3:logging-interceptor:4.11.0`

### Image Loading
- `io.coil-kt:coil-compose:2.4.0`

### Coroutines
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3`

### ViewModel
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0`

---

## 📝 Key Features Implementation

### Party Management
- **Add Pokémon**: Convert base Pokémon to PartyPokemon with random nature
- **Remove Pokémon**: Remove from party
- **Update Stats**: Modify current D&D stats
- **Level Management**: Gain/lose experience, automatic level calculation
- **Move Preparation**: Select 4 moves + free moves, assign energy costs
- **Status Effects**: Add effects/conditions with round-based duration
- **Evolution**: Track evolution requirements and trigger evolution

### Move System
- **Available Moves**: Filtered by current level
- **Move Tiers**: Organized by D&D spell slot tiers (1-9)
- **Energy Costs**: Moves require energy slots to use
- **Free Moves**: Some moves have no energy cost
- **Move Data**: Full move information (power, accuracy, type, description, effect)

### Experience System
- **D&D Experience Table**: Standard D&D 5e progression
- **Level Calculation**: Automatic level from total experience
- **Level Up/Down**: Automatic recalculation of:
  - Proficiency bonus
  - Available moves
  - Energy slots
  - Evolution eligibility

### Nature System
- **Random Assignment**: Assigned when Pokémon added to party
- **Stat Bonuses**: +proficiency bonus to one stat
- **Stat Penalties**: -2 penalty to one stat
- **Nature Change**: Can change nature after assignment

### Energy Slots
- **Wizard Progression**: Follows D&D Wizard spell slot table
- **Tier Management**: 1st through 9th level slots
- **Current/Max Tracking**: Track available vs. maximum slots
- **Full Rest**: Reset all slots to maximum
- **Manual Adjustment**: Can manually increase/decrease slots

### Status Effects & Conditions
- **Property Effects**: Modify stats (Attack, Defense, Speed, etc.)
- **Condition Effects**: Apply conditions (Poison, Paralysis, etc.)
- **Duration Tracking**: Round-based duration system
- **Round Passing**: Decrement all durations by 1
- **Automatic Cleanup**: Remove effects when duration reaches 0

---

## 🚀 Build & Deployment

### Build Configuration
- **Gradle**: Kotlin DSL (`.gradle.kts`)
- **AGP**: 8.12.1
- **Kotlin**: 2.0.21
- **Java**: 11

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

### APK Size Considerations
- **Offline Data**: ~20-30 MB increase
  - Pokémon data: ~2-3 MB
  - Sprites: ~15-25 MB
  - Other assets: ~1-2 MB

---

## 📋 Current Development Status

### Completed Features (from to-do.txt)
- ✅ Offline data system
- ✅ Party management
- ✅ D&D stat conversion
- ✅ Move system with tiers
- ✅ Experience and leveling
- ✅ Nature system
- ✅ Energy slots
- ✅ Status effects and conditions
- ✅ Evolution tracking
- ✅ Type effectiveness charts
- ✅ Dice rolling utility
- ✅ Inventory management
- ✅ Encounter tracking
- ✅ Swipe navigation
- ✅ Move descriptions

### In Progress / Planned
- Party information display improvements
- Additional utility features
- UI/UX refinements

---

## 🔍 Code Quality & Patterns

### Architecture Patterns
- **MVVM**: Clear separation of concerns
- **Repository Pattern**: Centralized data access
- **Dependency Injection**: Manual DI (could be improved with Hilt/Koin)
- **State Management**: Compose State + ViewModel

### Best Practices
- **Offline-First**: Primary data source is offline
- **Error Handling**: Graceful fallbacks at each layer
- **Type Safety**: Strong typing with Kotlin data classes
- **Immutable Data**: Data classes with copy() for updates

### Testing
- Unit tests in `app/src/test/java/`
- Android tests in `app/src/androidTest/java/`
- Test coverage for critical paths (moves, stats, etc.)

---

## 🌐 API Integration

### PokéAPI
- **Base URL**: `https://pokeapi.co/api/v2/`
- **Rate Limiting**: 100ms delay between requests in scripts
- **Data Source**: Primary source for initial data download
- **Fallback**: Used only when offline/local data unavailable

### Network Module
- Retrofit for API calls
- Gson for JSON parsing
- OkHttp with logging interceptor
- Coroutines for async operations

---

## 📚 Additional Resources

### Documentation Files
- `OFFLINE_DATA_README.md`: Detailed offline data system documentation
- `to-do.txt`: Feature checklist and development notes
- `rules.txt`: Project rules and guidelines

### Asset Files
- Pokémon sprites: `app/src/main/assets/front_images/`
- Pokedex design assets: `app/src/main/assets/pokedex_design/`

---

## 🎯 Future Enhancements

### Potential Improvements
1. **WebP Sprites**: Reduce APK size with WebP format
2. **Progressive Loading**: Load essential data first
3. **Delta Updates**: Only download new/changed data
4. **Compression**: Further reduce asset sizes
5. **Dependency Injection**: Add Hilt or Koin
6. **Database**: Migrate from SharedPreferences to Room
7. **Cloud Sync**: Sync party data across devices
8. **Battle System**: Full combat simulation
9. **Campaign Mode**: Story-driven gameplay
10. **Multiplayer**: Online party sharing

---

## 👥 Development Notes

### Branch Information
- **Current Branch**: `party-management`
- **Git Status**: Modified `moves_database.json`

### Key Design Decisions
1. **Offline-First**: Ensures app works without internet
2. **D&D Conversion**: Maintains balance with D&D 5e rules
3. **Immersive UI**: Fullscreen mode for tablet-like experience
4. **LTR Layout**: Consistent UI regardless of system language
5. **Local Persistence**: Simple SharedPreferences for MVP

---

## 📞 Support & Maintenance

### Common Issues
- **Large APK Size**: Consider WebP conversion for sprites
- **Data Updates**: Re-run download scripts and rebuild
- **Performance**: Offline data ensures fast loading
- **Compatibility**: Min SDK 24 ensures broad device support

### Update Process
1. Run data download scripts
2. Rebuild assets
3. Test offline functionality
4. Build and deploy new APK

---

**Last Updated**: Based on current codebase analysis
**Version**: 1.0 (versionCode: 1, versionName: "1.0")
**Maintainer**: Development team

