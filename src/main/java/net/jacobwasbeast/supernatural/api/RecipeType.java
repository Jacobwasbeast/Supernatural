package net.jacobwasbeast.supernatural.api;

public enum RecipeType {
    CHALK(int[][].class),
    SALT(int[][].class),
    GENERIC(Object[][].class);

    private final Class<?> matrixType;

    RecipeType(Class<?> matrixType) {
        this.matrixType = matrixType;
    }

    public Class<?> getMatrixType() {
        return matrixType;
    }

    public boolean isValidMatrix(Object matrix) {
        return matrixType.isInstance(matrix);
    }
}
