# Java Game Development Tutorial - From Basics to Terraria-like Games

## Table of Contents
1. [Getting Started](#getting-started)
2. [Lesson Structure](#lesson-structure)
3. [Core Concepts](#core-concepts)
4. [Advanced Topics](#advanced-topics)
5. [Building Your Own Terraria](#building-your-own-terraria)

## Getting Started

Welcome to Java game development! This tutorial will take you from absolute beginner to creating a Terraria-like game. Each lesson builds upon the previous one, introducing new concepts gradually.

### Prerequisites
- Java JDK 8 or higher installed
- Basic understanding of Java programming
- Any text editor or IDE (VS Code, IntelliJ, Eclipse)

## Lesson Structure

### 01-BasicWindow (`GameWindow.java`)
**What you'll learn:**
- Creating a game window using Java Swing
- Setting up basic window properties
- Understanding the Event Dispatch Thread

**Key concepts:**
- JFrame basics
- Window properties (size, location, close operation)
- Event-driven programming

### 02-GameLoop (`GameLoop.java`)
**What you'll learn:**
- The fundamental game loop pattern
- Custom rendering with paintComponent()
- Frame rate control
- Basic input handling

**Key concepts:**
- Update-Render loop
- Graphics2D rendering
- KeyListener interface
- Thread management

### 03-Player (`PlayerGame.java`)
**What you'll learn:**
- Object-oriented game design
- Player character implementation
- Physics simulation (gravity, jumping)
- Game state management

**Key concepts:**
- Player class design
- Velocity and acceleration
- Collision detection basics
- State management

### 04-TileWorld (`TileWorldGame.java`)
**What you'll learn:**
- Tile-based world systems
- Camera implementation
- World generation
- Player-world interaction

**Key concepts:**
- 2D arrays for world representation
- Coordinate system conversion
- Camera following
- Mouse input handling

### 05-TerrainGeneration (`TerrainGame.java`)
**What you'll learn:**
- Advanced procedural terrain generation
- Simplex noise for natural landscapes
- Cave generation systems
- Ore placement algorithms

**Key concepts:**
- SimplexNoise class implementation
- Multi-layer terrain generation
- Cave systems using noise functions
- Resource distribution (coal, iron, gold)

### 06-InventorySystem (`InventoryGame.java`)
**What you'll learn:**
- Complete inventory management
- Item stacking and storage
- Hotbar UI system
- Item collection and placement

**Key concepts:**
- ItemStack class design
- Inventory arrays and management
- UI rendering for hotbar
- Mouse-based item interaction

### 07-HealthDamageSystem (`HealthDamageGame.java`)
**What you'll learn:**
- Survival mechanics implementation
- Enemy AI with state machines
- Health and damage systems
- Environmental hazards

**Key concepts:**
- Player health management
- Enemy class with AI states (WANDER, CHASE, ATTACK)
- Damage indicators and visual feedback
- Invulnerability frames

### 08-DayNightCycle (`DayNightGame.java`)
**What you'll learn:**
- Time-based gameplay systems
- Dynamic sky color transitions
- Simple lighting system
- Light propagation and shadows

**Key concepts:**
- TimeSystem class for day/night progression
- LightingSystem with light sources
- Ambient light calculations
- Time-based enemy spawning

### 09-BackgroundLayers (`ParallaxGame.java`)
**What you'll learn:**
- Parallax scrolling for depth perception
- Multi-layered background rendering
- Celestial bodies (sun, moon, stars, clouds)
- Enhanced atmospheric effects

**Key concepts:**
- ParallaxLayer class with different scroll speeds
- Rendering order for layered graphics
- Star twinkling and cloud movement
- Sun/moon arc movement across sky

### 10-LightingEffects (`LightingEffectsGame.java`)
**What you'll learn:**
- Enhanced lighting with LAMP blocks
- Simple shadow rendering
- Warm light glow effects
- Visual atmosphere for caves

**Key concepts:**
- LAMP tile type with maximum light emission
- Gradient glow effects around light sources
- Shadow darkening for solid blocks
- Color tinting for warm lamplight

## Core Concepts

### Game Loop
Every game follows this basic pattern:
```
while (game is running) {
    1. Handle Input
    2. Update Game State
    3. Render Everything
    4. Control Frame Rate
}
```

### Coordinate Systems
- **Screen Coordinates**: What you see on screen (0,0 at top-left)
- **World Coordinates**: Position in the game world
- **Tile Coordinates**: Which tile in the grid

### Collision Detection
For tile-based games like Terraria:
1. Convert player position to tile coordinates
2. Check if player overlaps with solid tiles
3. Prevent movement or adjust position

### Camera System
The camera determines what part of the world is visible:
- Camera position = What part of world to show
- Screen position = World position - Camera position

## Advanced Topics

### Better Terrain Generation
- Perlin noise for natural-looking terrain
- Multiple terrain layers (grass, dirt, stone, ore)
- Cave generation algorithms
- Biome systems

### Enhanced Physics
- More realistic gravity and acceleration
- Friction and air resistance
- Liquid simulation (water, lava)
- Particle systems

### Game Features
- Inventory system
- Crafting mechanics
- Item drops and pickup
- Health and damage system
- Save/load functionality

### Performance Optimization
- Only render visible tiles
- Chunk-based world loading
- Object pooling for particles
- Efficient collision detection

## Building Your Own Terraria

### Phase 1: Foundation (COMPLETED! ✅)
✅ **01-BasicWindow** - Basic window and rendering
✅ **02-GameLoop** - Game loop implementation  
✅ **03-Player** - Player character with movement
✅ **04-TileWorld** - Tile-based world system
✅ **05-TerrainGeneration** - Advanced terrain with caves and ores
✅ **06-InventorySystem** - Complete inventory management
✅ **07-HealthDamageSystem** - Health, damage, and enemy AI
✅ **08-DayNightCycle** - Time system with lighting
✅ **09-BackgroundLayers** - Parallax scrolling and celestial bodies
✅ **10-LightingEffects** - Enhanced lamp lighting and shadows

### Phase 2: Advanced Features (READY TO START! 🚀)
- [ ] Tool system with durability
- [ ] Crafting system and recipes
- [ ] Multiple biomes (desert, snow, jungle)
- [ ] Weather system (rain, snow)
- [ ] NPC system and trading
- [ ] Boss battles

### Phase 3: Polish and Optimization (PLANNED 📋)
- [ ] Better graphics and animations
- [ ] Sound effects and music
- [ ] Save/load functionality
- [ ] Performance optimization
- [ ] Additional game modes

## Code Examples and Explanations

### Creating a Game Window
```java
public class GameWindow extends JFrame {
    public GameWindow() {
        setTitle("My Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setVisible(true);
    }
}
```

### Basic Game Loop
```java
public void run() {
    while (running) {
        update();    // Update game state
        repaint();   // Trigger paintComponent()
        
        // Control frame rate
        Thread.sleep(16); // ~60 FPS
    }
}
```

### Tile-Based Collision
```java
private boolean checkCollision() {
    int tileX = (int) (playerX / TILE_SIZE);
    int tileY = (int) (playerY / TILE_SIZE);
    return world.isSolid(tileX, tileY);
}
```

## 🎮 Features Roadmap

### ✅ Phase 1 — Foundation Systems (COMPLETED!)
- [x] **01-BasicWindow** - Basic game window setup
- [x] **02-GameLoop** - Fundamental game loop and rendering
- [x] **03-Player** - Player character with physics
- [x] **04-TileWorld** - Tile-based world and camera system
- [x] **05-TerrainGeneration** - Procedural terrain with caves and ores
- [x] **06-InventorySystem** - Complete inventory and hotbar UI
- [x] **07-HealthDamageSystem** - Health, damage, and enemy AI
- [x] **08-DayNightCycle** - Time system with dynamic lighting

### 🔧 Phase 2 — Advanced Systems (NEXT UP!)
- [ ] **11-CraftingSystem** - Recipe-based item creation
  ```java
  class CraftingRecipe {
      private Map<TileType, Integer> ingredients;
      private ItemStack result;
      public boolean canCraft(Inventory inventory);
  }
  ```
- [ ] **12-ToolSystem** - Tools with durability and efficiency
  ```java
  class Tool {
      private ToolType type; // PICKAXE, AXE, SHOVEL
      private int durability;
      private float miningSpeed;
  }
  ```
- [ ] **13-MultipleBiomes** - Enhanced terrain variety
  ```java
  enum BiomeType { GRASSLAND, DESERT, SNOW, JUNGLE }
  class BiomeGenerator {
      public BiomeType getBiome(int worldX);
      public TileType getTerrainBlock(BiomeType biome, int depth);
  }
  ```
- [ ] **12-NPCSystem** - Trading and interactions
  ```java
  class NPC {
      private DialogueTree dialogue;
      private Inventory shop;
      private AIBehavior behavior;
  }
  ```

### 🎨 Phase 3 — Polish & Optimization
- [ ] **Enhanced Graphics** - Sprite-based rendering
- [ ] **Sound System** - Audio effects and music
- [ ] **Save/Load** - Persistent game worlds
- [ ] **Performance Optimization** - Chunk loading, culling

### ⚙️ Phase 3 — Advanced Mechanics
- [ ] **Enhanced Procedural Generation**
  - Improved cave systems using cellular automata
  - Ore veins instead of random placement
  - Underground lakes and lava pools
- [ ] **Multiple Biomes** (grassland, desert, snow, jungle)
  ```java
  enum BiomeType { GRASSLAND, DESERT, SNOW, JUNGLE }
  class BiomeGenerator {
      public BiomeType getBiome(int worldX) {
          // Use noise functions to determine biome
      }
  }
  ```
- [ ] **Weather System** (rain, thunder, snow)
- [ ] **Save/Load World** - Persistent game worlds
  ```java
  class WorldSerializer {
      public void saveWorld(World world, String filename);
      public World loadWorld(String filename);
  }
  ```
- [ ] **NPC System** - Trading and interactions
- [ ] **Crafting Recipes** - Combine items to create new ones
- [ ] **Particle Effects** - Visual feedback for actions
- [ ] **Ambient Sounds** - Audio atmosphere

### 🚀 Phase 4 — Unique Twist (Your Creative Ideas!)

Choose one or combine multiple unique features:

#### 🔮 **Time-Travel Biomes**
- Different eras accessible through portals
- Prehistoric (dinosaurs), Medieval (castles), Future (tech blocks)
- Items from one era affect others

#### 🌱 **Self-Healing World**
- World slowly regenerates destroyed areas
- Players must balance destruction with preservation
- Some areas heal faster than others

#### 🧩 **Puzzle Biomes**
- Areas that require solving puzzles to access resources
- Moving platforms, pressure plates, logic gates
- Rewards rare materials for clever solutions

#### 🌊 **Fluid Dynamics**
- Realistic water and lava flow
- Flooding mechanics and drainage systems
- Steam generation when water meets lava

#### 🏗️ **Modular Building System**
- Advanced construction with pipes, wires, mechanisms
- Functional machines and automation
- Player-created contraptions

## 🎯 What We've Accomplished So Far

### ✅ **All 10 Lessons Complete!** 
You've successfully built a fully functional Terraria-like game with:

#### **Core Systems:**
- **Game Window & Loop** - Professional game architecture
- **Player Physics** - Gravity, jumping, collision detection
- **Tile-Based World** - Infinite-feeling game world
- **Camera System** - Smooth following camera

#### **World Generation:**
- **Procedural Terrain** - Using SimplexNoise for realistic landscapes
- **Cave Systems** - Underground exploration areas
- **Ore Distribution** - Coal, iron, and gold ore placement
- **Multiple Tile Types** - Grass, dirt, stone, sand, lava, torches, lamps

#### **Gameplay Features:**
- **Complete Inventory** - 36-slot inventory with 9-slot hotbar
- **Item Management** - Stacking, collection, placement systems
- **Health & Combat** - 100 HP system with damage and regeneration
- **Enemy AI** - Intelligent enemies with WANDER/CHASE/ATTACK states
- **Day/Night Cycle** - 2-minute cycles with extended nights (60% night time)
- **Dynamic Lighting** - Light sources, propagation, and shadows
- **LAMP Blocks** - Placeable light sources with warm glow effects

#### **Visual Polish:**
- **Dynamic Sky Colors** - Realistic day/night transitions with darker nights (RGB 5,5,15)
- **Celestial Bodies** - Animated sun, moon, twinkling stars, and moving clouds
- **Parallax Backgrounds** - 4 layers of depth with varying scroll speeds
- **Lighting Effects** - Torches, lava, and lamps emit light
- **Lamp Glow Effects** - Warm gradient circles around lamp blocks
- **Simple Shadows** - Darkness overlay on unlit solid blocks
- **Health Bar UI** - Visual health representation
- **Hotbar Interface** - Item selection and management
- **Damage Indicators** - Visual feedback for combat

### 🎮 **Current Game Features:**
- **Movement:** WASD/Arrow keys for movement, Space to jump
- **Building:** Left-click to break blocks, right-click to place
- **Inventory:** Numbers 1-9 to select hotbar slots, E to view full inventory
- **Survival:** Health system with environmental hazards
- **Combat:** Fight enemies that spawn more frequently at night
- **Exploration:** Caves, ores, and varied terrain to discover
- **Lighting:** Place lamps to light up dark caves with warm glow effects
- **Atmosphere:** Watch the sun and moon traverse the sky, stars twinkle, clouds drift by

## Implementation Priority Guide

### 🚀 Ready for Phase 2! Choose Your Next Feature:

1. **Crafting System** (Recommended Next)
   - Combine items to create tools and equipment
   - Recipe system for complex items
   - Workbench mechanics

2. **Tool System** 
   - Different tools for different blocks
   - Tool durability and upgrading
   - Mining speed improvements

3. **Multiple Biomes**
   - Desert, snow, jungle environments
   - Biome-specific blocks and enemies
   - Varied terrain generation

4. **Enhanced Lighting**
   - Colored lighting effects
   - Light-based puzzles
   - Improved shadow rendering

### Code Examples from Our Journey:

#### Advanced Inventory System (Lesson 6):
```java
class ItemStack {
    private TileType itemType;
    private int quantity;
    private int maxStackSize;
    
    public boolean canAddMore() {
        return quantity < maxStackSize;
    }
    
    public void addQuantity(int amount) {
        quantity = Math.min(maxStackSize, quantity + amount);
    }
}
```

#### Enemy AI Implementation (Lesson 7):
```java
class Enemy {
    enum AIState { WANDER, CHASE, ATTACK }
    
    private void updateAI() {
        float distToPlayer = distance(x, y, target.getX(), target.getY());
        
        switch (state) {
            case WANDER:
                if (distToPlayer < CHASE_RANGE) {
                    state = AIState.CHASE;
                }
                break;
            case CHASE:
                if (target.getX() < x) {
                    velocityX = -MOVE_SPEED;
                } else {
                    velocityX = MOVE_SPEED;
                }
                break;
        }
    }
}
```

#### Day/Night Cycle with Lighting (Lesson 8):
```java
class TimeSystem {
    private float timeOfDay = 0.5f; // 0.0 = midnight, 0.5 = noon
    
    public Color getSkyColor() {
        if (timeOfDay >= 0.0f && timeOfDay < 0.3f) {
            // Extended night period - darker and scarier!
            float t = timeOfDay / 0.3f;
            return interpolateColor(new Color(5, 5, 15), new Color(255, 100, 50), t);
        }
        // ... more time periods
    }
    
    public boolean isNight() {
        return timeOfDay < 0.3f || timeOfDay > 0.7f; // 60% night time
    }
}

// Celestial bodies for atmosphere
class CelestialBodies {
    private Star[] stars = new Star[100];
    private Cloud[] clouds = new Cloud[8];
    
    public void drawSun(Graphics2D g2d, float timeOfDay) {
        float angle = (float) (timeOfDay * Math.PI * 2 - Math.PI / 2);
        int sunX = (int) (centerX + Math.cos(angle) * radius);
        int sunY = (int) (centerY + Math.sin(angle) * radius);
        // Draw sun with warm glow
    }
    
    public void drawStars(Graphics2D g2d, boolean isNight) {
        if (isNight) {
            for (Star star : stars) {
                star.twinkle(); // Animated twinkling effect
                star.draw(g2d);
            }
        }
    }
}
```

#### Parallax Background System (Lesson 9):
```java
class ParallaxLayer {
    private float scrollSpeed;
    private Color color;
    private int height;
    
    public void draw(Graphics2D g2d, float cameraX) {
        // Offset based on scroll speed for depth effect
        float offsetX = cameraX * scrollSpeed;
        g2d.setColor(color);
        g2d.fillRect((int) -offsetX, startY, width, height);
    }
}

// Multiple layers create depth illusion
ParallaxLayer[] layers = {
    new ParallaxLayer(0.1f, darkColor),    // Farthest - slowest
    new ParallaxLayer(0.25f, mediumColor), // Middle layer
    new ParallaxLayer(0.5f, lightColor),   // Closer - faster
    new ParallaxLayer(0.75f, nearColor)    // Nearest - fastest
};
```

#### Lamp Lighting Effects (Lesson 10):
```java
// LAMP tile in TileType enum
LAMP(10, true, 15, new Color(255, 220, 150)),

class LightingSystem {
    // Apply warm glow to lamp-lit areas
    public Color applyLampGlow(Color baseColor, int x, int y) {
        if (world.getTile(x, y) == TileType.LAMP) {
            // Add warm orange tint
            return new Color(
                Math.min(255, baseColor.getRed() + 30),
                Math.min(255, baseColor.getGreen() + 15),
                baseColor.getBlue()
            );
        }
        return baseColor;
    }
}

// Draw gradient glow around lamps
private void drawLampGlow(Graphics2D g2d) {
    for (int x = 0; x < worldWidth; x++) {
        for (int y = 0; y < worldHeight; y++) {
            if (world.getTile(x, y) == TileType.LAMP) {
                // 3-layer gradient for smooth glow
                for (int i = 2; i >= 0; i--) {
                    int radius = 16 + (i * 16); // 48, 32, 16 pixels
                    int alpha = 15 + (i * 10);  // Fading alpha
                    g2d.setColor(new Color(255, 180, 80, alpha));
                    g2d.fillOval(screenX - radius, screenY - radius, 
                                radius * 2, radius * 2);
                }
            }
        }
    }
}

// Simple shadow rendering
private void drawSimpleShadows(Graphics2D g2d) {
    if (world.isShadowCaster(x, y) && lightLevel < 5) {
        g2d.setColor(new Color(0, 0, 0, 100)); // Dark overlay
        g2d.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
    }
}
```

#### Lighting System Implementation:
```java
class LightingSystem {
    private int[][] lightLevels;
    
    public void calculateLighting(float ambientLight) {
        addSunlight((int) (15 * ambientLight));
        addBlockLight(); // Torches, lava
        propagateLight(); // Spread light to adjacent tiles
    }
    
    public Color applyLighting(Color baseColor, int lightLevel, float ambientLight) {
        float totalLight = Math.max(lightLevel / 15.0f, ambientLight * 0.3f);
        return new Color(
            (int) (baseColor.getRed() * totalLight),
            (int) (baseColor.getGreen() * totalLight),
            (int) (baseColor.getBlue() * totalLight)
        );
    }
}
```

## Next Steps

1. **Choose Your Phase 2 Feature**: Pick one feature that excites you most
2. **Plan Implementation**: Break it down into small, manageable steps
3. **Code Incrementally**: Add one small piece at a time
4. **Test Frequently**: Make sure each addition works before moving on
5. **Iterate**: Improve and refine based on what you learn

## Resources

- [Oracle Java Documentation](https://docs.oracle.com/javase/)
- [Java 2D Graphics Tutorial](https://docs.oracle.com/javase/tutorial/2d/)
- [Game Programming Patterns](http://gameprogrammingpatterns.com/)
- [Terraria Wiki](https://terraria.wiki.gg/) - For game mechanics inspiration

## Tips for Success

1. **Start Simple**: Don't try to build everything at once
2. **Understand Before Moving On**: Make sure you understand each concept
3. **Experiment**: Change values and see what happens
4. **Ask Questions**: Don't hesitate to research when stuck
5. **Have Fun**: Game development should be enjoyable!

Remember: Every professional game developer started with simple projects like these. The key is to keep learning and building!