# Update Checklist

This checklist is for updating Shopkeepers to support new Paper/Minecraft versions. This branch only supports Paper servers.

## Paper/Minecraft Update

* Add a new CompatVersion entry in the Compat class for the new Paper version.
  * The compat version format is typically `1_X_Y` (e.g., `1_21_12`).
  * Note: For some minor Minecraft updates, the compat version may not necessarily align exactly with the Minecraft version.

* Add a new compat module (subproject) for the new version:
  * Copy an existing compat module (e.g., `modules/v1_21_11`) and rename the module and package folders to match the new version.
  * Update the Paper version inside the `build.gradle.kts` file of the new module (e.g., `extra["craftbukkitVersion"] = "1.21.12-R0.1-SNAPSHOT"`).
  * Add an entry for the module in the root `settings.gradle.kts` file (if using subprojects).
  * Update the CompatProviderImpl class:
    * Package name to match the new version.
    * Output of `#getVersionId()` to match the compat version.
    * Update all NMS version-specific imports and references.
    * Check all NMS version-specific code:
      * Methods or fields might no longer exist or might have been renamed.
      * Paper uses Mojang mappings, so NMS classes use Mojang names.
  * Also update the package names of all NMS module test classes.

* Update the Compat class to load the new provider:
  * Add the new version to the provider loading logic.
  * Update version checks and error messages.

* Update the root `build.gradle.kts`:
  * Update the Paper dev bundle version if needed: `paperweight.paperDevBundle("1.21.12-R0.1-SNAPSHOT")`.
  * Update the Java toolchain version if Paper requires a new JDK version.

* If Paper requires a new JDK version:
  * Update the JDK version in the root `build.gradle.kts` Java toolchain configuration.
  * Update the JDK version inside the GitHub workflow `.github/workflows/build.yml`.

* New mobs:
  * Test if they can be used for shopkeepers.
  * Add a note about potential issues in SKLivingShopObjectTypes.
  * If there are no severe issues, add them to the by default enabled living shop types.
  * Check which equipment slots are supported and adjust EquipmentUtils and
    SKLivingShopObject#getEditableEquipmentSlots.

* New features for new or existing shop objects (mobs, signs, etc.):
  * If not yet existing: Add a new shop object type and register it inside SKLivingShopObjectTypes.
  * Add new editor buttons and messages to the editor menu of the shop object.

* New blocks or items:
  * Check the ItemUtils if there are any material lists or mappings that need to be updated.
    * Containers, chests, shulker boxes, signs, rails.
    * Wool material by dye color, carpet material by dye color.
  * Also check if the supported containers in ShopContainers require changes.

* New enchantments:
  * Check the aliases inside EnchantmentUtils.

* New potion types or items with potion data:
  * Check the aliases and the parsing inside PotionUtils.

* New MerchantRecipe properties:
  * Update the comparator in MerchantUtils.
  * Check the MerchantRecipe constructions in MerchantUtils.

* New explosion result enum values:
  * Check FallbackCompatProvider and CompatProviderImpls and map destroying explosion results correctly.

* If there are major differences, consider dropping support for older Minecraft versions:
  * Remove the corresponding modules:
    * Module folders.
    * Entries in root `settings.gradle.kts` (if using subprojects).
    * CompatVersion entries in Compat class.
  * Update the minimal Paper dependency version inside the `build.gradle.kts` file.
  * Update the `api-version` inside the `paper-plugin.yml` file.
  * Default config file and Settings class: Update the `data-version` setting and all item data settings to match the new lowest supported Minecraft server version.
  * Update the compat module dependency in the `test` module to the new lowest compat module version.
  * Update the Minecraft version-specific test code inside the `main` module. The test cases and the default config might need to be updated (e.g., if there have been changes to Minecraft's or Bukkit's item serialization).
  * Update the code base (optional):
    * Check for legacy data migrations that could be removed now.
    * Check if there are new Paper/Bukkit features that can replace portions of the existing compat-specific code.
    * Use the EntityType enum to get the name of default enabled mobs inside the Settings.
    * Check for TODO notes that mention Bukkit version dependencies.

## On Every Update

* Build and test the new version.
* Make sure the changelog is complete. Fill in the release date.
* Update the version in the root `gradle.properties` file (remove the `-SNAPSHOT` tag if present).
* Commit, build and deploy.
* Add a new git tag for that version.
* Update the version in the root `gradle.properties` file for working on the next version. Add the `-SNAPSHOT` tag if using snapshot versions.
* If not yet done, add a new entry inside the changelog for the next version.
* Commit.

## Update Documentation

* Update the documentation in the `docs/` folder, depending on the changes:
  * `CHANGELOG.md` - Add detailed changelog entry.
  * `features.md` - Document new features.
  * `usage.md` - Document new commands or usage changes.
  * `configuration.md` - Document new config options.
  * `DEBUGGING.md` - Update if debugging features changed.

* Update the project pages, if required:
  * GitHub releases page.
  * Modrinth (if applicable).
  * Other distribution platforms.

* If there have been message changes, update the language files:
  * Update the default language file in `modules/main/src/main/resources/lang/`.
  * Update other language files if maintained.

## Upload

* Write changelog: Update `docs/CHANGELOG.md` with the new version's changes.
* Create a GitHub release with the changelog.
* Upload to distribution platforms as needed.
