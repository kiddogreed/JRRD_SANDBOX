# 🩺 Health and Damage System - Lesson 7

## 🎯 **What's New in This Lesson**

This builds on your inventory system and adds comprehensive **survival mechanics**! Your game now has:

### ✨ **New Features Added:**

#### 1. 🩺 **Player Health System**
- **Health Points:** 100 HP maximum
- **Damage Cooldown:** 1-second invincibility after taking damage
- **Health Regeneration:** Automatically heals 5 HP every 5 seconds
- **Visual Feedback:** Player flashes when invulnerable
- **Death & Respawn:** Player respawns at starting position when health reaches 0

#### 2. 👾 **Enemy AI System**
- **Simple Enemies:** Red rectangular enemies that spawn around the world
- **AI States:** 
  - **WANDER** - Random movement when player is far away
  - **CHASE** - Actively pursues player when within 80 pixels
  - **ATTACK** - Deals 20 damage when touching player
- **Enemy Physics:** Enemies have gravity, collision detection, and can jump over obstacles
- **Enemy Health:** Enemies have 50 HP and can be killed by clicking on them (25 damage per hit)
- **Smart Behavior:** Enemies jump over walls while chasing the player

#### 3. 🔥 **Environmental Hazards**
- **Lava Tiles:** New orange blocks that deal 10 damage when touched
- **Automatic Damage:** Standing in lava continuously damages the player
- **Visual Warning:** Lava has a distinct orange color to warn players

#### 4. 📊 **Visual Feedback Systems**
- **Health Bar:** Shows current/max health with color coding (green → yellow → red)
- **Damage Indicators:** Floating "-20" numbers appear when damage is dealt
- **Invulnerability Indicator:** "INVULNERABLE" text appears during damage cooldown
- **Enemy Health Bars:** Small health bars above each enemy

#### 5. 🎮 **Enhanced Gameplay**
- **Combat System:** Click on enemies to attack them (like mining blocks)
- **Survival Challenge:** Avoid enemies and environmental hazards
- **Risk/Reward:** Deep areas have lava but also valuable ores

---

## 🎮 **How to Play**

### Controls:
- **A/D or Arrow Keys:** Move left/right
- **Space/W:** Jump  
- **1-9 Keys:** Select hotbar slots
- **Left Click:** Break blocks OR attack enemies
- **Right Click:** Place blocks from inventory

### Survival Tips:
1. **Avoid Red Enemies** - They chase and attack you!
2. **Stay Away from Lava** - Orange blocks deal continuous damage
3. **Use Your Invincibility** - After taking damage, you have 1 second to escape
4. **Health Regenerates** - Wait 5 seconds after combat to start healing
5. **Fight Back** - Click on enemies to damage them

---

## 💻 **Technical Implementation Details**

### Health System Architecture:
```java
// Player health fields
private int health = 100;
private int maxHealth = 100;
private long lastDamageTime = 0;
private long lastRegenTime = 0;
private boolean isInvulnerable = false;

// Core health methods
public boolean takeDamage(int damage) {
    if (!isInvulnerable) {
        health = Math.max(0, health - damage);
        isInvulnerable = true;
        lastDamageTime = System.currentTimeMillis();
        return true; // Damage was dealt
    }
    return false; // No damage (invulnerable)
}

public void heal(int healAmount) {
    health = Math.min(maxHealth, health + healAmount);
}
```

### Enemy AI System:
```java
enum AIState { WANDER, CHASE, ATTACK }

private void updateAI() {
    float distToPlayer = distance(x, y, target.getX(), target.getY());
    
    switch(state) {
        case WANDER -> {
            if (distToPlayer < CHASE_RANGE) state = AIState.CHASE;
            // Random movement
        }
        case CHASE -> {
            if (distToPlayer > CHASE_RANGE * 1.5) state = AIState.WANDER;
            else if (distToPlayer < ATTACK_RANGE) state = AIState.ATTACK;
            // Move toward player, jump over obstacles
        }
        case ATTACK -> {
            if (distToPlayer > ATTACK_RANGE) state = AIState.CHASE;
            // Deal damage when touching player
        }
    }
}
```

### Environmental Damage:
```java
// Check if player is standing in damaging tiles
private void checkEnvironmentalDamage() {
    int tileX = (int) ((x + width / 2) / World.TILE_SIZE);
    int tileY = (int) ((y + height / 2) / World.TILE_SIZE);
    
    if (world.isDamaging(tileX, tileY)) {
        takeDamage(10); // Lava damage
    }
}
```

---

## 🎯 **Key Learning Concepts**

### 1. **Game State Management**
- Managing multiple timers (damage cooldown, health regen)
- Tracking boolean states (invulnerability, on ground)
- Coordinating different systems (health, physics, AI)

### 2. **AI Programming Basics**
- **State Machines:** Clean way to manage enemy behavior
- **Decision Making:** Distance-based state transitions
- **Pathfinding:** Simple obstacle jumping
- **Target Tracking:** Following the player smoothly

### 3. **Visual Feedback Design**
- **Immediate Feedback:** Damage numbers, health bars
- **Status Indicators:** Invulnerability effects
- **Color Coding:** Health bar colors, enemy colors
- **Animation:** Floating damage indicators

### 4. **Game Balance Considerations**
- **Damage Values:** How much damage should enemies/environment deal?
- **Timing:** How long should invincibility last?
- **Regeneration:** How fast should health recover?
- **Enemy Spawn:** How many enemies make the game challenging but not impossible?

---

## 🚀 **What's Next?**

Now that you have health and damage working, you can add:

### Immediate Improvements:
1. **Different Enemy Types** - Ranged enemies, flying enemies, stronger enemies
2. **Weapons/Tools** - Swords that deal more damage than clicking
3. **Armor System** - Equipment that reduces damage taken
4. **Food/Healing Items** - Consumables that restore health

### Advanced Features:
1. **Status Effects** - Poison, burning, speed boosts
2. **Boss Enemies** - Large enemies with special attacks
3. **Sound Effects** - Audio feedback for damage, healing, combat
4. **Particle Effects** - Blood splashes, hit sparks, healing auras

---

## 🏆 **Achievement Unlocked!**

**Congratulations!** You've successfully implemented:
- ✅ **Survival Mechanics** - Health, damage, death, respawn
- ✅ **AI Programming** - Enemy behavior and pathfinding  
- ✅ **Combat System** - Player vs enemy interactions
- ✅ **Environmental Hazards** - Dangerous world elements
- ✅ **Visual Feedback** - UI elements and damage indicators

You now have the foundation for any **action/survival game**! This is the same type of system used in:
- Terraria (health, enemies, environmental damage)
- Minecraft (health, mobs, lava damage)
- Stardew Valley (health, combat, monsters)
- Any survival/action game

---

## 🎮 **Try These Challenges**

1. **Modify Enemy Behavior** - Make some enemies faster or stronger
2. **Add New Hazards** - Poisonous water, spike traps, falling rocks
3. **Create Different Enemy Types** - Flying enemies, ranged attackers
4. **Implement Weapons** - Swords, bows, magic spells
5. **Add Sound Effects** - Damage sounds, enemy sounds, ambient audio

---

**Your game is becoming more and more like a real indie game!** 🎉

Every feature you add teaches you fundamental game development concepts that professional developers use every day.

**What survival feature would you like to add next?** 🤔