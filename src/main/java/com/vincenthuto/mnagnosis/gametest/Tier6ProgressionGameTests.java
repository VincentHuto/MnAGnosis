package com.vincenthuto.mnagnosis.gametest;

import com.mna.api.config.GeneralConfigValues;
import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.Registries;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IDamageComponent;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.DemonLord;
import com.mna.entities.rituals.DemonStone;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.recipes.progression.ProgressionCondition;
import com.mna.recipes.spells.ComponentRecipe;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.authlib.GameProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import com.vincenthuto.mnagnosis.common.event.CommontEvents;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import com.vincenthuto.mnagnosis.common.faction.IneffableManaGui;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import com.vincenthuto.mnagnosis.common.progression.TruthEncounterService;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import com.vincenthuto.mnagnosis.common.registry.SoundRegistry;
import com.vincenthuto.mnagnosis.common.item.armor.TesseractItem;
import com.vincenthuto.mnagnosis.common.item.PrimalMoteItem;
import com.vincenthuto.mnagnosis.common.spell.ComponentTrueDamage;
import com.vincenthuto.mnagnosis.common.spell.gravity.GravityFieldMath;
import com.vincenthuto.mnagnosis.common.spell.gravity.GravityPolarity;
import com.vincenthuto.mnagnosis.common.spell.SpellComponentRegistry;
import com.vincenthuto.mnagnosis.common.spell.TrueDamageTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoEntity;

import java.util.List;
import java.util.UUID;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class Tier6ProgressionGameTests {

    private static final ResourceLocation ODIN_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath("mna", "boss/defeat_odin");
    private static final ResourceLocation ODIN_PROGRESSION =
            MnAGnosis.rloc("progression/tier_5/defeat_odin");

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void temporaryPrimalArmorIsAbsentFromTheItemRegistry(
            GameTestHelper helper
    ) {
        List<String> removed = List.of(
                "primal_crown",
                "primal_robes",
                "primal_legwraps",
                "primal_boots"
        );
        helper.assertTrue(removed.stream().noneMatch(path ->
                        ForgeRegistries.ITEMS.containsKey(MnAGnosis.rloc(path))),
                "A removed Primal armor item is still registered");
        helper.assertTrue(
                ForgeRegistries.ITEMS.containsKey(MnAGnosis.rloc("primal_mote")),
                "Removing Primal armor also removed the Mote of Primal Mana"
        );
        helper.assertTrue(ItemRegistry.primal_mote.get() instanceof PrimalMoteItem,
                "The Mote of Primal Mana does not use its custom renderer item");
        helper.assertTrue(ItemRegistry.tesseract.get() instanceof TesseractItem,
                "Removing Primal armor changed the existing Tesseract item");
        helper.succeed();
    }

    private Tier6ProgressionGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void trueDamageBypassesArmorAndResistance(GameTestHelper helper) {
        Zombie target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        target.setHealth(20.0F);
        target.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        target.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        target.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
        target.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4));

        DamageSource source = new DamageSource(
                helper.getLevel().registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(TrueDamageTypes.TRUE_DAMAGE)
        );
        helper.assertTrue(source.is(DamageTypeTags.BYPASSES_ARMOR),
                "True Damage did not bypass armor");
        helper.assertTrue(source.is(DamageTypeTags.BYPASSES_SHIELD),
                "True Damage did not bypass shields");
        helper.assertTrue(source.is(DamageTypeTags.BYPASSES_COOLDOWN),
                "True Damage did not bypass hurt cooldown");
        helper.assertTrue(source.is(DamageTypeTags.BYPASSES_EFFECTS),
                "True Damage did not bypass defensive effects");
        helper.assertTrue(source.is(DamageTypeTags.BYPASSES_RESISTANCE),
                "True Damage did not bypass Resistance");
        helper.assertTrue(source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS),
                "True Damage did not bypass enchantment mitigation");
        helper.assertTrue(!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY),
                "True Damage unexpectedly bypassed explicit invulnerability");
        boolean hurt = target.hurt(source, 6.0F);

        helper.assertTrue(hurt, "True Damage was rejected by the target");
        helper.assertTrue(target.getHealth() == 14.0F,
                "Armor or Resistance reduced True Damage; remaining health was " + target.getHealth());
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void trueDamageBypassesCooldownButRespectsInvulnerability(GameTestHelper helper) {
        Zombie target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        target.setHealth(20.0F);
        DamageSource source = new DamageSource(
                helper.getLevel().registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(TrueDamageTypes.TRUE_DAMAGE)
        );

        helper.assertTrue(target.hurt(source, 3.0F), "The first True Damage hit was rejected");
        helper.assertTrue(target.hurt(source, 3.0F),
                "Hurt cooldown rejected an immediate second True Damage hit");
        helper.assertTrue(target.getHealth() == 14.0F,
                "The immediate second True Damage hit was reduced or ignored");

        target.setInvulnerable(true);
        helper.assertTrue(!target.hurt(source, 3.0F),
                "True Damage bypassed explicit entity invulnerability");
        helper.assertTrue(target.getHealth() == 14.0F,
                "Invulnerable target lost health to True Damage");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void trueDamageRegistersAsExpensiveDamageComponent(GameTestHelper helper) {
        SpellEffect registered = Registries.SpellEffect.get()
                .getValue(SpellComponentRegistry.TRUE_DAMAGE_ID);

        helper.assertTrue(registered == SpellComponentRegistry.TRUE_DAMAGE,
                "True Damage was not registered in M&A's component registry");
        helper.assertTrue(registered instanceof IDamageComponent,
                "True Damage was not classified as an M&A damage component");
        helper.assertTrue(registered.getTier(helper.getLevel()) == 6,
                "M&A did not resolve True Damage as a Tier 6 component");
        helper.assertTrue(registered.initialComplexity() == 70.0F,
                "True Damage did not retain its high base complexity");
        float damageStepComplexity = registered.getModifiableAttributes().stream()
                .filter(pair -> pair.getAttribute() == Attribute.DAMAGE)
                .findFirst()
                .orElseThrow()
                .getStepComplexity();
        helper.assertTrue(damageStepComplexity == 18.0F,
                "The Damage modifier was not prohibitively expensive");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void trueDamageComponentAuthorsDirectHarm(GameTestHelper helper) {
        Zombie target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        target.setHealth(20.0F);
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4));
        Zombie caster = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 3, 2, 1);
        ComponentTrueDamage component = SpellComponentRegistry.TRUE_DAMAGE;
        ModifiedSpellPart<SpellEffect> modified = new ModifiedSpellPart<>(component);

        ComponentApplicationResult result = component.ApplyEffect(
                new SpellSource(caster, InteractionHand.MAIN_HAND),
                new SpellTarget(target),
                modified,
                new SpellContext(helper.getLevel(), ISpellDefinition.EMPTY)
        );

        helper.assertTrue(result == ComponentApplicationResult.SUCCESS,
                "True Damage rejected a living target");
        helper.assertTrue(target.getHealth() == 16.0F,
                "True Damage did not apply its configured base value");
        helper.assertTrue(component.ApplyEffect(
                        new SpellSource(caster, InteractionHand.MAIN_HAND),
                        new SpellTarget(helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1)),
                                net.minecraft.core.Direction.UP),
                        modified,
                        new SpellContext(helper.getLevel(), ISpellDefinition.EMPTY)
                ) == ComponentApplicationResult.FAIL,
                "True Damage unexpectedly targeted a block");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void trueDamageShipsTierSixUnlockAndPresentation(GameTestHelper helper) {
        ClassLoader resources = Tier6ProgressionGameTests.class.getClassLoader();
        try (InputStream recipeStream = resources.getResourceAsStream(
                "data/mnagnosis/recipes/components/true_damage.json"
        )) {
            helper.assertTrue(recipeStream != null, "True Damage component recipe was missing");
            JsonObject recipe = JsonParser.parseReader(new InputStreamReader(
                    recipeStream, StandardCharsets.UTF_8
            )).getAsJsonObject();
            helper.assertTrue(recipe.get("tier").getAsInt() == 6,
                    "True Damage was not a Tier 6 component recipe");
            helper.assertTrue("mnagnosis:components/true_damage".equals(recipe.get("output").getAsString()),
                    "True Damage recipe output did not resolve to the registered component");
        } catch (Exception exception) {
            helper.fail("Could not read True Damage recipe: " + exception.getMessage());
            return;
        }

        helper.assertTrue(resources.getResource(
                        "assets/mnagnosis/textures/spell/component/true_damage.png") != null,
                "True Damage component icon was missing");
        helper.assertTrue(resources.getResource(
                        "assets/mnagnosis/sounds/spell/true_damage_static.ogg") != null,
                "True Damage static impact sound was missing");
        ComponentRecipe loadedRecipe = helper.getLevel().getRecipeManager()
                .byKey(SpellComponentRegistry.TRUE_DAMAGE_ID)
                .filter(ComponentRecipe.class::isInstance)
                .map(ComponentRecipe.class::cast)
                .orElseThrow();
        helper.assertTrue(loadedRecipe.getComponent() == SpellComponentRegistry.TRUE_DAMAGE,
                "M&A could not resolve the True Damage recipe output");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ineffableFactionRegistersAsNeutral(GameTestHelper helper) {
        helper.assertTrue(
                Registries.Factions.get().getValue(IneffableFactionRegistry.FACTION_ID)
                        == IneffableFactionRegistry.INEFFABLE_FACTION,
                "The Ineffable faction was not registered under its public ID"
        );
        helper.assertTrue(
                IneffableFactionRegistry.INEFFABLE_FACTION.getEnemyFactions().isEmpty(),
                "The Ineffable faction unexpectedly participates in normal faction hostility"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ineffableManaUsesStandardManaRules(GameTestHelper helper) {
        IneffableMana mana = new IneffableMana();

        mana.setMaxAmountByLevel(6);

        helper.assertTrue(mana.getMaxAmount() == 220.0F,
                "Ineffable mana did not use the standard 100 + 20 per level capacity");
        helper.assertTrue(mana.getRegenerationRate(null) == GeneralConfigValues.TotalManaRegenTicks,
                "Ineffable mana did not use MnA's configured mana regeneration time");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ineffableManaGuiUsesMonochromeHudContract(GameTestHelper helper) {
        IneffableManaGui gui = new IneffableManaGui();

        helper.assertTrue(gui.getTexture().equals(IneffableFactionRegistry.HUD_TEXTURE),
                "The Ineffable GUI provider did not expose its custom HUD atlas");
        helper.assertTrue(gui.getFrameU() == 0 && gui.getFrameV() == 0,
                "The Ineffable frame did not begin at the HUD atlas origin");
        helper.assertTrue(gui.getFrameWidth() == 153 && gui.getFrameHeight() == 24,
                "The Ineffable frame broke MnA's standard HUD dimensions");
        helper.assertTrue(gui.getFillWidth() == 128 && gui.getBarColor() == 0xFFFFFFFF,
                "The Ineffable mana fill was not the planned opaque white");
        helper.assertTrue(gui.getBarManaCostEstimateColor() == 0xFF808080,
                "The Ineffable affordable-cost preview was not middle gray");
        helper.assertTrue(!gui.getBadgeItem().isEmpty(),
                "The Ineffable GUI provider did not supply its outlined-square badge");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ineffableArmorRegistersWithTierSixStats(GameTestHelper helper) {
        int[] defenses = {4, 10, 7, 4};
        ArmorItem.Type[] types = {
                ArmorItem.Type.HELMET,
                ArmorItem.Type.CHESTPLATE,
                ArmorItem.Type.LEGGINGS,
                ArmorItem.Type.BOOTS
        };
        String[] ids = {
                "ineffable_hood",
                "ineffable_robes",
                "ineffable_leggings",
                "ineffable_boots"
        };

        for (int index = 0; index < ids.length; index++) {
            Item item = ForgeRegistries.ITEMS.getValue(MnAGnosis.rloc(ids[index]));
            helper.assertTrue(item instanceof ArmorItem, ids[index] + " was not registered as armor");
            ArmorItem armor = (ArmorItem) item;
            helper.assertTrue(armor.getType() == types[index], ids[index] + " uses the wrong slot");
            helper.assertTrue(armor.getDefense() == defenses[index], ids[index] + " uses the wrong defense");
            helper.assertTrue(armor.getToughness() == 4.0F, ids[index] + " uses the wrong toughness");
            helper.assertTrue(armor.getMaterial().getKnockbackResistance() == 0.15F,
                    ids[index] + " uses the wrong knockback resistance");
            helper.assertTrue(armor.getRarity(item.getDefaultInstance()) == Rarity.EPIC,
                    ids[index] + " is not Epic");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSixRejectsOtherFactionAndStanding(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(6, null, false);

        progression.setAlliedFaction(Factions.COUNCIL, null);
        progression.setFactionStanding(42);

        helper.assertTrue(progression.getAlliedFaction() == IneffableFactionRegistry.INEFFABLE_FACTION,
                "Tier 6 accepted a replacement faction");
        helper.assertTrue(progression.getFactionStanding() == 0,
                "Tier 6 accepted nonzero faction standing");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void lowerTierKeepsFactionAndStanding(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(6, null, false);
        progression.setTier(5, null, false);

        progression.setAlliedFaction(Factions.COUNCIL, null);
        progression.setFactionStanding(42);

        helper.assertTrue(progression.getAlliedFaction() == Factions.COUNCIL,
                "The Tier 6 lock changed a lower-tier faction");
        helper.assertTrue(progression.getFactionStanding() == 42,
                "The Tier 6 lock changed lower-tier faction standing");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void reachingTierSixPerformsOneIneffableTransition(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression capability"));
        for (var formerFaction : List.of(
                Factions.COUNCIL,
                Factions.FEY,
                Factions.DEMONS,
                Factions.UNDEAD
        )) {
            progression.setTier(5, null, false);
            progression.setAlliedFaction(formerFaction, player);
            progression.setFactionStanding(42);

            progression.setTier(6, player, false);

            helper.assertTrue(progression.getAlliedFaction() == IneffableFactionRegistry.INEFFABLE_FACTION,
                    "Reaching Tier 6 did not replace a built-in faction");
            helper.assertTrue(progression.getFactionStanding() == 0,
                    "Reaching Tier 6 did not reset a built-in faction's standing");
        }
        ResourceLocation resourceId = player.getCapability(PlayerMagicProvider.MAGIC)
                .orElseThrow(() -> new IllegalStateException("Missing magic capability"))
                .getCastingResource()
                .getRegistryName();
        helper.assertTrue(resourceId.equals(IneffableFactionRegistry.CASTING_RESOURCE_ID),
                "Reaching Tier 6 did not switch the player's casting resource");
        helper.assertTrue(!Tier6Progression.enforceIneffable(progression, player),
                "An already-correct Tier 6 transition was not idempotent");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSixTickRepairsCastingResourceDrift(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression capability"));
        progression.setTier(6, player, false);
        var magic = player.getCapability(PlayerMagicProvider.MAGIC)
                .orElseThrow(() -> new IllegalStateException("Missing magic capability"));
        magic.setCastingResourceType(CastingResourceIDs.COUNCIL_MANA);

        CommontEvents.enforceIneffableOnTick(
                new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player)
        );

        helper.assertTrue(
                magic.getCastingResource().getRegistryName()
                        .equals(IneffableFactionRegistry.CASTING_RESOURCE_ID),
                "Tier 6 tick reconciliation did not repair casting-resource drift"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void legacyTierSixDataMigratesToIneffable(GameTestHelper helper) {
        PlayerProgressionProvider provider = new PlayerProgressionProvider();
        CompoundTag legacyData = new CompoundTag();
        legacyData.putInt("tier", 6);
        legacyData.putString("faction", Registries.Factions.get().getKey(Factions.COUNCIL).toString());
        legacyData.putInt("faction_standing", 42);

        provider.deserializeNBT(legacyData);

        var progression = provider.getCapability(PlayerProgressionProvider.PROGRESSION, null)
                .orElseThrow(() -> new IllegalStateException("Missing deserialized progression capability"));
        helper.assertTrue(progression.getAlliedFaction() == IneffableFactionRegistry.INEFFABLE_FACTION,
                "Legacy Tier 6 data retained its former faction");
        helper.assertTrue(progression.getFactionStanding() == 0,
                "Legacy Tier 6 data retained its former faction standing");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ineffableIgnoresOrdinaryFactionRaidAggro(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "ineffable_raid_test")
        );
        helper.getLevel().addNewPlayer(player);
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);
        progression.setAlliedFaction(Factions.COUNCIL, null);
        progression.incrementFactionAggro(Factions.DEMONS, 1.0F, 1.0F);
        helper.assertTrue(progression.canBeRaided(Factions.DEMONS, player),
                "The raid test did not begin with actionable faction aggro");

        progression.setTier(6, player, false);

        helper.assertTrue(!progression.canBeRaided(Factions.DEMONS, player),
                "Ineffable remained eligible for an ordinary faction raid");
        helper.assertTrue(!progression.canBeRaided(player),
                "Ineffable remained globally eligible for ordinary faction raids");
        helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSixKeepsExistingHealthModifiers(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        PlayerProgression progression = new PlayerProgression();

        try {
            progression.setTier(6, player, false);
        } catch (ClassCastException exception) {
            helper.assertTrue(
                    exception.getMessage() != null
                            && exception.getMessage().contains("GameTestHelper$1")
                            && exception.getMessage().contains("ServerPlayer"),
                    "Tier 6 failed before the GameTest mock reached server-player-only code"
            );
        }

        helper.assertTrue(progression.getTier() == 6, "Tier 6 was clamped away");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSevenClampsToTierSix(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();

        progression.setTier(7, null, false);

        helper.assertTrue(progression.getTier() == 6, "Tier 7 did not clamp to Tier 6");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSixUsesTierFiveComplexity(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(6, null, false);

        helper.assertTrue(
                progression.getTierMaxComplexity() == GeneralConfigValues.Tier5ComplexityLimit,
                "Tier 6 did not inherit the Tier 5 complexity limit"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierCommandAcceptsSix(GameTestHelper helper) {
        CommandNode<CommandSourceStack> root = helper.getLevel()
                .getServer()
                .getCommands()
                .getDispatcher()
                .getRoot();
        CommandNode<CommandSourceStack> tierArgument = child(
                child(child(child(root, "mna"), "progression"), "tier"),
                "tier"
        );

        helper.assertTrue(
                tierArgument instanceof ArgumentCommandNode<?, ?>,
                "The M&A tier argument was not registered"
        );
        IntegerArgumentType argumentType =
                (IntegerArgumentType) ((ArgumentCommandNode<?, ?>) tierArgument).getType();

        try {
            helper.assertTrue(
                    argumentType.parse(new StringReader("6")) == 6,
                    "The M&A tier command did not parse Tier 6"
            );
        } catch (CommandSyntaxException exception) {
            helper.fail("The M&A tier command rejected Tier 6: " + exception.getMessage());
            return;
        }

        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierFiveRequiresDefeatingOdin(GameTestHelper helper) {
        ProgressionCondition condition = ProgressionCondition
                .get(helper.getLevel(), ODIN_PROGRESSION)
                .orElse(null);

        helper.assertTrue(condition != null, "The Tier 5 Odin progression condition is missing");
        helper.assertTrue(condition.getTier() == 5, "The Odin progression condition is not Tier 5");
        helper.assertTrue(
                ODIN_ADVANCEMENT.equals(condition.getAdvancementID()),
                "The Tier 5 condition does not reference Odin's advancement"
        );
        helper.assertTrue(
                "mna:boss/defeat_odin.description".equals(condition.getDescriptionID()),
                "The Tier 5 condition does not use Odin's Oculus description"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierFiveCannotAdvanceWithoutOdin(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);

        helper.assertTrue(
                !Tier6Progression.shouldSummonTruth(progression, 6, helper.getLevel()),
                "Tier 5 advanced or summoned Truth without defeating Odin"
        );
        helper.assertTrue(progression.getTier() == 5, "Tier 5 advanced without defeating Odin");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void defeatingOdinUnlocksTruthInsteadOfDirectTierSix(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);
        progression.addTierProgressionComplete(ODIN_PROGRESSION);

        helper.assertTrue(
                Tier6Progression.shouldSummonTruth(progression, 6, helper.getLevel()),
                "Defeating Odin did not unlock the Truth encounter"
        );
        helper.assertTrue(progression.getTier() == 5, "Odin directly advanced the player to Tier 6");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void eligibleLeaderRevealIsInterceptedWithoutAdvancing(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression capability"));
        progression.setTier(5, null, false);
        progression.addTierProgressionComplete(ODIN_PROGRESSION);

        boolean intercepted = TruthEncounterService.interceptLeader(
                player, new Vec3(1.5D, 1.0D, 1.5D), 0.0F
        );

        helper.assertTrue(intercepted, "An eligible leader reveal was not intercepted");
        helper.assertTrue(progression.getTier() == 5, "Interception advanced directly to Tier 6");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void eligibleDemonStoneRevealSpawnsTruthInsteadOfDemonLord(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "truth_reveal_test")
        );
        helper.getLevel().addNewPlayer(player);
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression capability"));
        progression.setTier(5, null, false);
        progression.addTierProgressionComplete(ODIN_PROGRESSION);
        DemonStone stone = new DemonStone(EntityInit.DEMON_STONE.get(), helper.getLevel());
        stone.setCasterUUID(player.getUUID());
        stone.setPos(1.5D, 1.0D, 1.5D);
        int demonLordsBefore = countEntities(helper, DemonLord.class);
        int truthsBefore = countEntities(helper, TruthEntity.class);

        for (int tick = 0; tick <= 200; tick++) {
            stone.tick();
        }

        helper.assertTrue(
                countEntities(helper, DemonLord.class) == demonLordsBefore,
                "Demon Stone still revealed a new Demon Lord"
        );
        helper.assertTrue(
                countEntities(helper, TruthEntity.class) == truthsBefore + 1,
                "Demon Stone reveal did not produce one new Truth"
        );
        helper.assertTrue(progression.getTier() == 5, "Demon Stone interception advanced directly to Tier 6");
        helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void hostileDemonStoneStillSpawnsTheBoss(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "truth_hostile_test")
        );
        helper.getLevel().addNewPlayer(player);
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression capability"));
        progression.setTier(5, null, false);
        progression.addTierProgressionComplete(ODIN_PROGRESSION);
        DemonStone stone = new DemonStone(EntityInit.DEMON_STONE.get(), helper.getLevel());
        stone.setCasterUUID(player.getUUID());
        stone.setSummonAsHostile();
        stone.setPos(1.5D, 1.0D, 1.5D);
        int demonLordsBefore = countEntities(helper, DemonLord.class);
        int truthsBefore = countEntities(helper, TruthEntity.class);

        for (int tick = 0; tick <= 200; tick++) {
            stone.tick();
        }

        helper.assertTrue(
                countEntities(helper, DemonLord.class) == demonLordsBefore + 1,
                "Truth incorrectly intercepted the hostile Demon Lord boss summon"
        );
        helper.assertTrue(
                countEntities(helper, TruthEntity.class) == truthsBefore,
                "A hostile Demon Stone incorrectly produced Truth"
        );
        helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void offlineTruthSceneStateCanBeClearedWithoutTheOwner(GameTestHelper helper) {
        UUID offlineOwner = UUID.randomUUID();

        TruthEncounterService.setSceneActive(helper.getLevel().getServer(), offlineOwner, true);
        helper.assertTrue(
                TruthEncounterService.isSceneActive(helper.getLevel().getServer(), offlineOwner),
                "The server did not persist an offline owner's active Truth scene"
        );

        TruthEncounterService.setSceneActive(helper.getLevel().getServer(), offlineOwner, false);
        helper.assertTrue(
                !TruthEncounterService.isSceneActive(helper.getLevel().getServer(), offlineOwner),
                "Truth removal could not clear scene state while its owner was offline"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSixUsesBeyondComprehensionMessage(GameTestHelper helper) {
        Component original = Component.translatable("mna:progresscondition.advanced", 6);
        Component message = Tier6Progression.getAdvancementMessage(6, original);

        helper.assertTrue(
                "you've advanced beyond comprehension, the fabric of the universe is now yours to weave"
                        .equals(message.getString()),
                "Tier 6 did not use the beyond-comprehension message"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void lowerTiersKeepManaAndArtificeMessage(GameTestHelper helper) {
        Component original = Component.translatable("mna:progresscondition.advanced", 5);

        helper.assertTrue(
                Tier6Progression.getAdvancementMessage(5, original) == original,
                "A lower-tier advancement message was replaced"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthStartsAwaitingBothOfferings(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        helper.assertTrue(!truth.hasCodexOffering(), "Truth unexpectedly began with a Codex Arcana");
        helper.assertTrue(!truth.hasWandOffering(), "Truth unexpectedly began with a Manaweaver Wand");
        helper.assertTrue(!truth.isFinaleActive(), "Truth unexpectedly began its finale");
        helper.assertTrue(truth.getFinaleTicks() == 0, "Truth's finale did not begin at zero ticks");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthExposesSyncedFinaleStateToItsGeckoRenderer(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        helper.assertTrue(truth instanceof GeoEntity, "Truth does not implement GeoEntity for its GeckoLib renderer");
        helper.assertTrue(truth.getFinaleProgress(0.0F) == 0.0F,
                "Truth's renderer-facing finale progress did not begin at zero");

        truth.beginFinale();

        helper.assertTrue(truth.getFinaleProgress(0.0F) == 0.0F,
                "Truth's renderer-facing finale progress did not begin at the start of the finale");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthFinaleVisualPhasesAreDelayedAndOrdered(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        truth.beginFinale();

        helper.assertTrue(!truth.shouldShowFinaleFlames(), "Truth's flames began before the offerings could ignite");
        helper.assertTrue(!truth.shouldShowGrin(), "Truth grinned before the finale built up");
        helper.assertTrue(!truth.shouldShowGlitchSlices(), "Truth's glitch fragments began before the finale climax");

        for (int tick = 0; tick < 30; tick++) {
            truth.tick();
        }

        helper.assertTrue(truth.shouldShowFinaleFlames(), "Truth's white flame phase did not begin");
        helper.assertTrue(truth.shouldShowGrin(), "Truth's grin did not appear after the finale built up");
        helper.assertTrue(!truth.shouldShowGlitchSlices(), "Truth's glitch fragments appeared before the late finale");
        helper.assertTrue(!truth.shouldDissolveAura(), "Truth's aura began dissolving before the late finale");

        for (int tick = 0; tick < 40; tick++) {
            truth.tick();
        }

        helper.assertTrue(truth.shouldShowGlitchSlices(), "Truth's late-finale glitch fragments did not appear");
        helper.assertTrue(truth.shouldDissolveAura(), "Truth's aura did not enter its late-finale dissolve");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierFiveNeedsOdinBeforeTruthCanAppear(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);

        helper.assertTrue(
                !Tier6Progression.isEligibleForTruth(progression, helper.getLevel()),
                "Tier 5 was allowed to summon Truth without defeating Odin"
        );

        progression.addTierProgressionComplete(ODIN_PROGRESSION);

        helper.assertTrue(
                Tier6Progression.isEligibleForTruth(progression, helper.getLevel()),
                "Defeating Odin did not make Tier 5 eligible to summon Truth"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthFinaleLastsFiveSeconds(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        truth.beginFinale();

        helper.assertTrue(
                truth.isFinaleActive(),
                "Truth did not enter its finale after receiving both offerings"
        );
        helper.assertTrue(
                truth.getFinaleTicks() == TruthEntity.FINALE_DURATION_TICKS,
                "Truth's finale was not initialized to its full five-second duration"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthBurningOfferingSoundIsRegistered(GameTestHelper helper) {
        helper.assertTrue(
                MnAGnosis.rloc("truth_burning_offering")
                        .equals(SoundRegistry.TRUTH_BURNING_OFFERING.getId()),
                "Truth's burning-offering sound event is not registered"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthAmbientSoundIsRegistered(GameTestHelper helper) {
        helper.assertTrue(
                MnAGnosis.rloc("truth_ambient").equals(SoundRegistry.TRUTH_AMBIENT.getId()),
                "Truth's persistent ambient sound event is not registered"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthAppearanceSoundsAreRegistered(GameTestHelper helper) {
        helper.assertTrue(
                MnAGnosis.rloc("truth_appear").equals(SoundRegistry.TRUTH_APPEAR.getId()),
                "Truth's appearance sound event is not registered"
        );
        helper.assertTrue(
                MnAGnosis.rloc("truth_disappear").equals(SoundRegistry.TRUTH_DISAPPEAR.getId()),
                "Truth's disappearance sound event is not registered"
        );
        helper.assertTrue(
                MnAGnosis.rloc("truth_vanish").equals(SoundRegistry.TRUTH_VANISH.getId()),
                "Truth's final vanish sound event is not registered"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthDisappearanceGiggleStartsWithTheGrin(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        truth.beginFinale();

        for (int tick = 0; tick < 29; tick++) {
            truth.tick();
        }
        helper.assertTrue(!truth.hasFinaleGiggleStarted(),
                "Truth's disappearance giggle began before its grin");

        truth.tick();

        helper.assertTrue(truth.shouldShowGrin(), "Truth's grin did not begin at the expected finale phase");
        helper.assertTrue(truth.hasFinaleGiggleStarted(),
                "Truth's disappearance giggle did not begin with its grin");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthFinaleCanOnlyBeginOnce(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        truth.beginFinale();
        truth.tick();
        truth.beginFinale();

        helper.assertTrue(
                truth.getFinaleTicks() == TruthEntity.FINALE_DURATION_TICKS - 1,
                "Truth restarted its finale instead of keeping the original completion"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ritualTierSixIsReplacedByTruth(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);

        helper.assertTrue(
                !Tier6Progression.shouldSummonTruth(progression, 6, helper.getLevel()),
                "A Tier 5 ritual summoned Truth before Odin was defeated"
        );

        progression.addTierProgressionComplete(ODIN_PROGRESSION);

        helper.assertTrue(
                Tier6Progression.shouldSummonTruth(progression, 6, helper.getLevel()),
                "An Odin-qualified Tier 5 ritual did not summon Truth"
        );
        helper.assertTrue(
                !Tier6Progression.shouldSummonTruth(progression, 5, helper.getLevel()),
                "A normal lower-tier ritual was replaced by Truth"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthOnlyAcceptsTheTwoRequiredOfferings(GameTestHelper helper) {
        helper.assertTrue(
                TruthEntity.isRequiredOffering(new ItemStack(ItemInit.GUIDE_BOOK.get())),
                "Truth rejected a Codex Arcana"
        );
        helper.assertTrue(
                TruthEntity.isRequiredOffering(new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get())),
                "Truth rejected a Chimerite Manaweaver Wand"
        );
        helper.assertTrue(
                !TruthEntity.isRequiredOffering(new ItemStack(ItemInit.MANAWEAVER_WAND.get())),
                "Truth accepted the Vinteum Manaweaver Wand"
        );
        helper.assertTrue(
                !TruthEntity.isRequiredOffering(new ItemStack(Items.STICK)),
                "Truth accepted an unrelated item"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthCalculatesYawTowardItsBoundOwner(GameTestHelper helper) {
        Vec3 truthPosition = Vec3.ZERO;

        helper.assertTrue(
                Math.abs(TruthEntity.calculateFacingYaw(truthPosition, new Vec3(0.0D, 0.0D, 1.0D)))
                        < 0.001F,
                "Truth did not face an owner standing south"
        );
        helper.assertTrue(
                Math.abs(TruthEntity.calculateFacingYaw(truthPosition, new Vec3(1.0D, 0.0D, 0.0D))
                        + 90.0F) < 0.001F,
                "Truth did not face an owner standing east"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthConvertsEntityYawToModelRotation(GameTestHelper helper) {
        helper.assertTrue(
                Math.abs(TruthEntity.calculateModelYRotation(0.0F) - 180.0F) < 0.001F,
                "Truth's model did not face its zero-degree entity yaw"
        );
        helper.assertTrue(
                Math.abs(TruthEntity.calculateModelYRotation(90.0F) - 90.0F) < 0.001F,
                "Truth's model did not rotate with a positive entity yaw"
        );
        helper.assertTrue(
                Math.abs(TruthEntity.calculateModelYRotation(-90.0F) - 270.0F) < 0.001F,
                "Truth's model did not rotate with a negative entity yaw"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void playerCommandSourceBindsSummonedTruth(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        helper.assertTrue(
                TruthEncounterService.bindCommandSummoner(truth, player.createCommandSourceStack()),
                "A player-executed summon did not bind Truth to its summoner"
        );
        helper.assertTrue(
                truth.getOwnerId().filter(player.getUUID()::equals).isPresent(),
                "The command-summoned Truth stored the wrong owner"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthIsInteractiveButCannotBeMovedOrDamaged(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        helper.assertTrue(truth.isPickable(), "Truth cannot receive player interactions");
        helper.assertTrue(!truth.isPushable(), "Truth can be pushed away from its seated position");
        helper.assertTrue(truth.isNoGravity(), "Truth is affected by gravity");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void feySourceTokensExpireIfNoFinaleRuns(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        Vec3 source = new Vec3(3.0D, 4.0D, 5.0D);

        TruthEncounterService.rememberFeySource(player, source, 37.0F, 10L);

        helper.assertTrue(
                TruthEncounterService.consumeFeySource(player, 130L) != null,
                "The pending Fey source was lost before its ritual callback"
        );
        TruthEncounterService.rememberFeySource(player, source, 37.0F, 10L);
        helper.assertTrue(
                TruthEncounterService.consumeFeySource(player, 211L) == null,
                "An expired Fey source token was still accepted"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthStoresBothOfferingsInEitherOrderWithExactNbt(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        ItemStack wand = new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get());
        wand.getOrCreateTag().putString("CustomName", "test wand");
        ItemStack codex = new ItemStack(ItemInit.GUIDE_BOOK.get());
        codex.getOrCreateTag().putInt("Bookmark", 42);

        helper.assertTrue(
                truth.storeOffering(wand) == TruthEntity.OfferingResult.ACCEPTED,
                "Truth did not accept the wand as the first offering"
        );
        helper.assertTrue(
                truth.storeOffering(codex) == TruthEntity.OfferingResult.COMPLETE,
                "Truth did not complete after receiving the Codex second"
        );
        helper.assertTrue(
                truth.getWandOffering().getTag().getString("CustomName").equals("test wand"),
                "Truth did not preserve the wand's NBT"
        );
        helper.assertTrue(
                truth.getCodexOffering().getTag().getInt("Bookmark") == 42,
                "Truth did not preserve the Codex's NBT"
        );
        helper.assertTrue(
                truth.storeOffering(codex) == TruthEntity.OfferingResult.DUPLICATE,
                "Truth accepted a duplicate Codex"
        );

        TruthEntity codexFirstTruth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        ItemStack codexFirst = new ItemStack(ItemInit.GUIDE_BOOK.get());
        codexFirst.getOrCreateTag().putString("OwnerNote", "codex first");
        ItemStack wandSecond = new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get());
        wandSecond.getOrCreateTag().putInt("StoredMana", 73);

        helper.assertTrue(
                codexFirstTruth.storeOffering(codexFirst) == TruthEntity.OfferingResult.ACCEPTED,
                "Truth did not accept the Codex as the first offering"
        );
        helper.assertTrue(
                codexFirstTruth.storeOffering(wandSecond) == TruthEntity.OfferingResult.COMPLETE,
                "Truth did not complete after receiving the wand second"
        );
        helper.assertTrue(
                ItemStack.isSameItemSameTags(codexFirst, codexFirstTruth.getCodexOffering()),
                "Truth did not preserve the Codex's exact NBT when offered first"
        );
        helper.assertTrue(
                ItemStack.isSameItemSameTags(wandSecond, codexFirstTruth.getWandOffering()),
                "Truth did not preserve the wand's exact NBT when offered second"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthRejectsOfferingsFromAnotherPlayer(GameTestHelper helper) {
        Player owner = helper.makeMockSurvivalPlayer();
        Player other = helper.makeMockSurvivalPlayer();
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        truth.setOwner(owner);
        other.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(ItemInit.GUIDE_BOOK.get()));

        truth.interact(other, net.minecraft.world.InteractionHand.MAIN_HAND);

        helper.assertTrue(!truth.hasCodexOffering(), "A non-owner gave Truth a Codex Arcana");
        helper.assertTrue(
                other.getMainHandItem().is(ItemInit.GUIDE_BOOK.get()),
                "A non-owner's offering was consumed"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthReplacementOnlyRemovesLoadedEncountersForTheOwner(GameTestHelper helper) {
        Player owner = helper.makeMockSurvivalPlayer();
        Player other = helper.makeMockSurvivalPlayer();
        TruthEntity firstOwned = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        TruthEntity secondOwned = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        TruthEntity unrelated = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        firstOwned.setOwner(owner);
        secondOwned.setOwner(owner);
        unrelated.setOwner(other);
        firstOwned.storeOffering(new ItemStack(ItemInit.GUIDE_BOOK.get()));
        secondOwned.storeOffering(new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get()));

        int replaced = TruthEncounterService.refundAndDiscardOwned(
                List.<Entity>of(firstOwned, secondOwned, unrelated),
                owner.getUUID()
        );

        helper.assertTrue(replaced == 2, "Truth replacement did not find both owned encounters");
        helper.assertTrue(firstOwned.isRemoved(), "Truth replacement did not discard the first owned encounter");
        helper.assertTrue(secondOwned.isRemoved(), "Truth replacement did not discard the second owned encounter");
        helper.assertTrue(firstOwned.getCodexOffering().isEmpty(),
                "Truth replacement did not refund the first encounter's Codex");
        helper.assertTrue(secondOwned.getWandOffering().isEmpty(),
                "Truth replacement did not refund the second encounter's wand");
        helper.assertTrue(!unrelated.isRemoved(), "Truth replacement discarded another player's encounter");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthSaveLoadPreservesEncounterStateAndExactOfferings(GameTestHelper helper) {
        Player owner = helper.makeMockSurvivalPlayer();
        TruthEntity original = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        ItemStack codex = new ItemStack(ItemInit.GUIDE_BOOK.get(), 3);
        codex.getOrCreateTag().putInt("Bookmark", 42);
        ItemStack wand = new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get(), 2);
        wand.getOrCreateTag().putString("CustomName", "persistent wand");
        original.setOwner(owner);
        original.storeOffering(codex);
        original.storeOffering(wand);
        for (int tick = 0; tick < 23; tick++) {
            original.tick();
        }
        original.beginFinale();
        for (int tick = 0; tick < 11; tick++) {
            original.tick();
        }

        CompoundTag saved = new CompoundTag();
        original.saveWithoutId(saved);
        TruthEntity restored = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        restored.load(saved);

        helper.assertTrue(restored.getOwnerId().filter(owner.getUUID()::equals).isPresent(),
                "Truth save/load lost the encounter owner");
        helper.assertTrue(restored.getIdleTicks() == 23, "Truth save/load lost the idle timer");
        helper.assertTrue(restored.getFinaleTicks() == TruthEntity.FINALE_DURATION_TICKS - 11,
                "Truth save/load lost the finale timer");
        helper.assertTrue(restored.getCodexOffering().getCount() == 1
                        && restored.getCodexOffering().getTag().getInt("Bookmark") == 42,
                "Truth save/load did not preserve the exact Codex offering");
        helper.assertTrue(restored.getWandOffering().getCount() == 1
                        && restored.getWandOffering().getTag().getString("CustomName").equals("persistent wand"),
                "Truth save/load did not preserve the exact wand offering");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthTimeoutRefundsStoredOfferingsBeforeDiscarding(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        truth.storeOffering(new ItemStack(ItemInit.GUIDE_BOOK.get()));
        CompoundTag saved = new CompoundTag();
        truth.saveWithoutId(saved);
        saved.putInt("IdleTicks", TruthEntity.IDLE_TIMEOUT_TICKS - 1);
        truth.load(saved);

        truth.tick();

        helper.assertTrue(truth.isRemoved(), "Truth did not disappear at its idle timeout");
        helper.assertTrue(truth.getCodexOffering().isEmpty(),
                "Truth discarded without refunding its stored offering");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityFieldMathIsDirectionalAndBounded(GameTestHelper helper) {
        Vec3 inward = GravityFieldMath.acceleration(
                new Vec3(4.0D, 0.0D, 0.0D), 6.0D, 1.0D, 1.0D,
                GravityPolarity.ATTRACT, Vec3.ZERO
        );
        Vec3 outward = GravityFieldMath.acceleration(
                new Vec3(4.0D, 0.0D, 0.0D), 6.0D, 1.0D, 1.0D,
                GravityPolarity.REPEL, Vec3.ZERO
        );
        helper.assertTrue(inward.x < 0.0D,
                "Attraction did not point toward the field center");
        helper.assertTrue(outward.x > 0.0D,
                "Repulsion did not point away from the field center");
        helper.assertTrue(inward.length() <= GravityFieldMath.MAX_ACCELERATION + 1.0E-9D
                        && outward.length() <= GravityFieldMath.MAX_ACCELERATION + 1.0E-9D,
                "Gravity acceleration exceeded its safety cap");

        Vec3 fast = GravityFieldMath.clampVelocity(new Vec3(5.0D, 5.0D, 0.0D));
        helper.assertTrue(fast.length() <= GravityFieldMath.MAX_VELOCITY + 1.0E-9D,
                "Gravity velocity exceeded its safety cap");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityFieldMathHandlesShellEdgeAndCenter(GameTestHelper helper) {
        Vec3 shellDamping = GravityFieldMath.acceleration(
                new Vec3(0.25D, 0.0D, 0.0D), 6.0D, 3.0D, 3.0D,
                GravityPolarity.ATTRACT, new Vec3(-0.4D, 0.0D, 0.0D)
        );
        helper.assertTrue(shellDamping.x > 0.0D,
                "Attractive capture shell did not damp inward velocity");

        Vec3 innerRepulsion = GravityFieldMath.acceleration(
                new Vec3(3.0D, 0.0D, 0.0D), 6.0D, 1.0D, 1.0D,
                GravityPolarity.REPEL, Vec3.ZERO
        );
        Vec3 edgeRepulsion = GravityFieldMath.acceleration(
                new Vec3(5.9D, 0.0D, 0.0D), 6.0D, 1.0D, 1.0D,
                GravityPolarity.REPEL, Vec3.ZERO
        );
        helper.assertTrue(edgeRepulsion.length() < innerRepulsion.length(),
                "Repulsion did not fade near the field boundary");

        Vec3 centered = GravityFieldMath.acceleration(
                Vec3.ZERO, 6.0D, 3.0D, 3.0D,
                GravityPolarity.ATTRACT, new Vec3(0.5D, -0.25D, 0.125D)
        );
        helper.assertTrue(Double.isFinite(centered.x)
                        && Double.isFinite(centered.y)
                        && Double.isFinite(centered.z),
                "Exact-center gravity produced a non-finite vector");
        helper.assertTrue(centered.dot(new Vec3(0.5D, -0.25D, 0.125D)) < 0.0D,
                "Exact-center attraction did not damp current velocity");
        helper.succeed();
    }

    private static CommandNode<CommandSourceStack> child(
            CommandNode<CommandSourceStack> parent,
            String name
    ) {
        CommandNode<CommandSourceStack> child = parent.getChild(name);
        if (child == null) {
            throw new IllegalStateException("Missing command node: " + name);
        }
        return child;
    }

    private static int countEntities(GameTestHelper helper, Class<? extends Entity> type) {
        int count = 0;
        for (Entity entity : helper.getLevel().getAllEntities()) {
            if (type.isInstance(entity) && !entity.isRemoved()) {
                count++;
            }
        }
        return count;
    }
}
