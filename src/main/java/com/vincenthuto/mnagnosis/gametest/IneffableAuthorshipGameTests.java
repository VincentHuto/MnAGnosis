package com.vincenthuto.mnagnosis.gametest;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.authorship.state.Contradiction;
import com.vincenthuto.mnagnosis.common.authorship.state.ContradictionLedger;
import com.vincenthuto.mnagnosis.common.authorship.state.LedgerTransition;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class IneffableAuthorshipGameTests {

    private IneffableAuthorshipGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void fourthContradictionVentsOldest(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction first = contradiction("vector", 10.0F, 3, 1);
        ledger.add(first);
        ledger.add(contradiction("motion", 20.0F, 3, 2));
        ledger.add(contradiction("vitality", 30.0F, 3, 3));

        LedgerTransition transition =
                ledger.add(contradiction("revelation", 40.0F, 3, 4));

        helper.assertTrue(transition.vented().equals(List.of(first)),
                "A fourth Contradiction did not Vent the oldest debt");
        helper.assertTrue(ledger.size() == 3,
                "The ledger did not remain capped at three debts");
        helper.assertTrue(ledger.oldest().orElseThrow().order() == 2,
                "The second-created debt did not become the oldest");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void newlyCreatedContradictionDoesNotAge(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction existing = contradiction("vector", 10.0F, 3, 1);
        Contradiction createdThisCast = contradiction("motion", 20.0F, 3, 2);
        ledger.add(existing);
        ledger.add(createdThisCast);

        ledger.age(Set.of(createdThisCast.id()));

        helper.assertTrue(ledger.entries().get(0).safeCasts() == 2,
                "An existing debt did not age after another cast");
        helper.assertTrue(ledger.entries().get(1).safeCasts() == 3,
                "The debt created by this cast aged immediately");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void closureExemptsResolvedContradictionFromAging(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction closureTarget = contradiction("vitality", 15.0F, 2, 1);
        Contradiction unresolved = contradiction("motion", 25.0F, 2, 2);
        ledger.add(closureTarget);
        ledger.add(unresolved);

        ledger.age(Set.of(closureTarget.id()));

        helper.assertTrue(ledger.entries().get(0).safeCasts() == 2,
                "A Closure target aged during its resolving cast");
        helper.assertTrue(ledger.entries().get(1).safeCasts() == 1,
                "An unresolved debt was incorrectly exempted from aging");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void expiredContradictionsVentOldestFirst(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction first = contradiction("vector", 10.0F, 1, 1);
        Contradiction second = contradiction("motion", 20.0F, 1, 2);
        Contradiction survivor = contradiction("vitality", 30.0F, 2, 3);
        ledger.add(first);
        ledger.add(second);
        ledger.add(survivor);

        LedgerTransition transition = ledger.age(Set.of());

        helper.assertTrue(transition.vented().equals(List.of(first, second)),
                "Simultaneous expiry did not Vent debts in creation order");
        helper.assertTrue(transition.remaining().size() == 1
                        && transition.remaining().get(0).id().equals(survivor.id())
                        && transition.remaining().get(0).safeCasts() == 1,
                "A non-expired debt was not preserved and aged exactly once");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void totalParadoxIsDerivedFromCurrentDebts(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction closed = contradiction("vector", 12.5F, 3, 1);
        ledger.add(closed);
        ledger.add(contradiction("motion", 20.25F, 3, 2));
        ledger.add(contradiction("vitality", 7.25F, 3, 3));
        ledger.close(closed.id());

        helper.assertTrue(Math.abs(ledger.totalParadox() - 27.5F) < 0.0001F,
                "Total Paradox was not derived from the debts still in the ledger");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void contradictionLedgerNbtRoundTripPreservesOrderAndPayload(
            GameTestHelper helper
    ) {
        ContradictionLedger original = new ContradictionLedger();
        original.add(contradiction("revelation", 11.5F, 2, 8));
        original.add(contradiction("presence", 17.75F, 3, 4));

        ContradictionLedger loaded = ContradictionLedger.load(original.save());

        helper.assertTrue(loaded.entries().size() == 2,
                "NBT load changed the debt count");
        helper.assertTrue(loaded.entries().get(0).order() == 4
                        && loaded.entries().get(1).order() == 8,
                "NBT load did not restore debts in creation order");
        helper.assertTrue(loaded.entries().get(0).payload().getString("marker")
                        .equals("payload-presence"),
                "NBT load did not preserve the first debt payload");
        helper.assertTrue(loaded.entries().get(1).payload().getString("marker")
                        .equals("payload-revelation"),
                "NBT load did not preserve the second debt payload");
        helper.assertTrue(Math.abs(loaded.totalParadox() - 29.25F) < 0.0001F,
                "NBT load changed the ledger's derived Paradox");
        helper.succeed();
    }

    private static Contradiction contradiction(
            String interpretation,
            float paradox,
            int safeCasts,
            long order
    ) {
        CompoundTag payload = new CompoundTag();
        payload.putString("marker", "payload-" + interpretation);
        return new Contradiction(
                UUID.nameUUIDFromBytes(("debt-" + interpretation + "-" + order).getBytes()),
                MnAGnosis.rloc("inversion"),
                ResourceLocation.fromNamespaceAndPath(MnAGnosis.MODID, interpretation),
                paradox,
                safeCasts,
                order,
                payload
        );
    }
}
