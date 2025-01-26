package net.jacobwasbeast.supernatural.items;

import net.jacobwasbeast.supernatural.ModEntities;
import net.jacobwasbeast.supernatural.api.PsalmTargetManager;
import net.jacobwasbeast.supernatural.entities.DemonEntity;
import net.jacobwasbeast.supernatural.entities.DemonVillager;
import net.jacobwasbeast.supernatural.entities.FakeLightning;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class Psalm extends Item {
    private static final String[] PSALM_WORDS = {
            "Exorcizamus", "te,", "omnis", "immundus", "spiritus,", "omnis", "satanica",
            "potestas,", "omnis", "incursio", "infernalis", "adversarii,", "omnis",
            "legio,", "omnis", "congregatio", "et", "secta", "diabolica.", "Ergo,",
            "omnis", "legio", "diabolica,", "adiuramus", "te...", "cessa", "decipere",
            "humans", "creaturas,", "eisque", "æternæ", "perditionìs", "venenum",
            "propinare...", "Vade,", "satana,", "inventor", "et", "magister", "omnis",
            "fallaciæ,", "hostis", "humanæ", "salutis...", "Humiliare", "sub",
            "potenti", "manu", "Dei;", "contremisce", "et", "effuge,", "invocato",
            "a", "nobis", "sancto", "et", "terribili", "nomine...", "quem",
            "inferi", "tremunt...", "Ab", "insidiis", "diaboli,", "libera", "nos,",
            "Domine.", "Ut", "Ecclesiam", "tuam", "secura", "tibi", "facias",
            "libertate", "servire,", "te", "rogamus,", "audi", "nos."
    };

    private static final int[] PSALM_TIMINGS = {
            30, 15, 20, 15, 30, 15, 20,
            15, 20, 15, 30, 15, 20, 15,
            30, 20, 15, 30, 20, 15, 30,
            20, 15, 30, 15, 20, 15, 30,
            20, 15, 20, 15, 30, 15, 20,
            15, 30, 20, 15, 20, 15, 30,
            20, 15, 30, 15, 20, 15, 30,
            20, 15, 30, 15, 20, 15, 30,
            20, 15, 20, 15, 30, 15, 20,
            15, 30, 20, 15, 20, 15, 30,
            20, 15, 30, 15, 20, 15, 30,
            20, 15, 30, 15, 20, 15, 30,
            20, 15, 20, 15, 30, 15, 20,
            15, 30, 20, 15, 30, 15, 20,
            15, 30, 20, 15, 30, 15, 20,
            15, 30, 20, 15, 20, 15, 30,
            20, 15
    };

    private int currentWordIndex = 0;
    private int tickCounter = 0;
    private boolean isActive = false;
    private boolean isCooldown = false; // Cooldown state
    private int cooldownTimer = 0; // Timer for cooldown

    public Psalm(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        // Start the process of reading the psalm
        if (!world.isClient && !isActive && !isCooldown) {
            isActive = true;
            player.setCurrentHand(hand);  // Start holding down the item
            player.sendMessage(Text.of("You begin to read the psalm..."), true);
        }
        else if (isCooldown) {
            player.sendMessage(Text.of("The psalm is on cooldown for " + cooldownTimer + " ticks."), true);
        }
        return TypedActionResult.consume(player.getStackInHand(hand));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        PlayerEntity player = (PlayerEntity) user;
        if (!world.isClient && isActive) {
            tickCounter++;

            // Every X ticks (dynamic based on the word), say the next word of the psalm
            if (tickCounter >= PSALM_TIMINGS[currentWordIndex] && user instanceof PlayerEntity) {

                if (currentWordIndex < PSALM_WORDS.length) {
                    player.sendMessage(Text.of(PSALM_WORDS[currentWordIndex]), false);
                    tickCounter = 0; // Reset the tick counter for the next word
                    currentWordIndex++;

                    // Display remaining words
                    int wordsLeft = PSALM_WORDS.length - currentWordIndex;
                    player.sendMessage(Text.of("Words left to read: " + wordsLeft), true);
                    if (!PsalmTargetManager.getInstance().isTargeted(player)) {
                        PsalmTargetManager.getInstance().addTarget(player);
                    }
                } else {
                    // Psalm is finished, eject demons
                    ejectDemons(player, world);
                    resetPsalm();
                    isCooldown = true; // Start cooldown after completing the psalm
                    cooldownTimer = 100; // Set cooldown duration
                    PsalmTargetManager.getInstance().removeTarget(player);
                    player.sendMessage(Text.of("The psalm is complete! A cooldown is now active."), true);
                }
            }
        }

        // Cooldown logic
        if (isCooldown) {
            cooldownTimer--;
            if (cooldownTimer <= 0) { // Cooldown duration reached
                isCooldown = false; // Reset cooldown
                player.sendMessage(Text.of("You can read the psalm again."), true);
            } else {
                player.sendMessage(Text.of("Cooldown: " + cooldownTimer + " ticks remaining."), true);
            }
        }
    }

    /**
     * Eject all demon villagers in a 50-block radius.
     */
    private void ejectDemons(PlayerEntity player, World world) {
        if (!world.isClient) {
            player.sendMessage(Text.of("Ejecting nearby demons..."), true);

            // Find all DemonVillager entities in a 50-block radius
            Box searchBox = new Box(player.getPos().add(-50, -50, -50), player.getPos().add(50, 50, 50));
            List<DemonVillager> nearbyDemonsVillagers = world.getEntitiesByClass(DemonVillager.class, searchBox, demonVillager -> true);

            // Eject each demon villager
            for (DemonVillager demon : nearbyDemonsVillagers) {
                demon.ejectVillager();
                // summon fake lightning
                FakeLightning fakeLightning = new FakeLightning(ModEntities.FAKELIGHTNING, world);
                fakeLightning.refreshPositionAndAngles(demon.getX(), demon.getY(), demon.getZ(), demon.getYaw(), demon.getPitch());
                ((ServerWorld) world).spawnEntity(fakeLightning);
            }

            List<DemonEntity> nearbyDemons = world.getEntitiesByClass(DemonEntity.class, searchBox, demonVillager -> true);
            for (DemonEntity demon : nearbyDemons) {
                demon.runAway = true;
                // summon fake lightning
                FakeLightning fakeLightning = new FakeLightning(ModEntities.FAKELIGHTNING, world);
                fakeLightning.refreshPositionAndAngles(demon.getX(), demon.getY(), demon.getZ(), demon.getYaw(), demon.getPitch());
                ((ServerWorld) world).spawnEntity(fakeLightning);
            }
            player.sendMessage(Text.of((nearbyDemons.size() + nearbyDemonsVillagers.size()) + " demons exorcism complete."), true);
        }
    }

    /**
     * Reset the psalm reading process.
     */
    public void resetPsalm() {
        currentWordIndex = 0;
        tickCounter = 0;
        isActive = false;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;  // Allow long holding of the item
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient && isActive) {
            PlayerEntity player = (PlayerEntity) user;
            player.sendMessage(Text.of("You stopped reading the psalm."), true);
            resetPsalm();
            PsalmTargetManager.getInstance().removeTarget(player);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if ((player.getStackInHand(Hand.MAIN_HAND).getItem() == this || player.getStackInHand(Hand.OFF_HAND).getItem() == this) && !isActive && !isCooldown) {
                player.sendMessage(Text.of("Hold right-click to read the psalm."), true);
            }
            if (cooldownTimer > 0) {
                cooldownTimer--;
            }
            else {
                isCooldown = false;
            }
        }
    }
}
