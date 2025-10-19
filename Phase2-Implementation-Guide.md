## 🚀 Phase 2 Implementation Guide - Starting Your Terraria Clone

This guide will help you implement the Phase 2 features step by step. Pick any feature that interests you most!

### 🎒 1. INVENTORY SYSTEM (Recommended First Feature)

The inventory system is the foundation for most other features. Here's how to implement it:

#### Step 1: Create Item Classes

```java
// ItemStack.java - Represents a stack of items
class ItemStack {
    private TileType itemType;
    private int quantity;
    private int maxStackSize;
    
    public ItemStack(TileType type, int quantity) {
        this.itemType = type;
        this.quantity = quantity;
        this.maxStackSize = getMaxStackSize(type);
    }
    
    private int getMaxStackSize(TileType type) {
        switch (type) {
            case DIRT:
            case STONE:
            case SAND:
                return 64; // Most blocks stack to 64
            case COAL_ORE:
            case IRON_ORE:
            case GOLD_ORE:
                return 32; // Ores stack to 32
            default:
                return 1;  // Tools and special items don't stack
        }
    }
    
    public boolean canAddMore() {
        return quantity < maxStackSize;
    }
    
    public void addQuantity(int amount) {
        quantity = Math.min(maxStackSize, quantity + amount);
    }
    
    public void removeQuantity(int amount) {
        quantity = Math.max(0, quantity - amount);
    }
    
    // Getters
    public TileType getItemType() { return itemType; }
    public int getQuantity() { return quantity; }
    public boolean isEmpty() { return quantity <= 0; }
}
```

#### Step 2: Add Inventory to Player

```java
// Add these fields to your Player class
private ItemStack[] inventory = new ItemStack[36]; // 4 rows of 9 slots
private ItemStack[] hotbar = new ItemStack[9];     // Quick access bar
private int selectedSlot = 0;

// Add these methods to Player class
public void collectItem(TileType itemType, int quantity) {
    // First try to add to existing stacks
    for (int i = 0; i < inventory.length; i++) {
        if (inventory[i] != null && 
            inventory[i].getItemType() == itemType && 
            inventory[i].canAddMore()) {
            
            int canAdd = Math.min(quantity, 
                inventory[i].getMaxStackSize() - inventory[i].getQuantity());
            inventory[i].addQuantity(canAdd);
            quantity -= canAdd;
            
            if (quantity <= 0) return; // All items added
        }
    }
    
    // Add to empty slots
    for (int i = 0; i < inventory.length; i++) {
        if (inventory[i] == null && quantity > 0) {
            inventory[i] = new ItemStack(itemType, 
                Math.min(quantity, ItemStack.getMaxStackSize(itemType)));
            quantity -= inventory[i].getQuantity();
        }
    }
}

public ItemStack getSelectedItem() {
    if (selectedSlot < hotbar.length && hotbar[selectedSlot] != null) {
        return hotbar[selectedSlot];
    }
    return null;
}

public void selectSlot(int slot) {
    if (slot >= 0 && slot < hotbar.length) {
        selectedSlot = slot;
    }
}
```

#### Step 3: Modify World Interaction

```java
// In your main game class, modify the mouse click handler:
@Override
public void mouseClicked(MouseEvent e) {
    int worldX = (e.getX() + camera.getX()) / World.TILE_SIZE;
    int worldY = (e.getY() + camera.getY()) / World.TILE_SIZE;
    
    if (e.getButton() == MouseEvent.BUTTON1) {
        // Left click - break block and add to inventory
        TileType tileToBreak = world.getTile(worldX, worldY);
        if (tileToBreak != TileType.AIR) {
            world.setTile(worldX, worldY, TileType.AIR);
            player.collectItem(tileToBreak, 1); // Add to inventory!
        }
    } else if (e.getButton() == MouseEvent.BUTTON3) {
        // Right click - place block from inventory
        ItemStack selectedItem = player.getSelectedItem();
        if (selectedItem != null && world.getTile(worldX, worldY) == TileType.AIR) {
            world.setTile(worldX, worldY, selectedItem.getItemType());
            selectedItem.removeQuantity(1);
            if (selectedItem.isEmpty()) {
                // Remove empty stack from hotbar
                hotbar[selectedSlot] = null;
            }
        }
    }
}
```

#### Step 4: Add Hotbar UI

```java
// Add this method to your main game class
private void drawHotbar(Graphics2D g2d) {
    int hotbarY = WINDOW_HEIGHT - 60;
    int slotSize = 40;
    int spacing = 5;
    int startX = (WINDOW_WIDTH - (9 * slotSize + 8 * spacing)) / 2;
    
    for (int i = 0; i < 9; i++) {
        int slotX = startX + i * (slotSize + spacing);
        
        // Draw slot background
        if (i == player.getSelectedSlot()) {
            g2d.setColor(Color.YELLOW); // Highlight selected slot
        } else {
            g2d.setColor(Color.GRAY);
        }
        g2d.fillRect(slotX, hotbarY, slotSize, slotSize);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(slotX, hotbarY, slotSize, slotSize);
        
        // Draw item in slot
        ItemStack item = player.getHotbarItem(i);
        if (item != null) {
            // Draw item (simplified - just use tile color)
            g2d.setColor(item.getItemType().getColor());
            g2d.fillRect(slotX + 5, hotbarY + 5, slotSize - 10, slotSize - 10);
            
            // Draw quantity
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(String.valueOf(item.getQuantity()), 
                          slotX + slotSize - 15, hotbarY + slotSize - 5);
        }
    }
}

// Call this method in your paintComponent method after drawing the world
```

#### Step 5: Add Hotbar Selection Controls

```java
// Add these key handlers to your keyPressed method
case KeyEvent.VK_1: case KeyEvent.VK_2: case KeyEvent.VK_3:
case KeyEvent.VK_4: case KeyEvent.VK_5: case KeyEvent.VK_6:
case KeyEvent.VK_7: case KeyEvent.VK_8: case KeyEvent.VK_9:
    int slot = e.getKeyCode() - KeyEvent.VK_1; // Convert to 0-8
    player.selectSlot(slot);
    break;
```

### 🔨 2. TOOL SYSTEM (Build on Inventory)

Once you have inventory working, tools are the next logical step:

#### Create Tool Classes

```java
enum ToolType {
    HAND(1.0f),      // Default "tool"
    PICKAXE(3.0f),   // Good for stone and ores
    SHOVEL(2.0f),    // Good for dirt and sand
    AXE(2.5f);       // For future wood blocks
    
    private final float miningSpeed;
    
    ToolType(float speed) {
        this.miningSpeed = speed;
    }
    
    public float getMiningSpeed() { return miningSpeed; }
}

class Tool extends ItemStack {
    private ToolType toolType;
    private int durability;
    private int maxDurability;
    
    public Tool(ToolType type) {
        super(TileType.AIR, 1); // Tools don't correspond to tile types
        this.toolType = type;
        this.maxDurability = getMaxDurability(type);
        this.durability = maxDurability;
    }
    
    private int getMaxDurability(ToolType type) {
        switch (type) {
            case PICKAXE: return 100;
            case SHOVEL: return 80;
            case AXE: return 90;
            default: return Integer.MAX_VALUE; // Hand never breaks
        }
    }
    
    public boolean use() {
        if (toolType == ToolType.HAND) return true;
        
        durability--;
        return durability > 0; // Returns false if tool breaks
    }
    
    public float getMiningSpeed(TileType tileType) {
        float baseSpeed = toolType.getMiningSpeed();
        
        // Tool effectiveness based on block type
        switch (toolType) {
            case PICKAXE:
                if (tileType == TileType.STONE || 
                    tileType == TileType.COAL_ORE || 
                    tileType == TileType.IRON_ORE || 
                    tileType == TileType.GOLD_ORE) {
                    return baseSpeed;
                }
                return baseSpeed * 0.5f; // Less effective on other blocks
                
            case SHOVEL:
                if (tileType == TileType.DIRT || tileType == TileType.SAND) {
                    return baseSpeed;
                }
                return baseSpeed * 0.3f;
                
            default:
                return baseSpeed;
        }
    }
}
```

### ❤️ 3. HEALTH SYSTEM (Simple but Essential)

```java
// Add to Player class
private int health = 100;
private int maxHealth = 100;
private long lastDamageTime = 0;
private static final long DAMAGE_COOLDOWN = 1000; // 1 second invincibility

public void takeDamage(int damage) {
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastDamageTime > DAMAGE_COOLDOWN) {
        health = Math.max(0, health - damage);
        lastDamageTime = currentTime;
        
        if (health <= 0) {
            die();
        }
    }
}

public void heal(int healAmount) {
    health = Math.min(maxHealth, health + healAmount);
}

private void die() {
    // Reset player position to spawn
    x = World.TILE_SIZE * 100;
    y = World.TILE_SIZE * 10;
    health = maxHealth;
    
    // Could also: drop items, show death screen, etc.
}

// Add health bar to UI
private void drawHealthBar(Graphics2D g2d) {
    int barWidth = 200;
    int barHeight = 20;
    int x = 10;
    int y = 100;
    
    // Background
    g2d.setColor(Color.DARK_GRAY);
    g2d.fillRect(x, y, barWidth, barHeight);
    
    // Health bar
    float healthPercent = (float) health / maxHealth;
    g2d.setColor(Color.RED);
    g2d.fillRect(x, y, (int) (barWidth * healthPercent), barHeight);
    
    // Border
    g2d.setColor(Color.WHITE);
    g2d.drawRect(x, y, barWidth, barHeight);
    
    // Text
    g2d.drawString(health + "/" + maxHealth, x + 5, y + 15);
}
```

### 🎯 Quick Start Tips

1. **Start with Inventory** - It's the most useful and will make testing other features easier
2. **Test Each Addition** - Make sure each feature works before moving to the next
3. **Keep It Simple** - Don't try to make everything perfect the first time
4. **Use Debugging** - Add print statements to see what's happening
5. **Save Frequently** - Back up your working code before making big changes

### 🔧 Common Issues and Solutions

**Problem**: Items don't appear in inventory
**Solution**: Make sure you're calling `player.collectItem()` in your mouse click handler

**Problem**: Hotbar doesn't update
**Solution**: Check that you're synchronizing hotbar with inventory (they should reference the same ItemStack objects)

**Problem**: Game feels slow
**Solution**: Only draw UI elements that have changed, don't redraw everything every frame

### 🎮 Testing Your Features

Create a simple test world to verify your features:
1. Place different block types
2. Break them and check inventory
3. Try placing from inventory
4. Test hotbar selection with number keys
5. For tools: test mining speed differences
6. For health: add a simple damage trigger (like touching lava)

Once you have one Phase 2 feature working well, you can move on to the next one. Each feature will teach you something new about game development!

### 📚 Next Feature Recommendations

After implementing inventory:
1. **Tools** (builds directly on inventory)
2. **Health** (independent, good for learning game state)
3. **Day/Night** (visual effects, time systems)
4. **Enemies** (AI, more complex interactions)

Remember: The goal is to learn and have fun. Don't worry about making everything perfect - you can always improve it later!