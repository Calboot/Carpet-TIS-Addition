package carpettisaddition.translations;

import carpet.CarpetSettings;
import carpettisaddition.CarpetTISAdditionServer;
import carpettisaddition.CarpetTISAdditionSettings;
import carpettisaddition.mixins.translations.ServerPlayerEntityAccessor;
import carpettisaddition.mixins.translations.StyleAccessor;
import carpettisaddition.utils.FileUtil;
import carpettisaddition.utils.Messenger;
import com.google.common.collect.Maps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TISAdditionTranslations
{
    public static final String DEFAULT_LANGUAGE = "en_us";
    public static final String TRANSLATION_NAMESPACE = CarpetTISAdditionServer.compactName;  // "carpettisaddition"
    public static final String TRANSLATION_KEY_PREFIX = TRANSLATION_NAMESPACE + ".";  // "carpettisaddition."
    private static final String RESOURCE_DIR = String.format("assets/%s/lang", TRANSLATION_NAMESPACE);

    @VisibleForTesting
    public static final Map<String, Map<String, String>> translationStorage = Maps.newLinkedHashMap();

    @NotNull
    public static Map<String, String> getTranslationFromResourcePath(String lang)
    {
        return translationStorage.getOrDefault(lang, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public static void loadTranslations()
    {
        try
        {
            String dataStr = FileUtil.readResourceFileAsString(RESOURCE_DIR + "/meta/languages.yml");
            Map<String, Object> yamlMap = new Yaml().load(dataStr);
            ((List<String>)yamlMap.get("languages")).forEach(TISAdditionTranslations::loadTranslation);
        }
        catch (Exception e)
        {
            CarpetTISAdditionServer.LOGGER.error("Failed to read language list", e);
        }
    }

    private static void loadTranslation(String lang)
    {
        translationStorage.computeIfAbsent(lang, l -> {
            String dataStr;
            try
            {
                dataStr = FileUtil.readResourceFileAsString(String.format("%s/%s.yml", RESOURCE_DIR, lang));
            }
            catch (IOException e)
            {
                return Collections.emptyMap();
            }
            try
            {
                Map<String, Object> yamlMap = new Yaml().load(dataStr);
                Map<String, String> translation = Maps.newLinkedHashMap();
                build(translation, yamlMap, "");
                return translation;
            }
            catch (Exception e)
            {
                CarpetTISAdditionServer.LOGGER.error("Failed to load translation of language " + lang, e);
                return Collections.emptyMap();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static void build(Map<String, String> translation, Map<String, Object> yamlMap, String prefix)
    {
        yamlMap.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key : (!key.equals(".") ? prefix + "." + key : prefix);
            if (value instanceof String)
            {
                translation.put(fullKey, (String)value);
            }
            else if (value instanceof Map)
            {
                build(translation, (Map<String, Object>)value, fullKey);
            }
            else
            {
                throw new RuntimeException(String.format("Unknown type %s in with key %s", value.getClass(), fullKey));
            }
        });
    }

    public static String getServerLanguage()
    {
        return CarpetSettings.language.equalsIgnoreCase("none") ? DEFAULT_LANGUAGE : CarpetSettings.language;
    }

    /**
     * key -> translated formatting string
     */
    @Nullable
    public static String translateKeyToFormattingString(String lang, String key)
    {
        return getTranslationFromResourcePath(lang.toLowerCase()).get(key);
    }

    public static BaseText translate(BaseText text, String lang)
    {
        return translateText(Messenger.copy(text), lang);
    }

    public static BaseText translate(BaseText text)
    {
        return translate(text, getServerLanguage());
    }

    public static BaseText translate(BaseText text, ServerPlayerEntity player)
    {
        if (CarpetTISAdditionSettings.ultraSecretSetting.equals("translation"))
        {
            return translate(text);
        }
        return translate(text, ((ServerPlayerEntityAccessor)player).getClientLanguage());
    }

    private static BaseText translateText(BaseText text, @NotNull String lang)
    {
        if (text instanceof TranslatableText)
        {
            TranslatableText translatableText = (TranslatableText)text;

            // translate arguments
            for (int i = 0; i < translatableText.getArgs().length; i++)
            {
                Object arg = translatableText.getArgs()[i];
                if (arg instanceof BaseText)
                {
                    translatableText.getArgs()[i] = translateText((BaseText)arg, lang);
                }
            }

            // do translation logic
            if (translatableText.getKey().startsWith(TRANSLATION_KEY_PREFIX))
            {
                String msgKeyString = translateKeyToFormattingString(lang, translatableText.getKey());
                if (msgKeyString == null && !lang.equals(DEFAULT_LANGUAGE))
                {
                    msgKeyString = translateKeyToFormattingString(DEFAULT_LANGUAGE, translatableText.getKey());
                }
                if (msgKeyString != null)
                {
                    BaseText origin = text;
                    try
                    {
                        text = Messenger.format(msgKeyString, translatableText.getArgs());
                    }
                    catch (IllegalArgumentException e)
                    {
                        text = Messenger.s(msgKeyString);
                    }

                    // migrating text data
                    text.getSiblings().addAll(origin.getSiblings());
                    text.setStyle(origin.getStyle());
                }
                else
                {
                    CarpetTISAdditionServer.LOGGER.warn("TISCM: Unknown translation key {}", translatableText.getKey());
                }
            }
        }

        // translate hover text
        HoverEvent hoverEvent = ((StyleAccessor)text.getStyle()).getHoverEventField();
        if (hoverEvent != null)
        {
            text.getStyle().setHoverEvent(new HoverEvent(hoverEvent.getAction(), translateText((BaseText)hoverEvent.getValue(), lang)));
        }

        // translate sibling texts
        List<Text> siblings = text.getSiblings();
        for (int i = 0; i < siblings.size(); i++)
        {
            siblings.set(i, translateText((BaseText)siblings.get(i), lang));
        }
        return text;
    }
}
