package com.verbally.app.style

import android.content.Context
import android.content.SharedPreferences
import com.verbally.app.settings.AppLanguage

data class AppStyleRule(
    val language: AppLanguage,
    val style: OutputStyle,
    val rule: String,
    val isCustom: Boolean,
)

interface AppStyleRuleRepository {
    fun ruleFor(language: AppLanguage, style: OutputStyle): AppStyleRule
    fun customRuleFor(language: AppLanguage, style: OutputStyle): String?
    fun saveCustomRule(language: AppLanguage, style: OutputStyle, rule: String)
    fun restoreDefault(language: AppLanguage, style: OutputStyle)
}

class InMemoryAppStyleRuleRepository : AppStyleRuleRepository {
    private val rules = mutableMapOf<String, String>()

    override fun ruleFor(language: AppLanguage, style: OutputStyle): AppStyleRule {
        val normalizedLanguage = language.normalizedStyleRuleLanguage()
        val customRule = customRuleFor(normalizedLanguage, style)
        return AppStyleRule(
            language = normalizedLanguage,
            style = style,
            rule = customRule ?: StyleRuleDefaults.defaultRuleFor(normalizedLanguage, style),
            isCustom = customRule != null,
        )
    }

    override fun customRuleFor(language: AppLanguage, style: OutputStyle): String? =
        rules[keyFor(language.normalizedStyleRuleLanguage(), style)]

    override fun saveCustomRule(language: AppLanguage, style: OutputStyle, rule: String) {
        val trimmedRule = rule.trim()
        if (trimmedRule.isBlank() || trimmedRule == StyleRuleDefaults.defaultRuleFor(language, style)) {
            restoreDefault(language, style)
            return
        }
        rules[keyFor(language.normalizedStyleRuleLanguage(), style)] = trimmedRule
    }

    override fun restoreDefault(language: AppLanguage, style: OutputStyle) {
        rules.remove(keyFor(language.normalizedStyleRuleLanguage(), style))
    }
}

class SharedPreferencesAppStyleRuleRepository(
    context: Context,
) : AppStyleRuleRepository {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("verbally_app_style_rules", Context.MODE_PRIVATE)

    override fun ruleFor(language: AppLanguage, style: OutputStyle): AppStyleRule {
        val normalizedLanguage = language.normalizedStyleRuleLanguage()
        val customRule = customRuleFor(normalizedLanguage, style)
        return AppStyleRule(
            language = normalizedLanguage,
            style = style,
            rule = customRule ?: StyleRuleDefaults.defaultRuleFor(normalizedLanguage, style),
            isCustom = customRule != null,
        )
    }

    override fun customRuleFor(language: AppLanguage, style: OutputStyle): String? =
        prefs.getString(keyFor(language.normalizedStyleRuleLanguage(), style), null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }

    override fun saveCustomRule(language: AppLanguage, style: OutputStyle, rule: String) {
        val normalizedLanguage = language.normalizedStyleRuleLanguage()
        val trimmedRule = rule.trim()
        if (trimmedRule.isBlank() || trimmedRule == StyleRuleDefaults.defaultRuleFor(normalizedLanguage, style)) {
            restoreDefault(normalizedLanguage, style)
            return
        }
        prefs.edit()
            .putString(keyFor(normalizedLanguage, style), trimmedRule)
            .apply()
    }

    override fun restoreDefault(language: AppLanguage, style: OutputStyle) {
        prefs.edit()
            .remove(keyFor(language.normalizedStyleRuleLanguage(), style))
            .apply()
    }
}

object StyleRuleDefaults {
    fun defaultRuleFor(language: AppLanguage, style: OutputStyle): String =
        when (language.normalizedStyleRuleLanguage()) {
            AppLanguage.ENGLISH -> englishRule(style)
            AppLanguage.SPANISH -> spanishRule(style)
            AppLanguage.FRENCH -> frenchRule(style)
            AppLanguage.GERMAN -> germanRule(style)
            AppLanguage.ITALIAN -> italianRule(style)
            AppLanguage.PORTUGUESE_BRAZIL -> portugueseRule(style)
            AppLanguage.JAPANESE -> japaneseRule(style)
            AppLanguage.KOREAN -> koreanRule(style)
            AppLanguage.SIMPLIFIED_CHINESE -> simplifiedChineseRule(style)
            AppLanguage.SYSTEM,
            AppLanguage.TRADITIONAL_CHINESE -> traditionalChineseRule(style)
        }

    fun mandatoryGuardrails(language: AppLanguage): String =
        when (language.normalizedStyleRuleLanguage()) {
            AppLanguage.ENGLISH -> """
                Protection rules:
                - Content preservation has priority over nicer formatting.
                - If "more natural" conflicts with "keep the original wording", keep the original wording.
                - Formal/Casual affects formatting only; it must not affect meaning, word choice, amount of information, or tone intensity.
                - Do not rewrite, shorten, replace words with synonyms, or translate.
                - Do not add facts that are not in the original transcript.
            """.trimIndent()
            AppLanguage.SPANISH -> """
                Reglas de protección:
                - Conservar el contenido tiene prioridad sobre que el formato se vea mejor.
                - Si "más natural" entra en conflicto con "conservar las palabras originales", conserva las palabras originales.
                - Formal/Casual solo afecta el formato; no debe afectar el significado, las palabras, la cantidad de información ni la intensidad del tono.
                - No reescribas, no acortes, no reemplaces palabras por sinónimos y no traduzcas.
                - No agregues hechos que no estén en la transcripción original.
            """.trimIndent()
            AppLanguage.FRENCH -> """
                Règles de protection :
                - La conservation du contenu prime sur une mise en forme plus jolie.
                - Si « plus naturel » contredit « garder les mots d'origine », garde les mots d'origine.
                - Formal/Casual affecte seulement la forme ; cela ne doit pas changer le sens, les mots, la quantité d'information ni l'intensité du ton.
                - Ne réécris pas, ne raccourcis pas, ne remplace pas par des synonymes et ne traduis pas.
                - N'ajoute aucun fait absent de la transcription originale.
            """.trimIndent()
            AppLanguage.GERMAN -> """
                Schutzregeln:
                - Inhaltserhalt hat Vorrang vor schönerer Formatierung.
                - Wenn "natürlicher" mit "ursprüngliche Wörter behalten" kollidiert, behalte die ursprünglichen Wörter.
                - Formal/Casual beeinflusst nur die Formatierung; Bedeutung, Wortwahl, Informationsmenge und Tonstärke dürfen sich nicht ändern.
                - Schreibe nicht um, kürze nicht, ersetze keine Wörter durch Synonyme und übersetze nicht.
                - Füge keine Fakten hinzu, die nicht im Originaltranskript vorkommen.
            """.trimIndent()
            AppLanguage.ITALIAN -> """
                Regole di protezione:
                - Conservare il contenuto ha priorità su una formattazione più bella.
                - Se "più naturale" entra in conflitto con "conserva le parole originali", conserva le parole originali.
                - Formal/Casual influisce solo sulla formattazione; non deve cambiare significato, parole, quantità di informazioni o intensità del tono.
                - Non riscrivere, non accorciare, non sostituire con sinonimi e non tradurre.
                - Non aggiungere fatti che non compaiono nella trascrizione originale.
            """.trimIndent()
            AppLanguage.PORTUGUESE_BRAZIL -> """
                Regras de proteção:
                - Preservar o conteúdo tem prioridade sobre deixar a formatação mais bonita.
                - Se "mais natural" entrar em conflito com "manter as palavras originais", mantenha as palavras originais.
                - Formal/Casual afeta apenas a formatação; não deve alterar significado, escolha de palavras, quantidade de informação nem intensidade do tom.
                - Não reescreva, não encurte, não troque por sinônimos e não traduza.
                - Não adicione fatos que não estejam na transcrição original.
            """.trimIndent()
            AppLanguage.JAPANESE -> """
                保護ルール：
                - 内容を保つことを、見た目のよい整形より優先してください。
                - 「より自然」と「元の語を残す」が衝突する場合は、元の語を残してください。
                - Formal/Casual は形式だけに影響し、意味、用語、情報量、口調の強さを変えてはいけません。
                - 書き換え、短縮、同義語への置き換え、翻訳をしないでください。
                - 元の文字起こしにない事実を追加しないでください。
            """.trimIndent()
            AppLanguage.KOREAN -> """
                보호 규칙:
                - 보기 좋은 형식보다 내용 보존을 우선하세요.
                - "더 자연스럽게"와 "원래 단어 유지"가 충돌하면 원래 단어를 유지하세요.
                - Formal/Casual은 형식에만 영향을 주며 의미, 단어 선택, 정보량, 말투의 강도를 바꾸면 안 됩니다.
                - 다시 쓰거나, 줄이거나, 동의어로 바꾸거나, 번역하지 마세요.
                - 원문 전사에 없는 사실을 추가하지 마세요.
            """.trimIndent()
            AppLanguage.SIMPLIFIED_CHINESE -> """
                保护规则：
                - 内容保留优先于格式好看。
                - 如果“更自然”和“保留原字”冲突，选择保留原字。
                - Formal/Casual 只影响格式，不影响意思、用字、信息量与语气强度。
                - 不要改写、不要缩短、不要替换同义词、不要翻译。
                - 不要新增原文没有的事实。
            """.trimIndent()
            AppLanguage.SYSTEM,
            AppLanguage.TRADITIONAL_CHINESE -> """
                保護規則：
                - 內容保留優先於格式好看。
                - 如果「更自然」和「保留原字」衝突，選擇保留原字。
                - Formal/Casual 只影響格式，不影響意思、用字、資訊量與語氣強度。
                - 不要改寫、不要縮短、不要替換同義詞、不要翻譯。
                - 不要新增原文沒有的事實。
            """.trimIndent()
        }

    private fun traditionalChineseRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            只整理標點符號、斷句、換行與必要空格。
            可以補上適合正式文字的標點，但不要換成更正式的說法。
            不要改中文字、英文字、數字、專有名詞。
            不要刪減內容，不要補內容，不要替換同義詞。
            除非是明顯語音辨識錯字，否則保留原本用詞。
        """.trimIndent()
        OutputStyle.CASUAL -> """
            只調整標點、空格與自然斷句，讓文字看起來像聊天輸入。
            可以減少過度正式的標點，改用自然斷句、空格或換行。
            不要改中文字、英文字、數字、專有名詞。
            不要刪減內容，不要補內容，不要替換同義詞。
            不要把語氣改得更可愛、更順、更像客服或更像社群貼文。
        """.trimIndent()
    }

    private fun simplifiedChineseRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            只整理标点符号、断句、换行与必要空格。
            可以补上适合正式文字的标点，但不要换成更正式的说法。
            不要改中文字、英文字、数字、专有名词。
            不要删减内容，不要补内容，不要替换同义词。
            除非是明显语音识别错字，否则保留原本用词。
        """.trimIndent()
        OutputStyle.CASUAL -> """
            只调整标点、空格与自然断句，让文字看起来像聊天输入。
            可以减少过度正式的标点，改用自然断句、空格或换行。
            不要改中文字、英文字、数字、专有名词。
            不要删减内容，不要补内容，不要替换同义词。
            不要把语气改得更可爱、更顺、更像客服或更像社交贴文。
        """.trimIndent()
    }

    private fun englishRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            Adjust punctuation, sentence breaks, line breaks, capitalization, and necessary spacing only.
            You may add punctuation appropriate for formal text, but do not make the wording more formal.
            Do not change words, numbers, proper nouns, or named entities.
            Do not delete content, add content, or replace words with synonyms.
            Correct only clear transcription errors; if uncertain, keep the original wording.
        """.trimIndent()
        OutputStyle.CASUAL -> """
            Adjust punctuation, spacing, and natural line breaks so the text reads like a chat message.
            Use lighter punctuation, natural breaks, spaces, or line breaks when appropriate.
            Do not change words, numbers, proper nouns, or named entities.
            Do not delete content, add content, or replace words with synonyms.
            Do not make the tone cuter, smoother, more like customer support, or more like a social post.
        """.trimIndent()
    }

    private fun spanishRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            Ajusta solo la puntuación, los cortes de frase, los saltos de línea, las mayúsculas y los espacios necesarios.
            Puedes agregar puntuación adecuada para texto formal, pero no vuelvas las palabras más formales.
            No cambies palabras, números, nombres propios ni entidades.
            No elimines contenido, no agregues contenido ni reemplaces palabras por sinónimos.
            Corrige solo errores claros de transcripción; si no estás seguro, conserva la redacción original.
        """.trimIndent()
        OutputStyle.CASUAL -> """
            Ajusta la puntuación, los espacios y los cortes naturales para que el texto se lea como un mensaje de chat.
            Usa puntuación más ligera, cortes naturales, espacios o saltos de línea cuando corresponda.
            No cambies palabras, números, nombres propios ni entidades.
            No elimines contenido, no agregues contenido ni reemplaces palabras por sinónimos.
            No hagas que el tono sea más tierno, más fluido, más de atención al cliente o más de publicación social.
        """.trimIndent()
    }

    private fun frenchRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            Ajuste uniquement la ponctuation, les coupures de phrase, les retours à la ligne, les majuscules et les espaces nécessaires.
            Tu peux ajouter une ponctuation adaptée à un texte formel, mais ne rends pas les mots plus formels.
            Ne change pas les mots, les nombres, les noms propres ni les entités nommées.
            Ne supprime pas de contenu, n'ajoute pas de contenu et ne remplace pas par des synonymes.
            Corrige seulement les erreurs de transcription évidentes ; en cas de doute, garde la formulation d'origine.
        """.trimIndent()
        OutputStyle.CASUAL -> """
            Ajuste la ponctuation, les espaces et les coupures naturelles pour que le texte ressemble à un message de chat.
            Utilise une ponctuation plus légère, des coupures naturelles, des espaces ou des retours à la ligne si nécessaire.
            Ne change pas les mots, les nombres, les noms propres ni les entités nommées.
            Ne supprime pas de contenu, n'ajoute pas de contenu et ne remplace pas par des synonymes.
            Ne rends pas le ton plus mignon, plus fluide, plus service client ou plus réseau social.
        """.trimIndent()
    }

    private fun germanRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            Passe nur Zeichensetzung, Satzgrenzen, Zeilenumbrüche, Groß-/Kleinschreibung und notwendige Leerzeichen an.
            Du darfst passende Zeichensetzung für formellen Text ergänzen, aber die Formulierungen nicht formeller machen.
            Ändere keine Wörter, Zahlen, Eigennamen oder benannten Entitäten.
            Lösche keine Inhalte, füge keine Inhalte hinzu und ersetze keine Wörter durch Synonyme.
            Korrigiere nur eindeutige Transkriptionsfehler; wenn du unsicher bist, behalte die ursprüngliche Formulierung.
        """.trimIndent()
        OutputStyle.CASUAL -> """
            Passe Zeichensetzung, Abstände und natürliche Umbrüche so an, dass der Text wie eine Chatnachricht wirkt.
            Verwende bei Bedarf leichtere Zeichensetzung, natürliche Trennungen, Leerzeichen oder Zeilenumbrüche.
            Ändere keine Wörter, Zahlen, Eigennamen oder benannten Entitäten.
            Lösche keine Inhalte, füge keine Inhalte hinzu und ersetze keine Wörter durch Synonyme.
            Mache den Ton nicht niedlicher, glatter, kundenserviceartiger oder wie einen Social-Media-Beitrag.
        """.trimIndent()
    }

    private fun italianRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            Regola solo punteggiatura, interruzioni di frase, a capo, maiuscole e spazi necessari.
            Puoi aggiungere punteggiatura adatta a un testo formale, ma non rendere le parole più formali.
            Non cambiare parole, numeri, nomi propri o entità nominate.
            Non eliminare contenuto, non aggiungere contenuto e non sostituire parole con sinonimi.
            Correggi solo errori di trascrizione evidenti; se non sei sicuro, conserva la formulazione originale.
        """.trimIndent()
        OutputStyle.CASUAL -> """
            Regola punteggiatura, spazi e interruzioni naturali perché il testo sembri un messaggio di chat.
            Usa punteggiatura più leggera, interruzioni naturali, spazi o a capo quando serve.
            Non cambiare parole, numeri, nomi propri o entità nominate.
            Non eliminare contenuto, non aggiungere contenuto e non sostituire parole con sinonimi.
            Non rendere il tono più carino, più scorrevole, più da assistenza clienti o più da post social.
        """.trimIndent()
    }

    private fun portugueseRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            Ajuste apenas pontuação, quebras de frase, quebras de linha, maiúsculas e espaçamento necessário.
            Você pode adicionar pontuação adequada para texto formal, mas não deixe as palavras mais formais.
            Não altere palavras, números, nomes próprios nem entidades nomeadas.
            Não remova conteúdo, não adicione conteúdo nem troque palavras por sinônimos.
            Corrija apenas erros claros de transcrição; se não tiver certeza, preserve a redação original.
        """.trimIndent()
        OutputStyle.CASUAL -> """
            Ajuste pontuação, espaços e quebras naturais para que o texto pareça uma mensagem de chat.
            Use pontuação mais leve, quebras naturais, espaços ou quebras de linha quando fizer sentido.
            Não altere palavras, números, nomes próprios nem entidades nomeadas.
            Não remova conteúdo, não adicione conteúdo nem troque palavras por sinônimos.
            Não deixe o tom mais fofo, mais fluido, mais parecido com atendimento ao cliente ou mais parecido com post social.
        """.trimIndent()
    }

    private fun japaneseRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            句読点、文の区切り、改行、必要な空白だけを整えてください。
            正式な文章に合う句読点は補ってもかまいませんが、より丁寧な表現に言い換えないでください。
            語、数字、固有名詞、名前付きの対象を変えないでください。
            内容を削らないでください。内容を足さないでください。同義語に置き換えないでください。
            明らかな文字起こしの誤りだけを修正してください。迷う場合は元の表現を残してください。
        """.trimIndent()
        OutputStyle.CASUAL -> """
            チャット入力らしく見えるように、句読点、空白、自然な区切りだけを調整してください。
            必要に応じて軽めの句読点、自然な区切り、空白、改行を使ってください。
            語、数字、固有名詞、名前付きの対象を変えないでください。
            内容を削らないでください。内容を足さないでください。同義語に置き換えないでください。
            口調をかわいくしたり、滑らかにしたり、客服風やSNS投稿風にしたりしないでください。
        """.trimIndent()
    }

    private fun koreanRule(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            문장 부호, 문장 나눔, 줄바꿈, 필요한 띄어쓰기만 정리하세요.
            격식 있는 글에 맞는 문장 부호는 추가할 수 있지만, 표현을 더 격식 있게 바꾸지 마세요.
            단어, 숫자, 고유명사, 명명된 대상을 바꾸지 마세요.
            내용을 삭제하거나 추가하거나 동의어로 바꾸지 마세요.
            명백한 전사 오류만 수정하세요. 확실하지 않으면 원래 표현을 유지하세요.
        """.trimIndent()
        OutputStyle.CASUAL -> """
            채팅 입력처럼 보이도록 문장 부호, 띄어쓰기, 자연스러운 끊어 쓰기만 조정하세요.
            필요하면 가벼운 문장 부호, 자연스러운 끊어 쓰기, 띄어쓰기, 줄바꿈을 사용하세요.
            단어, 숫자, 고유명사, 명명된 대상을 바꾸지 마세요.
            내용을 삭제하거나 추가하거나 동의어로 바꾸지 마세요.
            말투를 더 귀엽게, 매끄럽게, 고객 응대처럼, 소셜 게시물처럼 만들지 마세요.
        """.trimIndent()
    }
}

fun AppLanguage.normalizedStyleRuleLanguage(): AppLanguage =
    if (this == AppLanguage.SYSTEM) AppLanguage.TRADITIONAL_CHINESE else this

private fun keyFor(language: AppLanguage, style: OutputStyle): String =
    "${language.normalizedStyleRuleLanguage().name.lowercase()}_${style.name.lowercase()}_rule"
