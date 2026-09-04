package net.derfruhling.serenity.localization

import net.derfruhling.serenity.annotations.NativeName

/**
 * Mostly taken from [this page](https://en.wikipedia.org/wiki/IETF_language_tag).
 *
 * Thanks, Wikipedia!
 */
@Suppress("EnumEntryName", "NonAsciiCharacters", "PropertyName")
enum class Language(val iso639_1: String?, val iso639_3: String, val legacyIso639_2: String? = null) : LangFormattable {
    @NativeName("Afrikaans")
    AFRIKAANS("af", "afr"),

    @NativeName("አማርኛ")
    AMHARIC("am", "amh"),

    @NativeName("العربية")
    ARABIC("ar", "ara"),

    @NativeName("Mapudungun")
    MAPUDUNGUN("arn"),

    @NativeName("الدارجة المغربية")
    MOROCCAN_ARABIC("ary"),

    @NativeName("অসমীয়া")
    ASSAMESE("as", "asm"),

    @NativeName("Azərbaycan")
    AZERBAIJANI("az", "aze"),

    @NativeName("Башҡорт")
    BASHKIR("ba", "bak"),

    @NativeName("беларуская")
    BELARUSIAN("be", "bel"),

    @NativeName("български")
    BULGARIAN("bg", "bul"),

    @NativeName("বাংলা")
    BENGALI("bn", "ben"),

    @NativeName("བོད་ཡིག")
    TIBETAN("bo", "bod", "tib" /* legacy */),

    @NativeName("brezhoneg")
    BRETON("br", "bre"),

    @NativeName("bosanski", "босански")
    BOSNIAN("bs", "bos"),

    @NativeName("català")
    CATALAN("ca", "cat"),

    @NativeName("کوردیی ناوەندی")
    CENTRAL_KURDISH("ckb"),

    @NativeName("Corsu")
    CORSICAN("co", "cos"),

    @NativeName("čeština")
    CZECH("cs", "ces", "cze" /* legacy */),

    @NativeName("Cymraeg")
    WELSH("cy", "cym", "wel" /* legacy */),

    @NativeName("dansk")
    DANISH("da", "dan"),

    @NativeName("Deutsch")
    GERMAN("de", "deu", "ger" /* legacy */),

    @NativeName("dolnoserbšćina")
    LOWER_SORBIAN("dsb"),

    @NativeName("ދިވެހިބަސް")
    DIVEHI("dv", "div"),

    @NativeName("Ελληνικά")
    GREEK("el", "ell", "gre" /* legacy */),

    @NativeName("English")
    ENGLISH("en", "eng"),

    @NativeName("español")
    SPANISH("es", "spa"),

    @NativeName("eesti")
    ESTONIAN("et", "est"),

    @NativeName("euskara")
    BASQUE("eu", "eus", "baq" /* legacy */),

    @NativeName("فارسى")
    PERSIAN("fa", "fas", "per" /* legacy */),

    @NativeName("suomi")
    FINNISH("fi", "fin"),

    @NativeName("Filipino")
    FILIPINO("fil"),

    @NativeName("føroyskt")
    FAROESE("fo", "fao"),

    @NativeName("français")
    FRENCH("fr", "fra", "fre" /* legacy */),

    @NativeName("Frysk")
    FRISIAN("fy", "fry"),

    @NativeName("Gaeilge")
    IRISH("ga", "gle"),

    @NativeName("Gàidhlig")
    SCOTTISH_GAELIC("gd", "gla"),

    @NativeName("Taetae ni Kiribati")
    GILBERTESE("gil"),

    @NativeName("galego")
    GALICIAN("gl", "glg"),

    @NativeName("Schweizerdeutsch")
    SWISS_GERMAN("gsw"),

    @NativeName("ગુજરાતી")
    GUJARATI("gu", "guj"),

    @NativeName("Hausa")
    HAUSA("ha", "hau"),

    @NativeName("עברית")
    HEBREW("he", "heb"),

    @NativeName("हिंदी")
    HINDI("hi", "hin"),

    @NativeName("hrvatski")
    CROATIAN("hr", "hrv"),

    @NativeName("hornjoserbšćina")
    UPPER_SORBIAN("hsb"),

    @NativeName("magyar")
    HUNGARIAN("hu", "hun"),

    @NativeName("Հայերեն")
    ARMENIAN("hy", "hye", "arm" /* legacy */),

    @NativeName("Bahasa Indonesia")
    INDONESIAN("id", "ind"),

    @NativeName("Igbo")
    IGBO("ig", "ibo"),

    @NativeName("ꆈꌠꁱꂷ")
    YI("ii", "iii"),

    @NativeName("íslenska")
    ICELANDIC("is", "isl", "ice" /* legacy */),

    @NativeName("italiano")
    ITALIAN("it", "ita"),

    @NativeName("Inuktitut", "ᐃᓄᒃᑎᑐᑦ (ᑲᓇᑕ)")
    INUKTITUT("iu", "iku"),

    @NativeName("日本語")
    JAPANESE("ja", "jpn"),

    @NativeName("ქართული")
    GEORGIAN("ka", "kat", "geo" /* legacy */),

    @NativeName("Қазақша")
    KAZAKH("kk", "kaz"),

    @NativeName("kalaallisut")
    GREENLANDIC("kl", "kal"),

    @NativeName("ខ្មែរ")
    KHMER("km", "khm"),

    @NativeName("ಕನ್ನಡ")
    KANNADA("kn", "kan"),

    @NativeName("한국어")
    KOREAN("ko", "kor"),

    @NativeName("कोंकणी")
    KONKANI("kok"),

    @NativeName("Kurdî", "کوردی")
    KURDISH("ku", "kur"),

    @NativeName("Кыргыз")
    KYRGYZ("ky", "kir"),

    @NativeName("Lëtzebuergesch")
    LUXEMBOURGISH("lb", "ltz"),

    @NativeName("ລາວ")
    LAO("lo", "lao"),

    @NativeName("lietuvių")
    LITHUANIAN("lt", "lit"),

    @NativeName("latviešu")
    LATVIAN("lv", "lav"),

    @NativeName("Reo Māori")
    MĀORI("mi", "mri", "mao" /* legacy */),

    @NativeName("македонски јазик")
    MACEDONIAN("mk", "mkd", "mac" /* legacy */),

    @NativeName("മലയാളം")
    MALAYALAM("ml", "mal"),

    // this renders weird
    @NativeName("Монгол хэл", "ᠮᠤᠨᠭᠭᠤᠯ ᠬᠡᠯᠡ")
    MONGOLIAN("mn", "mon"),

    @NativeName("Kanien'kéha")
    MOHAWK("moh"),

    @NativeName("मराठी")
    MARATHI("mr", "mar"),

    @NativeName("Bahasa Malaysia")
    MALAY("ms", "msa", "may" /* legacy */),

    @NativeName("Malti")
    MALTESE("mt", "mlt"),

    @NativeName("မြန်မာဘာသာ")
    BURMESE("my", "mya", "bur" /* legacy */),

    @NativeName("norsk (bokmål)")
    NORWEGIAN_BOKMÅL("nb", "nob"),

    @NativeName("नेपाली (नेपाल)")
    NEPALI("ne", "nep"),

    @NativeName("Nederlands")
    DUTCH("nl", "nld", "dut" /* legacy */),

    @NativeName("norsk (nynorsk)")
    NORWEGIAN_NYNORSK("nn", "nno"),

    @NativeName("norsk")
    NORWEGIAN("no", "nor"),

    @NativeName("occitan")
    OCCITAN("oc", "oci"),

    // is it Oriya or Odia??? both?????
    @NativeName("ଓଡ଼ିଆ")
    ODIA("or", "ori"),

    @NativeName("Papiamentu")
    PAPIAMENTO("pap"),

    @NativeName("ਪੰਜਾਬੀ", "پنجابی")
    PUNJABI("pa", "pan"),

    @NativeName("polski")
    POLISH("pl", "pol"),

    @NativeName("درى")
    DARI("prs"),

    @NativeName("پښتو")
    PASHTO("ps", "pus"),

    @NativeName("português")
    PORTUGUESE("pt", "por"),

    @NativeName("K'iche")
    `K'ICHE`("quc"),

    @NativeName("runasimi")
    QUECHUA("qu", "que"),

    @NativeName("Rumantsch")
    ROMANSH("rm", "roh"),

    @NativeName("română")
    ROMANIAN("ro", "ron", "rum" /* legacy */),

    @NativeName("русский")
    RUSSIAN("ru", "rus"),

    @NativeName("Kinyarwanda")
    KINYARWANDA("rw", "kin"),

    @NativeName("संस्कृत")
    SANSKRIT("sa", "san"),

    @NativeName("caxa")
    YAKUT("sah"),

    @NativeName("سِنڌِي")
    SINDHI("sd", "snd"),

    @NativeName("davvisámegiella")
    SAMI_NORTHERN("se", "sme"),

    @NativeName("සිංහල")
    SINHALA("si", "sin"),

    @NativeName("slovenčina")
    SLOVAK("sk", "slk", "slo" /* legacy */),

    @NativeName("slovenščina")
    SLOVENIAN("sl", "slv"),

    @NativeName("åarjelsaemiengiele")
    SAMI_SOUTHERN("sma"),

    @NativeName("julevusámegiella")
    SAMI_LULE("smj"),

    @NativeName("sämikielâ")
    SAMI_INARI("smn"),

    @NativeName("sääʹmǩiõll")
    SAMI_SKOLT("sms"),

    @NativeName("shqip")
    ALBANIAN("sq", "sqi", "alb" /* legacy */),

    @NativeName("srpski", "српски")
    SERBIAN("sr", "srp"),

    @NativeName("Sesotho")
    SESOTHO("st", "sot"),

    @NativeName("svenska")
    SWEDISH("sv", "swe"),

    @NativeName("Kiswahili")
    KISWAHILI("sw", "swa"),

    @NativeName("ܣܘܪܝܝܐ")
    SYRIAC("syc"),

    @NativeName("தமிழ்")
    TAMIL("ta", "tam"),

    @NativeName("తెలుగు")
    TELUGU("te", "tel"),

    @NativeName("Тоҷикӣ")
    TAJIK("tg", "tgk"),

    @NativeName("ไทย")
    THAI("th", "tha"),

    @NativeName("türkmençe")
    TURKMEN("tk", "tuk"),

    @NativeName("Tagalog")
    TAGALOG("tl", "tgl"),

    @NativeName("Setswana")
    TSWANA("tn", "tsn"),

    @NativeName("Türkçe")
    TURKISH("tr", "tur"),

    @NativeName("Татарча")
    TATAR("tt", "tat"),

    @NativeName("Tamazight")
    TAZAZIGHT("tzm"),

    @NativeName("ئۇيغۇرچە")
    UYGHUR("ug", "uig"),

    @NativeName("українська")
    UKRAINIAN("uk", "ukr"),

    @NativeName("اُردو")
    URDU("ur", "urd"),

    @NativeName("Uzbek", "Ўзбек")
    UZBEK("uz", "uzb"),

    @NativeName("Tiếng Việt")
    VIETNAMESE("vi", "vie"),

    @NativeName("Wolof")
    WOLOF("wo", "wol"),

    @NativeName("isiXhosa")
    XHOSA("xh", "xho"),

    // this renders weird too
    @NativeName("יידיש")
    YIDDISH("yi", "yid"),

    @NativeName("Yoruba")
    YORUBA("yo", "yor"),

    @NativeName("中文")
    CHINESE("zh", "zho", "chi" /* legacy */),

    @NativeName("isiZulu")
    ZULU("zu", "zul");

    @Suppress("LocalVariableName")
    constructor(iso639_3: String) : this(null, iso639_3)

    val subtags = arrayOf(iso639_1, iso639_3, legacyIso639_2).filterNotNull().toTypedArray()

    override fun formatLang(): String {
        return iso639_1 ?: iso639_3
    }

    companion object {
        val bySubtag = entries.flatMap { it.subtags.map { tag -> tag to it } }.toMap()
    }
}
