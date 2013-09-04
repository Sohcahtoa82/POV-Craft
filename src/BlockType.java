
public enum BlockType {
	// Block IDs from http://www.minecraftwiki.net/wiki/Data_values
	AIR(0),
	STONE(1),
	GRASS_BLOCK(2),
	DIRT(3),
	COBBLESTONE (4),
	WOOD_PLANKS(5),
	SAPLING(6),
	BEDROCK(7),
	WATER_MOVING(8),
	WATER_STATIONARY(9),
	LAVA_MOVING(10),
	LAVA_STATIONARY(11),
	SAND(12),
	GRAVEL(13),
	GOLD_ORE(14),
	IRON_ORE(15),
	COAL_ORE(16),
	WOOD(17),
	LEAVES(18),
	SPONGE(19),
	GLASS(20),
	LAPIS_LAZULI_ORE(21),
	LAPIS_LAZULI_BLOCK(22),
	DISPENSER(23),
	SANDSTONE(24),
	NOTE_BLOCK(25),
	BED(26),
	POWERED_RAIL(27),
	DETECTER_RAIL(28),
	STICKY_PISTON(29),
	COBWEB(30),
	GRASS(31),
	DEAD_BRUSH(32),
	PISTON(33),
	PISTON_EXTENSION(34),
	WOOL(35),
	BLOCK_MOVED_BY_PISTON(36),
	DANDELION(37),
	ROSE(38),
	BROWN_MUSHROOM(39),
	RED_MUSHROOM(40),
	GOLD_BLOCK(41),
	IRON_BLOCK(42),
	DOUBLE_STONE_SLAB(43),
	STONE_SLAB(44),
	BRICKS(45),
	TNT(46),
	BOOKSHELF(47),
	MOSS_STONE(48),
	OBSIDIAN(49),
	TORCH(50),
	FIRE(51),
	MONSTER_SPAWNER(52),
	OAK_WOOD_STAIRS(53),
	CHEST(54),
	REDSTONE_WIRE(55),
	DIAMOND_ORE(56),
	DIAMOND_BLOCK(57),
	CRAFTING_TABLE(58),
	WHEAT(59),
	FARMLAND(60),
	FURNACE(61),
	FURNACE_BURNING(62),
	SIGN_POST(63),
	WOODEN_DOOR(64),
	LADDER(65),
	RAIL(66),
	COBBLESTONE_STAIRS(67),
	SIGN_WALL(68),
	LEVER(69),
	STONE_PRESSURE_PLATE(70),
	IRON_DOOR(71),
	WOODEN_PRESSURE_PLATE(72),
	REDSTONE_ORE(73),
	REDSTONE_ORE_GLOWING(74),
	REDSTONE_TORCH_INACTIVE(75),
	REDSTONE_TORCH_ACTIVE(76),
	STONE_BUTTON(77),
	SNOW(78),
	ICE(79),
	SNOW_BLOCK(80),
	CACTUS(81),
	CLAY(82),
	SUGAR_CANE(83),
	JUKEBOX(84),
	FENCE(85),
	PUMPKIN(86),
	NETHERRACK(87),
	SOUL_SAND(88),
	GLOWSTONE(89),
	NETHER_PORTAL(90),
	JACK_O_LANTERN(91),
	CAKE(92),
	REDSTONE_REPEATER_INACTIVE(93),
	REDSTONE_REPEATER_ACTIVE(94),
	LOCKED_CHEST(95),
	TRAPDOOR(96),
	MONSTER_EGG(97),
	STONE_BRICKS(98),
	HUGE_BROWN_MUSHROOM(99),
	HUGE_RED_MUSHROOM(100),
	IRON_BARS(101),
	GLASS_PANE(102),
	MELON(103),
	PUMPKIN_STEM(104),
	MELON_STEM(105),
	VINES(106),
	FENCE_GATE(107),
	BRICK_STAIRS(108),
	STONE_BRICK_STAIRS(109),
	MYCELIUM(110),
	LILY_PAD(111),
	NETHER_BRICK(112),
	NETHER_BRICK_FENCE(113),
	NETHER_BRICK_STAIRS(114),
	NETHER_WART(115),
	ENCHANTMENT_TABLE(116),
	BREWING_STAND(117),
	CAULDRON(118),
	END_PORTAL(119),
	END_PORTAL_BLOCK(120),
	END_STONE(121),
	DRAGON_EGG(122),
	REDSTONE_LAMP_INACTIVE(123),
	REDSTONE_LAMP_ACTIVE(124),
	WOODEN_DOUBLE_SLAB(125),
	WOODEN_SLAB(126),
	COCOA(127),
	SANDSTONE_STAIRS(128),
	EMERALD_ORE(129),
	ENDER_CHEST(130),
	TRIPWIRE_HOOK(131),
	TRIPWIRE(132),
	EMERALD_BLOCK(133),
	SPRUCE_WOOD_STAIRS(134),
	BIRCH_WOOD_STAIRS(135),
	JUNGLE_WOOD_STAIRS(136),
	COMMAND_BLOCK(137),
	BEACON(138),
	COBBLESTONE_WALL(139),
	FLOWER_POT(140),
	CARROTS(141),
	POTATOES(142),
	WOODEN_BUTTON(143),
	MOB_HEAD(144),
	ANVIL(145),
	TRAPPED_CHEST(146),
	WEIGHTED_PRESSURE_PLATE_LIGHT(147),
	WEIGHTED_PRESSURE_PLATE_HEAVY(148),
	REDSTONE_COMPARATOR_INACTIVE(149),
	REDSTONE_COMPARATOR_ACTIVE(150),
	DAYLIGHT_SENSOR(151),
	REDSTONE_BLOCK(152),
	NETHER_QUARTZ_ORE(153),
	HOPPER(154),
	QUARTZ_BLOCK(155),
	QUARTZ_STAIRS(156),
	ACTIVATOR_RAIL(157),
	DROPPER(158),
	STAINED_CLAY(159),
	// 160-169 are unused
	HAY_BLOCK(170),
	CARPET(171),
	HARDENED_CLAY(172),
	COAL_BLOCK(173)
	;
	
	static boolean[] unimplemented = new boolean[173];
	
	int blockType;
	BlockType(int blockType){
		this.blockType = blockType;
	}
	
	static String getBlockName(int type){
		return BlockType.values()[type].toString();
	}
}
