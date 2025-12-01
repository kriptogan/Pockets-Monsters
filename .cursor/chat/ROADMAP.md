# TCG Collection Tracking Tool - Implementation Roadmap

## 📋 Overview

This roadmap outlines the step-by-step transformation of **Pockets & Monsters** from a Pokémon-DnD game companion tool to a Pokémon TCG collection tracking tool.

**Key Changes Noted**:
- Visual indicator shows owned Pokémon (at least 1 card owned)
- Tracking system is for Pokémon (not just individual cards)
- Long press interaction is specifically on Pokémon in the Pokémon list

---

## 🎯 Phase 1: Core Collection Tracking

### 1.1 Data Models
- [ ] Create `CollectionStatus` data class
  - [ ] Add `pokemonId: Int`
  - [ ] Add `pokemonName: String`
  - [ ] Add `isOwned: Boolean`
  - [ ] Add `ownedSince: Long?` (optional timestamp)
  - [ ] Add `notes: String?` (optional user notes)

### 1.2 Collection Manager
- [ ] Create `CollectionManager` class
  - [ ] Implement `getCollectionStatus(pokemonId: Int): CollectionStatus?`
  - [ ] Implement `toggleOwnership(pokemonId: Int): Boolean`
  - [ ] Implement `isPokemonOwned(pokemonId: Int): Boolean`
  - [ ] Implement `getAllOwnedPokemon(): List<Int>`
  - [ ] Implement local persistence using SharedPreferences
  - [ ] Add data migration logic (if needed)

### 1.3 UI Components - Collection Indicator
- [ ] Update `PokemonCard` component
  - [ ] Add collection status parameter
  - [ ] Add green border styling for owned Pokémon
  - [ ] Ensure border is visible and clear
  - [ ] Test with different screen sizes

### 1.4 Gesture Handling
- [ ] Add long press gesture to Pokémon list items
  - [ ] Implement `onLongPress` handler in `PokemonCard`
  - [ ] Add haptic feedback on long press
  - [ ] Toggle collection status on long press
  - [ ] Add visual feedback (border color change)
  - [ ] Optional: Add toast message for user feedback

### 1.5 Integration
- [ ] Integrate `CollectionManager` with `PokemonViewModel`
  - [ ] Load collection status when loading Pokémon list
  - [ ] Update UI when collection status changes
  - [ ] Persist changes immediately
  - [ ] Handle edge cases (network errors, etc.)

### 1.6 Testing
- [ ] Test long press gesture on different devices
- [ ] Test collection status persistence (app restart)
- [ ] Test visual indicator visibility
- [ ] Test toggle functionality (mark/unmark)
- [ ] Test with large Pokémon lists (performance)

---

## 🗑️ Phase 2: Remove D&D Features

### 2.1 Remove D&D Stat Conversion
- [ ] Remove `DnDConverter` class or mark as deprecated
- [ ] Remove D&D stat display from UI
- [ ] Remove D&D-related calculations
- [ ] Clean up imports and dependencies

### 2.2 Remove Party Management
- [ ] Remove `PartyManager` class
- [ ] Remove `PartyPokemon` model (or keep for migration)
- [ ] Remove `PartyViewModel`
- [ ] Remove "My Party" screen
- [ ] Remove "Add to Party" functionality from Pokémon details
- [ ] Remove party-related navigation items

### 2.3 Remove Move System
- [ ] Remove move tier system
- [ ] Remove move preparation UI
- [ ] Remove energy slots system
- [ ] Remove move-related data models
- [ ] Clean up move-related code

### 2.4 Remove Experience/Leveling
- [ ] Remove experience tracking
- [ ] Remove level calculation logic
- [ ] Remove level display from UI
- [ ] Remove proficiency bonus calculations
- [ ] Remove evolution level tracking (D&D version)

### 2.5 Remove Status Effects
- [ ] Remove status effects tracking
- [ ] Remove effects/conditions UI
- [ ] Remove round tracking system
- [ ] Clean up status effect models

### 2.6 Remove Other D&D Features
- [ ] Remove encounter management
- [ ] Remove inventory system (if D&D-specific)
- [ ] Remove utilities screen (D&D-specific utilities)
- [ ] Remove dice rolling screen
- [ ] Remove weaknesses chart (if D&D-specific)
- [ ] Remove natures screen (if D&D-specific)
- [ ] Remove energy slots reference screen

### 2.7 Clean Up Dependencies
- [ ] Review and remove unused dependencies
- [ ] Update `build.gradle.kts` to remove unnecessary libraries
- [ ] Clean up unused imports across codebase
- [ ] Remove unused assets (D&D-related images, etc.)

### 2.8 Code Cleanup
- [ ] Remove unused classes and files
- [ ] Remove unused methods and functions
- [ ] Update comments and documentation
- [ ] Refactor code if needed

---

## 🔍 Phase 3: TCG Data Integration

### 3.1 Research & Planning
- [ ] Research TCG data sources
  - [ ] Check PokéAPI for TCG data availability
  - [ ] Research Pokémon TCG API (official/unofficial)
  - [ ] Evaluate TCGPlayer API
  - [ ] Consider manual data entry approach
  - [ ] Document findings and decision

### 3.2 Data Models
- [ ] Create `TCGSet` data class
  - [ ] Add `setId: String`
  - [ ] Add `setName: String`
  - [ ] Add `setCode: String`
  - [ ] Add `releaseDate: String?`
  - [ ] Add `setSymbol: String?`
  - [ ] Add `totalCards: Int`

- [ ] Create `TCGCard` data class
  - [ ] Add `cardId: String`
  - [ ] Add `pokemonId: Int`
  - [ ] Add `pokemonName: String`
  - [ ] Add `setName: String`
  - [ ] Add `setCode: String`
  - [ ] Add `cardNumber: String`
  - [ ] Add `rarity: String?`
  - [ ] Add `cardImageUrl: String`
  - [ ] Add `cardImageLocalPath: String?`
  - [ ] Add `cardType: String?`

- [ ] Create `PokemonTCGData` data class
  - [ ] Add `pokemonId: Int`
  - [ ] Add `pokemonName: String`
  - [ ] Add `sets: List<TCGSet>`
  - [ ] Add `cards: List<TCGCard>`

### 3.3 Data Loading Infrastructure
- [ ] Create `TCGDataLoader` class
  - [ ] Implement offline data loading (from assets)
  - [ ] Implement online data loading (from API, if available)
  - [ ] Implement data caching mechanism
  - [ ] Add error handling

- [ ] Create `TCGRepository` class
  - [ ] Implement `getTCGDataForPokemon(pokemonId: Int): PokemonTCGData?`
  - [ ] Implement `getTCGSetsForPokemon(pokemonId: Int): List<TCGSet>`
  - [ ] Implement `getCardsForPokemon(pokemonId: Int): List<TCGCard>`
  - [ ] Add offline-first approach
  - [ ] Add fallback to API if offline data unavailable

### 3.4 Data Storage
- [ ] Create TCG data JSON structure
- [ ] Add TCG data files to assets folder
- [ ] Implement JSON parsing for TCG data
- [ ] Add data validation
- [ ] Create data migration strategy (if updating existing data)

### 3.5 ViewModel Integration
- [ ] Update `PokemonViewModel` or create `TCGViewModel`
  - [ ] Add TCG data loading logic
  - [ ] Add state management for TCG data
  - [ ] Handle loading states (loading, success, error)
  - [ ] Cache TCG data appropriately

---

## 🖼️ Phase 4: Card Images

### 4.1 Image Loading Infrastructure
- [ ] Set up Coil image loading for card images
- [ ] Implement image caching strategy
- [ ] Add placeholder images for loading states
- [ ] Add error handling for failed image loads

### 4.2 Image Storage
- [ ] Decide on image storage approach
  - [ ] Option A: Bundle images with app (offline-first)
  - [ ] Option B: Download on-demand (smaller app size)
  - [ ] Option C: Hybrid approach
- [ ] Implement chosen approach
- [ ] Add image compression (WebP format if possible)
- [ ] Implement local image cache

### 4.3 Card Image Display
- [ ] Create `TCGCardImage` component
  - [ ] Display card image with proper sizing
  - [ ] Handle multiple cards per set
  - [ ] Add loading states
  - [ ] Add error states
  - [ ] Add tap to view full-size (optional)

### 4.4 Details Screen - TCG Sets Display
- [ ] Update `PokemonDetailScreen` component
  - [ ] Remove D&D stats display
  - [ ] Add TCG sets list section
  - [ ] Display set information (name, code, release date)
  - [ ] Display set symbol/logo (if available)
  - [ ] Display card images for each set
  - [ ] Handle multiple cards per set
  - [ ] Add scrollable layout for sets

### 4.5 Performance Optimization
- [ ] Implement lazy loading for card images
- [ ] Optimize image sizes for mobile
- [ ] Add image preloading for visible items
- [ ] Test performance with large number of sets/cards
- [ ] Monitor memory usage

---

## 🎨 Phase 5: UI/UX Polish

### 5.1 Visual Design Refinement
- [ ] Review and refine collection indicator design
  - [ ] Ensure green border is clearly visible
  - [ ] Test with different Pokémon images
  - [ ] Consider accessibility (colorblind-friendly alternatives)
  - [ ] Test on different screen sizes

### 5.2 Animations & Transitions
- [ ] Add smooth transitions for collection status changes
- [ ] Add animation for border appearance/disappearance
- [ ] Add loading animations for TCG data
- [ ] Add smooth scrolling for sets list
- [ ] Add image loading animations

### 5.3 Haptic Feedback
- [ ] Add haptic feedback on long press
- [ ] Add haptic feedback on collection status toggle
- [ ] Test haptic feedback on different devices
- [ ] Ensure feedback is not too aggressive

### 5.4 User Experience Improvements
- [ ] Add loading states for all async operations
- [ ] Add error messages for failed operations
- [ ] Add empty states (no TCG data available, etc.)
- [ ] Improve navigation flow
- [ ] Add pull-to-refresh (if applicable)

### 5.5 Testing & Optimization
- [ ] Test on multiple devices and screen sizes
- [ ] Test with different Android versions
- [ ] Performance testing with large datasets
- [ ] Memory leak testing
- [ ] Battery usage optimization
- [ ] Network usage optimization (if using online data)

---

## 🧹 Phase 6: Cleanup & Documentation

### 6.1 Code Cleanup
- [ ] Remove all unused code
- [ ] Remove unused imports
- [ ] Remove unused dependencies
- [ ] Clean up comments
- [ ] Refactor if needed

### 6.2 Navigation Updates
- [ ] Update bottom navigation
  - [ ] Remove "My Party" tab
  - [ ] Remove "Utilities" tab (or replace with TCG utilities)
  - [ ] Remove "Inventory" tab
  - [ ] Remove "Encounter" tab
  - [ ] Keep "Pokédex" tab
  - [ ] Consider adding "My Collection" tab (future)

### 6.3 Documentation Updates
- [ ] Update `PROJECT_DOCUMENTATION.md`
- [ ] Update `README.md` (if exists)
- [ ] Update code comments
- [ ] Create user guide (optional)
- [ ] Update app description

### 6.4 Testing
- [ ] Comprehensive testing of all features
- [ ] Test collection tracking end-to-end
- [ ] Test TCG data display
- [ ] Test card image loading
- [ ] Test edge cases
- [ ] Test data persistence
- [ ] Test app updates/migrations

---

## 📊 Progress Tracking

### Overall Progress
- **Phase 1 (Collection Tracking)**: 0/6 tasks completed
- **Phase 2 (Remove D&D Features)**: 0/8 tasks completed
- **Phase 3 (TCG Data Integration)**: 0/5 tasks completed
- **Phase 4 (Card Images)**: 0/5 tasks completed
- **Phase 5 (UI/UX Polish)**: 0/5 tasks completed
- **Phase 6 (Cleanup & Documentation)**: 0/4 tasks completed

**Total Progress**: 0/33 major tasks completed

### Current Phase
- [ ] Phase 1: Core Collection Tracking
- [ ] Phase 2: Remove D&D Features
- [ ] Phase 3: TCG Data Integration
- [ ] Phase 4: Card Images
- [ ] Phase 5: UI/UX Polish
- [ ] Phase 6: Cleanup & Documentation

---

## 🎯 MVP Checklist

### Minimum Viable Product Requirements
- [ ] Display all Pokémon in list/grid
- [ ] Long press to mark/unmark as owned
- [ ] Green border indicator for owned Pokémon (at least 1 card)
- [ ] Tap to view details
- [ ] Details screen shows TCG sets containing the Pokémon
- [ ] Card images displayed (even if limited initially)
- [ ] Collection status persists across app restarts

---

## 📝 Notes

### Important Considerations
- **Data Source**: TCG data source needs to be identified early (Phase 3.1)
- **Image Licensing**: Ensure card images can be used legally
- **Performance**: Card images may significantly increase app size
- **Offline Support**: Decide on offline vs. on-demand image loading
- **Migration**: Consider how to handle existing user data

### Dependencies Between Phases
- Phase 2 can be done in parallel with Phase 1 (after 1.1-1.3)
- Phase 3 depends on Phase 2 completion (clean slate)
- Phase 4 depends on Phase 3 completion (need TCG data first)
- Phase 5 can be done incrementally during other phases
- Phase 6 should be done last

### Risk Areas
- **TCG Data Availability**: May need manual data entry if no API available
- **Card Images**: May require significant storage or bandwidth
- **Performance**: Large number of images may impact performance
- **Data Migration**: Existing users may have party data to migrate

---

**Last Updated**: Based on transformation request  
**Status**: Planning Phase - Ready for Implementation

