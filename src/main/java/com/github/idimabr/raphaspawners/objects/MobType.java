package com.github.idimabr.raphaspawners.objects;

import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum MobType {

    AXOLOTL("Axalote"),
    BAT("Morcego"),
    BEE("Abelha"),
    BLAZE("Blaze"),
    BOAT("Barco"),
    CAT("Gato"),
    CAVE_SPIDER("Aranha da Caverna"),
    CHICKEN("Galinha"),
    COD("Bacalhau"),
    COW("Vaca"),
    CREEPER("Creeper"),
    DOLPHIN("Golfinho"),
    DONKEY("Burro"),
    DROWNED("Afogado"),
    ELDER_GUARDIAN("Guardião Mestre"),
    ENDER_DRAGON("Dragão do Fim"),
    ENDERMAN("Enderman"),
    ENDERMITE("Endermite"),
    EVOKER("Invocador"),
    FOX("Raposa"),
    GHAST("Ghast"),
    GIANT("Zumbi Gigante"),
    GLOW_ITEM_FRAME("Moldura Brilhante"),
    GLOW_SQUID("Lula Brilhante"),
    GOAT("Cabra"),
    GUARDIAN("Guardião"),
    HORSE("Cavalo"),
    HOGLIN("Hoglin"),
    HUSK("Zumbi do Deserto"),
    ILLUSIONER("Ilusionista"),
    IRON_GOLEM("Golem de Ferro"),
    LLAMA("Lhama"),
    MAGMA_CUBE("Cubo de Magma"),
    MULE("Mula"),
    MUSHROOM_COW("Vaca de Cogumelo"),
    OCELOT("Jaguatirica"),
    PANDA("Panda"),
    PARROT("Papagaio"),
    PHANTOM("Phantom"),
    PIG("Porco"),
    PIGLIN("Piglin"),
    PIGLIN_BRUTE("Piglin Bárbaro"),
    PIG_ZOMBIE("Porco Zumbi"),
    PILLAGER("Saqueador"),
    POLAR_BEAR("Urso Polar"),
    PUFFERFISH("Baiacu"),
    RABBIT("Coelho"),
    RAVAGER("Devastador"),
    SALMON("Salmão"),
    SHEEP("Ovelha"),
    SHULKER("Shulker"),
    SILVERFISH("Silverfish"),
    SKELETON("Esqueleto"),
    SKELETON_HORSE("Cavalo Esqueleto"),
    SLIME("Slime"),
    SMALL_FIREBALL("Bola de Fogo Pequena"),
    SNOWBALL("Bola de Neve"),
    SNOWMAN("Golem de Neve"),
    SPIDER("Aranha"),
    SQUID("Lula"),
    STRIDER("Lavagante"),
    STRAY("Esqueleto Vagante"),
    TRADER_LLAMA("Lhama"),
    TRIDENT("Tridente"),
    TROPICAL_FISH("Peixe Tropical"),
    TURTLE("Tartaruga"),
    VEX("Fantasma"),
    VILLAGER("Aldeão"),
    VINDICATOR("Vingador"),
    WANDERING_TRADER("Vendedor Ambulante"),
    WITCH("Bruxa"),
    WITHER("Wither"),
    WITHER_SKELETON("Esqueleto Wither"),
    WITHER_SKULL("Cabeça do Wither"),
    WOLF("Lobo"),
    ZOGLIN("Zoglin"),
    ZOMBIE("Zumbi"),
    ZOMBIE_HORSE("Cavalo Zumbi"),
    ZOMBIE_VILLAGER("Aldeão Zumbi"),
    ZOMBIFIED_PIGLIN("Piglin Zumbi");

    private String name;

    MobType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static boolean hasExists(String name){
        for (EntityType value : Arrays.stream(EntityType.values()).filter(EntityType::isAlive).collect(Collectors.toList())) {
            if(value.name().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
