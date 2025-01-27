package net.jacobwasbeast.supernatural.api;

import de.dafuqs.chalk.common.Chalk;
import de.dafuqs.chalk.common.ChalkRegistry;
import de.dafuqs.chalk.common.blocks.ChalkMarkBlock;
import net.jacobwasbeast.supernatural.ModBlocks;
import net.jacobwasbeast.supernatural.SupernaturalMain;
import net.jacobwasbeast.supernatural.blocks.RitualChalk;
import net.jacobwasbeast.supernatural.blocks.entities.RitualChalkEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;

public class RitualManager {
    public HashMap<String, Ritual> rituals = new HashMap<>();
    public static RitualManager instance;
    public static Ritual NULL_RITUAL;

    public static RitualManager getInstance() {
        if (instance == null) {
            instance = new RitualManager();
        }
        return instance;
    }

    public RitualManager() {
        // Initialize rituals with their respective recipes
        Recipe<int[][]> devilsTrap = new Recipe<>(RecipeType.CHALK, new int[][]{
                {-1, 4, 7, 4, -1},
                {4, -1, -1, -1, 4},
                {5, -1, -1, -1, 3},
                {4, -1, -1, -1, 4},
                {-1, 4, 1, 4, -1}
        });

        Recipe<int[][]> protectionCircle = new Recipe<>(RecipeType.SALT, new int[][]{
                {-1, 0, 0, 0, -1},
                {0, -1, -1, -1, 0},
                {0, -1, -1, -1, 0},
                {0, -1, -1, -1, 0},
                {-1, 0, 0, 0, -1}
        });

        // NULL ritual as a fallback
        Recipe<int[][]> NULL = new Recipe<>(RecipeType.CHALK, new int[][]{
                {-1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1}
        });

        NULL_RITUAL = new Ritual("NULL", "NULL", SupernaturalMain.id("NULL"), NULL, 5);

        // Register rituals
        rituals.put("DEVILS_TRAP", new Ritual("Devil's Trap", "A trap used to capture demons", SupernaturalMain.id("devilstrap"), devilsTrap, 5));
        rituals.put("PROTECTION_CIRCLE", new Ritual("Protection's Circle", "A circle used to protect against supernatural entities (mainly ghosts and spirits)", SupernaturalMain.id("protectioncircle"), protectionCircle, 5));
    }

    /**
     * Validates if a ritual is valid at the given position in the world.
     *
     * @param world The world context.
     * @param pos   The position to validate the ritual.
     * @return The valid RitualMatch if found; otherwise, null.
     */
    public RitualMatch isValid(World world, BlockPos pos) {
        for (Ritual ritual : rituals.values()) {
            Recipe<int[][]> recipe = ritual.getRecipe();
            int[][] originalMatrix = recipe.getRecipeMatrix();
            int ritualHeight = originalMatrix.length;
            int ritualWidth = originalMatrix[0].length;

            for (int rotation = 0; rotation < 4; rotation++) {
                int[][] rotatedMatrix = rotateMatrix(originalMatrix, rotation);
                int rotatedHeight = rotatedMatrix.length;
                int rotatedWidth = rotatedMatrix[0].length;

                boolean isValid = true;

                // Iterate through each cell in the rotated matrix
                for (int y = 0; y < rotatedHeight; y++) {
                    for (int x = 0; x < rotatedWidth; x++) {
                        int state = rotatedMatrix[y][x];
                        if (state == -1) {
                            continue; // Skip non-essential positions
                        }

                        // Calculate the block position relative to the ritual center
                        BlockPos checkPos = pos.add(
                                x - rotatedWidth / 2,
                                0,
                                y - rotatedHeight / 2
                        );
                        BlockPos checkPosUp = checkPos.up();
                        BlockPos checkPosDown = checkPos.down();

                        RecipeType type = recipe.getType();
                        switch (type) {
                            case CHALK:
                                if (getChalkOrientation(world, checkPos) != state &&
                                        getChalkOrientation(world, checkPosUp) != state &&
                                        getChalkOrientation(world, checkPosDown) != state) {
                                    isValid = false;
                                    break;
                                }
                                break;
                            case SALT:
                                boolean hasSalt = isSaltPresent(world, checkPos);
                                boolean hasSaltUp = isSaltPresent(world, checkPosUp);
                                boolean hasSaltDown = isSaltPresent(world, checkPosDown);
                                if (state == 0 && !(hasSalt || hasSaltUp || hasSaltDown)) {
                                    isValid = false;
                                    break;
                                }
                                break;
                            default:
                                // Handle other RecipeTypes if necessary
                                break;
                        }

                        if (!isValid) {
                            break;
                        }
                    }
                    if (!isValid) {
                        break;
                    }
                }

                if (isValid) {
                    return new RitualMatch(ritual, rotation);
                }
            }
        }
        return null; // No valid ritual found
    }

    /**
     * Rotates the given matrix by 90 degrees clockwise the specified number of times.
     *
     * @param matrix    The original matrix.
     * @param rotations The number of 90-degree rotations (0-3).
     * @return The rotated matrix.
     */
    private int[][] rotateMatrix(int[][] matrix, int rotations) {
        int[][] rotatedMatrix = matrix;
        for (int i = 0; i < rotations; i++) {
            rotatedMatrix = rotate90DegreesClockwise(rotatedMatrix);
        }
        return rotatedMatrix;
    }

    /**
     * Rotates the given matrix by 90 degrees clockwise once.
     *
     * @param matrix The matrix to rotate.
     * @return The rotated matrix.
     */
    private int[][] rotate90DegreesClockwise(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] rotated = new int[cols][rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                rotated[x][rows - 1 - y] = matrix[y][x];
            }
        }
        return rotated;
    }

    /**
     * Applies NBT data to the blocks involved in a ritual.
     *
     * @param world    The world context.
     * @param pos      The center position of the ritual.
     * @param nbtData  The NBT data to apply.
     */
    public void applyNBTToBlocks(World world, BlockPos pos, NbtCompound nbtData) {
        // Validate the ritual at the given position
        RitualMatch ritualMatch = isValid(world, pos);
        if (ritualMatch == null || ritualMatch.getRitual() == NULL_RITUAL) {
            // No valid ritual found; abort NBT application
            return;
        }

        Ritual ritual = ritualMatch.getRitual();
        int rotation = ritualMatch.getRotation();
        Recipe<int[][]> recipe = ritual.getRecipe();
        int[][] originalMatrix = recipe.getRecipeMatrix();
        int[][] rotatedMatrix = rotateMatrix(originalMatrix, rotation);
        int ritualHeight = rotatedMatrix.length;
        int ritualWidth = rotatedMatrix[0].length;

        // Iterate through each cell in the rotated matrix
        for (int y = 0; y < ritualHeight; y++) {
            for (int x = 0; x < ritualWidth; x++) {
                int state = rotatedMatrix[y][x];
                if (state == -1) {
                    continue; // Skip positions not part of the ritual
                }

                // Calculate the target position relative to the ritual center
                BlockPos targetPos = pos.add(
                        x - ritualWidth / 2,
                        0,
                        y - ritualHeight / 2
                );

                // Check the current position, one block above, and one block below
                BlockPos[] positionsToCheck = {
                        targetPos,
                        targetPos.up(),
                        targetPos.down()
                };

                boolean validChalkBlockFound = false;
                BlockPos actualTargetPos = null;

                for (BlockPos checkPos : positionsToCheck) {
                    BlockState blockState = world.getBlockState(checkPos);
                    if (blockState.isAir()) {
                        continue; // Skip air blocks
                    }

                    // Check if the block is a chalk block with a valid orientation
                    if (blockState.getOrEmpty(ChalkMarkBlock.ORIENTATION).isPresent()) {
                        validChalkBlockFound = true;
                        actualTargetPos = checkPos;
                        break;
                    }
                }

                if (!validChalkBlockFound) {
                    continue; // Skip if no valid chalk block was found
                }

                // Modify the target position's block state
                BlockState blockState = world.getBlockState(actualTargetPos);
                int orientation = blockState.get(ChalkMarkBlock.ORIENTATION);
                Direction facing = blockState.get(ChalkMarkBlock.FACING);

                BlockState newBlockState = ModBlocks.CHALK_SYMBOL.getDefaultState()
                        .with(RitualChalk.ORIENTATION, orientation)
                        .with(RitualChalk.FACING, facing);

                // Apply the new block state
                world.setBlockState(actualTargetPos, newBlockState);
                world.markDirty(actualTargetPos);

                // Handle BlockEntity NBT data
                BlockEntity blockEntity = world.getBlockEntity(actualTargetPos);
                if (blockEntity != null) {
                    NbtCompound blockNbt = blockEntity.createNbt();
                    blockNbt.copyFrom(nbtData); // Replace with your merging logic if needed
                    blockEntity.readNbt(blockNbt);
                    blockEntity.markDirty();
                }
            }
        }
    }

    /**
     * Retrieves the orientation of the chalk block at the specified position.
     *
     * @param world The world context.
     * @param pos   The position of the chalk block.
     * @return The orientation value, or -1 if not present.
     */
    private int getChalkOrientation(World world, BlockPos pos) {
        return world.getBlockState(pos).getOrEmpty(ChalkMarkBlock.ORIENTATION).orElse(-1);
    }

    /**
     * Checks if salt is present at the specified position.
     *
     * @param world The world context.
     * @param pos   The position to check for salt.
     * @return True if salt is present; otherwise, false.
     */
    private boolean isSaltPresent(World world, BlockPos pos) {
        // Implement actual logic to check for salt blocks
        // Example:
        BlockState state = world.getBlockState(pos);
        return state.isOf(ModBlocks.SALT); // Replace with your salt block
    }

    /**
     * Checks if a position is within the Devil's Trap ritual area.
     *
     * @param world The world context.
     * @param pos   The center position to check.
     * @return True if within Devil's Trap; otherwise, false.
     */
    public boolean isInDevilsTrap(World world, BlockPos pos) {
        Ritual ritual = rituals.get("DEVILS_TRAP");
        if (ritual == null) {
            return false;
        }

        Recipe<int[][]> recipe = ritual.getRecipe();
        int[][] matrix = recipe.getRecipeMatrix();
        int ritualHeight = matrix.length;
        int ritualWidth = matrix[0].length;
        int amountNeeded = 6;
        int amount = 0;

        // Iterate through each cell in the ritual matrix
        for (int y = 0; y < ritualHeight; y++) {
            for (int x = 0; x < ritualWidth; x++) {
                int state = matrix[y][x];
                if (state == -1) {
                    continue; // Skip non-essential positions
                }

                BlockPos checkPos = pos.add(
                        x - ritualWidth / 2,
                        0,
                        y - ritualHeight / 2
                );

                BlockState stateAtPos = world.getBlockState(checkPos);
                if (stateAtPos.isOf(ModBlocks.CHALK_SYMBOL)) {
                    BlockEntity be = world.getBlockEntity(checkPos);
                    if (be instanceof RitualChalkEntity) {
                        RitualChalkEntity entity = (RitualChalkEntity) be;
                        if (entity.ritual.equals(ritual.reference.getPath())) {
                            amount++;
                        }
                    }
                }
            }
        }

        return amount >= amountNeeded;
    }
}