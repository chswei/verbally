package com.verbally.app.providers

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

    fun defaultCleanupPromptFor(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> """
            Task:
            Please turn the following voice transcript into natural text that can be pasted directly into the current text field.

            Basic text-processing rules:
            - Preserve the original language, tone, and any mixed-language/code-switching ratio.
            - Do not translate, and do not convert mixed-language text into a single language.
            - Remove only obvious speech-recognition noise, stutter-like repetition, or filler words the user clearly did not intend to keep.
            - Correct only clear transcription mistakes; if uncertain, keep the original word.
            - Do not add facts that are not in the original transcript.
            - Do not polish the wording to be prettier, more formal, cuter, or smoother.
            - Output only the cleaned text, with no explanation.

            Original transcript:
            $TranscriptPlaceholder
        """.trimIndent()
        AppLanguage.SPANISH -> """
            Tarea:
            Convierte la siguiente transcripción de voz en texto natural que pueda pegarse directamente en el campo de texto actual.

            Reglas básicas de procesamiento de texto:
            - Conserva el idioma original, el tono y la proporción de cualquier mezcla de idiomas.
            - No traduzcas ni conviertas el texto multilingüe en un solo idioma.
            - Elimina solo ruido evidente de reconocimiento de voz, repeticiones tipo tartamudeo o muletillas que el usuario claramente no quiso conservar.
            - Corrige solo errores claros de transcripción; si no estás seguro, conserva la palabra original.
            - No agregues hechos que no estén en la transcripción original.
            - No embellezcas la redacción para que sea más bonita, formal, cercana o fluida.
            - Devuelve solo el texto limpio, sin explicación.

            Transcripción original:
            $TranscriptPlaceholder
        """.trimIndent()
        AppLanguage.FRENCH -> """
            Tâche :
            Transforme la transcription vocale suivante en texte naturel pouvant être collé directement dans le champ de texte actuel.

            Règles de traitement de base :
            - Conserve la langue d'origine, le ton et la proportion des éventuels mélanges de langues.
            - Ne traduis pas et ne transforme pas un texte multilingue en une seule langue.
            - Supprime uniquement le bruit évident de reconnaissance vocale, les répétitions de type bégaiement ou les mots de remplissage que l'utilisateur ne voulait clairement pas garder.
            - Corrige seulement les erreurs de transcription évidentes ; en cas de doute, garde le mot d'origine.
            - N'ajoute aucun fait absent de la transcription originale.
            - Ne polis pas le texte pour le rendre plus joli, plus formel, plus mignon ou plus fluide.
            - Ne renvoie que le texte nettoyé, sans explication.

            Transcription originale :
            $TranscriptPlaceholder
        """.trimIndent()
        AppLanguage.GERMAN -> """
            Aufgabe:
            Wandle die folgende Sprachtranskription in natürlichen Text um, der direkt in das aktuelle Textfeld eingefügt werden kann.

            Regeln für die grundlegende Textverarbeitung:
            - Behalte die ursprüngliche Sprache, den Ton und das Verhältnis gemischter Sprachen bei.
            - Übersetze nicht und vereinheitliche mehrsprachigen Text nicht zu einer einzigen Sprache.
            - Entferne nur offensichtliches Spracherkennungsrauschen, stotterartige Wiederholungen oder Füllwörter, die der Nutzer eindeutig nicht behalten wollte.
            - Korrigiere nur eindeutige Transkriptionsfehler; wenn du unsicher bist, behalte das ursprüngliche Wort.
            - Füge keine Fakten hinzu, die nicht im Originaltranskript vorkommen.
            - Glätte den Text nicht, um ihn schöner, formeller, niedlicher oder flüssiger wirken zu lassen.
            - Gib nur den bereinigten Text aus, ohne Erklärung.

            Originaltranskript:
            $TranscriptPlaceholder
        """.trimIndent()
        AppLanguage.ITALIAN -> """
            Compito:
            Trasforma la seguente trascrizione vocale in testo naturale da incollare direttamente nel campo di testo corrente.

            Regole di base per il trattamento del testo:
            - Mantieni la lingua originale, il tono e la proporzione di eventuali lingue mescolate.
            - Non tradurre e non trasformare il testo multilingue in una sola lingua.
            - Rimuovi solo rumore evidente del riconoscimento vocale, ripetizioni simili a balbettii o intercalari che l'utente chiaramente non voleva conservare.
            - Correggi solo errori chiari di trascrizione; se non sei sicuro, conserva la parola originale.
            - Non aggiungere fatti che non compaiono nella trascrizione originale.
            - Non lucidare il testo per renderlo più bello, formale, carino o scorrevole.
            - Restituisci solo il testo ripulito, senza spiegazioni.

            Trascrizione originale:
            $TranscriptPlaceholder
        """.trimIndent()
        AppLanguage.PORTUGUESE_BRAZIL -> """
            Tarefa:
            Transforme a seguinte transcrição de voz em um texto natural que possa ser colado diretamente no campo de texto atual.

            Regras básicas de processamento de texto:
            - Preserve o idioma original, o tom e a proporção de qualquer mistura de idiomas.
            - Não traduza nem transforme texto multilíngue em um único idioma.
            - Remova apenas ruído evidente de reconhecimento de voz, repetições tipo gagueira ou vícios de fala que o usuário claramente não quis manter.
            - Corrija apenas erros claros de transcrição; se não tiver certeza, preserve a palavra original.
            - Não adicione fatos que não estejam na transcrição original.
            - Não refine o texto para deixá-lo mais bonito, formal, fofo ou fluido.
            - Retorne apenas o texto limpo, sem explicação.

            Transcrição original:
            $TranscriptPlaceholder
        """.trimIndent()
        AppLanguage.JAPANESE -> """
            タスク：
            次の音声文字起こしを、現在の入力欄にそのまま貼り付けられる自然な文章に整えてください。

            基本文字処理ルール：
            - 元の言語、口調、複数言語が混ざっている比率を保ってください。
            - 翻訳しないでください。複数言語の文章を一つの言語に統一しないでください。
            - 明らかな音声認識ノイズ、どもりのような重複、ユーザーが明らかに残したくなかった口癖だけを取り除いてください。
            - 明らかな文字起こしの誤りだけを修正してください。迷う場合は元の語を残してください。
            - 元の文字起こしにない事実を追加しないでください。
            - 文章をよりきれい、丁寧、かわいい、滑らかにするための言い換えはしないでください。
            - 整えた文章だけを出力し、説明は加えないでください。

            元の文字起こし：
            $TranscriptPlaceholder
        """.trimIndent()
        AppLanguage.KOREAN -> """
            작업:
            다음 음성 전사 내용을 현재 입력란에 바로 붙여 넣을 수 있는 자연스러운 문장으로 정리해 주세요.

            기본 텍스트 처리 규칙:
            - 원래 언어, 말투, 여러 언어가 섞인 비율을 유지하세요.
            - 번역하지 말고, 여러 언어가 섞인 내용을 하나의 언어로 바꾸지 마세요.
            - 명백한 음성 인식 노이즈, 말더듬처럼 반복된 말, 사용자가 분명히 남기고 싶지 않았던 군말만 제거하세요.
            - 명백한 전사 오류만 수정하세요. 확실하지 않으면 원래 단어를 유지하세요.
            - 원문 전사에 없는 사실을 추가하지 마세요.
            - 문장을 더 예쁘게, 격식 있게, 귀엽게, 매끄럽게 보이도록 다듬지 마세요.
            - 설명 없이 정리된 텍스트만 출력하세요.

            원문 전사:
            $TranscriptPlaceholder
        """.trimIndent()
        AppLanguage.SIMPLIFIED_CHINESE -> """
            任务：
            请将以下语音转录整理成可以直接粘贴到当前文本框的自然文字。

            基本文字处理规则：
            - 保留原本语言、语气与混合语言比例。
            - 不要翻译，不要把混合语言改成单一语言。
            - 只移除明显语音识别噪声、口吃式重复，或用户明显不想留下的口头填充。
            - 只修正明显的语音识别错误；如果不确定，保留原字。
            - 不要新增原文没有的事实。
            - 不要润色成更漂亮、更正式、更可爱或更顺的说法。
            - 只输出整理后的文字，不要加说明。

            原始转录：
            $TranscriptPlaceholder
        """.trimIndent()
        AppLanguage.SYSTEM,
        AppLanguage.TRADITIONAL_CHINESE -> """
            任務：
            請將以下語音轉錄整理成可以直接貼到目前文字框的自然文字。

            基本文字處理規則：
            - 保留原本語言、語氣與中英混用比例。
            - 不要翻譯，不要把中英混用改成單一語言。
            - 只移除明顯語音辨識雜訊、口吃式重複，或使用者明顯不想留下的口頭填充。
            - 只修正明顯的語音辨識錯誤；如果不確定，保留原字。
            - 不要新增原文沒有的事實。
            - 不要潤飾成更漂亮、更正式、更可愛或更順的說法。
            - 只輸出整理後文字，不要加說明。

            原始轉錄：
            $TranscriptPlaceholder
        """.trimIndent()
    }

    fun isBuiltInDefaultPrompt(prompt: String): Boolean {
        val normalizedPrompt = prompt.trim()
        return AppLanguage.entries.any { language ->
            normalizedPrompt == defaultCleanupPromptFor(language).trim()
        } || isLegacyBuiltInDefaultPrompt(normalizedPrompt)
    }

    private fun isLegacyBuiltInDefaultPrompt(prompt: String): Boolean {
        if (!prompt.contains(TranscriptPlaceholder)) return false
        return legacyDefaultCleanupPrompts().any { legacyPrompt ->
            prompt == legacyPrompt.trim()
        }
    }

    private fun legacyDefaultCleanupPrompts(): List<String> = listOf(
        """
            Please turn the following voice transcript into natural text that can be pasted directly into the current text field.

            Rules:
            - Preserve the original language, tone, and any mixed-language/code-switching ratio.
            - Do not translate, and do not convert mixed-language text into a single language.
            - Remove filler words, repeated words, and obvious speech-recognition noise.
            - Correct clear transcription mistakes.
            - Do not add facts that are not in the original transcript.
            - Output only the cleaned text, with no explanation.

            Original transcript:
            $TranscriptPlaceholder
        """.trimIndent(),
        """
            Convierte la siguiente transcripción de voz en texto natural que pueda pegarse directamente en el campo de texto actual.

            Reglas:
            - Conserva el idioma original, el tono y la proporción de cualquier mezcla de idiomas.
            - No traduzcas ni conviertas el texto multilingüe en un solo idioma.
            - Elimina muletillas, palabras repetidas y ruido evidente de reconocimiento de voz.
            - Corrige errores claros de transcripción.
            - No agregues hechos que no estén en la transcripción original.
            - Devuelve solo el texto limpio, sin explicación.

            Transcripción original:
            $TranscriptPlaceholder
        """.trimIndent(),
        """
            Transforme la transcription vocale suivante en texte naturel pouvant être collé directement dans le champ de texte actuel.

            Règles :
            - Conserve la langue d'origine, le ton et la proportion des éventuels mélanges de langues.
            - Ne traduis pas et ne transforme pas un texte multilingue en une seule langue.
            - Supprime les mots de remplissage, les répétitions et le bruit évident de reconnaissance vocale.
            - Corrige les erreurs de transcription évidentes.
            - N'ajoute aucun fait absent de la transcription originale.
            - Ne renvoie que le texte nettoyé, sans explication.

            Transcription originale :
            $TranscriptPlaceholder
        """.trimIndent(),
        """
            Wandle die folgende Sprachtranskription in natürlichen Text um, der direkt in das aktuelle Textfeld eingefügt werden kann.

            Regeln:
            - Behalte die ursprüngliche Sprache, den Ton und das Verhältnis gemischter Sprachen bei.
            - Übersetze nicht und vereinheitliche mehrsprachigen Text nicht zu einer einzigen Sprache.
            - Entferne Füllwörter, Wiederholungen und offensichtliche Spracherkennungsfehler.
            - Korrigiere klare Transkriptionsfehler.
            - Füge keine Fakten hinzu, die nicht im Originaltranskript vorkommen.
            - Gib nur den bereinigten Text aus, ohne Erklärung.

            Originaltranskript:
            $TranscriptPlaceholder
        """.trimIndent(),
        """
            Trasforma la seguente trascrizione vocale in testo naturale da incollare direttamente nel campo di testo corrente.

            Regole:
            - Mantieni la lingua originale, il tono e la proporzione di eventuali lingue mescolate.
            - Non tradurre e non trasformare il testo multilingue in una sola lingua.
            - Rimuovi intercalari, parole ripetute e rumore evidente del riconoscimento vocale.
            - Correggi errori chiari di trascrizione.
            - Non aggiungere fatti che non compaiono nella trascrizione originale.
            - Restituisci solo il testo ripulito, senza spiegazioni.

            Trascrizione originale:
            $TranscriptPlaceholder
        """.trimIndent(),
        """
            Transforme a seguinte transcrição de voz em um texto natural que possa ser colado diretamente no campo de texto atual.

            Regras:
            - Preserve o idioma original, o tom e a proporção de qualquer mistura de idiomas.
            - Não traduza nem transforme texto multilíngue em um único idioma.
            - Remova vícios de fala, palavras repetidas e ruídos evidentes do reconhecimento de voz.
            - Corrija erros claros de transcrição.
            - Não adicione fatos que não estejam na transcrição original.
            - Retorne apenas o texto limpo, sem explicação.

            Transcrição original:
            $TranscriptPlaceholder
        """.trimIndent(),
        """
            次の音声文字起こしを、現在の入力欄にそのまま貼り付けられる自然な文章に整えてください。

            ルール：
            - 元の言語、口調、複数言語が混ざっている比率を保ってください。
            - 翻訳しないでください。複数言語の文章を一つの言語に統一しないでください。
            - 口癖、重複語、明らかな音声認識ノイズを取り除いてください。
            - 明らかな文字起こしの誤りを修正してください。
            - 元の文字起こしにない事実を追加しないでください。
            - 整えた文章だけを出力し、説明は加えないでください。

            元の文字起こし：
            $TranscriptPlaceholder
        """.trimIndent(),
        """
            다음 음성 전사 내용을 현재 입력란에 바로 붙여 넣을 수 있는 자연스러운 문장으로 정리해 주세요.

            규칙:
            - 원래 언어, 말투, 여러 언어가 섞인 비율을 유지하세요.
            - 번역하지 말고, 여러 언어가 섞인 내용을 하나의 언어로 바꾸지 마세요.
            - 군말, 반복어, 명백한 음성 인식 노이즈를 제거하세요.
            - 명백한 전사 오류를 수정하세요.
            - 원문 전사에 없는 사실을 추가하지 마세요.
            - 설명 없이 정리된 텍스트만 출력하세요.

            원문 전사:
            $TranscriptPlaceholder
        """.trimIndent(),
        """
            请将以下语音转录整理成可以直接粘贴到当前文本框的自然文字。

            规则：
            - 保留原本语言、语气与混合语言比例。
            - 不要翻译，不要把混合语言改成单一语言。
            - 去除口头禅、重复词与明显语音识别噪声。
            - 修正常见错字。
            - 不要新增原文没有的事实。
            - 只输出整理后的文字，不要加说明。

            原始转录：
            $TranscriptPlaceholder
        """.trimIndent(),
        """
            請將以下語音轉錄整理成可以直接貼到目前文字框的自然文字。

            規則：
            - 保留原本語言、語氣與中英混用比例。
            - 不要翻譯，不要把中英混用改成單一語言。
            - 去除口頭禪、重複詞與明顯語音辨識雜訊。
            - 修正常見錯字。
            - 不要新增原文沒有的事實。
            - 只輸出整理後文字，不要加說明。

            原始轉錄：
            $TranscriptPlaceholder
        """.trimIndent(),
    )

    fun naturalCleanupPrompt(rawTranscript: String): String =
        cleanupPrompt(defaultCleanupPrompt, rawTranscript)

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
        val dictionaryContext = dictionaryContext(dictionaryEntries)
        if (dictionaryContext.isBlank()) return prompt

        val transcriptLabelIndex = prompt.indexOf("原始轉錄：")
        return if (transcriptLabelIndex >= 0) {
            buildString {
                append(prompt.substring(0, transcriptLabelIndex).trimEnd())
                append("\n\n")
                append(dictionaryContext)
                append("\n\n")
                append(prompt.substring(transcriptLabelIndex))
            }
        } else {
            """
                $prompt

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
        if (this == AppLanguage.SYSTEM) AppLanguage.TRADITIONAL_CHINESE else this
}
