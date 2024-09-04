/*
 * 版权所有 (c) 2014-2024 Wurst-Imperium 和贡献者。
 *
 * 本源代码受 GNU 通用公共许可证版本 3 的条款约束。如果没有随本文件分发 GPL 副本，
 * 您可以在以下网址获取一份：https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

public class WurstTranslator implements SynchronousResourceReloader {
    private final WurstClient wurst = WurstClient.INSTANCE;
    private TranslationStorage mcEnglish;

    private final HashMap<String, String> englishOnlyStrings = new HashMap<>();
    private final HashMap<String, String> currentLangStrings = new HashMap<>();

    @Override
    public void reload(ResourceManager manager) {
        mcEnglish = TranslationStorage.load(manager, Lists.newArrayList("en_us"), false);

        currentLangStrings.clear();
        loadTranslations(manager, getCurrentLangCodes(), currentLangStrings::put);

        englishOnlyStrings.clear();
        loadTranslations(manager, List.of("en_us"), englishOnlyStrings::put);
    }

    /**
     * 使用给定的参数将给定的键翻译成当前语言，
     * 如果启用了“强制英文”设置，则翻译成英文。支持 Wurst 和原版翻译。
     */
    public String translate(String key, Object... args) {
        return isForcedEnglish() ? translateEnglish(key, args) : translateCurrent(key, args);
    }

    private String translateCurrent(String key, Object... args) {
        String string = currentLangStrings.get(key);
        if (string != null) {
            try {
                return String.format(string, args);
            } catch (IllegalFormatException e) {
                return key;
            }
        }
        return translateMc(key, args);
    }

    /**
     * 使用给定的参数将给定的键翻译成英文，无论当前语言是什么。支持 Wurst 和原版翻译。
     */
    public String translateEnglish(String key, Object... args) {
        String string = englishOnlyStrings.getOrDefault(key, mcEnglish.get(key));
        try {
            return String.format(string, args);
        } catch (IllegalFormatException e) {
            return key;
        }
    }

    /**
     * 使用给定的参数将给定的键翻译成当前语言，
     * 如果启用了“强制英文”设置，则翻译成英文，仅使用 Minecraft 自己的翻译。
     */
    public String translateMc(String key, Object... args) {
        return I18n.hasTranslation(key) ? I18n.translate(key, args) : key;
    }

    /**
     * 使用给定的参数将给定的键翻译成英文，无论当前语言是什么，仅使用 Minecraft 自己的翻译。
     */
    public String translateMcEnglish(String key, Object... args) {
        try {
            return String.format(mcEnglish.get(key), args);
        } catch (IllegalFormatException e) {
            return key;
        }
    }

    public boolean isForcedEnglish() {
        return wurst.getOtfs().translationsOtf.getForceEnglish().isChecked();
    }

    /**
     * 返回 Minecraft 英文字符串的翻译存储，无论当前语言是什么。不包括 Wurst 的任何翻译。
     */
    public TranslationStorage getMcEnglish() {
        return mcEnglish;
    }

    private ArrayList<String> getCurrentLangCodes() {
        String mainLangCode = MinecraftClient.getInstance().getLanguageManager().getLanguage().toLowerCase();
        ArrayList<String> langCodes = new ArrayList<>();
        langCodes.add("en_us");
        if (!"en_us".equals(mainLangCode)) {
            langCodes.add(mainLangCode);
        }
        return langCodes;
    }

    private void loadTranslations(ResourceManager manager, Iterable<String> langCodes, BiConsumer<String, String> entryConsumer) {
        for (String langCode : langCodes) {
            String langFilePath = "translations/" + langCode + ".json";
            Identifier langId = Identifier.of("wurst", langFilePath);
            for (Resource resource : manager.getAllResources(langId)) {
                try (InputStream stream = resource.getInputStream()) {
                    Language.load(stream, entryConsumer);
                } catch (IOException e) {
                    System.err.println("从包 " + resource.getPackId() + " 加载 " + langCode + " 的翻译失败");
                    e.printStackTrace();
                }
            }
        }
    }
}
