# Lesson 11: Crafting System

## Overview
This lesson adds a comprehensive **Crafting System** to the Terraria-like game, allowing players to combine resources to create new items and tools. This is a crucial gameplay mechanic that adds depth and progression to the game.

## What's New in This Lesson

### 1. **Crafting Recipe System**
- **CraftingRecipe Class**: Stores recipe information including:
  - Recipe name
  - Result item and quantity
  - Required ingredients and quantities
  - Workbench requirement flag
  - Methods to check if player can craft and execute crafting

### 2. **Crafting System Manager**
- **CraftingSystem Class**: Manages all crafting recipes
  - Stores collection of all available recipes
  - Filters recipes based on workbench proximity
  - Returns available recipes for current context

### 3. **Player Inventory Integration**
- New Player methods for crafting:
  - `hasItem(TileType, int)`: Check if player has sufficient items
  - `removeItem(TileType, int)`: Consume items for crafting
  - `addItem(TileType, int)`: Add crafted items to inventory
  - `isNearWorkbench()`: Check if player is within 5 tiles of a workbench

### 4. **Crafting UI Menu**
- Toggle with **'C'** key
- Semi-transparent overlay with recipe list
- Visual indicators:
  - **Green background**: Recipe can be crafted (player has ingredients)
  - **Red background**: Missing ingredients
  - **Cyan border**: Currently selected recipe
  - **Yellow text**: Workbench required indicator
- Click on recipes to craft them instantly

### 5. **New Tile Types**
- **WORKBENCH** (ID 11): Brown crafting station block
  - Required to craft advanced items
  - Crafted from 4 Stone (basic recipe)
- **STONE_BRICKS** (ID 12): Gray decorative building block
  - Crafted from 4 Stone at a workbench
  - Provides refined building material

### 6. **Initial Recipe Set**
Four working recipes to start:

1. **Workbench** (Basic Recipe)
   - Ingredients: 4x Stone
   - Result: 1x Workbench
   - Requirement: None (can craft anywhere)
   
2. **Torch x4** (Basic Recipe)
   - Ingredients: 1x Coal + 1x Stone
   - Result: 4x Torch
   - Requirement: None
   
3. **Lamp** (Advanced Recipe)
   - Ingredients: 1x Torch + 2x Iron + 1x Stone
   - Result: 1x Lamp
   - Requirement: Must be near Workbench
   
4. **Stone Bricks x4** (Advanced Recipe)
   - Ingredients: 4x Stone
   - Result: 4x Stone Bricks
   - Requirement: Must be near Workbench

## How to Play

### Controls
- **A/D or Arrow Keys**: Move left/right
- **Space/W/Up**: Jump
- **C**: Open/Close Crafting Menu (NEW!)
- **Mouse Left**: Break blocks
- **Mouse Right**: Place blocks
- **1-9**: Select hotbar slot

### Crafting Workflow
1. **Gather Resources**: Mine blocks to collect materials
2. **Open Crafting Menu**: Press 'C' to view available recipes
3. **Craft Basic Items**: 
   - First, craft a Workbench (4 Stone)
   - Place the Workbench in the world
4. **Unlock Advanced Recipes**:
   - Stand near the placed Workbench
   - Press 'C' - more recipes will appear
5. **Select & Craft**:
   - Click on a recipe to craft (if you have ingredients)
   - Item appears in your inventory immediately

### Tips
- **Workbench is Essential**: Many recipes require being near a workbench
- **Green = Ready**: Green-highlighted recipes can be crafted now
- **Red = Missing Items**: Red-highlighted recipes need more materials
- **Proximity Matters**: Stand within 5 tiles of a workbench for advanced crafting

## Code Architecture

### Class Structure
```
CraftingRecipe
├── Properties: name, result, ingredients, quantities, requiresWorkbench
├── canCraft(Player): Check if player has ingredients
└── craft(Player): Consume ingredients and give result

CraftingSystem
├── List<CraftingRecipe> recipes
├── initializeRecipes(): Set up all available recipes
└── getAvailableRecipes(boolean): Filter by workbench requirement

CraftingGame (Main Class)
├── CraftingSystem craftingSystem
├── boolean craftingMenuOpen
├── int selectedRecipe
├── drawCraftingUI(Graphics2D): Render crafting interface
└── mouseClicked(MouseEvent): Handle recipe selection
```

### Player Helper Methods
```java
// Check if player has specific items
boolean hasItem(TileType type, int quantity)

// Remove items from inventory (for crafting)
void removeItem(TileType type, int quantity)

// Add items to inventory (crafting results)
void addItem(TileType type, int quantity)

// Check proximity to workbench (5-tile radius)
boolean isNearWorkbench()
```

## Compilation & Execution

```bash
# Compile
javac CraftingGame.java

# Run
java CraftingGame
```

## Technical Details

### Recipe Definition
```java
new CraftingRecipe(
    "Recipe Name",
    TileType.RESULT,           // What you get
    resultQuantity,             // How many you get
    new TileType[]{             // What you need
        TileType.INGREDIENT1,
        TileType.INGREDIENT2
    },
    new int[]{quantity1, quantity2}, // How many of each
    requiresWorkbench           // true/false
)
```

### UI Layout
- **Menu Size**: 500x400px centered on screen
- **Recipe Height**: 60px per recipe + 10px spacing
- **Recipe Display**: Name, ingredients, result icon, quantity
- **Color Coding**: Green (craftable), Red (missing items), Cyan (selected)

## Future Enhancements

This system is designed to be easily expandable:
- Add more recipes (weapons, tools, armor, etc.)
- Implement different crafting stations (Furnace, Anvil, etc.)
- Add recipe discovery/unlocking system
- Create recipe categories and filtering
- Add crafting animations and sound effects
- Implement drag-and-drop ingredient placement
- Create recipe tooltips with descriptions

## Connection to Previous Lessons

**Builds Upon:**
- Lesson 1-2: Basic game structure and player movement
- Lesson 3-4: World generation and tile system
- Lesson 5: Inventory system (extended with helper methods)
- Lesson 6-7: Item collection and hotbar
- Lesson 8-9: Enemy system and combat
- Lesson 10: Lighting system (Lamps now craftable!)

**New Additions:**
- CraftingRecipe and CraftingSystem classes
- Crafting UI with mouse interaction
- Player inventory management methods
- Workbench proximity detection
- Recipe filtering system

## Learning Objectives

By completing this lesson, you will understand:
1. ✅ How to design a flexible recipe system
2. ✅ Managing complex UI interactions with mouse clicks
3. ✅ Implementing game progression systems (basic → workbench → advanced)
4. ✅ Integrating new systems with existing inventory
5. ✅ Creating visual feedback for player actions
6. ✅ Organizing recipes into categories (basic vs advanced)
7. ✅ Proximity-based feature unlocking

## Next Steps

With the crafting system in place, you can now:
- **Lesson 12**: Tools & Equipment (pickaxes, swords with durability)
- **Lesson 13**: Save/Load System (persist crafted items)
- **Lesson 14**: Advanced Combat (craftable weapons and armor)
- **Lesson 15**: More Crafting Stations (Furnace for smelting, Anvil for weapons)

---

**Previous Lesson**: [10-LightingEffects](../10-LightingEffects) - Day/night cycle and lighting system  
**Next Lesson**: TBD - Tools & Equipment System

**See Also**: 
- [ANALYSIS-AND-ROADMAP.md](../ANALYSIS-AND-ROADMAP.md) - Full 20-week development roadmap
- [QUICK-REFERENCE.md](../QUICK-REFERENCE.md) - Quick-start guide for next features
