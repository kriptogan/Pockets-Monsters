# TCG Collection Tracking Tool - Transformation Request

## 📋 Project Transformation Overview

**Current Purpose**: Pokémon-DnD game companion tool  
**New Purpose**: Pokémon TCG (Trading Card Game) collection tracking tool

This document describes the transformation of the **Pockets & Monsters** application from a D&D game companion tool to a TCG card collection tracker.

---

## 🎯 Core Concept Change

### Current State
The app currently serves as a companion tool for playing Pokémon in a D&D-style tabletop RPG format, featuring:
- D&D stat conversion
- Party management
- Move systems with energy slots
- Experience and leveling
- Status effects tracking
- Encounter management

### Desired State
Transform the app into a **Pokémon TCG collection tracking tool** that helps users:
- Track which Pokémon cards they own
- Browse all available Pokémon
- View TCG set information for each Pokémon
- See card images from different sets
- Manage their collection visually

---

## ✨ Feature Requirements

### 1. Pokémon List Screen (Maintained)
- **Keep existing functionality**: Display list/grid of all Pokémon
- **Keep search functionality**: Search and filter Pokémon
- **Keep navigation**: Same navigation structure
- **Visual indicator**: Add visual feedback for owned pokemon (atleast 1 card)

### 2. Pokemon Tracking System

#### 2.1 Mark as "Already Have"
- **Interaction**: Long press (click and hold) on a Pokémon in the pokemon list
- **Action**: Toggle "already have" status
- **Visual Feedback**: 
  - Green border around the Pokémon image when marked as owned
  - Clear visual distinction between owned and unowned cards
- **Persistence**: Save collection status locally (SharedPreferences or similar)

#### 2.2 Collection State Management
- **Data Model**: Track which Pokémon cards are owned
- **Storage**: Persistent local storage
- **Default State**: All Pokémon start as "not owned"
- **Toggle Functionality**: Can mark/unmark ownership

### 3. Pokémon Details Screen (Modified)

#### 3.1 Current Details Screen (Keep)
- **Navigation**: Tap on Pokémon to enter details (same as current behavior)
- **Pokémon Image**: Display Pokémon image (same as current)

#### 3.2 New Details Content (Replace D&D Stats)
**Remove**:
- D&D stat conversions
- Party management features
- Move systems
- Experience/leveling
- Status effects
- Energy slots

**Add**:
- **TCG Sets List**: Display all TCG sets that contain this Pokémon
  - Set name
  - Set identifier/code
  - Set release date (if available)
  - Set symbol/logo (if available)
- **Card Images**: Display actual card images from each set
  - Show card image(s) for each set
  - Multiple cards per set if applicable
  - High-quality card images
  - Card name/number within set

### 4. TCG Data Integration

#### 4.1 Data Source Requirements
- **TCG Set Information**: Need data source for:
  - Which sets contain which Pokémon
  - Set metadata (name, release date, symbol)
  - Card information (card number, rarity, etc.)
- **Card Images**: Need access to:
  - Card image URLs or local assets
  - High-resolution card images
  - Multiple card variants per set (if applicable)

#### 4.2 Potential Data Sources
- **PokéAPI TCG Extension**: Check if PokéAPI has TCG data
- **TCGPlayer API**: Commercial TCG data API
- **Pokémon TCG API**: Official or community-maintained API
- **Local Data**: Pre-downloaded TCG set data (similar to current offline approach)

---

## 🎨 UI/UX Changes

### Visual Design Updates

#### Collection Indicator
- **Owned Cards**: Green border around Pokémon image
- **Unowned Cards**: Default/no border
- **Visual Clarity**: Clear distinction between states
- **Accessibility**: Consider colorblind-friendly alternatives

#### Details Screen Layout
```
┌─────────────────────────────┐
│   Pokémon Image (Large)     │
│                             │
├─────────────────────────────┤
│   Pokémon Name              │
│   Type(s)                   │
├─────────────────────────────┤
│   TCG Sets Containing This  │
│   Pokémon:                   │
│                             │
│   ┌───────────────────────┐ │
│   │ [Set Logo] Set Name   │ │
│   │ Card Image(s)         │ │
│   │ Release: MM/YYYY       │ │
│   └───────────────────────┘ │
│                             │
│   ┌───────────────────────┐ │
│   │ [Set Logo] Set Name   │ │
│   │ Card Image(s)         │ │
│   │ Release: MM/YYYY      │ │
│   └───────────────────────┘ │
│                             │
│   ... (more sets)           │
└─────────────────────────────┘
```

### Interaction Patterns

#### Long Press Gesture
- **Trigger**: Long press on Pokémon card/item
- **Feedback**: 
  - Visual feedback (border color change)
  - Haptic feedback (vibration)
  - Optional: Toast message ("Added to collection" / "Removed from collection")
- **State Toggle**: Switch between owned/unowned

#### Tap Gesture
- **Trigger**: Single tap on Pokémon card/item
- **Action**: Navigate to details screen
- **Behavior**: Same as current implementation

---

## 📊 Data Model Changes

### New Data Models Required

#### Collection Status
```kotlin
data class CollectionStatus(
    val pokemonId: Int,
    val pokemonName: String,
    val isOwned: Boolean,
    val ownedSince: Long? = null, // Timestamp when marked as owned
    val notes: String? = null // Optional user notes
)
```

#### TCG Set Information
```kotlin
data class TCGSet(
    val setId: String,
    val setName: String,
    val setCode: String,
    val releaseDate: String?,
    val setSymbol: String?, // URL or local path to set symbol
    val totalCards: Int
)
```

#### TCG Card Information
```kotlin
data class TCGCard(
    val cardId: String,
    val pokemonId: Int,
    val pokemonName: String,
    val setName: String,
    val setCode: String,
    val cardNumber: String,
    val rarity: String?,
    val cardImageUrl: String, // URL to card image
    val cardImageLocalPath: String? = null, // Local cached path
    val cardType: String? // e.g., "Holo", "Reverse Holo", "Normal"
)
```

#### Pokémon TCG Data
```kotlin
data class PokemonTCGData(
    val pokemonId: Int,
    val pokemonName: String,
    val sets: List<TCGSet>,
    val cards: List<TCGCard>
)
```

### Storage Requirements

#### Local Storage
- **Collection Status**: Store which Pokémon are owned
  - Format: JSON or key-value pairs
  - Location: SharedPreferences or local database
- **TCG Data**: Store TCG set and card information
  - Format: JSON files (similar to current pokemons.json)
  - Location: Assets folder or downloaded data
- **Card Images**: Cache card images locally
  - Format: Images (PNG/WebP)
  - Location: Local cache directory

---

## 🔄 Implementation Phases

### Phase 1: Core Collection Tracking
1. Add long press gesture detection
2. Implement collection status toggle
3. Add visual indicator (green border)
4. Implement local persistence for collection status
5. Update UI to show collection status

### Phase 2: Remove D&D Features
1. Remove D&D stat conversion logic
2. Remove party management features
3. Remove move system
4. Remove experience/leveling system
5. Remove status effects tracking
6. Clean up unused code and dependencies

### Phase 3: TCG Data Integration
1. Research and identify TCG data source
2. Create data models for TCG sets and cards
3. Implement data loading (offline-first approach)
4. Create TCG data repository/service
5. Update details screen to show TCG sets

### Phase 4: Card Images
1. Implement card image loading
2. Add image caching mechanism
3. Display card images in details screen
4. Handle multiple cards per set
5. Optimize image loading and display

### Phase 5: UI/UX Polish
1. Refine visual design
2. Improve collection indicator visibility
3. Add animations/transitions
4. Add haptic feedback
5. Test and optimize performance

---

## 🛠️ Technical Considerations

### Data Source Research Needed
- **PokéAPI TCG Data**: Check if available
- **Pokémon TCG API**: Research official/unofficial APIs
- **TCGPlayer API**: Evaluate commercial options
- **Scraping**: Last resort if no API available
- **Manual Data Entry**: Initial approach for MVP

### Image Handling
- **Image Loading**: Use Coil (already in dependencies)
- **Caching**: Implement local image cache
- **Offline Support**: Download card images with app or on-demand
- **Image Size**: Optimize for mobile (consider WebP format)
- **Lazy Loading**: Load images as user scrolls

### Performance Considerations
- **Large Dataset**: TCG data can be extensive
- **Image Storage**: Card images will increase app size significantly
- **Caching Strategy**: Smart caching to balance storage and performance
- **Lazy Loading**: Load TCG data only when needed

### Migration Strategy
- **Data Migration**: Existing user data (party, etc.) may need migration
- **Backward Compatibility**: Consider if any features should be preserved
- **User Communication**: Inform users about major changes

---

## 📱 Screen-by-Screen Changes

### 1. Pokédex Screen
**Keep**:
- Pokémon list/grid display
- Search functionality
- Navigation to details

**Add**:
- Collection status indicator (green border for owned)
- Long press gesture handler
- Visual feedback for collection status

**Remove**:
- Add to party functionality
- D&D-related UI elements

### 2. Pokémon Details Screen
**Keep**:
- Pokémon image display
- Basic Pokémon information (name, types)
- Navigation back to list

**Remove**:
- All D&D stats and information
- Party management features
- Move lists and energy slots
- Experience/leveling UI
- Status effects UI

**Add**:
- TCG sets list
- Card images display
- Set information (name, release date, symbol)
- Card details (number, rarity, type)

### 3. My Party Screen
**Remove Entirely**:
- This screen is no longer needed for TCG collection tracking
- Can be replaced with "My Collection" screen (future enhancement)

### 4. Utilities Screen
**Remove Entirely**:
- D&D-specific utilities (weaknesses chart, natures, energy slots, dice rolling)
- Can be replaced with TCG-specific utilities (set information, collection statistics, etc.)

### 5. Inventory Screen
**Remove Entirely**:
- Not relevant for TCG collection tracking

### 6. Encounter Screen
**Remove Entirely**:
- Not relevant for TCG collection tracking

---

## 🎯 Success Criteria

### MVP (Minimum Viable Product)
- ✅ Display all Pokémon in list/grid
- ✅ Long press to mark/unmark as owned
- ✅ Green border indicator for owned cards
- ✅ Tap to view details
- ✅ Details screen shows TCG sets containing the Pokémon
- ✅ Card images displayed (even if limited initially)

### Future Enhancements
- Collection statistics (percentage owned, by set, etc.)
- Wishlist functionality
- Collection export/import
- Price tracking (if using commercial API)
- Set completion tracking
- Card condition tracking
- Trading/wishlist features

---

## 📝 Notes and Considerations

### Data Availability
- TCG data may not be as readily available as Pokémon game data
- May need to manually curate initial dataset
- Card images may require separate licensing considerations

### User Experience
- Ensure smooth transition from current app
- Maintain familiar navigation patterns where possible
- Clear visual feedback for all interactions
- Fast loading times for card images

### Technical Debt
- Remove unused D&D-related code
- Clean up dependencies no longer needed
- Update documentation to reflect new purpose
- Consider refactoring for new architecture if needed

---

## 🔍 Questions to Resolve

1. **TCG Data Source**: What is the best source for TCG set and card data?
2. **Card Images**: Where can we get card images? Are there licensing restrictions?
3. **Offline Support**: Should card images be bundled with app or downloaded on-demand?
4. **Collection Statistics**: Should we add collection statistics in MVP or later?
5. **Migration**: How should we handle existing user data (party, etc.)?
6. **Navigation**: Should we keep all current screens or completely redesign navigation?

---

## 📚 References

- Current project documentation: `PROJECT_DOCUMENTATION.md`
- PokéAPI documentation: https://pokeapi.co/
- Pokémon TCG API (if available): Research needed
- TCGPlayer API: https://docs.tcgplayer.com/

---

**Document Version**: 1.0  
**Created**: Based on transformation request  
**Status**: Planning Phase

