package it.technoade.technoblocks.api;

import org.bukkit.Material;

/**
 * A material plus its legacy data value. On 1.13+ the data value is always 0.
 */
public final class MaterialSpec {

    private final Material material;
    private final byte data;

    public MaterialSpec(Material material, byte data) {
        if (material == null) throw new IllegalArgumentException("material");
        this.material = material;
        this.data = data;
    }

    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaterialSpec)) return false;
        MaterialSpec other = (MaterialSpec) o;
        return material == other.material && data == other.data;
    }

    @Override
    public int hashCode() {
        return 31 * material.hashCode() + data;
    }

    @Override
    public String toString() {
        return material.name() + ":" + data;
    }
}
