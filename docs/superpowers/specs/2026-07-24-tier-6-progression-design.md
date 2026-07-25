# Tier 6 Progression Design

## Goal

Extend Mana and Artifice 3.1.0.7 on Minecraft 1.20.1 so MnAGnosis supports a real Tier 6 without modifying Mana and Artifice itself. Tier 5 players must see "Defeat Odin" in the Oculus, receive progression credit from the existing `mna:boss/defeat_odin` advancement, and reach Tier 6 by repeating their existing faction tier-up action.

## Player-visible behavior

- Player progression accepts and persists tiers 1 through 6.
- The `/mna progression tier` command accepts 6.
- At Tier 5, the Oculus shows a single requirement: defeat Odin.
- Earning the existing Odin-defeat advancement marks that Tier 5 requirement complete.
- After completing the requirement, the player's normal faction progression action advances Tier 5 to Tier 6:
  - Council: perform the Ancient Council ritual.
  - Fey: complete the Faerie Queen faction interaction.
  - Demons: complete the Demon Lord faction interaction.
  - Undead: complete the Cold Dark faction ritual/effect.
- Tier 6 is the terminal tier. The Oculus shows the normal maximum-tier message.
- Tier 6 adds no new maximum-health modifier; the cumulative Tier 1 through Tier 5 modifiers remain in effect.
- Tier 6 inherits Mana and Artifice's configured Tier 5 spell-complexity limit.

## Architecture

Use narrow Mixin injections at the semantic tier ceilings instead of overwriting complete Mana and Artifice methods. A small MnAGnosis progression-rules class owns the value `6` and terminal-tier predicates so every injection uses the same definition.

The compatibility layer has four responsibilities:

1. Core state: change only the upper clamp argument in `PlayerProgression.setTier` and supply Tier 5's configured complexity limit at Tier 6.
2. Progression discovery: allow Mana and Artifice's advancement handler to track and restore progression steps while a player is Tier 5.
3. Presentation and administration: treat Tier 6 as maximum in the Oculus and accept 6 in the M&A command.
4. Faction completion: lift the Tier 5 ceiling in the Council, Fey, and Demon advancement paths and guard the Undead path so it advances at most to Tier 6.

The existing five-iteration health loops and five-entry `Tier_Health_Boost_IDs` array remain unchanged. MnAGnosis will not reflectively replace final API fields.

## Data flow

MnAGnosis supplies a Tier 5 `mna:progression-condition` recipe whose `advancement` is `mna:boss/defeat_odin`. Mana and Artifice's recipe manager exposes that condition to both the Oculus and `ProgressionEventHandler`.

When the player earns the Odin advancement, the widened event-handler ceiling records the MnAGnosis condition ID on the progression capability. The faction ritual/action already checks the capability's tier-progress readiness before reaching its advancement branch. The faction compatibility mixin then permits the existing `current tier + 1` call at Tier 5. `PlayerProgressionMixin` clamps the requested value to 6, clears the completed Tier 5 steps, triggers normal synchronization/advancements, and leaves existing health bonuses intact.

## Injection targets

- `PlayerProgression.setTier`: modify only argument index 2 of `MathUtils.clamp(int, int, int)`.
- `PlayerProgression.getTierMaxComplexity`: return `GeneralConfigValues.Tier5ComplexityLimit` only when the stored tier is 6.
- `CommandMna.progressionCommands`: change the one tier-command upper-bound constant from 5 to 6.
- `ProgressionEventHandler.onPlayerAdvancement` and `confirmExistingAdvancements`: change their terminal-tier comparisons from 5 to 6.
- `GuiOcculus.init`, `renderBg`, and `renderLabels`: change their progression maximum comparison from 5 to 6.
- `AncientCouncil` completion callback, `FaerieQueen.interactAt`, and `DemonLord.interactAt`: change their advancement ceiling from 5 to 6.
- `EffectColdDark.removeAttributeModifiers`: redirect the progression `setTier` call through a guard that refuses values above 6.

Every injection is required and constrained to its named method and target. A changed upstream method therefore fails fast at startup instead of silently disabling Tier 6.

## Compatibility and edge cases

- Loading NBT or receiving a sync packet with tier 6 works because both paths call the transformed `setTier`.
- Requests below 1 or above 6 remain clamped.
- Calling a faction action at Tier 6 cannot advance, clear progression, or emit a false Tier 7 transition.
- A Tier 5 player who defeated Odin before installing MnAGnosis receives credit on login through `confirmExistingAdvancements`.
- Players retain all Tier 1 through Tier 5 health modifiers at Tier 6, with no array access at index 5.
- Tier 5 keeps its existing configured complexity behavior, and Tier 6 inherits the same value.
- Existing boss, raid, recipe, rote-spell, and network calculations already consume the integer tier generically and need no patch.

## Testing

Forge GameTests will exercise the transformed Mana and Artifice classes in a live modded server:

- Tier 6 can be assigned with a real mock player without a health-array exception.
- A request for Tier 7 clamps to Tier 6.
- Tier 6 complexity equals the configured Tier 5 complexity.
- Reapplying a terminal faction advancement cannot produce Tier 7 state.

Data tests will load the Tier 5 progression condition through Minecraft's recipe manager and assert that it has tier 5 and references `mna:boss/defeat_odin`.

Verification also includes:

- `compileJava` and resource processing, proving all Mixin targets and JSON compile.
- `runGameTestServer`, proving the live transformations and data behavior.
- A bounded client startup smoke test, proving the client-only Oculus mixin applies successfully.

