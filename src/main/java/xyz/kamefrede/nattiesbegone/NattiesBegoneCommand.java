package xyz.kamefrede.nattiesbegone;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.command.EnumArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class NattiesBegoneCommand {
	public static final Dynamic2CommandExceptionType SPAWN_REASON_NOT_PRESENT = new Dynamic2CommandExceptionType((classification, spawnReason) -> new TranslationTextComponent("nattiesbegone.spawn_reason.not_present", spawnReason, classification));

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> commandBuilder = Commands.literal("nattiesbegone")
				.requires(s -> s.hasPermissionLevel(2))
				.then(Commands.literal("help").executes(context -> {
					for (int i = 0; i < 7; i++) {
						context.getSource().sendFeedback(new TranslationTextComponent("nettiesbegone.help." + i), false);
					}
					return Command.SINGLE_SUCCESS;
				}))
				.then(Commands.literal("batswitch")
						.executes(context -> {
							Config.darkKnightFalls = !Config.darkKnightFalls;
							Config.updateConfig();
							if (Config.darkKnightFalls) {
								context.getSource().sendFeedback(new TranslationTextComponent("nettiesbegone.darkknightfalls"), false);
							} else {
								context.getSource().sendFeedback(new TranslationTextComponent("nettiesbegone.darkknightrises"), false);
							}
							return Command.SINGLE_SUCCESS;
						}))
				.then(Commands.literal("add")
						.then(Commands.argument("classification", EntityClassificationArgument.entityClassificationArgument())
								.then(Commands.argument("spawnReason", EnumArgument.enumArgument(SpawnReason.class))
										.suggests((ctx, builder) -> {
											EntityClassification classification = ctx.getArgument("classification", EntityClassification.class);
											List<SpawnReason> iterable = new ArrayList<SpawnReason>(Arrays.asList(SpawnReason.class.getEnumConstants()));
											if (Config.disallowedSpawnRules.containsKey(classification)) {
												iterable.removeAll(Config.disallowedSpawnRules.get(classification));
											}
											return ISuggestionProvider.suggest(iterable.stream().map(Object::toString), builder);
										})
										.executes(context -> {
											EntityClassification classification = context.getArgument("classification", EntityClassification.class);
											SpawnReason reason = context.getArgument("spawnReason", SpawnReason.class);
											if (Config.disallowedSpawnRules.containsKey(classification)) {
												if (Config.disallowedSpawnRules.get(classification).contains(reason)) {
													return Command.SINGLE_SUCCESS;
												}
												Config.disallowedSpawnRules.get(classification).add(reason);
											} else {
												HashSet<SpawnReason> set = new HashSet<>(Collections.singleton(reason));
												Config.disallowedSpawnRules.put(classification, set);
											}
											Config.updateConfig();
											return Command.SINGLE_SUCCESS;
										}))))
				.then(Commands.literal("remove")
						.then(Commands.argument("classification", EntityClassificationArgument.entityClassificationArgument())
								.suggests((ctx, builder) -> {
									if (Config.disallowedSpawnRules.isEmpty()) {
										return ISuggestionProvider.suggest(Collections.emptyList(), builder);
									} else {
										return ISuggestionProvider.suggest(Config.disallowedSpawnRules.keySet().stream().map(EntityClassification::getName), builder);
									}
								})
								.then(Commands.argument("spawnReason", EnumArgument.enumArgument(SpawnReason.class))
										.suggests((ctx, builder) -> {
											EntityClassification classification = ctx.getArgument("classification", EntityClassification.class);
											if (Config.disallowedSpawnRules.isEmpty() || !Config.disallowedSpawnRules.containsKey(classification)) {
												return ISuggestionProvider.suggest(Collections.emptyList(), builder);
											} else {
												return ISuggestionProvider.suggest(Config.disallowedSpawnRules.get(classification).stream().map(Objects::toString), builder);
											}
										})
										.executes(context -> {
											EntityClassification classification = context.getArgument("classification", EntityClassification.class);
											SpawnReason reason = context.getArgument("spawnReason", SpawnReason.class);
											if (Config.disallowedSpawnRules.isEmpty() || !Config.disallowedSpawnRules.containsKey(classification)) {
												throw EntityClassificationArgument.ENTITY_CLASSIFICATION_UNKNOWN.create(classification.getName());
											}
											if (!Config.disallowedSpawnRules.get(classification).contains(reason)) {
												throw SPAWN_REASON_NOT_PRESENT.create(classification.getName(), reason.toString());
											}
											Config.disallowedSpawnRules.get(classification).remove(reason);
											Config.updateConfig();
											return Command.SINGLE_SUCCESS;
										}))))
				.then(Commands.literal("get")
						.then(Commands.argument("classification", EntityClassificationArgument.entityClassificationArgument())
								.suggests((ctx, builder) -> {
									if (Config.disallowedSpawnRules.isEmpty()) {
										return ISuggestionProvider.suggest(Collections.emptyList(), builder);
									} else {
										return ISuggestionProvider.suggest(Config.disallowedSpawnRules.keySet().stream().map(EntityClassification::getName), builder);
									}
								}).executes(context -> {
									EntityClassification classification = context.getArgument("classification", EntityClassification.class);
									if (Config.disallowedSpawnRules.isEmpty() || !Config.disallowedSpawnRules.containsKey(classification)) {
										context.getSource().sendFeedback(new TranslationTextComponent("nattiesbegone.empty_classification", classification.getName()), true);
									}
									StringBuilder sb = new StringBuilder();
									Config.disallowedSpawnRules.get(classification).stream().map(SpawnReason::toString).forEach(consumer -> sb.append(consumer).append("; "));
									context.getSource().sendFeedback(new TranslationTextComponent("nattiesbegone.classification_print", classification.getName(), sb), true);
									return Command.SINGLE_SUCCESS;
								})));
		LiteralCommandNode<CommandSource> command = dispatcher.register(commandBuilder);
	}
}
