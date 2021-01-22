package xyz.kamefrede.nattiesbegone;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import xyz.kamefrede.nattiesbegone.mixin.AccessorEntityClassification;

public class EntityClassificationArgument implements ArgumentType<EntityClassification> {
	public static final DynamicCommandExceptionType ENTITY_CLASSIFICATION_UNKNOWN = new DynamicCommandExceptionType((classification) -> new TranslationTextComponent("nattiesbegone.entity_classification.unknown", classification));
	private static final Collection<String> EXAMPLES = Arrays.asList("monster", "creature");

	public static EntityClassificationArgument entityClassificationArgument() {
		return new EntityClassificationArgument();
	}

	@Override
	public EntityClassification parse(StringReader reader) throws CommandSyntaxException {
		String classification_string = reader.readUnquotedString();
		if (!AccessorEntityClassification.getVALUES_MAP().containsKey(classification_string))
			throw ENTITY_CLASSIFICATION_UNKNOWN.create(classification_string);
		return AccessorEntityClassification.getVALUES_MAP().get(classification_string);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return ISuggestionProvider.suggest(AccessorEntityClassification.getVALUES_MAP().keySet(), builder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
