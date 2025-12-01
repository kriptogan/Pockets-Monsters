# Pockets & Monsters - Project Documentation

## 📋 Project Overview

**Pockets & Monsters** is an Android application for tracking Pokémon Trading Card Game (TCG) collections. The app allows users to browse all Pokémon, mark which cards they own, and view TCG sets and card images for each Pokémon. It features offline data support, collection tracking, and integration with the official Pokémon TCG API.

### Key Characteristics
- **Platform**: Android (Kotlin)
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Data Sources**: 
  - PokéAPI (with offline fallback) for Pokémon data
  - Pokémon TCG API (api.pokemontcg.io/v2/) for TCG data
- **Target SDK**: 36 (Android 15)
- **Min SDK**: 24 (Android 7.0)
- **Package**: `com.kriptogan.pocketsmonsters`

---

## 🎯 Core Concept

The application is a TCG collection tracking tool:
- Browse all Pokémon in a grid or list view
- Mark Pokémon as owned (long press gesture)
- Visual indicator (green border) for owned Pokémon
- View TCG sets containing each Pokémon
- Display card images from different sets
- Collection status persists across app restarts

---

## 🏗️ Architecture

### Project Structure

```
app/src/main/java/com/kriptogan/pocketsmonsters/
├── MainActivity.kt                    # Entry point, fullscreen immersive mode
├── data/
│   ├── api/
│   │   ├── PokeApiService.kt          # Retrofit API service for PokéAPI
│   │   └── TCGApiService.kt           # Retrofit API service for Pokémon TCG API
│   ├── collection/
│   │   ├── CollectionManager.kt       # Manages collection status persistence
│   │   └── CollectionStatus.kt        # Collection status data model
│   ├── local/
│   │   └── LocalStorage.kt            # SharedPreferences data persistence
│   ├── models/
│   │   ├── Pokemon.kt                  # Base Pokémon data model
│   │   ├── Stat.kt                     # Stat definitions
│   │   ├── Type.kt                     # Type information
│   │   └── ...                         # Other data models
│   ├── network/
│   │   └── NetworkModule.kt           # Retrofit/OkHttp configuration
│   ├── offline/
│   │   └── OfflineDataLoader.kt       # Loads data from assets
│   ├── repository/
│   │   └── PokemonRepository.kt       # Data access layer
│   └── tcg/
│       ├── TCGRepository.kt            # TCG data access layer
│       ├── TCGSet.kt                   # TCG set data model
│       ├── TCGCard.kt                  # TCG card data model
│       └── PokemonTCGData.kt            # Aggregated TCG data model
├── ui/
│   ├── components/                    # Reusable UI components
│   │   ├── PokedexContainer.kt        # Main container with Pokedex design
│   │   ├── PokemonCard.kt             # Pokémon display card (list view)
│   │   ├── PokemonGridScreen.kt       # Grid view with PokemonGridCard
│   │   ├── PokemonDetailScreen.kt     # Detailed Pokémon view with TCG data
│   │   └── CustomBottomNavigation.kt  # Bottom navigation bar
│   ├── screens/                       # Main application screens
│   │   └── PokedexScreen.kt           # Pokémon search and browse
│   ├── theme/                         # Material Design 3 theming
│   └── viewmodel/
│       └── PokemonViewModel.kt        # Main state management
└── ...
```

### Data Flow

1. **Pokémon Data Loading Priority**:
   - Offline assets (bundled in APK) → Primary source
   - Local storage (SharedPreferences) → Fallback
   - PokéAPI → Last resort

2. **TCG Data Loading**:
   - Pokémon TCG API (api.pokemontcg.io/v2/) → Primary source
   - Offline assets → Future enhancement (placeholder)
   - Searches by national Pokédex number

3. **State Management**:
   - ViewModels hold UI state
   - Repository pattern for data access
   - Local persistence via SharedPreferences for collection status

4. **Collection Tracking**:
   - CollectionManager handles ownership status
   - Persisted in SharedPreferences
   - Visual indicator (green border) in UI
   - PartyPokemon adds runtime state (level, HP, moves, etc.)

---

## 📱 Main Features

### 1. Pokédex Screen (Collection Tracker)
- **Search & Browse**: Search through all Pokémon (1000+)
- **Grid View**: 4-column grid display with Pokémon images
- **Collection Tracking**: 
  - Long press to mark/unmark Pokémon as owned
  - Green border indicator (3dp) for owned Pokémon
  - Collection status persists across app restarts
- **Pokémon Details**: 
  - Pokémon image and name
  - Type information
  - TCG sets containing the Pokémon
  - Card images from different sets
  - Set information (name, release date, symbol)
- **Visual Feedback**:
  - Haptic feedback on interactions
  - Toast messages for collection changes
  - Smooth animations and transitions

### 2. TCG Data Integration
- **TCG Sets Display**: Shows all sets containing the selected Pokémon
- **Card Images**: Displays card images from the official Pokémon TCG API
- **Set Information**: 
  - Set name and release date
  - Set symbol/logo
  - Number of cards per set
- **API Integration**: 
  - Official Pokémon TCG API (api.pokemontcg.io/v2/)
  - Searches by national Pokédex number
  - Automatic image caching via Coil

---

## 🎴 Collection Tracking System

### Collection Status
- **Ownership Tracking**: Boolean flag for each Pokémon
- **Persistence**: Stored in SharedPreferences
- **Visual Indicator**: Green border (3dp) around owned Pokémon images
- **Toggle Method**: Long press gesture on any Pokémon card

### Collection Manager
- **CollectionManager**: Handles all collection-related operations
- **Methods**:
  - `getCollectionStatus(pokemonId: Int)`: Get ownership status
  - `toggleOwnership(pokemonId: Int, pokemonName: String)`: Toggle ownership
  - `isPokemonOwned(pokemonId: Int)`: Check if owned
  - `getAllOwnedPokemon()`: Get all owned Pokémon IDs

### TCG Data Integration
- **TCGRepository**: Manages TCG data fetching
- **API Integration**: Official Pokémon TCG API
- **Data Models**:
  - `TCGSet`: Set information (name, release date, symbol)
  - `TCGCard`: Card information (images, rarity, number)
  - `PokemonTCGData`: Aggregated data for a Pokémon

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

### CollectionStatus
```kotlin
data class CollectionStatus(
    val pokemonId: Int,
    val pokemonName: String,
    var isOwned: Boolean,
    val ownedSince: Long? = null,
    val notes: String? = null
)
```

### TCGSet
```kotlin
data class TCGSet(
    val id: String,
    val name: String,
    val series: String?,
    val releaseDate: String?,
    val images: TCGSetImages?,
    val total: Int?
)
```

### TCGCard
```kotlin
data class TCGCard(
    val id: String,
    val name: String,
    val set: TCGCardSet?,
    val images: TCGCardImages?,
    val nationalPokedexNumbers: List<Int>?,
    val rarity: String?,
    val number: String?
)
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

