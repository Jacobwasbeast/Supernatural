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
import net.minecraft.world.World;

import java.util.HashMap;

public class RitualManager {
    public HashMap<String, Ritual> rituals = new HashMap<String, Ritual>();
    public static RitualManager instance;
    public static Ritual NULL_RITUAL;

    public static RitualManager getInstance() {
        if (instance == null) {
            instance = new RitualManager();
        }
        return instance;
    }

    public RitualManager() {
        Recipe<int[][]> devilsTrap = new Recipe<int[][]>(RecipeType.CHALK, new int[][]{
                {-1, 4, 7, 4, -1},
                {4, -1, -1, -1, 4},
                {5, -1, -1, -1, 3},
                {4, -1, -1, -1, 4},
                {-1, 4, 1, 4, -1}
        });
        Recipe<int[][]> protectionCircle = new Recipe<int[][]>(RecipeType.SALT, new int[][]{
                {-1, 0, 0, 0, -1},
                {0, -1, -1, -1, 0},
                {0, -1, -1, -1, 0},
                {0, -1, -1, -1, 0},
                {-1, 0, 0, 0, -1}
        });
        Recipe<int[][]> NULL = new Recipe<int[][]>(RecipeType.CHALK, new int[][]{
                {-1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1}
        });
        NULL_RITUAL = new Ritual("NULL", "NULL", SupernaturalMain.id("NULL"), NULL,5);
        rituals.put("DEVILS_TRAP", new Ritual("Devil's Trap", "A trap used to capture demons", SupernaturalMain.id("devilstrap"), devilsTrap,5));
        rituals.put("PROTECTION_CIRCLE", new Ritual("Protection's Circle", "A circle used to protect against supernatural entities (mainly ghosts and spirits)", SupernaturalMain.id("protectioncircle"), protectionCircle,5));
    }

    /**
     * Validates if a ritual is valid at the given position in the world.
     *
     * @param world The world context.
     * @param pos   The position to validate the ritual.
     * @return The valid Ritual if found; otherwise, NULL_RITUAL.
     */
    public RitualMatch isValid(World world, BlockPos pos) {
        for (Ritual ritual : rituals.values()) {
            Recipe<int[][]> recipe = ritual.getRecipe();
            int[][] originalMatrix = recipe.getRecipeMatrix();

            for (int rotation = 0; rotation < 4; rotation++) {
                int[][] rotatedMatrix = rotateMatrix(originalMatrix, rotation);
                boolean isValid = true;

                outerLoop:
                for (int y = 0; y < rotatedMatrix.length; y++) {
                    for (int x = 0; x < rotatedMatrix[y].length; x++) {
                        int state = rotatedMatrix[y][x];
                        if (state == -1) {
                            continue;
                        }

                        BlockPos checkPos = pos.add(
                                x - rotatedMatrix.length / 2,
                                0,
                                y - rotatedMatrix[0].length / 2
                        );
                        BlockPos checkPosUp = checkPos.up(1);
                        BlockPos checkPosDown = checkPos.down(1);

                        RecipeType type = recipe.getType();

                        switch (type) {
                            case CHALK:
                                if (getChalkOrientation(world, checkPos) != state
                                        && getChalkOrientation(world, checkPosUp) != state
                                        && getChalkOrientation(world, checkPosDown) != state) {
                                    isValid = false;
                                    break outerLoop;
                                }
                                break;

                            case SALT:
                                boolean hasSalt = isSaltPresent(world, checkPos);
                                boolean hasSaltUp = isSaltPresent(world, checkPosUp);
                                boolean hasSaltDown = isSaltPresent(world, checkPosDown);

                                if (state == 0 && !(hasSalt || hasSaltUp || hasSaltDown)) {
                                    isValid = false;
                                    break outerLoop;
                                }
                                break;

                            default:
                                // Handle other RecipeTypes if necessary
                                break;
                        }
                    }
                }

                if (isValid) {
                    return new RitualMatch(ritual, rotation);
                }
            }
        }
        return null; // Return null if no valid ritual is found
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
        int[][] matrix = rotateMatrix(originalMatrix, rotation); // Ensure matrix aligns with rotation

        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[y].length; x++) {
                int state = matrix[y][x];
                if (state == -1) {
                    continue; // Skip positions that are not part of the ritual
                }

                // Compute the target position relative to the center of the ritual matrix
                BlockPos targetPos = pos.add(
                        x - matrix[y].length / 2,
                        0,
                        y - matrix.length / 2
                );

                // Check the current position, one block above, and one block below
                BlockPos[] positionsToCheck = {
                        targetPos,
                        targetPos.up(),   // One block above
                        targetPos.down()  // One block below
                };

                boolean validChalkBlockFound = false;
                for (BlockPos checkPos : positionsToCheck) {
                    BlockState blockState = world.getBlockState(checkPos);

                    if (blockState.isAir()) {
                        continue; // Skip air blocks or uninitialized blocks
                    }

                    // Check if the block is a chalk block with a valid orientation
                    if (blockState.getOrEmpty(ChalkMarkBlock.ORIENTATION).orElse(-1) != -1) {
                        validChalkBlockFound = true;
                        targetPos = checkPos;
                        break;
                    }
                }

                if (!validChalkBlockFound) {
                    continue; // Skip if no valid chalk block was found
                }

                // Modify the target position's block state
                BlockState blockState = world.getBlockState(targetPos);
                var orientation = blockState.get(ChalkMarkBlock.ORIENTATION);
                var facing = blockState.get(ChalkMarkBlock.FACING);
                BlockState newBlockState = ModBlocks.CHALK_SYMBOL.getDefaultState()
                        .with(RitualChalk.ORIENTATION, orientation)
                        .with(RitualChalk.FACING, facing);

                // Apply the new block state
                world.setBlockState(targetPos, newBlockState);
                world.markDirty(targetPos);

                // Handle BlockEntity NBT data
                BlockEntity blockEntity = world.getBlockEntity(targetPos);
                // Apply the provided NBT data to the block entity
                NbtCompound blockNbt = blockEntity.createNbt();
                blockNbt.copyFrom(nbtData); // Merge or replace as per your requirement
                blockEntity.readNbt(blockNbt);
                blockEntity.markDirty();
            }
        }
    }

    private int getChalkOrientation(World world, BlockPos pos) {
        return world.getBlockState(pos).getOrEmpty(ChalkMarkBlock.ORIENTATION).orElse(-1);
    }

    private boolean isSaltPresent(World world, BlockPos pos) {
        // Placeholder for checking if salt is present at the specified position
        // Replace this with actual logic to determine if salt is present
        return false;
    }

    public boolean isInDevilsTrap(World world, BlockPos pos) {
        // Check a 4x4 area centered at pos for the ritual chalk block
        int amountNeeded = 6;
        int amount = 0;
        for (int x = -2; x <= 1; x++) { // Offset for 4x4 from center
            for (int z = -2; z <= 1; z++) {
                BlockPos checkPos = pos.add(x, 0, z);
                BlockState state = world.getBlockState(checkPos);
                if (state.isOf(ModBlocks.CHALK_SYMBOL)) {
                    RitualChalkEntity entity = (RitualChalkEntity)world.getBlockEntity(checkPos);
                    if (entity.ritual.equals(rituals.get("DEVILS_TRAP").reference.getPath())) {
                        amount++;
                    }
                }
            }
        }
        if (amount==amountNeeded) {
            return true;
        }
        return false;
    }
}
