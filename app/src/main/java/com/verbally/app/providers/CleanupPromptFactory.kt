package com.verbally.app.providers

import com.verbally.app.DictationContentGuard
import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.settings.AppLanguage
import com.verbally.app.style.AppCategory
import com.verbally.app.style.CleanupStyleContext
import com.verbally.app.style.OutputStyle
import com.verbally.app.style.StyleRuleDefaults

object CleanupPromptFactory {
    const val TranscriptPlaceholder = "{{transcript}}"
    private const val DictionaryContextLimit = 100

    val defaultCleanupPrompt: String = defaultCleanupPromptFor(AppLanguage.TRADITIONAL_CHINESE)

    fun defaultCleanupPromptFor(language: AppLanguage): String = DefaultCleanupPrompts.forLanguage(language)

    fun isBuiltInDefaultPrompt(prompt: String): Boolean {
        val normalizedPrompt = prompt.trim()
        return AppLanguage.entries.any { language ->
            normalizedPrompt == defaultCleanupPromptFor(language).trim()
        }
    }

    fun cleanupPrompt(
        promptTemplate: String,
        rawTranscript: String,
        dictionaryEntries: List<DictionaryEntry> = emptyList(),
        styleContext: CleanupStyleContext? = null,
    ): String {
        if (styleContext != null) {
            return styledCleanupPrompt(promptTemplate, rawTranscript, dictionaryEntries, styleContext)
        }
        val template = promptTemplate.trim().ifBlank { defaultCleanupPrompt }
        val prompt = if (template.contains(TranscriptPlaceholder)) {
            template.replace(TranscriptPlaceholder, rawTranscript)
        } else {
            """
                $template

                原始轉錄：
                $rawTranscript
            """.trimIndent()
        }
        val promptWithNoDictationRule = insertBeforeTranscriptLabel(
            prompt = prompt,
            block = noDictationInstruction(AppLanguage.TRADITIONAL_CHINESE),
        )
        val dictionaryContext = dictionaryContext(dictionaryEntries)
        if (dictionaryContext.isBlank()) return promptWithNoDictationRule

        val transcriptLabelIndex = promptWithNoDictationRule.indexOf("原始轉錄：")
        return if (transcriptLabelIndex >= 0) {
            buildString {
                append(promptWithNoDictationRule.substring(0, transcriptLabelIndex).trimEnd())
                append("\n\n")
                append(dictionaryContext)
                append("\n\n")
                append(promptWithNoDictationRule.substring(transcriptLabelIndex))
            }
        } else {
            """
                $promptWithNoDictationRule

                $dictionaryContext
            """.trimIndent()
        }
    }

    private fun styledCleanupPrompt(
        promptTemplate: String,
        rawTranscript: String,
        dictionaryEntries: List<DictionaryEntry>,
        styleContext: CleanupStyleContext,
    ): String {
        val basicPrompt = basicPromptInstructions(promptTemplate, styleContext.language)
        val labels = promptLabels(styleContext.language)
        val dictionaryContext = dictionaryContext(dictionaryEntries, styleContext.language)
        return buildString {
            appendLine(processingOrderInstructions(styleContext.language))
            appendLine()
            appendLine(labels.basicPrompt)
            appendLine(basicPrompt)
            appendLine()
            appendLine(noDictationInstruction(styleContext.language))
            appendLine()
            if (dictionaryContext.isNotBlank()) {
                appendLine(dictionaryContext)
                appendLine()
            }
            appendLine("${labels.appCategory}${categoryLabel(styleContext.category, styleContext.language)}")
            appendLine(styleInstructions(styleContext))
            appendLine()
            appendLine(labels.originalTranscript)
            append(rawTranscript)
        }
    }

    private fun basicPromptInstructions(promptTemplate: String, language: AppLanguage): String {
        val template = promptTemplate.trim().ifBlank { defaultCleanupPrompt }
        val withoutDefaultTranscriptBlock = stripTranscriptPlaceholderBlock(template)
        return withoutDefaultTranscriptBlock
            .replace(TranscriptPlaceholder, transcriptReference(language))
            .trim()
    }

    private fun insertBeforeTranscriptLabel(prompt: String, block: String): String {
        val transcriptLabelIndex = transcriptFooterLabels()
            .map { prompt.indexOf(it) }
            .filter { it >= 0 }
            .minOrNull()

        return if (transcriptLabelIndex == null) {
            """
                $prompt

                $block
            """.trimIndent()
        } else {
            buildString {
                append(prompt.substring(0, transcriptLabelIndex).trimEnd())
                append("\n\n")
                append(block)
                append("\n\n")
                append(prompt.substring(transcriptLabelIndex))
            }
        }
    }

    private fun noDictationInstruction(language: AppLanguage): String {
        val sentinel = DictationContentGuard.NoDictationSentinel
        return when (language.normalizedPromptLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> """
                没有可听写内容规则：
                - 如果原始转录没有使用者说出的内容，或只有背景声、音乐、鸟叫、杂音、字幕署名，或常见空白幻觉（例如 you、the、I'm going to go to the next video），请只输出 $sentinel。
                - 不要输出「请输入内容」之类的提示文字。
            """.trimIndent()
            AppLanguage.SYSTEM,
            AppLanguage.TRADITIONAL_CHINESE -> """
                沒有可聽寫內容規則：
                - 如果原始轉錄沒有使用者說出的內容，或只有背景聲、音樂、鳥叫、雜音、字幕署名，或常見空白幻覺（例如 you、the、I'm going to go to the next video），請只輸出 $sentinel。
                - 不要輸出「請輸入內容」之類的提示文字。
            """.trimIndent()
            else -> """
                No-dictation rule:
                - If the original transcript contains no user-spoken content, or only background sound, music, bird sounds, noise, subtitle credits, or common empty-audio hallucinations such as "you", "the", or "I'm going to go to the next video", output only $sentinel.
                - Do not output prompts such as "please enter content".
            """.trimIndent()
        }
    }

    private fun stripTranscriptPlaceholderBlock(template: String): String {
        val placeholderIndex = template.indexOf(TranscriptPlaceholder)
        if (placeholderIndex < 0) return template

        val beforePlaceholder = template.substring(0, placeholderIndex).trimEnd()
        val labelLineStart = beforePlaceholder.lastIndexOf('\n').let { lastNewline ->
            if (lastNewline < 0) 0 else lastNewline + 1
        }
        val labelLine = beforePlaceholder.substring(labelLineStart).trim()
        if (!transcriptFooterLabels().contains(labelLine)) return template

        return template.substring(0, labelLineStart).trimEnd()
    }

    private fun transcriptFooterLabels(): Set<String> = setOf(
        "Original transcript:",
        "Transcripción original:",
        "Transcription originale :",
        "Originaltranskript:",
        "Trascrizione originale:",
        "Transcrição original:",
        "元の文字起こし：",
        "원문 전사:",
        "原始转录：",
        "原始轉錄：",
    )

    private fun transcriptReference(language: AppLanguage): String =
        when (language.normalizedPromptLanguage()) {
            AppLanguage.ENGLISH -> "the original transcript below"
            AppLanguage.SPANISH -> "la transcripción original de abajo"
            AppLanguage.FRENCH -> "la transcription originale ci-dessous"
            AppLanguage.GERMAN -> "das unten stehende Originaltranskript"
            AppLanguage.ITALIAN -> "la trascrizione originale qui sotto"
            AppLanguage.PORTUGUESE_BRAZIL -> "a transcrição original abaixo"
            AppLanguage.JAPANESE -> "下の元の文字起こし"
            AppLanguage.KOREAN -> "아래 원문 전사"
            AppLanguage.SIMPLIFIED_CHINESE -> "下方原始转录"
            AppLanguage.SYSTEM,
            AppLanguage.TRADITIONAL_CHINESE -> "下方原始轉錄"
        }

    private fun styleInstructions(styleContext: CleanupStyleContext): String {
        val rule = styleContext.customRule
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: StyleRuleDefaults.defaultRuleFor(styleContext.language, styleContext.style)
        val labels = promptLabels(styleContext.language)
        return buildString {
            appendLine("${labels.outputFormat}${styleContext.style.label}")
            appendLine(labels.styleRule(styleContext.style))
            appendLine(rule)
            appendLine()
            append(StyleRuleDefaults.mandatoryGuardrails(styleContext.language))
        }.trimEnd()
    }

    private fun dictionaryContext(
        entries: List<DictionaryEntry>,
        language: AppLanguage = AppLanguage.TRADITIONAL_CHINESE,
    ): String {
        val lines = entries
            .mapNotNull { entry ->
                val term = entry.term.trim()
                if (term.isBlank()) {
                    null
                } else {
                    val note = entry.note?.trim().orEmpty()
                    if (note.isBlank()) "- $term" else "- $term：$note"
                }
            }
            .take(DictionaryContextLimit)

        if (lines.isEmpty()) return ""
        return buildString {
            appendLine(promptLabels(language).dictionary)
            appendLine(dictionaryInstruction(language))
            lines.forEach { appendLine(it) }
        }.trimEnd()
    }

    private fun processingOrderInstructions(language: AppLanguage): String =
        when (language.normalizedPromptLanguage()) {
            AppLanguage.ENGLISH -> """
                Processing order:
                1. Apply basic text processing first.
                2. Then adjust punctuation, spacing, capitalization, and line breaks according to the output format.
                3. The output format controls formatting only; it is not permission to rewrite content.
            """.trimIndent()
            AppLanguage.SPANISH -> """
                Orden de procesamiento:
                1. Aplica primero el procesamiento básico de texto.
                2. Luego ajusta puntuación, espacios, mayúsculas y saltos de línea según el formato de salida.
                3. El formato de salida solo controla el formato; no autoriza reescribir el contenido.
            """.trimIndent()
            AppLanguage.FRENCH -> """
                Ordre de traitement :
                1. Applique d'abord le traitement de base du texte.
                2. Ajuste ensuite la ponctuation, les espaces, les majuscules et les retours à la ligne selon le format de sortie.
                3. Le format de sortie contrôle seulement la forme ; il n'autorise pas la réécriture du contenu.
            """.trimIndent()
            AppLanguage.GERMAN -> """
                Verarbeitungsreihenfolge:
                1. Wende zuerst die grundlegende Textverarbeitung an.
                2. Passe danach Zeichensetzung, Abstände, Groß-/Kleinschreibung und Zeilenumbrüche entsprechend dem Ausgabeformat an.
                3. Das Ausgabeformat steuert nur die Formatierung; es ist keine Erlaubnis, Inhalte umzuschreiben.
            """.trimIndent()
            AppLanguage.ITALIAN -> """
                Ordine di elaborazione:
                1. Applica prima il trattamento di base del testo.
                2. Poi regola punteggiatura, spazi, maiuscole e interruzioni di riga in base al formato di uscita.
                3. Il formato di uscita controlla solo la formattazione; non autorizza a riscrivere il contenuto.
            """.trimIndent()
            AppLanguage.PORTUGUESE_BRAZIL -> """
                Ordem de processamento:
                1. Aplique primeiro o processamento básico de texto.
                2. Depois ajuste pontuação, espaçamento, maiúsculas e quebras de linha conforme o formato de saída.
                3. O formato de saída controla apenas a formatação; não é permissão para reescrever o conteúdo.
            """.trimIndent()
            AppLanguage.JAPANESE -> """
                処理順序：
                1. 先に基本文字処理を行います。
                2. その後、出力形式に合わせて句読点、空白、改行を調整します。
                3. 出力形式は形式だけを制御し、内容を書き換える指示ではありません。
            """.trimIndent()
            AppLanguage.KOREAN -> """
                처리 순서:
                1. 먼저 기본 텍스트 처리를 적용하세요.
                2. 그다음 출력 형식에 맞춰 문장 부호, 띄어쓰기, 줄바꿈을 조정하세요.
                3. 출력 형식은 형식만 제어하며, 내용을 다시 쓰라는 허가가 아닙니다.
            """.trimIndent()
            AppLanguage.SIMPLIFIED_CHINESE -> """
                处理顺序：
                1. 先做基本文字处理。
                2. 再依照输出格式调整标点、空格与分段。
                3. 输出格式只控制格式，不是重写内容。
            """.trimIndent()
            AppLanguage.SYSTEM,
            AppLanguage.TRADITIONAL_CHINESE -> """
                處理順序：
                1. 先做基本文字處理。
                2. 再依照輸出格式調整標點、空格與分段。
                3. 輸出格式只控制格式，不是重寫內容。
            """.trimIndent()
        }

    private fun promptLabels(language: AppLanguage): PromptLabels =
        when (language.normalizedPromptLanguage()) {
            AppLanguage.ENGLISH -> PromptLabels(
                basicPrompt = "Basic text-processing prompt:",
                appCategory = "App category: ",
                outputFormat = "Output format: ",
                originalTranscript = "Original transcript:",
                dictionary = "User dictionary:",
                styleRule = { style -> "${style.label} rule:" },
            )
            AppLanguage.SPANISH -> PromptLabels(
                basicPrompt = "Indicaciones básicas de procesamiento de texto:",
                appCategory = "Categoría de app: ",
                outputFormat = "Formato de salida: ",
                originalTranscript = "Transcripción original:",
                dictionary = "Diccionario del usuario:",
                styleRule = { style -> "Reglas ${style.label}:" },
            )
            AppLanguage.FRENCH -> PromptLabels(
                basicPrompt = "Invite de traitement de base du texte :",
                appCategory = "Catégorie d'app : ",
                outputFormat = "Format de sortie : ",
                originalTranscript = "Transcription originale :",
                dictionary = "Dictionnaire utilisateur :",
                styleRule = { style -> "Règles ${style.label} :" },
            )
            AppLanguage.GERMAN -> PromptLabels(
                basicPrompt = "Prompt für grundlegende Textverarbeitung:",
                appCategory = "App-Kategorie: ",
                outputFormat = "Ausgabeformat: ",
                originalTranscript = "Originaltranskript:",
                dictionary = "Benutzerwörterbuch:",
                styleRule = { style -> "${style.label}-Regeln:" },
            )
            AppLanguage.ITALIAN -> PromptLabels(
                basicPrompt = "Prompt di base per il trattamento del testo:",
                appCategory = "Categoria app: ",
                outputFormat = "Formato di uscita: ",
                originalTranscript = "Trascrizione originale:",
                dictionary = "Dizionario utente:",
                styleRule = { style -> "Regole ${style.label}:" },
            )
            AppLanguage.PORTUGUESE_BRAZIL -> PromptLabels(
                basicPrompt = "Prompt básico de processamento de texto:",
                appCategory = "Categoria do app: ",
                outputFormat = "Formato de saída: ",
                originalTranscript = "Transcrição original:",
                dictionary = "Dicionário do usuário:",
                styleRule = { style -> "Regras ${style.label}:" },
            )
            AppLanguage.JAPANESE -> PromptLabels(
                basicPrompt = "基本文字処理プロンプト：",
                appCategory = "Appカテゴリ：",
                outputFormat = "出力形式：",
                originalTranscript = "元の文字起こし：",
                dictionary = "ユーザー辞書：",
                styleRule = { style -> "${style.label} ルール：" },
            )
            AppLanguage.KOREAN -> PromptLabels(
                basicPrompt = "기본 텍스트 처리 프롬프트:",
                appCategory = "앱 카테고리: ",
                outputFormat = "출력 형식: ",
                originalTranscript = "원문 전사:",
                dictionary = "사용자 사전:",
                styleRule = { style -> "${style.label} 규칙:" },
            )
            AppLanguage.SIMPLIFIED_CHINESE -> PromptLabels(
                basicPrompt = "基本文字处理提示词：",
                appCategory = "App 类别：",
                outputFormat = "输出格式：",
                originalTranscript = "原始转录：",
                dictionary = "用户字典：",
                styleRule = { style -> "${style.label} 规则：" },
            )
            AppLanguage.SYSTEM,
            AppLanguage.TRADITIONAL_CHINESE -> PromptLabels(
                basicPrompt = "基本文字處理提示詞：",
                appCategory = "App 類別：",
                outputFormat = "輸出格式：",
                originalTranscript = "原始轉錄：",
                dictionary = "使用者字典：",
                styleRule = { style -> "${style.label} 規則：" },
            )
        }

    private fun dictionaryInstruction(language: AppLanguage): String =
        when (language.normalizedPromptLanguage()) {
            AppLanguage.ENGLISH ->
                "Prefer these spellings when they are relevant to the original transcript; do not add content that is not present."
            AppLanguage.SPANISH ->
                "Prefiere estas grafías cuando sean relevantes para la transcripción original; no agregues contenido que no esté presente."
            AppLanguage.FRENCH ->
                "Préfère ces graphies lorsqu'elles sont pertinentes pour la transcription originale ; n'ajoute pas de contenu absent."
            AppLanguage.GERMAN ->
                "Bevorzuge diese Schreibweisen, wenn sie zum Originaltranskript passen; füge keine nicht vorhandenen Inhalte hinzu."
            AppLanguage.ITALIAN ->
                "Preferisci queste grafie quando sono pertinenti alla trascrizione originale; non aggiungere contenuti assenti."
            AppLanguage.PORTUGUESE_BRAZIL ->
                "Prefira estas grafias quando forem relevantes para a transcrição original; não adicione conteúdo que não esteja presente."
            AppLanguage.JAPANESE ->
                "元の文字起こしの意味に関係する場合だけ、以下の表記を優先してください。元にない内容は追加しないでください。"
            AppLanguage.KOREAN ->
                "원문 전사와 관련이 있을 때만 아래 표기를 우선하세요. 원문에 없는 내용은 추가하지 마세요."
            AppLanguage.SIMPLIFIED_CHINESE ->
                "优先保留以下词汇的写法；只有在原始转录语义相关时使用，不要新增原文没有的内容。"
            AppLanguage.SYSTEM,
            AppLanguage.TRADITIONAL_CHINESE ->
                "優先保留以下詞彙的寫法；只有在原始轉錄語意相關時使用，不要新增原文沒有的內容。"
        }

    private fun categoryLabel(category: AppCategory, language: AppLanguage): String =
        when (language.normalizedPromptLanguage()) {
            AppLanguage.ENGLISH -> when (category) {
                AppCategory.CHAT -> "Chat"
                AppCategory.WORK -> "Work"
                AppCategory.OTHER -> "Other"
            }
            AppLanguage.SPANISH -> when (category) {
                AppCategory.CHAT -> "Chat"
                AppCategory.WORK -> "Trabajo"
                AppCategory.OTHER -> "Otro"
            }
            AppLanguage.FRENCH -> when (category) {
                AppCategory.CHAT -> "Chat"
                AppCategory.WORK -> "Travail"
                AppCategory.OTHER -> "Autre"
            }
            AppLanguage.GERMAN -> when (category) {
                AppCategory.CHAT -> "Chat"
                AppCategory.WORK -> "Arbeit"
                AppCategory.OTHER -> "Andere"
            }
            AppLanguage.ITALIAN -> when (category) {
                AppCategory.CHAT -> "Chat"
                AppCategory.WORK -> "Lavoro"
                AppCategory.OTHER -> "Altro"
            }
            AppLanguage.PORTUGUESE_BRAZIL -> when (category) {
                AppCategory.CHAT -> "Chat"
                AppCategory.WORK -> "Trabalho"
                AppCategory.OTHER -> "Outros"
            }
            AppLanguage.JAPANESE -> when (category) {
                AppCategory.CHAT -> "チャット"
                AppCategory.WORK -> "仕事"
                AppCategory.OTHER -> "その他"
            }
            AppLanguage.KOREAN -> when (category) {
                AppCategory.CHAT -> "채팅"
                AppCategory.WORK -> "업무"
                AppCategory.OTHER -> "기타"
            }
            AppLanguage.SIMPLIFIED_CHINESE -> when (category) {
                AppCategory.CHAT -> "聊天"
                AppCategory.WORK -> "工作"
                AppCategory.OTHER -> "其他"
            }
            AppLanguage.SYSTEM,
            AppLanguage.TRADITIONAL_CHINESE -> category.label
        }

    private data class PromptLabels(
        val basicPrompt: String,
        val appCategory: String,
        val outputFormat: String,
        val originalTranscript: String,
        val dictionary: String,
        val styleRule: (OutputStyle) -> String,
    )

    private fun AppLanguage.normalizedPromptLanguage(): AppLanguage =
        if (this == AppLanguage.SYSTEM) AppLanguage.ENGLISH else this
}
