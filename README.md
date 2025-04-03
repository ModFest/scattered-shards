<!--suppress HtmlDeprecatedTag, XmlDeprecatedElement -->
<center><img alt="mod preview" src="https://cdn.modrinth.com/data/DB9GU3tx/images/fa20b10e5056ddf03b3f938f8818c7f0d0bbd2c7.png" /></center>

<center>
Collectible cards for adventure maps!<br/>
Maintained for use at <a href="https://modfest.net">ModFest</a> events.<br/>
Requires <a href="https://modrinth.com/mod/connector">Connector</a> and <a href="https://modrinth.com/mod/forgified-fabric-api">FFAPI</a> on (neo)forge.<br/>
</center>

---

**Scattered Shards** adds a system of collectible "shards" that can be created via a UI and placed in-world.

Type `/shards` or use a *Shard Tablet* any time to view shows obtained and missing shards. There is also a keybind to open the tablet;
it is not bound by default, so you may want to create a default setting for your modpack!

**Features:**
- Shards can have titles, descriptions, and face art in the form of item stacks or arbitrary textures
  - Shards can also have a "hint", which is shown in standard galactic until moused over
- Shards have a "type" e.g. clubs, foil, rare, secret, difficult (data-driven)
  - Shard types can have custom names, textures, and collection sounds (resource-driven)
- Shards are arranged into "sets" which can have one shard of each type
	- Suggested as **installed mod IDs** by default, but can be completely arbitrary (locations, skillsets, etc)

![shards screen preview](https://cdn.modrinth.com/data/DB9GU3tx/images/ba00e12bef9b8d90d096a71bba11d71c14f6e01f.png)

### Shard Creation

- Run `/shardc creator new [set] [type]` to open the shard creator - set can be an arbitary ID
  - In the shard creator, you can fill in the title, description, hint, and icon for the shard
  - If you used an installed mod ID for the set, you can enable "use mod icon" to use the mod icon
- To allow regular players to collect the shard, either:
  - Run `/shard block [shard]` and place the provided block - this collects the shard when walking over
  - Place a command block with e.g. `/shard award @n [shard]` - this collects the shard for the nearest player

![shard creator screen preview](https://cdn.modrinth.com/data/DB9GU3tx/images/a35729532f21b838fcfa91bcf3490cd5bbd6bbec.png)

### Shard Types

See [Wiki: Creating Shard Types](https://github.com/ModFest/scattered-shards/wiki/Creating-Shard-Types) for a tutorial on customizing what types of shards are available to create.

---

This mod was originally created by Falkreon and acikek as a base mod for [BlanketCon '23](https://modfest.net/bc23).<br/>
Feel free to contribute bugfixes and improvements!

### Sound Effects

- ["Victory Fanfare (Light Wills Ever)"](https://freesound.org/people/SilverIllusionist/sounds/669324/) by SilverIllusionist
- ["Healing 5 (Crystalline Respite)"](https://freesound.org/people/SilverIllusionist/sounds/654071/) by SilverIllusionist
- ["Healing 4 (Wave of Vigour)"](https://freesound.org/people/SilverIllusionist/sounds/654070/) by SilverIllusionist
- ["Level Up/Mission Complete (Cyberpunk Vibe) 2"](https://freesound.org/people/SilverIllusionist/sounds/661240/) by SilverIllusionist
- ["Reversed Crash Cymbal"](https://freesound.org/people/TheEndOfACycle/sounds/674291/) by TheEndOfACycle
- ["Success! Quest Complete! (RPG Sound)](https://freesound.org/people/qubodup/sounds/166540/) by qubodup
