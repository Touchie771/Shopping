# Shopping

A Minecraft plugin that provides a player-driven marketplace with both fixed-price shops and timed auctions.

## Features

### Shop System
- **Player Shops**: List items for sale at fixed prices
- **Paginated GUI**: Browse items with easy navigation (36 items per page)
- **Automatic Sorting**: Items sorted by price from lowest to highest
- **Item Management**: Remove your listings anytime
- **Economy Integration**: Uses Vault for all transactions

### Auction System
- **Timed Auctions**: List items with custom duration (1 minute to 24 hours)
- **Competitive Bidding**: Players compete with incremental bids
- **Automatic Completion**: Auctions end automatically when time expires
- **Refund System**: Previous bidders are refunded immediately when outbid
- **Offline Support**: Winners receive items even when offline
- **Paginated GUI**: Browse auctions with easy navigation (36 items per page)

## Requirements

- **Minecraft Server**: 1.21 or higher (Paper/Spigot)
- **Java**: 21 or higher
- **Dependencies**: 
  - [Vault](https://www.spigotmc.org/resources/vault.34315/) - Economy provider
  - An economy plugin (e.g., EssentialsX, CMI)

## Installation

1. Download the latest `Shopping-X.X.X-all.jar` from [Releases](https://github.com/Touchie771/Shopping/releases)
2. Place the JAR in your server's `plugins/` folder
3. Ensure Vault and an economy plugin are installed
4. Restart your server
5. Configure permissions (all default to `true`)

## Commands

### Shop Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/shop` | Open the shop menu | `shopping.menu` |
| `/shop sell <price>` | List held item for sale | `shopping.sell` |
| `/shop remove` | Open menu to remove your items | `shopping.remove` |

### Auction Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/auction` (or `/auc`) | Open auction menu | `shopping.auction` |
| `/auction start <price> <minutes>` | Start auction with held item | `shopping.auction.start` |
| `/auction list` | List all active auctions (text) | `shopping.auction.list` |
| `/auction cancel` | Cancel all your auctions | `shopping.auction.cancel` |
| `/auction claim` | Claim won items from offline wins | `shopping.auction.claim` |

## Permissions

All permissions default to `true` (everyone has access):

```yaml
shopping.menu              # View shop
shopping.sell              # Sell items
shopping.remove            # Remove own items
shopping.auction           # View auctions
shopping.auction.start     # Start auctions
shopping.auction.bid       # Bid on auctions
shopping.auction.list      # List auctions
shopping.auction.cancel    # Cancel auctions
shopping.auction.claim     # Claim items
```

## Usage Examples

### Selling in Shop
```
1. Hold the item you want to sell
2. Run: /shop sell 100
3. Item is listed for $100
4. When someone buys it, you receive the money
```

### Starting an Auction
```
1. Hold the item you want to auction
2. Run: /auction start 50 30
3. Auction starts at $50 for 30 minutes
4. Highest bidder wins when time expires
```

### Bidding on Auctions
```
1. Open auction menu: /auction
2. Click on any auction item
3. Automatically bids current price + $1
4. If outbid, you're refunded immediately
```

## Data Storage

The plugin stores data in YAML files:
- `plugins/Shopping/items.yml` - Shop listings
- `plugins/Shopping/auctions.yml` - Active auctions
- `plugins/Shopping/pending_items.yml` - Items waiting to be claimed

## Building from Source

```bash
git clone https://github.com/Touchie771/Shopping.git
cd Shopping
./gradlew build
# Output: build/libs/Shopping-1.0.0-all.jar
```

## Technologies

- **Language**: Java 21
- **Build Tool**: Gradle 8.5
- **Framework**: Paper/Spigot API 1.21
- **Command Library**: [LiteCommands](https://github.com/Rollczi/LiteCommands)
- **Economy**: Vault API
- **Components**: Adventure API

## Support

- **Issues**: [GitHub Issues](https://github.com/Touchie771/Shopping/issues)
- **Wiki**: [Coming Soon]
- **Discord**: [Coming Soon]

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

- **Author**: Touchie771
- **Command Framework**: LiteCommands by Rollczi
- **Economy API**: Vault