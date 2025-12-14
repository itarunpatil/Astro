package com.astro.storm.data.localization

/**
 * Centralized String Resources for AstroStorm
 *
 * This object contains all translatable strings organized by category.
 * Each string has an English and Nepali translation.
 *
 * Usage:
 * ```kotlin
 * val text = StringResources.get(StringKey.HOME_TAB, language)
 * ```
 *
 * For Compose:
 * ```kotlin
 * Text(text = stringResource(StringKey.HOME_TAB))
 * ```
 */
object StringResources {

    /**
     * Get localized string for a given key
     */
    fun get(key: StringKeyInterface, language: Language): String {
        return when (language) {
            Language.ENGLISH -> key.en
            Language.NEPALI -> key.ne
        }
    }

    /**
     * Get localized string with format arguments
     */
    fun get(key: StringKeyInterface, language: Language, vararg args: Any): String {
        val template = get(key, language)
        return try {
            String.format(template, *args)
        } catch (e: Exception) {
            template
        }
    }
}

/**
 * Interface for all string key types to support multiple enums
 */
interface StringKeyInterface {
    val en: String
    val ne: String
}


/**
 * Core UI string keys (navigation, settings, buttons, yoga, profile)
 * Part 1 of 4 split enums to avoid JVM method size limit
 */
enum class StringKey(override val en: String, override val ne: String) : StringKeyInterface {

    // ============================================
    // NAVIGATION & TABS
    // ============================================
    TAB_HOME("Home", "गृह"),
    TAB_INSIGHTS("Insights", "अन्तर्दृष्टि"),
    TAB_SETTINGS("Settings", "सेटिङ्स"),

    // ============================================
    // HOME TAB - SECTION HEADERS
    // ============================================
    HOME_CHART_ANALYSIS("Chart Analysis", "कुण्डली विश्लेषण"),
    HOME_COMING_SOON("Coming Soon", "छिट्टै आउँदैछ"),
    HOME_SOON_BADGE("Soon", "छिट्टै"),

    // ============================================
    // HOME TAB - FEATURE CARDS
    // ============================================
    FEATURE_BIRTH_CHART("Birth Chart", "जन्म कुण्डली"),
    FEATURE_BIRTH_CHART_DESC("View your complete Vedic birth chart", "आफ्नो पूर्ण वैदिक जन्म कुण्डली हेर्नुहोस्"),

    FEATURE_PLANETS("Planets", "ग्रहहरू"),
    FEATURE_PLANETS_DESC("Detailed planetary positions", "विस्तृत ग्रह स्थिति"),

    FEATURE_YOGAS("Yogas", "योगहरू"),
    FEATURE_YOGAS_DESC("Planetary combinations & effects", "ग्रह संयोजन र प्रभावहरू"),

    FEATURE_DASHAS("Dashas", "दशाहरू"),
    FEATURE_DASHAS_DESC("Planetary period timeline", "ग्रह अवधि समयरेखा"),

    FEATURE_TRANSITS("Transits", "गोचरहरू"),
    FEATURE_TRANSITS_DESC("Current planetary movements", "हालको ग्रह गतिविधिहरू"),

    FEATURE_ASHTAKAVARGA("Ashtakavarga", "अष्टकवर्ग"),
    FEATURE_ASHTAKAVARGA_DESC("Strength analysis by house", "भावानुसार बल विश्लेषण"),

    // Ashtakavarga Details
    ASHTAKAVARGA_ABOUT_TITLE("About Ashtakavarga", "अष्टकवर्ग बारेमा"),
    ASHTAKAVARGA_ABOUT_DESC("Ashtakavarga is an ancient Vedic astrology technique for assessing planetary strength and predicting transit effects.", "अष्टकवर्ग वैदिक ज्योतिषको एक प्राचीन प्रविधि हो जसले ग्रहको शक्ति मापन र गोचर प्रभाव पूर्वानुमान गर्दछ।"),
    ASHTAKAVARGA_SAV_TITLE("Sarvashtakavarga (SAV)", "सर्वाष्टकवर्ग (SAV)"),
    ASHTAKAVARGA_SAV_DESC("Combined strength points from all planets in each zodiac sign. Higher values (28+) indicate favorable areas.", "सबै ग्रहबाट प्रत्येक राशिमा संयोजित शक्ति बिन्दु। उच्च मान (२८+) अनुकूल क्षेत्रहरू संकेत गर्छ।"),
    ASHTAKAVARGA_BAV_TITLE("Bhinnashtakavarga (BAV)", "भिन्नाष्टकवर्ग (BAV)"),
    ASHTAKAVARGA_BAV_DESC("Individual planet strength in each sign (0-8 bindus). Use this to predict transit effects.", "प्रत्येक राशिमा व्यक्तिगत ग्रहको शक्ति (०-८ बिन्दु)। गोचर प्रभाव पूर्वानुमान गर्न यो प्रयोग गर्नुहोस्।"),

    FEATURE_PANCHANGA("Panchanga", "पञ्चाङ्ग"),
    FEATURE_PANCHANGA_DESC("Vedic calendar elements", "वैदिक पात्रो तत्वहरू"),

    FEATURE_MATCHMAKING("Matchmaking", "कुण्डली मिलान"),
    FEATURE_MATCHMAKING_DESC("Kundli Milan compatibility", "विवाह मेलापक गुण मिलान"),

    FEATURE_MUHURTA("Muhurta", "मुहूर्त"),
    FEATURE_MUHURTA_DESC("Auspicious timing finder", "शुभ समय खोजकर्ता"),

    FEATURE_REMEDIES("Remedies", "उपायहरू"),
    FEATURE_REMEDIES_DESC("Personalized remedies", "व्यक्तिगत उपायहरू"),

    FEATURE_VARSHAPHALA("Varshaphala", "वर्षफल"),
    FEATURE_VARSHAPHALA_DESC("Solar return horoscope", "वार्षिक राशिफल"),

    // Varshaphala Details
    VARSHAPHALA_TAB_OVERVIEW("Overview", "अवलोकन"),
    VARSHAPHALA_TAB_TAJIKA("Tajika", "तजिका"),
    VARSHAPHALA_TAB_SAHAMS("Sahams", "सहम"),
    VARSHAPHALA_TAB_DASHA("Dasha", "दशा"),
    VARSHAPHALA_TAB_HOUSES("Houses", "भावहरू"),
    VARSHAPHALA_ANNUAL_HOROSCOPE("Annual Horoscope", "वार्षिक राशिफल"),
    VARSHAPHALA_AGE("Age %d", "आयु %d"),
    VARSHAPHALA_PREV_YEAR("Previous year", "अघिल्लो वर्ष"),
    VARSHAPHALA_NEXT_YEAR("Next year", "अर्को वर्ष"),
    VARSHAPHALA_SELECT_CHART("Select a birth chart to view Varshaphala", "वर्षफल हेर्न जन्म कुण्डली छान्नुहोस्"),
    VARSHAPHALA_COMPUTING("Computing Annual Horoscope...", "वार्षिक राशिफल गणना गर्दै..."),
    VARSHAPHALA_COMPUTING_DESC("Calculating Tajika aspects, Sahams & Mudda Dasha", "तजिका पक्ष, सहम र मुद्द दशा गणना गर्दै"),
    VARSHAPHALA_ERROR("Calculation Error", "गणना त्रुटि"),
    VARSHAPHALA_RESET_YEAR("Reset to Current Year", "वर्तमान वर्षमा रिसेट"),
    VARSHAPHALA_SOLAR_RETURN("Solar Return", "सौर प्रतिफल"),
    VARSHAPHALA_YEAR_LORD("Year Lord", "वर्ष स्वामी"),
    VARSHAPHALA_MUNTHA("Muntha", "मुन्थ"),
    VARSHAPHALA_MUNTHA_LORD("Lord: %s", "स्वामी: %s"),
    VARSHAPHALA_TAJIKA_CHART("Tajika Annual Chart", "तजिका वार्षिक कुण्डली"),
    VARSHAPHALA_PANCHA_VARGIYA("Pancha Vargiya Bala", "पञ्च वर्गीय बल"),
    VARSHAPHALA_TRI_PATAKI("Tri-Pataki Chakra", "त्रि-पतकी चक्र"),
    VARSHAPHALA_MAJOR_THEMES("Major Themes", "मुख्य विषयहरू"),
    VARSHAPHALA_MONTHLY_OUTLOOK("Monthly Outlook", "मासिक दृष्टिकोण"),
    VARSHAPHALA_FAVORABLE("Favorable", "अनुकूल"),
    VARSHAPHALA_KEY_DATES("Key Dates", "महत्त्वपूर्ण मितिहरू"),
    VARSHAPHALA_OVERALL_PREDICTION("Overall Prediction", "समग्र भविष्यवाणी"),
    VARSHAPHALA_TAJIKA_SUMMARY("Tajika Yogas Summary", "तजिका योग सारांश"),
    VARSHAPHALA_SAHAMS_TITLE("Sahams (Arabic Parts)", "सहम (अरबी भाग)"),
    VARSHAPHALA_SAHAMS_DESC("Sensitive points calculated from planetary positions", "ग्रह स्थितिबाट गणना गरिएको संवेदनशील बिन्दुहरू"),
    VARSHAPHALA_MUDDA_DASHA("Mudda Dasha", "मुद्द दशा"),
    VARSHAPHALA_MUDDA_DASHA_DESC("Annual planetary periods based on Moon's position", "चन्द्रको स्थितिमा आधारित वार्षिक ग्रह अवधिहरू"),
    VARSHAPHALA_POSITION("Position", "स्थिति"),
    VARSHAPHALA_HOUSE("House", "भाव"),
    VARSHAPHALA_DAYS("%d days", "%d दिन"),

    FEATURE_PRASHNA("Prashna", "प्रश्न"),
    FEATURE_PRASHNA_DESC("Horary astrology", "प्रश्न ज्योतिष"),

    // Prashna Details
    PRASHNA_NEW_QUESTION("New question", "नयाँ प्रश्न"),
    PRASHNA_KUNDALI("Prashna Kundali", "प्रश्न कुण्डली"),
    PRASHNA_HORARY("Horary Astrology", "प्रश्न ज्योतिष"),
    PRASHNA_INTRO("Ask your question and receive guidance based on the planetary positions at this very moment.", "आफ्नो प्रश्न सोध्नुहोस् र यस क्षणको ग्रह स्थितिको आधारमा मार्गदर्शन प्राप्त गर्नुहोस्।"),
    PRASHNA_YOUR_QUESTION("Your Question", "तपाईंको प्रश्न"),
    PRASHNA_QUESTION_HINT("Enter your question here...", "यहाँ आफ्नो प्रश्न प्रविष्ट गर्नुहोस्..."),
    PRASHNA_QUESTION_HELP("Be specific and clear. Frame your question with a sincere intent.", "स्पष्ट र विशिष्ट हुनुहोस्। ईमानदार इरादाले आफ्नो प्रश्न बनाउनुहोस्।"),
    PRASHNA_CATEGORY("Question Category", "प्रश्न वर्ग"),
    PRASHNA_LOCATION("Question Location", "प्रश्न स्थान"),
    PRASHNA_TIME_NOW("Now", "अहिले"),
    PRASHNA_ANALYZE("Analyze Question", "प्रश्न विश्लेषण गर्नुहोस्"),
    PRASHNA_ABOUT("About Prashna", "प्रश्न बारेमा"),
    PRASHNA_ANALYZING("Analyzing your question...", "तपाईंको प्रश्न विश्लेषण गर्दै..."),
    PRASHNA_CALCULATING("Calculating planetary positions and yogas", "ग्रह स्थिति र योगहरू गणना गर्दै"),
    PRASHNA_ANALYSIS_FAILED("Analysis Failed", "विश्लेषण असफल"),
    PRASHNA_UNFAVORABLE("Unfavorable", "प्रतिकूल"),
    PRASHNA_FAVORABLE("Favorable", "अनुकूल"),
    PRASHNA_SCORE("Score: %d", "अंक: %d"),
    PRASHNA_QUESTION_DETAILS("Question Details", "प्रश्न विवरण"),
    PRASHNA_MOON_ANALYSIS("Moon Analysis", "चन्द्र विश्लेषण"),
    PRASHNA_LAGNA_ANALYSIS("Lagna Analysis", "लग्न विश्लेषण"),
    PRASHNA_RISING_SIGN("Rising Sign", "उदय राशि"),
    PRASHNA_LAGNA_LORD("Lagna Lord", "लग्नेश"),
    PRASHNA_LORD_POSITION("Lord Position", "स्वामी स्थिति"),
    PRASHNA_CONDITION("Condition", "अवस्था"),
    PRASHNA_PLANETS_IN_LAGNA("Planets in Lagna", "लग्नमा ग्रहहरू"),
    PRASHNA_MOON_VOID("Moon is Void of Course - delays or changes likely", "चन्द्र शून्य गतिमा छ - ढिलाइ वा परिवर्तन सम्भावित"),
    PRASHNA_TIMING("Timing Prediction", "समय भविष्यवाणी"),
    PRASHNA_ESTIMATED_TIMEFRAME("Estimated Timeframe", "अनुमानित समयावधि"),
    PRASHNA_SPECIAL_YOGAS("Special Prashna Yogas", "विशेष प्रश्न योगहरू"),
    PRASHNA_SUPPORTING_FACTORS("Supporting Factors", "समर्थक कारकहरू"),
    PRASHNA_CHALLENGES("Challenges", "चुनौतीहरू"),
    PRASHNA_RECOMMENDATIONS("Recommendations", "सिफारिसहरू"),
    PRASHNA_MOON_PHASE_WAXING("Waxing", "शुक्ल पक्ष"),
    PRASHNA_MOON_PHASE_WANING("Waning", "कृष्ण पक्ष"),
    PRASHNA_SIGN("Sign", "राशि"),
    PRASHNA_NAKSHATRA_LORD("Nakshatra Lord", "नक्षत्र स्वामी"),
    PRASHNA_TITHI("Tithi", "तिथि"),
    PRASHNA_PHASE("Phase", "पक्ष"),
    PRASHNA_INST_1("Prashna Kundali is cast for the moment the question arises in your mind or when you ask it.", "प्रश्न कुण्डली तपाईंको मनमा प्रश्न उठेको क्षणमा वा तपाईंले सोधेको बेला बनाइन्छ।"),
    PRASHNA_INST_2("The Moon is the primary significator representing your mind and the matter at hand.", "चन्द्रमा प्राथमिक कारक हो जसले तपाईंको मन र हातमा रहेको विषयलाई प्रतिनिधित्व गर्दछ।"),
    PRASHNA_INST_3("Frame your question with sincerity and focused intent for accurate guidance.", "सटीक मार्गदर्शनको लागि आफ्नो प्रश्न ईमानदारिता र केन्द्रित उद्देश्यसहित बनाउनुहोस्।"),
    PRASHNA_INST_4("The analysis considers Lagna, Moon, relevant houses, and special Prashna yogas.", "विश्लेषणले लग्न, चन्द्रमा, सम्बन्धित भावहरू, र विशेष प्रश्न योगहरूलाई विचार गर्छ।"),
    PRASHNA_INST_5("Timing predictions are based on planetary movements and house lord positions.", "समय भविष्यवाणीहरू ग्रह गतिविधि र भाव स्वामी स्थितिमा आधारित छन्।"),
    PRASHNA_ANALYZE_ERROR("Failed to analyze question", "प्रश्न विश्लेषण गर्न असफल"),
    PRASHNA_CONFIDENCE("%d%% confidence", "%d%% विश्वास"),

    FEATURE_SYNASTRY("Synastry", "सिनास्ट्री"),
    FEATURE_SYNASTRY_DESC("Chart comparison", "कुण्डली तुलना"),

    FEATURE_NAKSHATRAS("Nakshatras", "नक्षत्रहरू"),
    FEATURE_NAKSHATRAS_DESC("Deep nakshatra analysis", "गहन नक्षत्र विश्लेषण"),

    FEATURE_SHADBALA("Shadbala", "षड्बल"),
    FEATURE_SHADBALA_DESC("Six-fold strength", "छवटा बलहरू"),

    // ============================================
    // EMPTY/ERROR STATES
    // ============================================
    NO_PROFILE_SELECTED("No Profile Selected", "कुनै प्रोफाइल छानिएको छैन"),
    NO_PROFILE_MESSAGE("Select or create a profile to view your personalized astrological insights.", "आफ्नो व्यक्तिगत ज्योतिष अन्तर्दृष्टि हेर्न प्रोफाइल छान्नुहोस् वा बनाउनुहोस्।"),
    NO_PROFILE_MESSAGE_LONG("Select or create a profile to view your personalized astrological insights and predictions", "आफ्नो व्यक्तिगत ज्योतिष अन्तर्दृष्टि र भविष्यवाणी हेर्न प्रोफाइल छान्नुहोस् वा बनाउनुहोस्"),

    // Error States
    ERROR_PARTIAL("Some insights unavailable", "केही अन्तर्दृष्टिहरू उपलब्ध छैनन्"),
    ERROR_CALCULATIONS_FAILED("%d calculation(s) could not be completed", "%d गणना(हरू) पूरा हुन सकेन"),
    ERROR_UNABLE_TO_LOAD("Unable to Load Insights", "अन्तर्दृष्टि लोड गर्न असमर्थ"),
    ERROR_HOROSCOPE_UNAVAILABLE("%s's horoscope unavailable", "%s को राशिफल उपलब्ध छैन"),
    ERROR_EPHEMERIS_DATA("Unable to calculate planetary positions for this period. This may be due to ephemeris data limitations.", "यस अवधिको लागि ग्रह स्थितिहरू गणना गर्न असमर्थ। यो ईफेमेरिस डाटा सीमितताको कारण हुन सक्छ।"),
    ERROR_SOMETHING_WRONG("Something went wrong", "केही गलत भयो"),

    // ============================================
    // BUTTONS & ACTIONS
    // ============================================
    BTN_RETRY("Retry", "पुनः प्रयास"),
    BTN_TRY_AGAIN("Try Again", "फेरि प्रयास गर्नुहोस्"),
    BTN_PREVIEW("Preview", "पूर्वावलोकन"),
    BTN_OK("OK", "ठीक छ"),
    BTN_CANCEL("Cancel", "रद्द गर्नुहोस्"),
    BTN_DELETE("Delete", "मेट्नुहोस्"),
    BTN_SAVE("Save", "सेभ गर्नुहोस्"),
    BTN_GENERATE("Generate", "उत्पन्न गर्नुहोस्"),
    BTN_GENERATE_SAVE("Generate & Save", "उत्पन्न गर्नुहोस् र सेभ गर्नुहोस्"),
    BTN_GO_BACK("Go back", "पछाडि जानुहोस्"),
    BTN_BACK("Back", "पछाडि"),
    BTN_CLOSE("Close", "बन्द गर्नुहोस्"),
    BTN_REFRESH("Refresh", "रिफ्रेस"),

    // ============================================
    // INSIGHTS TAB
    // ============================================
    INSIGHTS_OVERALL_ENERGY("Overall Energy", "समग्र ऊर्जा"),
    INSIGHTS_LIFE_AREAS("Life Areas", "जीवन क्षेत्रहरू"),
    INSIGHTS_LUCKY_ELEMENTS("Lucky Elements", "भाग्यशाली तत्वहरू"),
    INSIGHTS_TODAYS_AFFIRMATION("Today's Affirmation", "आजको प्रतिज्ञा"),
    INSIGHTS_WEEKLY_ENERGY("Weekly Energy Flow", "साप्ताहिक ऊर्जा प्रवाह"),
    INSIGHTS_KEY_DATES("Key Dates", "महत्त्वपूर्ण मितिहरू"),
    INSIGHTS_WEEKLY_OVERVIEW("Weekly Overview by Area", "क्षेत्रअनुसार साप्ताहिक अवलोकन"),
    INSIGHTS_WEEKLY_ADVICE("Weekly Advice", "साप्ताहिक सल्लाह"),
    INSIGHTS_RECOMMENDATIONS("Recommendations", "सिफारिसहरू"),
    INSIGHTS_CAUTIONS("Cautions", "सावधानीहरू"),

    // Horoscope periods
    PERIOD_TODAY("Today", "आज"),
    PERIOD_TOMORROW("Tomorrow", "भोलि"),
    PERIOD_WEEKLY("Weekly", "साप्ताहिक"),

    // Lucky elements labels
    LUCKY_NUMBER("Number", "अंक"),
    LUCKY_COLOR("Color", "रंग"),
    LUCKY_DIRECTION("Direction", "दिशा"),
    LUCKY_GEMSTONE("Gemstone", "रत्न"),

    // ============================================
    // DASHA SECTION
    // ============================================
    DASHA_CURRENT_PERIOD("Current Planetary Period", "हालको ग्रह अवधि"),
    DASHA_ACTIVE("Active", "सक्रिय"),
    DASHA_MAHADASHA("Mahadasha", "महादशा"),
    DASHA_ANTARDASHA("Antardasha", "अन्तर्दशा"),
    DASHA_PRATYANTARDASHA("Pratyantardasha:", "प्रत्यन्तर्दशा:"),
    DASHA_UPCOMING("Upcoming Periods", "आगामी अवधिहरू"),
    DASHA_REMAINING("remaining", "बाँकी"),
    DASHA_LAST_IN_MAHADASHA("Current Antardasha is the last in this Mahadasha", "हालको अन्तर्दशा यस महादशाको अन्तिम हो"),
    DASHA_STARTS("Starts %s", "%s मा सुरु हुन्छ"),
    DASHA_VIMSHOTTARI("Vimshottari Dasha", "विम्शोत्तरी दशा"),
    DASHA_JUMP_TO_TODAY("Jump to current period", "हालको अवधिमा जानुहोस्"),
    DASHA_CALCULATING("Calculating...", "गणना गर्दै..."),
    DASHA_ERROR("Error", "त्रुटि"),
    DASHA_CALCULATING_TIMELINE("Calculating Dasha Timeline", "दशा समयरेखा गणना गर्दै"),
    DASHA_CALCULATING_DESC("Computing planetary periods based on\nMoon's Nakshatra position...", "चन्द्रको नक्षत्र स्थितिको आधारमा\nग्रह अवधिहरू गणना गर्दै..."),
    DASHA_CALCULATION_FAILED("Calculation Failed", "गणना असफल भयो"),
    DASHA_NO_CHART_SELECTED("No Chart Selected", "कुनै कुण्डली छानिएको छैन"),
    DASHA_NO_CHART_MESSAGE("Please select or create a birth profile\nto view the Dasha timeline.", "दशा समयरेखा हेर्न कृपया जन्म प्रोफाइल\nछान्नुहोस् वा बनाउनुहोस्।"),

    // Dasha Level Names
    DASHA_SOOKSHMADASHA("Sookshmadasha", "सूक्ष्मदशा"),
    DASHA_PRANADASHA("Pranadasha", "प्राणदशा"),
    DASHA_DEHADASHA("Dehadasha", "देहदशा"),
    DASHA_BHUKTI("Bhukti", "भुक्ति"),
    DASHA_PRATYANTAR("Pratyantar", "प्रत्यन्तर"),
    DASHA_SOOKSHMA("Sookshma", "सूक्ष्म"),
    DASHA_PRANA("Prana", "प्राण"),
    DASHA_DEHA("Deha", "देह"),

    // Dasha Format Labels
    DASHA_DURATION("Duration", "अवधि"),
    DASHA_PERIOD("Period", "अवधि"),
    DASHA_STATUS("Status", "स्थिति"),
    DASHA_CURRENTLY_ACTIVE("Currently Active", "हाल सक्रिय"),
    DASHA_PROGRESS("Progress", "प्रगति"),
    DASHA_NO_ACTIVE_PERIOD("No active Dasha period", "कुनै सक्रिय दशा अवधि छैन"),

    // Time Units
    YEARS("years", "वर्ष"),
    DAYS("days", "दिन"),
    TO("to", "देखि"),
    DAYS_SHORT("d", "दि"),
    HOURS_SHORT("h", "घ"),
    MINUTES_SHORT("m", "मि"),

    // Yogini Dasha Names
    YOGINI_MANGALA("Mangala", "मङ्गला"),
    YOGINI_PINGALA("Pingala", "पिङ्गला"),
    YOGINI_DHANYA("Dhanya", "धन्या"),
    YOGINI_BHRAMARI("Bhramari", "भ्रामरी"),
    YOGINI_BHADRIKA("Bhadrika", "भद्रिका"),
    YOGINI_ULKA("Ulka", "उल्का"),
    YOGINI_SIDDHA("Siddha", "सिद्धा"),
    YOGINI_SANKATA("Sankata", "सङ्कटा"),

    // Yogini Deity Names
    YOGINI_DEITY_CHANDRA("Chandra (Moon)", "चन्द्र"),
    YOGINI_DEITY_SURYA("Surya (Sun)", "सूर्य"),
    YOGINI_DEITY_GURU("Guru (Jupiter)", "गुरु (बृहस्पति)"),
    YOGINI_DEITY_MANGAL("Mangal (Mars)", "मङ्गल"),
    YOGINI_DEITY_BUDHA("Budha (Mercury)", "बुध"),
    YOGINI_DEITY_SHANI("Shani (Saturn)", "शनि"),
    YOGINI_DEITY_SHUKRA("Shukra (Venus)", "शुक्र"),
    YOGINI_DEITY_RAHU("Rahu", "राहु"),

    // Nature Types
    NATURE_BENEFIC("Benefic", "शुभ"),
    NATURE_MALEFIC("Malefic", "अशुभ"),
    NATURE_MIXED("Mixed", "मिश्रित"),

    // ============================================
    // TRANSITS
    // ============================================
    TRANSITS_CURRENT("Current Transits", "हालका गोचरहरू"),
    TRANSITS_MOON_IN("Moon in %s", "चन्द्रमा %s मा"),

    // ============================================
    // ENERGY DESCRIPTIONS
    // ============================================
    ENERGY_EXCEPTIONAL("Exceptional cosmic alignment - seize every opportunity!", "असाधारण ब्रह्माण्डीय संरेखण - हरेक अवसर समात्नुहोस्!"),
    ENERGY_EXCELLENT("Excellent day ahead - favorable for important decisions", "उत्कृष्ट दिन अगाडि - महत्त्वपूर्ण निर्णयहरूको लागि अनुकूल"),
    ENERGY_STRONG("Strong positive energy - good for new initiatives", "बलियो सकारात्मक ऊर्जा - नयाँ पहलहरूको लागि राम्रो"),
    ENERGY_FAVORABLE("Favorable energy - maintain steady progress", "अनुकूल ऊर्जा - स्थिर प्रगति कायम राख्नुहोस्"),
    ENERGY_BALANCED("Balanced energy - stay centered and focused", "सन्तुलित ऊर्जा - केन्द्रित र ध्यान केन्द्रित रहनुहोस्"),
    ENERGY_MODERATE("Moderate energy - pace yourself wisely", "मध्यम ऊर्जा - बुद्धिमानीपूर्वक आफ्नो गति मिलाउनुहोस्"),
    ENERGY_LOWER("Lower energy day - prioritize rest and reflection", "कम ऊर्जा दिन - आराम र चिन्तनलाई प्राथमिकता दिनुहोस्"),
    ENERGY_CHALLENGING("Challenging day - practice patience and self-care", "चुनौतीपूर्ण दिन - धैर्य र आत्म-हेरचाह अभ्यास गर्नुहोस्"),
    ENERGY_REST("Rest and recharge recommended - avoid major decisions", "आराम र रिचार्ज सिफारिस - प्रमुख निर्णयहरूबाट बच्नुहोस्"),

    // ============================================
    // LIFE AREAS
    // ============================================
    LIFE_AREA_CAREER("Career", "क्यारियर"),
    LIFE_AREA_LOVE("Love", "प्रेम"),
    LIFE_AREA_HEALTH("Health", "स्वास्थ्य"),
    LIFE_AREA_FINANCE("Finance", "वित्त"),
    LIFE_AREA_FAMILY("Family", "परिवार"),
    LIFE_AREA_SPIRITUALITY("Spirituality", "आध्यात्मिकता"),

    // ============================================
    // SETTINGS TAB
    // ============================================
    SETTINGS_PROFILE("Profile", "प्रोफाइल"),
    SETTINGS_EXPORT("Export", "निर्यात"),
    SETTINGS_PREFERENCES("Preferences", "प्राथमिकताहरू"),
    SETTINGS_ABOUT("About", "बारेमा"),

    // Profile settings
    SETTINGS_EDIT_PROFILE("Edit Profile", "प्रोफाइल सम्पादन"),
    SETTINGS_EDIT_PROFILE_DESC("Modify birth details", "जन्म विवरण परिमार्जन गर्नुहोस्"),
    SETTINGS_MANAGE_PROFILES("Manage Profiles", "प्रोफाइलहरू व्यवस्थापन"),
    SETTINGS_NO_PROFILE("No profile selected", "कुनै प्रोफाइल छानिएको छैन"),
    SETTINGS_TAP_TO_SELECT("Tap to select or create a profile", "प्रोफाइल छान्न वा बनाउन ट्याप गर्नुहोस्"),

    // Export settings
    SETTINGS_EXPORT_PDF("Export as PDF", "PDF को रूपमा निर्यात"),
    SETTINGS_EXPORT_PDF_DESC("Complete chart report", "पूर्ण कुण्डली रिपोर्ट"),
    SETTINGS_EXPORT_IMAGE("Export as Image", "छविको रूपमा निर्यात"),
    SETTINGS_EXPORT_IMAGE_DESC("High-quality chart image", "उच्च गुणस्तर कुण्डली छवि"),
    SETTINGS_EXPORT_CLIPBOARD("Copy to Clipboard", "क्लिपबोर्डमा कपी गर्नुहोस्"),
    SETTINGS_EXPORT_CLIPBOARD_DESC("Plain text format", "सादा पाठ ढाँचा"),
    SETTINGS_EXPORT_JSON("Export as JSON", "JSON को रूपमा निर्यात"),
    SETTINGS_EXPORT_JSON_DESC("Machine-readable format", "मेसिन-पठनयोग्य ढाँचा"),
    SETTINGS_EXPORT_CSV("CSV Data", "CSV डाटा"),
    SETTINGS_EXPORT_CSV_DESC("Spreadsheet format", "स्प्रेडसिट ढाँचा"),

    // Preferences
    SETTINGS_HOUSE_SYSTEM("House System", "भाव पद्धति"),
    SETTINGS_AYANAMSA("Ayanamsa", "अयनांश"),
    SETTINGS_LANGUAGE("Language", "भाषा"),
    SETTINGS_DATE_SYSTEM("Date System", "मिति प्रणाली"),
    SETTINGS_THEME("Theme", "थिम"),

    // Theme options
    THEME_LIGHT("Light", "उज्यालो"),
    THEME_LIGHT_DESC("Always use light theme", "सधैं उज्यालो थिम प्रयोग गर्नुहोस्"),
    THEME_DARK("Dark", "अँध्यारो"),
    THEME_DARK_DESC("Always use dark theme", "सधैं अँध्यारो थिम प्रयोग गर्नुहोस्"),
    THEME_SYSTEM("System", "प्रणाली"),
    THEME_SYSTEM_DESC("Follow device settings", "यन्त्र सेटिङ्स पछ्याउनुहोस्"),

    // About section
    SETTINGS_ABOUT_APP("About AstroStorm", "AstroStorm बारेमा"),
    SETTINGS_VERSION("Version %s", "संस्करण %s"),
    SETTINGS_CALC_ENGINE("Calculation Engine", "गणना इन्जिन"),
    SETTINGS_CALC_ENGINE_DESC("Swiss Ephemeris (JPL Mode)", "स्विस ईफेमेरिस (JPL मोड)"),
    SETTINGS_APP_TAGLINE("Ultra-Precision Vedic Astrology", "अति-सटीक वैदिक ज्योतिष"),
    SETTINGS_APP_DESC("Powered by Swiss Ephemeris with JPL planetary data for astronomical-grade accuracy in all calculations.", "सबै गणनाहरूमा खगोलीय-ग्रेड सटीकताको लागि JPL ग्रह डाटासहित स्विस ईफेमेरिसद्वारा संचालित।"),
    SETTINGS_LAHIRI("Lahiri Ayanamsa", "लहिरी अयनांश"),
    SETTINGS_PLACIDUS("Placidus Houses", "प्लासिडस भावहरू"),

    // Delete dialog
    DIALOG_DELETE_PROFILE("Delete Profile", "प्रोफाइल मेट्नुहोस्"),
    DIALOG_DELETE_CONFIRM("Are you sure you want to delete %s? This action cannot be undone.", "के तपाईं %s मेट्न चाहनुहुन्छ? यो कार्य पूर्ववत गर्न सकिँदैन।"),
    DIALOG_EXPORT_CHART("Export Chart", "कुण्डली निर्यात गर्नुहोस्"),

    // Chart detail labels
    CHART_ASCENDANT("Ascendant", "लग्न"),
    CHART_MOON_SIGN("Moon Sign", "चन्द्र राशि"),
    CHART_NAKSHATRA("Nakshatra", "नक्षत्र"),
    MISC_INFO("Information", "जानकारी"),

    // ============================================
    // CHART INPUT SCREEN
    // ============================================
    INPUT_NEW_CHART("New Birth Chart", "नयाँ जन्म कुण्डली"),
    INPUT_IDENTITY("Identity", "पहिचान"),
    INPUT_DATE_TIME("Date & Time", "मिति र समय"),
    INPUT_COORDINATES("Coordinates", "निर्देशांकहरू"),

    INPUT_FULL_NAME("Full name", "पूरा नाम"),
    INPUT_GENDER("Gender", "लिङ्ग"),
    INPUT_LOCATION("Location", "स्थान"),
    INPUT_SEARCH_LOCATION("Search city or enter manually", "शहर खोज्नुहोस् वा म्यानुअल रूपमा प्रविष्ट गर्नुहोस्"),
    INPUT_TIMEZONE("Timezone", "समय क्षेत्र"),
    INPUT_LATITUDE("Latitude", "अक्षांश"),
    INPUT_LONGITUDE("Longitude", "देशान्तर"),
    INPUT_ALTITUDE("Altitude (m) - Optional", "उचाई (मि.) - वैकल्पिक"),

    INPUT_SELECT_DATE("Select date", "मिति छान्नुहोस्"),
    INPUT_SELECT_TIME("Select time", "समय छान्नुहोस्"),

    // Validation errors
    ERROR_INPUT("Input Error", "इनपुट त्रुटि"),
    ERROR_INVALID_COORDS("Please enter valid latitude and longitude", "कृपया मान्य अक्षांश र देशान्तर प्रविष्ट गर्नुहोस्"),
    ERROR_LATITUDE_RANGE("Latitude must be between -90 and 90", "अक्षांश -९० र ९० बीचमा हुनुपर्छ"),
    ERROR_LONGITUDE_RANGE("Longitude must be between -180 and 180", "देशान्तर -१८० र १८० बीचमा हुनुपर्छ"),
    ERROR_CHECK_INPUT("Please check your input values", "कृपया आफ्नो इनपुट मानहरू जाँच गर्नुहोस्"),
    ERROR_CALCULATION_FAILED("Calculation failed", "गणना असफल भयो"),
    ERROR_RATE_LIMIT("Too many requests. Please wait.", "धेरै अनुरोधहरू। कृपया पर्खनुहोस्।"),
    ERROR_SEARCH_FAILED("Search failed. Please try again.", "खोज असफल भयो। कृपया फेरि प्रयास गर्नुहोस्।"),
    ERROR_RATE_LIMIT_EXCEEDED("Rate limit exceeded", "दर सीमा नाघ्यो"),

    // Location Search
    LOCATION_SEARCH("Search location", "स्थान खोज्नुहोस्"),
    LOCATION_PLACEHOLDER("Enter city or place name", "शहर वा ठाउँको नाम प्रविष्ट गर्नुहोस्"),
    LOCATION_CLEAR("Clear search", "खोज हटाउनुहोस्"),
    LOCATION_SELECT("Select %s", "%s छान्नुहोस्"),

    // ============================================
    // PROFILE EDIT SCREEN
    // ============================================
    EDIT_PROFILE_TITLE("Edit Profile", "प्रोफाइल सम्पादन"),
    EDIT_PROFILE_NO_DATA("No chart data available. Please select a profile to edit.", "कुनै कुण्डली डाटा उपलब्ध छैन। कृपया सम्पादन गर्न प्रोफाइल छान्नुहोस्।"),

    // ============================================
    // GENDER OPTIONS
    // ============================================
    GENDER_MALE("Male", "पुरुष"),
    GENDER_FEMALE("Female", "महिला"),
    GENDER_OTHER("Other", "अन्य"),

    // ============================================
    // PLANETS
    // ============================================
    PLANET_SUN("Sun", "सूर्य"),
    PLANET_MOON("Moon", "चन्द्र"),
    PLANET_MERCURY("Mercury", "बुध"),
    PLANET_VENUS("Venus", "शुक्र"),
    PLANET_MARS("Mars", "मंगल"),
    PLANET_JUPITER("Jupiter", "बृहस्पति"),
    PLANET_SATURN("Saturn", "शनि"),
    PLANET_RAHU("Rahu", "राहु"),
    PLANET_KETU("Ketu", "केतु"),
    PLANET_URANUS("Uranus", "युरेनस"),
    PLANET_NEPTUNE("Neptune", "नेप्च्युन"),
    PLANET_PLUTO("Pluto", "प्लुटो"),

    // ============================================
    // ZODIAC SIGNS
    // ============================================
    SIGN_ARIES("Aries", "मेष"),
    SIGN_TAURUS("Taurus", "वृष"),
    SIGN_GEMINI("Gemini", "मिथुन"),
    SIGN_CANCER("Cancer", "कर्कट"),
    SIGN_LEO("Leo", "सिंह"),
    SIGN_VIRGO("Virgo", "कन्या"),
    SIGN_LIBRA("Libra", "तुला"),
    SIGN_SCORPIO("Scorpio", "वृश्चिक"),
    SIGN_SAGITTARIUS("Sagittarius", "धनु"),
    SIGN_CAPRICORN("Capricorn", "मकर"),
    SIGN_AQUARIUS("Aquarius", "कुम्भ"),
    SIGN_PISCES("Pisces", "मीन"),

    // ============================================
    // NAKSHATRAS
    // ============================================
    NAKSHATRA_ASHWINI("Ashwini", "अश्विनी"),
    NAKSHATRA_BHARANI("Bharani", "भरणी"),
    NAKSHATRA_KRITTIKA("Krittika", "कृत्तिका"),
    NAKSHATRA_ROHINI("Rohini", "रोहिणी"),
    NAKSHATRA_MRIGASHIRA("Mrigashira", "मृगशिरा"),
    NAKSHATRA_ARDRA("Ardra", "आर्द्रा"),
    NAKSHATRA_PUNARVASU("Punarvasu", "पुनर्वसु"),
    NAKSHATRA_PUSHYA("Pushya", "पुष्य"),
    NAKSHATRA_ASHLESHA("Ashlesha", "आश्लेषा"),
    NAKSHATRA_MAGHA("Magha", "मघा"),
    NAKSHATRA_PURVA_PHALGUNI("Purva Phalguni", "पूर्वा फाल्गुनी"),
    NAKSHATRA_UTTARA_PHALGUNI("Uttara Phalguni", "उत्तरा फाल्गुनी"),
    NAKSHATRA_HASTA("Hasta", "हस्त"),
    NAKSHATRA_CHITRA("Chitra", "चित्रा"),
    NAKSHATRA_SWATI("Swati", "स्वाति"),
    NAKSHATRA_VISHAKHA("Vishakha", "विशाखा"),
    NAKSHATRA_ANURADHA("Anuradha", "अनुराधा"),
    NAKSHATRA_JYESHTHA("Jyeshtha", "ज्येष्ठा"),
    NAKSHATRA_MULA("Mula", "मूल"),
    NAKSHATRA_PURVA_ASHADHA("Purva Ashadha", "पूर्वाषाढा"),
    NAKSHATRA_UTTARA_ASHADHA("Uttara Ashadha", "उत्तराषाढा"),
    NAKSHATRA_SHRAVANA("Shravana", "श्रवण"),
    NAKSHATRA_DHANISHTHA("Dhanishtha", "धनिष्ठा"),
    NAKSHATRA_SHATABHISHA("Shatabhisha", "शतभिषा"),
    NAKSHATRA_PURVA_BHADRAPADA("Purva Bhadrapada", "पूर्वभाद्रपद"),
    NAKSHATRA_UTTARA_BHADRAPADA("Uttara Bhadrapada", "उत्तरभाद्रपद"),
    NAKSHATRA_REVATI("Revati", "रेवती"),

    // ============================================
    // HOUSE SYSTEMS
    // ============================================
    HOUSE_PLACIDUS("Placidus", "प्लासिडस"),
    HOUSE_KOCH("Koch", "कोच"),
    HOUSE_PORPHYRIUS("Porphyrius", "पोर्फिरियस"),
    HOUSE_REGIOMONTANUS("Regiomontanus", "रेजिओमोन्टानस"),
    HOUSE_CAMPANUS("Campanus", "क्याम्पानस"),
    HOUSE_EQUAL("Equal", "समान"),
    HOUSE_WHOLE_SIGN("Whole Sign", "सम्पूर्ण राशि"),
    HOUSE_VEHLOW("Vehlow", "भेहलो"),
    HOUSE_MERIDIAN("Meridian", "मेरिडियन"),
    HOUSE_MORINUS("Morinus", "मोरिनस"),
    HOUSE_ALCABITUS("Alcabitus", "अल्काबिटस"),

    // ============================================
    // AYANAMSA OPTIONS
    // ============================================
    AYANAMSA_LAHIRI("Lahiri", "लहिरी"),
    AYANAMSA_RAMAN("Raman", "रमण"),
    AYANAMSA_KRISHNAMURTI("Krishnamurti", "कृष्णमूर्ति"),
    AYANAMSA_TRUE_CHITRAPAKSHA("True Chitrapaksha", "सत्य चित्रपक्ष"),

    // ============================================
    // YOGA ANALYSIS
    // ============================================
    YOGA_ANALYSIS_SUMMARY("Yoga Analysis Summary", "योग विश्लेषण सारांश"),
    YOGA_OVERALL_STRENGTH("Overall Yoga Strength", "समग्र योग बल"),
    YOGA_TOTAL("Total Yogas", "कुल योगहरू"),
    YOGA_AUSPICIOUS("Auspicious", "शुभ"),
    YOGA_CHALLENGING("Challenging", "चुनौतीपूर्ण"),
    YOGA_ALL("All", "सबै"),
    YOGA_COUNT_DETECTED("%d yogas detected", "%d योगहरू पत्ता लागेको"),
    YOGA_INFORMATION("Yoga Information", "योग जानकारी"),
    YOGA_ABOUT_TITLE("About Vedic Yogas", "वैदिक योगहरूको बारेमा"),
    YOGA_ABOUT_DESCRIPTION("Yogas are special planetary combinations in Vedic astrology that indicate specific life patterns, talents, and karmic influences.", "योगहरू वैदिक ज्योतिषमा विशेष ग्रह संयोजनहरू हुन् जसले विशिष्ट जीवन ढाँचा, प्रतिभा र कर्म प्रभावहरू संकेत गर्छन्।"),
    YOGA_CATEGORIES_TITLE("Categories", "वर्गहरू"),
    YOGA_GOT_IT("Got it", "बुझें"),
    YOGA_NO_DATA("No yoga data available", "कुनै योग डाटा उपलब्ध छैन"),
    YOGA_NO_CHART_MESSAGE("Select or create a birth profile to view yogas.", "योगहरू हेर्न जन्म प्रोफाइल छान्नुहोस् वा बनाउनुहोस्।"),
    YOGAS_COUNT_DETECTED("%1\$d yogas detected in %2\$s", "%2\$s मा %1\$d योगहरू पत्ता लागेको"),

    // Yoga Categories
    YOGA_CATEGORY_WEALTH("Wealth Yogas", "धन योगहरू"),
    YOGA_CATEGORY_WEALTH_DESC("Combinations for prosperity", "समृद्धिका लागि संयोजनहरू"),
    YOGA_CATEGORY_RAJA("Raja Yogas", "राज योगहरू"),
    YOGA_CATEGORY_RAJA_DESC("Combinations for power & authority", "शक्ति र अधिकारका लागि संयोजनहरू"),
    YOGA_CATEGORY_SPIRITUAL("Spiritual Yogas", "आध्यात्मिक योगहरू"),
    YOGA_CATEGORY_SPIRITUAL_DESC("Combinations for spiritual growth", "आध्यात्मिक वृद्धिका लागि संयोजनहरू"),
    YOGA_CATEGORY_CHALLENGING("Challenging Yogas", "चुनौतीपूर्ण योगहरू"),
    YOGA_CATEGORY_CHALLENGING_DESC("Combinations indicating obstacles", "बाधाहरू संकेत गर्ने संयोजनहरू"),
    YOGA_CATEGORY_OTHER("Other Yogas", "अन्य योगहरू"),
    YOGA_CATEGORY_OTHER_DESC("Other planetary combinations", "अन्य ग्रह संयोजनहरू"),
    YOGA_CATEGORY_DHANA("Dhana Yogas", "धन योगहरू"),
    YOGA_CATEGORY_DHANA_DESC("Combinations for wealth", "धनका लागि संयोजनहरू"),
    YOGA_CATEGORY_MAHAPURUSHA("Mahapurusha Yogas", "महापुरुष योगहरू"),
    YOGA_CATEGORY_MAHAPURUSHA_DESC("Great personality combinations", "महान व्यक्तित्व संयोजनहरू"),
    YOGA_CATEGORY_NABHASA("Nabhasa Yogas", "नाभस योगहरू"),
    YOGA_CATEGORY_NABHASA_DESC("Celestial combinations", "आकाशीय संयोजनहरू"),
    YOGA_CATEGORY_CHANDRA("Chandra Yogas", "चन्द्र योगहरू"),
    YOGA_CATEGORY_CHANDRA_DESC("Moon-based combinations", "चन्द्रमामा आधारित संयोजनहरू"),
    YOGA_CATEGORY_SOLAR("Solar Yogas", "सूर्य योगहरू"),
    YOGA_CATEGORY_SOLAR_DESC("Sun-based combinations", "सूर्यमा आधारित संयोजनहरू"),
    YOGA_CATEGORY_SPECIAL("Special Yogas", "विशेष योगहरू"),
    YOGA_CATEGORY_SPECIAL_DESC("Rare and special combinations", "दुर्लभ र विशेष संयोजनहरू"),
    YOGA_CATEGORY_NEGATIVE("Negative Yogas", "नकारात्मक योगहरू"),
    YOGA_CATEGORY_NEGATIVE_DESC("Combinations indicating challenges", "चुनौतीहरू संकेत गर्ने संयोजनहरू"),

    // Yoga Tab Content UI Strings
    YOGA_MOST_SIGNIFICANT("Most Significant Yogas", "सबैभन्दा महत्त्वपूर्ण योगहरू"),
    YOGA_SANSKRIT("Sanskrit", "संस्कृत"),
    YOGA_EFFECTS("Effects", "प्रभावहरू"),
    YOGA_ACTIVATION("Activation", "सक्रियता"),
    YOGA_CANCELLATION_FACTORS("Cancellation/Mitigation Factors", "रद्द/न्यूनीकरण कारकहरू"),
    YOGA_NO_CATEGORY_FOUND("No %s found", "कुनै %s फेला परेन"),
    YOGA_NONE_DETECTED("No yogas detected", "कुनै योग पत्ता लागेन"),
    YOGA_HOUSE_PREFIX("H", "भाव"),

    // ============================================
    // PROFILE SWITCHER
    // ============================================
    PROFILE_SWITCH("Switch Profile", "प्रोफाइल बदल्नुहोस्"),
    PROFILE_ADD_NEW("Add New Profile", "नयाँ प्रोफाइल थप्नुहोस्"),
    PROFILE_ADD_NEW_CHART("Add new chart", "नयाँ कुण्डली थप्नुहोस्"),
    PROFILE_CURRENT("Current", "हालको"),
    PROFILE_NO_SAVED_CHARTS("No saved charts", "कुनै सुरक्षित कुण्डली छैन"),
    PROFILE_ADD_FIRST_CHART("Add your first chart to get started", "सुरु गर्न आफ्नो पहिलो कुण्डली थप्नुहोस्"),
    PROFILE_SELECTED("selected", "छानिएको"),
    PROFILE_SELECT("Select Profile", "प्रोफाइल छान्नुहोस्"),
    PROFILE_CURRENT_A11Y("Current profile: %s. Tap to switch profiles", "हालको प्रोफाइल: %s। प्रोफाइलहरू बदल्न ट्याप गर्नुहोस्"),
    PROFILE_NO_SELECTED_A11Y("No profile selected. Tap to select a profile", "कुनै प्रोफाइल छानिएको छैन। प्रोफाइल छान्न ट्याप गर्नुहोस्"),
    PROFILE_BIRTH_CHART("Birth chart", "जन्म कुण्डली");

}

/**
 * Matchmaking and Guna string keys
 * Part 2 of 4 split enums to avoid JVM method size limit
 */
enum class StringKeyMatch(override val en: String, override val ne: String) : StringKeyInterface {
    // ============================================
    // MATCHMAKING
    // ============================================
    MATCH_TITLE("Kundli Milan", "कुण्डली मिलान"),
    MATCH_SELECT_PROFILES("Select Profiles", "प्रोफाइलहरू छान्नुहोस्"),
    MATCH_PERSON_1("Person 1", "व्यक्ति १"),
    MATCH_PERSON_2("Person 2", "व्यक्ति २"),
    MATCH_CALCULATE("Calculate Match", "मिलान गणना गर्नुहोस्"),
    MATCH_TOTAL_POINTS("Total Points", "कुल अंकहरू"),
    MATCH_OUT_OF("out of", "मध्ये"),
    MATCH_COMPATIBILITY("Compatibility", "अनुकूलता"),
    MATCH_EXCELLENT("Excellent", "उत्कृष्ट"),
    MATCH_GOOD("Good", "राम्रो"),
    MATCH_AVERAGE("Average", "औसत"),
    MATCH_BELOW_AVERAGE("Below Average", "औसतमुनि"),

    // Matchmaking Tabs & Sections
    MATCH_OVERVIEW("Overview", "अवलोकन"),
    MATCH_GUNAS("Gunas", "गुणहरू"),
    MATCH_DOSHAS("Doshas", "दोषहरू"),
    MATCH_NAKSHATRAS("Nakshatras", "नक्षत्रहरू"),
    MATCH_SELECT_BRIDE("Select Bride", "वधू छान्नुहोस्"),
    MATCH_SELECT_GROOM("Select Groom", "वर छान्नुहोस्"),
    MATCH_SWAP_PROFILES("Swap Profiles", "प्रोफाइलहरू स्वाप गर्नुहोस्"),
    MATCH_ASHTAKOOTA("Ashtakoota Points", "अष्टकूट अंकहरू"),
    MATCH_SHARE_REPORT("Share Report", "रिपोर्ट साझा गर्नुहोस्"),
    MATCH_COPY_REPORT("Copy Report", "रिपोर्ट कपी गर्नुहोस्"),
    MATCH_SHARE_AS_TEXT("Share as Text", "पाठको रूपमा साझा गर्नुहोस्"),
    MATCH_SHARE_AS_IMAGE("Share as Image", "छविको रूपमा साझा गर्नुहोस्"),
    MATCH_CALCULATING("Calculating compatibility...", "अनुकूलता गणना गर्दै..."),
    MATCH_NO_PROFILES("No profiles available", "कुनै प्रोफाइल उपलब्ध छैन"),
    MATCH_SELECT_BOTH("Please select both profiles", "कृपया दुवै प्रोफाइल छान्नुहोस्"),

    // Additional Matchmaking Entries
    MATCH_REMEDIES("Remedies", "उपायहरू"),
    MATCH_COPIED_TO_CLIPBOARD("Report copied to clipboard", "रिपोर्ट क्लिपबोर्डमा कपी भयो"),
    MATCH_CLEAR_SELECTION("Clear Selection", "छनौट हटाउनुहोस्"),
    MATCH_BRIDE("Bride", "वधू"),
    MATCH_GROOM("Groom", "वर"),
    MATCH_CREATE_CHARTS_FIRST("Create birth charts first to use matchmaking", "मिलान प्रयोग गर्न पहिले जन्म कुण्डली बनाउनुहोस्"),
    MATCH_TAP_TO_SELECT("Tap to select", "छान्न ट्याप गर्नुहोस्"),
    MATCH_CONNECTED("Connected", "जोडिएको"),
    MATCH_NOT_CONNECTED("Not Connected", "जोडिएको छैन"),
    MATCH_ANALYZING_COMPATIBILITY("Analyzing compatibility...", "अनुकूलता विश्लेषण गर्दै..."),
    MATCH_CALCULATING_DOSHAS("Calculating doshas...", "दोषहरू गणना गर्दै..."),
    MATCH_CALCULATION_ERROR("Calculation Error", "गणना त्रुटि"),
    MATCH_MANGLIK("Manglik", "मांगलिक"),
    MATCH_NADI("Nadi", "नाडी"),
    MATCH_DOSHA_PRESENT("Dosha Present", "दोष उपस्थित"),
    MATCH_BHAKOOT("Bhakoot", "भकूट"),
    MATCH_NEEDS_ATTENTION("Needs Attention", "ध्यान आवश्यक"),
    MATCH_GUNA_DISTRIBUTION("Guna Distribution", "गुण वितरण"),
    MATCH_PROFILE_COMPARISON("Profile Comparison", "प्रोफाइल तुलना"),
    MATCH_VS("vs", "विरुद्ध"),
    MATCH_MOON_SIGN("Moon Sign", "चन्द्र राशि"),
    MATCH_NAKSHATRA("Nakshatra", "नक्षत्र"),
    MATCH_PADA("Pada", "पद"),
    MATCH_KEY_CONSIDERATIONS("Key Considerations", "मुख्य विचारहरू"),
    MATCH_FAVORABLE("Favorable", "अनुकूल"),
    MATCH_TOTAL_SCORE("Total Score", "कुल अंक"),
    MATCH_MANGLIK_DOSHA_ANALYSIS("Manglik Dosha Analysis", "मांगलिक दोष विश्लेषण"),
    MATCH_MARS_PLACEMENT("Mars Placement", "मंगल स्थिति"),
    MATCH_MARS_IN_HOUSE("Mars in House %d", "भाव %d मा मंगल"),
    MATCH_CONTRIBUTING_FACTORS("Contributing Factors", "योगदान गर्ने कारकहरू"),
    MATCH_CANCELLATION_FACTORS("Cancellation Factors", "रद्द गर्ने कारकहरू"),
    MATCH_NADI_DOSHA("Nadi Dosha", "नाडी दोष"),
    MATCH_HEALTH_PROGENY("Health & Progeny", "स्वास्थ्य र सन्तान"),
    MATCH_PRESENT("Present", "उपस्थित"),
    MATCH_ABSENT("Absent", "अनुपस्थित"),
    MATCH_NADI_WARNING("Same Nadi can indicate health and progeny concerns", "एउटै नाडीले स्वास्थ्य र सन्तान सम्बन्धी चिन्ता संकेत गर्न सक्छ"),
    MATCH_BHAKOOT_DOSHA("Bhakoot Dosha", "भकूट दोष"),
    MATCH_FINANCIAL_HARMONY("Financial Harmony", "आर्थिक सामञ्जस्य"),
    MATCH_BRIDE_RASHI("Bride Rashi", "वधू राशि"),
    MATCH_GROOM_RASHI("Groom Rashi", "वर राशि"),
    MATCH_NAKSHATRA_COMPATIBILITY("Nakshatra Compatibility", "नक्षत्र अनुकूलता"),
    MATCH_BIRTH_STAR("Birth Star", "जन्म नक्षत्र"),
    MATCH_BIRTH_NAKSHATRA("Birth Nakshatra", "जन्म नक्षत्र"),
    MATCH_NAKSHATRA_LORD("Nakshatra Lord", "नक्षत्र स्वामी"),
    MATCH_GANA("Gana", "गण"),
    MATCH_YONI("Yoni", "योनि"),
    MATCH_RAJJU_MATCHING("Rajju Matching", "रज्जु मिलान"),
    MATCH_LONGEVITY("Longevity", "दीर्घायु"),
    MATCH_CONFLICT("Conflict", "द्वन्द्व"),
    MATCH_COMPATIBLE("Compatible", "अनुकूल"),
    MATCH_RAJJU_DESCRIPTION("Rajju indicates the body parts and their compatibility in marriage.", "रज्जुले शरीरका अंगहरू र विवाहमा तिनीहरूको अनुकूलता संकेत गर्दछ।"),
    MATCH_VEDHA_ANALYSIS("Vedha Analysis", "वेध विश्लेषण"),
    MATCH_OBSTRUCTION_CHECK("Obstruction Check", "बाधा जाँच"),
    MATCH_NONE("None", "कुनै पनि छैन"),
    MATCH_VEDHA_DESCRIPTION("Vedha indicates mutual affliction between nakshatras.", "वेधले नक्षत्रहरू बीचको पारस्परिक पीडा संकेत गर्दछ।"),
    MATCH_STREE_DEERGHA("Stree Deergha", "स्त्री दीर्घ"),
    MATCH_PROSPERITY_FACTORS("Prosperity Factors", "समृद्धि कारकहरू"),
    MATCH_STREE_DEERGHA_LABEL("Stree Deergha", "स्त्री दीर्घ"),
    MATCH_MAHENDRA("Mahendra", "महेन्द्र"),
    MATCH_BENEFICIAL("Beneficial", "लाभदायक"),
    MATCH_SUGGESTED_REMEDIES("Suggested Remedies", "सुझाव गरिएका उपायहरू"),
    MATCH_VEDIC_RECOMMENDATIONS("Vedic Recommendations", "वैदिक सिफारिसहरू"),
    MATCH_REMEDIES_DISCLAIMER("Remedies should be performed under guidance of a qualified astrologer.", "उपायहरू योग्य ज्योतिषीको मार्गदर्शनमा गर्नुपर्छ।"),
    MATCH_NO_CHARTS("No Charts", "कुनै कुण्डली छैन"),
    MATCH_SELECT_BRIDE_PROFILE("Select bride profile", "वधू प्रोफाइल छान्नुहोस्"),
    MATCH_SELECT_GROOM_PROFILE("Select groom profile", "वर प्रोफाइल छान्नुहोस्"),
    MATCH_PREPARING_ANALYSIS("Preparing analysis...", "विश्लेषण तयार गर्दै..."),
    MATCH_CREATE_CHARTS("Create Charts", "कुण्डली बनाउनुहोस्"),
    MATCH_SELECT_TAP_CARDS("Select by tapping cards", "कार्डहरू ट्याप गरेर छान्नुहोस्"),
    MATCH_TAP_BRIDE_CARD("Tap bride card to select", "छान्न वधू कार्ड ट्याप गर्नुहोस्"),
    MATCH_TAP_GROOM_CARD("Tap groom card to select", "छान्न वर कार्ड ट्याप गर्नुहोस्"),
    MATCH_CHARTS_AVAILABLE("%d charts available", "%d कुण्डली उपलब्ध"),
    MATCH_NO_CHARTS_AVAILABLE("No charts available", "कुनै कुण्डली उपलब्ध छैन"),
    MATCH_SELECTED("Selected", "छानिएको"),
    MATCH_COPY_FULL_REPORT("Copy Full Report", "पूर्ण रिपोर्ट कपी गर्नुहोस्"),
    MATCH_COPY_FULL_DESC("Complete compatibility analysis", "पूर्ण अनुकूलता विश्लेषण"),
    MATCH_COPY_SUMMARY("Copy Summary", "सारांश कपी गर्नुहोस्"),
    MATCH_COPY_SUMMARY_DESC("Brief compatibility overview", "संक्षिप्त अनुकूलता अवलोकन"),
    MATCH_COPY_SCORES("Copy Scores", "अंकहरू कपी गर्नुहोस्"),
    MATCH_COPY_SCORES_DESC("Guna scores only", "गुण अंकहरू मात्र"),

    // Guna Details
    GUNA_VARNA("Varna", "वर्ण"),
    GUNA_VASHYA("Vashya", "वश्य"),
    GUNA_TARA("Tara", "तारा"),
    GUNA_YONI("Yoni", "योनि"),
    GUNA_GRAHA_MAITRI("Graha Maitri", "ग्रह मैत्री"),
    GUNA_GANA("Gana", "गण"),
    GUNA_BHAKOOT("Bhakoot", "भकूट"),
    GUNA_NADI("Nadi", "नाडी"),
    GUNA_POINTS("Points", "अंकहरू"),

    // Dosha Analysis
    DOSHA_MANGLIK("Manglik Dosha", "मांगलिक दोष"),
    DOSHA_NADI("Nadi Dosha", "नाडी दोष"),
    DOSHA_BHAKOOT("Bhakoot Dosha", "भकूट दोष"),
    DOSHA_PRESENT("Present", "उपस्थित"),
    DOSHA_ABSENT("Absent", "अनुपस्थित"),
    DOSHA_CANCELLED("Cancelled", "रद्द"),
    DOSHA_REMEDIES_AVAILABLE("Remedies Available", "उपायहरू उपलब्ध"),

    // ============================================
    // PANCHANGA
    // ============================================
    PANCHANGA_TITHI("Tithi", "तिथि"),
    PANCHANGA_VARA("Vara (Day)", "वार"),
    PANCHANGA_NAKSHATRA_LABEL("Nakshatra", "नक्षत्र"),
    PANCHANGA_YOGA("Yoga", "योग"),
    PANCHANGA_KARANA("Karana", "करण"),
    PANCHANGA_SUNRISE("Sunrise", "सूर्योदय"),
    PANCHANGA_SUNSET("Sunset", "सूर्यास्त"),
    PANCHANGA_MOONRISE("Moonrise", "चन्द्रोदय"),
    PANCHANGA_MOONSET("Moonset", "चन्द्रास्त"),

    // ============================================
    // MUHURTA
    // ============================================
    MUHURTA_TITLE("Muhurta Finder", "मुहूर्त खोजकर्ता"),
    MUHURTA_SELECT_EVENT("Select Event Type", "घटना प्रकार छान्नुहोस्"),
    MUHURTA_DATE_RANGE("Date Range", "मिति दायरा"),
    MUHURTA_FIND("Find Auspicious Times", "शुभ समय खोज्नुहोस्"),
    MUHURTA_RESULTS("Auspicious Muhurtas", "शुभ मुहूर्तहरू"),
    MUHURTA_PREV_DAY("Previous day", "अघिल्लो दिन"),
    MUHURTA_NEXT_DAY("Next day", "अर्को दिन"),
    MUHURTA_CALCULATING("Calculating muhurta...", "मुहूर्त गणना गर्दै..."),
    MUHURTA_ERROR("Something went wrong", "केही गलत भयो"),
    MUHURTA_SCORE("Score", "अंक"),
    MUHURTA_AUSPICIOUS_TIME("Auspicious Time", "शुभ समय"),
    MUHURTA_AVERAGE_TIME("Average Time", "सामान्य समय"),
    MUHURTA_PANCHANGA("Panchanga", "पञ्चाङ्ग"),
    MUHURTA_INAUSPICIOUS_PERIODS("Inauspicious Periods", "अशुभ अवधिहरू"),
    MUHURTA_RAHUKALA("Rahukala", "राहुकाल"),
    MUHURTA_RAHUKALA_DESC("Avoid important work", "महत्त्वपूर्ण कार्य बच्नुहोस्"),
    MUHURTA_YAMAGHANTA("Yamaghanta", "यमघन्ता"),
    MUHURTA_YAMAGHANTA_DESC("Avoid travel", "यात्रा बच्नुहोस्"),
    MUHURTA_GULIKA_KALA("Gulika Kala", "गुलिक काल"),
    MUHURTA_GULIKA_KALA_DESC("Avoid new beginnings", "नयाँ शुरुआत बच्नुहोस्"),
    MUHURTA_ACTIVE("ACTIVE", "सक्रिय"),
    MUHURTA_DAY_CHOGHADIYA("Day Choghadiya", "दिनको चोघड़िया"),
    MUHURTA_PERIODS("%d periods", "%d अवधिहरू"),
    MUHURTA_NOW("NOW", "अहिले"),
    MUHURTA_FROM("From", "बाट"),
    MUHURTA_TO("To", "सम्म"),
    MUHURTA_SEARCHING("Searching...", "खोज्दै..."),
    MUHURTA_FIND_DATES("Find Auspicious Dates", "शुभ मिति खोज्नुहोस्"),
    MUHURTA_FIND_AUSPICIOUS("Find Auspicious Dates", "शुभ मितिहरू खोज्नुहोस्"),
    MUHURTA_SEARCH_EMPTY("Search for Auspicious Times", "शुभ समय खोज्नुहोस्"),
    MUHURTA_SEARCH_HELP("Select an activity and date range to find the most favorable muhurtas", "सबैभन्दा अनुकूल मुहूर्तहरू खोज्न गतिविधि र मिति दायरा छान्नुहोस्"),
    MUHURTA_FINDING("Finding auspicious times...", "शुभ समय खोज्दै..."),
    MUHURTA_NO_RESULTS("No Auspicious Times Found", "कुनै शुभ समय फेला परेन"),
    MUHURTA_NO_RESULTS_HELP("Try expanding your date range", "मिति दायरा विस्तार गर्ने प्रयास गर्नुहोस्"),
    MUHURTA_SEARCH_ERROR("Search Failed", "खोज असफल"),
    MUHURTA_RESULTS_TITLE("Auspicious Times", "शुभ समयहरू"),
    MUHURTA_RESULTS_COUNT("%d found", "%d फेला परेको"),
    MUHURTA_DETAIL_DAY("Day", "दिन"),
    MUHURTA_DETAIL_CHOGHADIYA("Choghadiya", "चोघड़िया"),
    MUHURTA_TODAY("Today", "आज"),
    MUHURTA_PREV_DAY_A11Y("Previous day", "अघिल्लो दिन"),
    MUHURTA_NEXT_DAY_A11Y("Next day", "अर्को दिन"),
    MUHURTA_VARA("Vara", "वार"),
    MUHURTA_TITHI("Tithi", "तिथि"),
    MUHURTA_YOGA("Yoga", "योग"),
    MUHURTA_KARANA("Karana", "करण"),
    MUHURTA_SUNRISE_SUNSET("Sunrise / Sunset", "सूर्योदय / सूर्यास्त"),
    MUHURTA_SELECT_ACTIVITY("Select Activity", "गतिविधि चयन गर्नुहोस्"),
    MUHURTA_SUITABLE_ACTIVITIES("Suitable Activities", "उपयुक्त गतिविधिहरू"),
    MUHURTA_AVOID_ACTIVITIES("Activities to Avoid", "बच्नुपर्ने गतिविधिहरू"),
    MUHURTA_RECOMMENDATIONS("Recommendations", "सिफारिसहरू"),
    MUHURTA_DATE_RANGE_LABEL("Date Range", "मिति दायरा"),

    // ============================================
    // REMEDIES
    // ============================================
    REMEDIES_TITLE("Vedic Remedies", "वैदिक उपायहरू"),
    REMEDIES_GEMSTONES("Gemstones", "रत्नहरू"),
    REMEDIES_MANTRAS("Mantras", "मन्त्रहरू"),
    REMEDIES_YANTRAS("Yantras", "यन्त्रहरू"),
    REMEDIES_RITUALS("Rituals", "पूजा विधिहरू"),
    REMEDIES_CHARITY("Charity", "दान"),
    REMEDIES_OVERVIEW("Overview", "अवलोकन"),
    REMEDIES_PLANETS("Planets", "ग्रहहरू"),
    REMEDIES_ANALYSIS("Remedies Analysis", "उपाय विश्लेषण"),
    REMEDIES_CHART_STRENGTH("Chart Strength", "कुण्डली बल"),
    REMEDIES_PLANETS_WELL_PLACED("%d of %d planets well-placed", "%d मध्ये %d ग्रह राम्ररी स्थित"),
    REMEDIES_PLANETS_ATTENTION("Planets Requiring Attention", "ध्यान आवश्यक ग्रहहरू"),
    REMEDIES_ESSENTIAL("Essential Remedies", "आवश्यक उपायहरू"),
    REMEDIES_WEEKLY_SCHEDULE("Weekly Remedy Schedule", "साप्ताहिक उपाय तालिका"),
    REMEDIES_WEEKLY_SCHEDULE_DESC("Perform planet-specific remedies on their designated days for maximum effect", "अधिकतम प्रभावको लागि तिनीहरूको तोकिएको दिनमा ग्रह-विशेष उपायहरू गर्नुहोस्"),
    REMEDIES_LIFE_AREA_FOCUS("Life Area Focus", "जीवन क्षेत्र फोकस"),
    REMEDIES_GENERAL_RECOMMENDATIONS("General Recommendations", "सामान्य सिफारिसहरू"),
    REMEDIES_METHOD("Method", "विधि"),
    REMEDIES_TIMING("Timing", "समय"),
    REMEDIES_DURATION("Duration", "अवधि"),
    REMEDIES_MANTRA_SECTION("Mantra", "मन्त्र"),
    REMEDIES_BENEFITS("Benefits", "लाभहरू"),
    REMEDIES_CAUTIONS("Cautions", "सावधानीहरू"),
    REMEDIES_SEARCH("Search remedies...", "उपायहरू खोज्नुहोस्..."),
    REMEDIES_FILTER_ALL("All", "सबै"),
    REMEDIES_NO_RESULTS("No remedies found", "कुनै उपाय फेला परेन"),
    REMEDIES_NO_RESULTS_SEARCH("No remedies found for \"%s\"", "\"%s\" को लागि कुनै उपाय फेला परेन"),
    REMEDIES_NO_CATEGORY("No remedies in this category", "यस वर्गमा कुनै उपाय छैन"),
    REMEDIES_NO_CHART("No chart selected", "कुनै कुण्डली छानिएको छैन"),
    REMEDIES_SELECT_CHART("Select a chart to view remedies", "उपायहरू हेर्न कुण्डली छान्नुहोस्"),
    REMEDIES_ANALYZING("Analyzing your chart...", "तपाईंको कुण्डली विश्लेषण गर्दै..."),
    REMEDIES_PREPARING("Preparing personalized remedies", "व्यक्तिगत उपायहरू तयार गर्दै"),
    REMEDIES_COPY_MANTRA("Copy mantra", "मन्त्र कपी गर्नुहोस्"),
    REMEDIES_RECOMMENDED("%d remedies recommended", "%d उपायहरू सिफारिस गरिएको"),
    REMEDIES_BEST_DAY("Best performed on %s", "%s मा गर्नु उत्तम"),
    REMEDIES_TOTAL("Total", "कुल"),
    REMEDIES_ESSENTIAL_COUNT("Essential", "आवश्यक"),

    // Planetary Status
    PLANETARY_STATUS_EXALTED("Exalted", "उच्च"),
    PLANETARY_STATUS_DEBILITATED("Debilitated", "नीच"),
    PLANETARY_STATUS_RETROGRADE("Retrograde", "वक्री"),
    PLANETARY_STATUS_COMBUST("Combust", "अस्त"),
    PLANETARY_STATUS_OWN_SIGN("Own Sign", "स्वराशि"),
    PLANETARY_STATUS_MOOLATRIKONA("Moolatrikona", "मूलत्रिकोण"),
    PLANETARY_STATUS_FRIENDLY("Friendly", "मित्र"),
    PLANETARY_STATUS_ENEMY_SIGN("Enemy Sign", "शत्रु राशि"),

    // ============================================
    // TIME DURATION LABELS
    // ============================================
    TIME_DAYS("%dd", "%d दिन"),
    TIME_WEEKS("%dw", "%d हप्ता"),
    TIME_MONTHS("%dm", "%d महिना"),
    TIME_YEARS("%dy", "%d वर्ष"),
    TIME_IN("in %s", "%s मा"),

    // ============================================
    // BS DATE PICKER
    // ============================================
    BS_DATE_PICKER_TITLE("Select BS Date", "वि.सं. मिति छान्नुहोस्"),
    BS_YEAR("Year", "वर्ष"),
    BS_MONTH("Month", "महिना"),
    BS_DAY("Day", "दिन"),

    // BS Months
    BS_MONTH_BAISHAKH("Baishakh", "बैशाख"),
    BS_MONTH_JESTHA("Jestha", "जेठ"),
    BS_MONTH_ASHADH("Ashadh", "असार"),
    BS_MONTH_SHRAWAN("Shrawan", "साउन"),
    BS_MONTH_BHADRA("Bhadra", "भदौ"),
    BS_MONTH_ASHWIN("Ashwin", "असोज"),
    BS_MONTH_KARTIK("Kartik", "कार्तिक"),
    BS_MONTH_MANGSIR("Mangsir", "मंसिर"),
    BS_MONTH_POUSH("Poush", "पुष"),
    BS_MONTH_MAGH("Magh", "माघ"),
    BS_MONTH_FALGUN("Falgun", "फाल्गुन"),
    BS_MONTH_CHAITRA("Chaitra", "चैत्र"),

    // Days of week (Nepali)
    DAY_SUNDAY("Sunday", "आइतबार"),
    DAY_MONDAY("Monday", "सोमबार"),
    DAY_TUESDAY("Tuesday", "मंगलबार"),
    DAY_WEDNESDAY("Wednesday", "बुधबार"),
    DAY_THURSDAY("Thursday", "बिहिबार"),
    DAY_FRIDAY("Friday", "शुक्रबार"),
    DAY_SATURDAY("Saturday", "शनिबार"),
    DAY_ANY("any day", "कुनै पनि दिन"),

    // ============================================
    // MISCELLANEOUS
    // ============================================
    MISC_UNAVAILABLE("Unavailable", "उपलब्ध छैन"),
    MISC_LOADING("Loading...", "लोड हुँदैछ..."),
    MISC_NO_DATA("No data available", "डाटा उपलब्ध छैन"),
    MISC_UNKNOWN("Unknown", "अज्ञात"),
    MISC_EXPAND("Expand", "विस्तार"),
    MISC_COLLAPSE("Collapse", "संकुचन"),
    MISC_MORE("More", "थप"),
    MISC_LESS("Less", "कम"),

    // ============================================
    // MATCHMAKING CALCULATOR - VARNA
    // ============================================
    VARNA_BRAHMIN("Brahmin", "ब्राह्मण"),
    VARNA_KSHATRIYA("Kshatriya", "क्षत्रिय"),
    VARNA_VAISHYA("Vaishya", "वैश्य"),
    VARNA_SHUDRA("Shudra", "शूद्र"),

    // ============================================
    // MATCHMAKING CALCULATOR - VASHYA
    // ============================================
    VASHYA_CHATUSHPADA("Quadruped", "चतुष्पद"),
    VASHYA_MANAVA("Human", "मानव"),
    VASHYA_JALACHARA("Aquatic", "जलचर"),
    VASHYA_VANACHARA("Wild", "वनचर"),
    VASHYA_KEETA("Insect", "कीट"),

    // ============================================
    // MATCHMAKING CALCULATOR - GANA
    // ============================================
    GANA_DEVA("Deva", "देव"),
    GANA_DEVA_DESC("Divine - Sattvik, gentle, spiritual", "दैवी - सात्त्विक, सौम्य, आध्यात्मिक"),
    GANA_MANUSHYA("Manushya", "मनुष्य"),
    GANA_MANUSHYA_DESC("Human - Rajasik, balanced, worldly", "मानव - राजसिक, सन्तुलित, सांसारिक"),
    GANA_RAKSHASA("Rakshasa", "राक्षस"),
    GANA_RAKSHASA_DESC("Demon - Tamasik, aggressive, dominant", "दानव - तामसिक, आक्रामक, प्रभावशाली"),

    // ============================================
    // MATCHMAKING CALCULATOR - YONI ANIMALS
    // ============================================
    YONI_HORSE("Horse", "घोडा"),
    YONI_ELEPHANT("Elephant", "हात्ती"),
    YONI_SHEEP("Sheep", "भेडा"),
    YONI_SERPENT("Serpent", "सर्प"),
    YONI_DOG("Dog", "कुकुर"),
    YONI_CAT("Cat", "बिरालो"),
    YONI_RAT("Rat", "मुसो"),
    YONI_COW("Cow", "गाई"),
    YONI_BUFFALO("Buffalo", "भैंसी"),
    YONI_TIGER("Tiger", "बाघ"),
    YONI_DEER("Deer", "मृग"),
    YONI_MONKEY("Monkey", "बाँदर"),
    YONI_MONGOOSE("Mongoose", "न्यौरी"),
    YONI_LION("Lion", "सिंह"),
    YONI_MALE("Male", "पुरुष"),
    YONI_FEMALE("Female", "स्त्री"),

    // ============================================
    // MATCHMAKING CALCULATOR - NADI
    // ============================================
    NADI_ADI("Adi (Vata)", "आदि (वात)"),
    NADI_ADI_DESC("Beginning - Wind element, controls movement and nervous system", "आदि - वायु तत्व, गति र स्नायु प्रणाली नियन्त्रण गर्छ"),
    NADI_MADHYA("Madhya (Pitta)", "मध्य (पित्त)"),
    NADI_MADHYA_DESC("Middle - Fire element, controls digestion and metabolism", "मध्य - अग्नि तत्व, पाचन र चयापचय नियन्त्रण गर्छ"),
    NADI_ANTYA("Antya (Kapha)", "अन्त्य (कफ)"),
    NADI_ANTYA_DESC("End - Water element, controls structure and lubrication", "अन्त्य - जल तत्व, संरचना र स्नेहन नियन्त्रण गर्छ"),

    // ============================================
    // MATCHMAKING CALCULATOR - RAJJU
    // ============================================
    RAJJU_PADA("Pada Rajju", "पाद रज्जु"),
    RAJJU_PADA_BODY("Feet", "पाउ"),
    RAJJU_KATI("Kati Rajju", "कटि रज्जु"),
    RAJJU_KATI_BODY("Waist", "कम्मर"),
    RAJJU_NABHI("Nabhi Rajju", "नाभि रज्जु"),
    RAJJU_NABHI_BODY("Navel", "नाभि"),
    RAJJU_KANTHA("Kantha Rajju", "कण्ठ रज्जु"),
    RAJJU_KANTHA_BODY("Neck", "घाँटी"),
    RAJJU_SIRO("Siro Rajju", "शिरो रज्जु"),
    RAJJU_SIRO_BODY("Head", "शिर"),
    RAJJU_SIRO_WARNING("Most serious - affects longevity of spouse", "सबैभन्दा गम्भीर - जीवनसाथीको आयुमा असर पार्छ"),
    RAJJU_KANTHA_WARNING("May cause health issues to both", "दुवैलाई स्वास्थ्य समस्या हुन सक्छ"),
    RAJJU_NABHI_WARNING("May affect children", "सन्तानमा असर पर्न सक्छ"),
    RAJJU_KATI_WARNING("May cause financial difficulties", "आर्थिक कठिनाइहरू हुन सक्छ"),
    RAJJU_PADA_WARNING("May cause wandering tendencies", "भ्रमण प्रवृत्ति हुन सक्छ"),

    // ============================================
    // MATCHMAKING CALCULATOR - MANGLIK DOSHA
    // ============================================
    MANGLIK_NONE("No Manglik Dosha", "मांगलिक दोष छैन"),
    MANGLIK_PARTIAL("Partial Manglik", "आंशिक मांगलिक"),
    MANGLIK_FULL("Full Manglik", "पूर्ण मांगलिक"),
    MANGLIK_DOUBLE("Double Manglik (Severe)", "दोहोरो मांगलिक (गम्भीर)"),
    MANGLIK_NO_DOSHA_DESC("No Manglik Dosha present.", "मांगलिक दोष छैन।"),
    MANGLIK_DETECTED("detected", "पत्ता लाग्यो"),
    MANGLIK_INTENSITY("intensity", "तीव्रता"),
    MANGLIK_MARS_IN("Mars in", "मंगल"),
    FROM_LAGNA("from Lagna", "लग्नबाट"),
    FROM_MOON("from Moon", "चन्द्रबाट"),
    FROM_VENUS("from Venus", "शुक्रबाट"),
    MANGLIK_FACTOR_FROM_LAGNA("Mars in House {house} from Lagna", "लग्नबाट भाव {house} मा मंगल"),
    MANGLIK_FACTOR_FROM_MOON("Mars in House {house} from Moon", "चन्द्रबाट भाव {house} मा मंगल"),
    MANGLIK_FACTOR_FROM_VENUS("Mars in House {house} from Venus", "शुक्रबाट भाव {house} मा मंगल"),
    MANGLIK_BOTH_NON("Both non-Manglik - No concerns", "दुवै गैर-मांगलिक - कुनै चिन्ता छैन"),
    MANGLIK_BOTH_MATCH("Both Manglik - Doshas cancel each other (Manglik to Manglik match is recommended)", "दुवै मांगलिक - दोषहरू एकअर्कालाई निष्क्रिय गर्छन् (मांगलिकसँग मांगलिक मिलान सिफारिस गरिएको)"),
    MANGLIK_MINOR_IMBALANCE("Minor Manglik imbalance - Manageable with remedies", "सानो मांगलिक असन्तुलन - उपायहरूद्वारा व्यवस्थापनयोग्य"),
    MANGLIK_BRIDE_ONLY("Bride is Manglik while Groom is not - Kumbh Vivah or other remedies advised", "दुलही मांगलिक छिन् जबकि दुलाहा छैनन् - कुम्भ विवाह वा अन्य उपायहरू सल्लाह दिइएको"),
    MANGLIK_GROOM_ONLY("Groom is Manglik while Bride is not - Remedies strongly recommended", "दुलाहा मांगलिक छन् जबकि दुलही छैनन् - उपायहरू दृढतापूर्वक सिफारिस गरिएको"),
    MANGLIK_SIGNIFICANT_IMBALANCE("Significant Manglik imbalance - Careful consideration and remedies essential", "महत्त्वपूर्ण मांगलिक असन्तुलन - सावधानीपूर्ण विचार र उपायहरू आवश्यक"),

    // ============================================
    // MATCHMAKING CALCULATOR - COMPATIBILITY RATINGS
    // ============================================
    COMPAT_EXCELLENT("Excellent Match", "उत्कृष्ट मिलान"),
    COMPAT_EXCELLENT_DESC("Highly recommended for marriage. Strong compatibility across all factors with harmonious planetary alignments.", "विवाहको लागि अत्यधिक सिफारिस गरिएको। सबै कारकहरूमा बलियो अनुकूलता र सामञ्जस्यपूर्ण ग्रह संरेखण।"),
    COMPAT_GOOD("Good Match", "राम्रो मिलान"),
    COMPAT_GOOD_DESC("Recommended. Good overall compatibility with minor differences that can be easily managed.", "सिफारिस गरिएको। सजिलैसँग व्यवस्थापन गर्न सकिने सानातिना भिन्नताहरूसहित राम्रो समग्र अनुकूलता।"),
    COMPAT_AVERAGE("Average Match", "औसत मिलान"),
    COMPAT_AVERAGE_DESC("Acceptable with some remedies. Moderate compatibility requiring mutual understanding and effort.", "केही उपायहरूसहित स्वीकार्य। पारस्परिक बुझाइ र प्रयास आवश्यक पर्ने मध्यम अनुकूलता।"),
    COMPAT_BELOW_AVERAGE("Below Average", "औसतमुनि"),
    COMPAT_BELOW_AVERAGE_DESC("Caution advised. Several compatibility issues that need addressing through remedies and counseling.", "सावधानी आवश्यक। उपाय र परामर्शद्वारा सम्बोधन गर्नुपर्ने धेरै अनुकूलता समस्याहरू।"),
    COMPAT_POOR("Poor Match", "कमजोर मिलान"),
    COMPAT_POOR_DESC("Not recommended. Significant compatibility challenges that may cause ongoing difficulties.", "सिफारिस गरिएको छैन। निरन्तर कठिनाइहरू ल्याउन सक्ने महत्त्वपूर्ण अनुकूलता चुनौतीहरू।"),

    // ============================================
    // MATCHMAKING CALCULATOR - TARA NAMES
    // ============================================
    TARA_JANMA("Janma (Birth)", "जन्म"),
    TARA_SAMPAT("Sampat (Wealth)", "सम्पत् (धन)"),
    TARA_VIPAT("Vipat (Danger)", "विपत् (खतरा)"),
    TARA_KSHEMA("Kshema (Wellbeing)", "क्षेम (कल्याण)"),
    TARA_PRATYARI("Pratyari (Obstacle)", "प्रत्यरि (बाधा)"),
    TARA_SADHANA("Sadhana (Achievement)", "साधना (उपलब्धि)"),
    TARA_VADHA("Vadha (Death)", "वध (मृत्यु)"),
    TARA_MITRA("Mitra (Friend)", "मित्र"),
    TARA_PARAMA_MITRA("Parama Mitra (Best Friend)", "परम मित्र"),

    // ============================================
    // GUNA MILAN ANALYSIS STRINGS
    // ============================================
    // Varna Analysis
    VARNA_DESC("Spiritual compatibility and ego harmony", "आध्यात्मिक अनुकूलता र अहंकार सामञ्जस्य"),
    VARNA_COMPATIBLE("Compatible: Groom's Varna ({groom}) is equal to or higher than Bride's ({bride}). This indicates spiritual harmony.", "अनुकूल: दुलाहाको वर्ण ({groom}) दुलहीको ({bride}) बराबर वा माथि छ। यसले आध्यात्मिक सामञ्जस्य संकेत गर्छ।"),
    VARNA_INCOMPATIBLE("Mismatch: Bride's Varna ({bride}) is higher than Groom's ({groom}). May cause ego-related issues.", "बेमेल: दुलहीको वर्ण ({bride}) दुलाहाको ({groom}) भन्दा माथि छ। अहंकार-सम्बन्धित समस्या हुन सक्छ।"),

    // Vashya Analysis
    VASHYA_DESC("Mutual attraction and influence", "पारस्परिक आकर्षण र प्रभाव"),
    VASHYA_EXCELLENT("Excellent mutual attraction and influence. Both partners can positively influence each other.", "उत्कृष्ट पारस्परिक आकर्षण र प्रभाव। दुवै साझेदारहरू एकअर्कालाई सकारात्मक रूपमा प्रभाव पार्न सक्छन्।"),
    VASHYA_VERY_GOOD("Very good compatibility with balanced influence between partners.", "साझेदारहरू बीच सन्तुलित प्रभावसहित धेरै राम्रो अनुकूलता।"),
    VASHYA_GOOD("Good compatibility with moderate mutual influence.", "मध्यम पारस्परिक प्रभावसहित राम्रो अनुकूलता।"),
    VASHYA_PARTIAL("Partial compatibility. One partner may dominate relationship dynamics.", "आंशिक अनुकूलता। एउटा साझेदारले सम्बन्धको गतिशीलतामा प्रभुत्व जमाउन सक्छ।"),
    VASHYA_INCOMPATIBLE("Incompatible Vashya types. May cause power struggles in the relationship.", "असंगत वश्य प्रकारहरू। सम्बन्धमा शक्ति संघर्ष हुन सक्छ।"),

    // Tara Analysis
    TARA_DESC("Destiny and birth star compatibility", "भाग्य र जन्म तारा अनुकूलता"),
    TARA_EXCELLENT("Both have auspicious Taras - excellent destiny compatibility. Harmonious life path.", "दुवैको शुभ तारा छ - उत्कृष्ट भाग्य अनुकूलता। सामञ्जस्यपूर्ण जीवन मार्ग।"),
    TARA_MODERATE("One auspicious Tara present - moderate destiny compatibility.", "एउटा शुभ तारा उपस्थित - मध्यम भाग्य अनुकूलता।"),
    TARA_INAUSPICIOUS("Both Taras are inauspicious - may face obstacles together. Remedies recommended.", "दुवै तारा अशुभ - सँगै अवरोधहरू सामना गर्न सक्छन्। उपायहरू सिफारिस गरिएको।"),

    // Yoni Analysis
    YONI_DESC("Physical and sexual compatibility", "शारीरिक र यौन अनुकूलता"),
    YONI_SAME("Same Yoni animal - perfect physical and instinctual compatibility. Strong natural attraction.", "उही योनि पशु - उत्तम शारीरिक र सहज अनुकूलता। बलियो प्राकृतिक आकर्षण।"),
    YONI_FRIENDLY("Friendly Yonis - very good physical compatibility. Natural understanding.", "मित्र योनि - धेरै राम्रो शारीरिक अनुकूलता। प्राकृतिक बुझाइ।"),
    YONI_NEUTRAL("Neutral Yonis - moderate physical compatibility. Requires some adjustment.", "तटस्थ योनि - मध्यम शारीरिक अनुकूलता। केही समायोजन आवश्यक।"),
    YONI_UNFRIENDLY("Unfriendly Yonis - some physical and instinctual differences. Needs conscious effort.", "अमैत्रीपूर्ण योनि - केही शारीरिक र सहज भिन्नताहरू। सचेत प्रयास आवश्यक।"),
    YONI_ENEMY("Enemy Yonis - significant physical incompatibility. May face intimacy challenges.", "शत्रु योनि - महत्त्वपूर्ण शारीरिक असंगतता। अन्तरंगता चुनौतीहरू सामना गर्न सक्छ।"),

    // Graha Maitri Analysis
    GRAHA_MAITRI_DESC("Mental compatibility and friendship", "मानसिक अनुकूलता र मित्रता"),
    GRAHA_MAITRI_EXCELLENT("Same lord or mutual friends - excellent mental compatibility. Natural understanding.", "उही स्वामी वा पारस्परिक मित्र - उत्कृष्ट मानसिक अनुकूलता। प्राकृतिक बुझाइ।"),
    GRAHA_MAITRI_VERY_GOOD("One friend, one neutral - very good mental harmony. Good communication.", "एउटा मित्र, एउटा तटस्थ - धेरै राम्रो मानसिक सामञ्जस्य। राम्रो सञ्चार।"),
    GRAHA_MAITRI_AVERAGE("Neutral relationship - average mental compatibility. Requires effort for understanding.", "तटस्थ सम्बन्ध - औसत मानसिक अनुकूलता। बुझाइको लागि प्रयास आवश्यक।"),
    GRAHA_MAITRI_FRICTION("One enemy present - some mental friction. Different thought processes.", "एउटा शत्रु उपस्थित - केही मानसिक घर्षण। फरक विचार प्रक्रियाहरू।"),
    GRAHA_MAITRI_INCOMPATIBLE("Mutual enemies - significant mental incompatibility. May face frequent misunderstandings.", "पारस्परिक शत्रु - महत्त्वपूर्ण मानसिक असंगतता। बारम्बार गलतफहमीहरू हुन सक्छ।"),

    // Gana Analysis
    GANA_DESC("Temperament and behavior compatibility", "स्वभाव र व्यवहार अनुकूलता"),
    GANA_SAME("Same Gana - perfect temperamental harmony. Similar approach to life and values.", "उही गण - उत्तम स्वभावगत सामञ्जस्य। जीवन र मूल्यहरूमा समान दृष्टिकोण।"),
    GANA_COMPATIBLE("Compatible Ganas - good temperamental harmony with minor differences.", "अनुकूल गण - सानातिना भिन्नताहरूसहित राम्रो स्वभावगत सामञ्जस्य।"),
    GANA_PARTIAL("Partially compatible - some temperamental adjustment needed.", "आंशिक अनुकूल - केही स्वभावगत समायोजन आवश्यक।"),
    GANA_DIFFERENT("Different temperaments - significant adjustment required. May cause lifestyle clashes.", "फरक स्वभाव - महत्त्वपूर्ण समायोजन आवश्यक। जीवनशैली टकराव हुन सक्छ।"),
    GANA_OPPOSITE("Opposite temperaments - major incompatibility. Frequent conflicts likely.", "विपरीत स्वभाव - प्रमुख असंगतता। बारम्बार द्वन्द्व हुन सक्छ।"),

    // Bhakoot Analysis
    BHAKOOT_DESC("Love, health, and financial compatibility", "प्रेम, स्वास्थ्य र आर्थिक अनुकूलता"),
    BHAKOOT_NO_DOSHA("No Bhakoot dosha - excellent compatibility for love, health, and finances.", "भकुट दोष छैन - प्रेम, स्वास्थ्य र वित्तको लागि उत्कृष्ट अनुकूलता।"),
    BHAKOOT_CANCELLED("Bhakoot dosha cancelled by same sign lord - no adverse effects.", "उही राशि स्वामीद्वारा भकुट दोष रद्द - कुनै प्रतिकूल प्रभाव छैन।"),
    BHAKOOT_2_12("Dhan-Vyaya (2-12) Bhakoot Dosha - financial concerns possible.", "धन-व्यय (२-१२) भकुट दोष - आर्थिक चिन्ता सम्भव।"),
    BHAKOOT_2_12_DESC("May cause financial fluctuations and differences in spending habits.", "आर्थिक उतार-चढाव र खर्च बानीमा भिन्नता हुन सक्छ।"),
    BHAKOOT_6_8("Shadashtak (6-8) Bhakoot Dosha - health concerns may arise.", "षडाष्टक (६-८) भकुट दोष - स्वास्थ्य चिन्ता हुन सक्छ।"),
    BHAKOOT_6_8_DESC("May affect health and cause separation tendencies. Most serious Bhakoot dosha.", "स्वास्थ्यमा असर पार्न र विच्छेद प्रवृत्ति हुन सक्छ। सबैभन्दा गम्भीर भकुट दोष।"),
    BHAKOOT_5_9("Signs are in 5-9 (Trine) relationship - actually favorable.", "राशिहरू ५-९ (त्रिकोण) सम्बन्धमा छन् - वास्तवमा अनुकूल।"),
    BHAKOOT_5_9_DESC("Trine relationship is auspicious for progeny, dharma, and spiritual growth.", "त्रिकोण सम्बन्ध सन्तान, धर्म र आध्यात्मिक विकासको लागि शुभ छ।"),
    BHAKOOT_FAVORABLE("Signs are in favorable positions for marital harmony.", "राशिहरू वैवाहिक सामञ्जस्यको लागि अनुकूल स्थानमा छन्।"),
    BHAKOOT_CANCEL_SAME_LORD("Same lord ({lord}) rules both Moon signs - Full Cancellation", "उही स्वामी ({lord}) ले दुवै चन्द्र राशि शासन गर्छ - पूर्ण रद्द"),
    BHAKOOT_CANCEL_MUTUAL_FRIENDS("Moon sign lords ({lord1} & {lord2}) are mutual friends - Full Cancellation", "चन्द्र राशि स्वामी ({lord1} र {lord2}) पारस्परिक मित्र हुन् - पूर्ण रद्द"),
    BHAKOOT_CANCEL_EXALTATION("Lord is exalted in partner's sign - Partial Cancellation", "स्वामी साथीको राशिमा उच्च छ - आंशिक रद्द"),
    BHAKOOT_CANCEL_FRIENDLY("Moon sign lords have friendly disposition - Partial Cancellation", "चन्द्र राशि स्वामीहरूको मैत्रीपूर्ण स्वभाव छ - आंशिक रद्द"),
    BHAKOOT_CANCEL_ELEMENT("Both Moon signs share same element ({element}) - Partial Cancellation", "दुवै चन्द्र राशिले एउटै तत्व ({element}) साझा गर्छन् - आंशिक रद्द"),

    // Nadi Analysis
    NADI_DESC("Health and progeny compatibility (most important)", "स्वास्थ्य र सन्तान अनुकूलता (सबैभन्दा महत्त्वपूर्ण)"),
    NADI_DOSHA_PRESENT("NADI DOSHA PRESENT: Same Nadi ({nadi}) without cancellation. Serious concern affecting health and progeny.", "नाडी दोष उपस्थित: रद्द बिना उही नाडी ({nadi})। स्वास्थ्य र सन्तानलाई असर गर्ने गम्भीर चिन्ता।"),
    NADI_DOSHA_CANCELLED("Same Nadi but CANCELLED:", "उही नाडी तर रद्द:"),
    NADI_DIFFERENT("Different Nadis ({nadi1} & {nadi2}) - excellent health and progeny compatibility.", "फरक नाडी ({nadi1} र {nadi2}) - उत्कृष्ट स्वास्थ्य र सन्तान अनुकूलता।"),
    NADI_CANCEL_SAME_NAK_DIFF_RASHI("Same Nakshatra ({nakshatra}) but different Rashis - Full Cancellation", "उही नक्षत्र ({nakshatra}) तर फरक राशि - पूर्ण रद्द"),
    NADI_CANCEL_SAME_RASHI_DIFF_NAK("Same Rashi ({rashi}) but different Nakshatras - Full Cancellation", "उही राशि ({rashi}) तर फरक नक्षत्र - पूर्ण रद्द"),
    NADI_CANCEL_DIFF_PADA("Same Nakshatra and Rashi but different Padas ({pada1} vs {pada2}) - Partial Cancellation", "उही नक्षत्र र राशि तर फरक पाद ({pada1} बनाम {pada2}) - आंशिक रद्द"),
    NADI_CANCEL_SPECIAL_PAIR("Special Nakshatra pair ({nak1}-{nak2}) cancels Nadi dosha per classical texts", "विशेष नक्षत्र जोडी ({nak1}-{nak2}) ले शास्त्रीय ग्रन्थ अनुसार नाडी दोष रद्द गर्छ"),
    NADI_CANCEL_LORDS_FRIENDS("Moon sign lords ({lord1} & {lord2}) are mutual friends - Partial Cancellation", "चन्द्र राशि स्वामी ({lord1} र {lord2}) पारस्परिक मित्र हुन् - आंशिक रद्द"),
    NADI_CANCEL_SAME_NAK_LORD("Both Nakshatras ruled by {lord} - Partial Cancellation", "दुवै नक्षत्र {lord} द्वारा शासित - आंशिक रद्द"),

    // ============================================
    // DASHA CALCULATOR - LEVELS
    // ============================================
    DASHA_LEVEL_MAHADASHA("Mahadasha", "महादशा"),
    DASHA_LEVEL_ANTARDASHA("Antardasha", "अन्तर्दशा"),
    DASHA_LEVEL_PRATYANTARDASHA("Pratyantardasha", "प्रत्यन्तर्दशा"),
    DASHA_LEVEL_SOOKSHMADASHA("Sookshmadasha", "सूक्ष्मदशा"),
    DASHA_LEVEL_PRANADASHA("Pranadasha", "प्राणदशा"),
    DASHA_LEVEL_DEHADASHA("Dehadasha", "देहदशा"),

    // Dasha Tab Content Strings
    DASHA_CURRENT_DASHA_PERIOD("Current Dasha Period", "हालको दशा अवधि"),
    DASHA_BIRTH_NAKSHATRA("Birth Nakshatra", "जन्म नक्षत्र"),
    DASHA_LORD("Lord", "स्वामी"),
    DASHA_PADA("Pada", "पाद"),
    DASHA_PERIOD_INSIGHTS("Period Insights", "अवधि अन्तर्दृष्टि"),
    DASHA_SANDHI_ALERTS("Dasha Sandhi Alerts", "दशा सन्धि अलर्टहरू"),
    DASHA_UPCOMING_TRANSITIONS("%1\$d upcoming transition(s) within %2\$d days", "%2\$d दिनभित्र %1\$d आगामी सन्क्रमण(हरू)"),
    DASHA_TRANSITION("transition", "सन्क्रमण"),
    DASHA_SANDHI_EXPLANATION("Sandhi periods mark transitions between planetary periods. These are sensitive times requiring careful attention as the energy shifts from one planet to another.", "सन्धि अवधिहरूले ग्रह अवधिहरूबीचको सन्क्रमण चिन्ह लगाउँछन्। यी संवेदनशील समयहरू हुन् जहाँ एउटा ग्रहबाट अर्को ग्रहमा ऊर्जा परिवर्तन हुन्छ।"),
    DASHA_ACTIVE_NOW("Active Now", "अहिले सक्रिय"),
    DASHA_TODAY("Today", "आज"),
    DASHA_TOMORROW("Tomorrow", "भोलि"),
    DASHA_IN_DAYS("In %d days", "%d दिनमा"),
    DASHA_TIMELINE("Dasha Timeline", "दशा समयरेखा"),
    DASHA_COMPLETE_CYCLE("Complete 120-year Vimshottari cycle", "पूर्ण १२०-वर्षे विम्शोत्तरी चक्र"),
    DASHA_SUB_PERIODS("%d sub-periods", "%d उप-अवधिहरू"),
    DASHA_ANTARDASHAS("Antardashas", "अन्तर्दशाहरू"),
    DASHA_UNABLE_CALCULATE("Unable to calculate current dasha period", "हालको दशा अवधि गणना गर्न असक्षम"),
    DASHA_ABOUT_VIMSHOTTARI("About Vimshottari Dasha", "विम्शोत्तरी दशाको बारेमा"),
    DASHA_VIMSHOTTARI_DESC("The Vimshottari Dasha is the most widely used planetary period system in Vedic astrology (Jyotish). Derived from the Moon's nakshatra (lunar mansion) at birth, it divides the 120-year human lifespan into six levels of planetary periods. Starting from Mahadashas (major periods spanning years), it subdivides into Antardasha (months), Pratyantardasha (weeks), Sookshmadasha (days), Pranadasha (hours), and finally Dehadasha (minutes) — each governed by one of the nine Grahas.", "विम्शोत्तरी दशा वैदिक ज्योतिष (ज्योतिष) मा सबैभन्दा व्यापक रूपमा प्रयोग हुने ग्रह अवधि प्रणाली हो। जन्मको समयमा चन्द्रमाको नक्षत्रबाट व्युत्पन्न, यसले १२०-वर्षे मानव आयुलाई छ तहको ग्रह अवधिहरूमा विभाजन गर्छ।"),
    DASHA_PERIODS_SEQUENCE("Dasha Periods (Vimshottari Sequence)", "दशा अवधिहरू (विम्शोत्तरी क्रम)"),
    DASHA_TOTAL_CYCLE("Total Cycle: 120 Years", "कुल चक्र: १२० वर्ष"),
    DASHA_HIERARCHY("Dasha Hierarchy", "दशा पदानुक्रम"),
    DASHA_MAJOR_PERIOD_YEARS("Major period (years)", "मुख्य अवधि (वर्ष)"),
    DASHA_SUB_PERIOD_MONTHS("Sub-period (months)", "उप-अवधि (महिना)"),
    DASHA_SUB_SUB_PERIOD_WEEKS("Sub-sub-period (weeks)", "उप-उप-अवधि (हप्ता)"),
    DASHA_SUBTLE_PERIOD_DAYS("Subtle period (days)", "सूक्ष्म अवधि (दिन)"),
    DASHA_BREATH_PERIOD_HOURS("Breath period (hours)", "श्वास अवधि (घण्टा)"),
    DASHA_BODY_PERIOD_MINUTES("Body period (minutes)", "शरीर अवधि (मिनेट)"),
    DASHA_SANDHI_NOTE("Dasha Sandhi (junction periods) occur when transitioning between planetary periods and are considered sensitive times requiring careful attention.", "दशा सन्धि (जोड अवधिहरू) ग्रह अवधिहरूबीच सन्क्रमण हुँदा हुन्छ र यी संवेदनशील समयहरू मानिन्छन्।"),
    DASHA_PERCENT_COMPLETE("%s%% complete", "%s%% पूरा"),
    DASHA_YEARS_ABBR("yrs", "वर्ष"),
    DASHA_COLLAPSE("Collapse", "संकुचन गर्नुहोस्"),
    DASHA_EXPAND("Expand", "विस्तार गर्नुहोस्"),

    // ============================================
    // ASHTAKAVARGA CALCULATOR - STRENGTH LEVELS
    // ============================================
    STRENGTH_STRONG("Strong", "बलियो"),
    STRENGTH_GOOD("Good", "राम्रो"),
    STRENGTH_AVERAGE("Average", "औसत"),
    STRENGTH_WEAK("Weak", "कमजोर"),
    STRENGTH_EXCELLENT("Excellent", "उत्कृष्ट"),
    STRENGTH_CHALLENGING("Challenging", "चुनौतीपूर्ण"),
    STRENGTH_DIFFICULT("Difficult", "कठिन"),
    STRENGTH_ERROR("Error", "त्रुटि"),
    STRENGTH_ASCENDANT("Ascendant", "लग्न"),

    // ============================================
    // ASPECT CALCULATOR - TYPES
    // ============================================
    ASPECT_CONJUNCTION("Conjunction", "युति"),
    ASPECT_NATURE_HARMONIOUS("Harmonious", "सामञ्जस्यपूर्ण"),
    ASPECT_NATURE_CHALLENGING("Challenging", "चुनौतीपूर्ण"),
    ASPECT_NATURE_VARIABLE("Variable", "परिवर्तनशील"),
    ASPECT_NATURE_SIGNIFICANT("Significant", "महत्त्वपूर्ण"),

    // ============================================
    // COLORS (FOR HOROSCOPE)
    // ============================================
    COLOR_RED("Red", "रातो"),
    COLOR_ORANGE("Orange", "सुन्तला"),
    COLOR_GOLD("Gold", "सुनौलो"),
    COLOR_GREEN("Green", "हरियो"),
    COLOR_BROWN("Brown", "खैरो"),
    COLOR_WHITE("White", "सेतो"),
    COLOR_BLUE("Blue", "नीलो"),
    COLOR_LIGHT_BLUE("Light Blue", "हल्का नीलो"),
    COLOR_SILVER("Silver", "चाँदी"),
    COLOR_SEA_GREEN("Sea Green", "समुद्री हरियो"),
    COLOR_YELLOW("Yellow", "पहेँलो"),
    COLOR_PINK("Pink", "गुलाबी"),
    COLOR_PURPLE("Purple", "बैजनी"),
    COLOR_BLACK("Black", "कालो"),
    COLOR_GREY("Grey", "खरानी"),
    COLOR_CREAM("Cream", "क्रीम"),
    COLOR_MAROON("Maroon", "मरुन"),
    COLOR_INDIGO("Indigo", "इन्डिगो"),

    // ============================================
    // DIRECTIONS
    // ============================================
    DIR_EAST("East", "पूर्व"),
    DIR_WEST("West", "पश्चिम"),
    DIR_NORTH("North", "उत्तर"),
    DIR_SOUTH("South", "दक्षिण"),
    DIR_NORTH_EAST("North-East", "उत्तर-पूर्व"),
    DIR_NORTH_WEST("North-West", "उत्तर-पश्चिम"),
    DIR_SOUTH_EAST("South-East", "दक्षिण-पूर्व"),
    DIR_SOUTH_WEST("South-West", "दक्षिण-पश्चिम"),

    // ============================================
    // ELEMENTS
    // ============================================
    ELEMENT_FIRE("Fire", "अग्नि"),
    ELEMENT_EARTH("Earth", "पृथ्वी"),
    ELEMENT_AIR("Air", "वायु"),
    ELEMENT_WATER("Water", "जल"),

    // ============================================
    // PLANETARY RELATIONSHIPS
    // ============================================
    RELATION_FRIEND("Friend", "मित्र"),
    RELATION_ENEMY("Enemy", "शत्रु"),
    RELATION_NEUTRAL("Neutral", "तटस्थ"),
    RELATION_MUTUAL_FRIENDS("Mutual Friends", "पारस्परिक मित्र"),
    RELATION_MUTUAL_ENEMIES("Mutual Enemies", "पारस्परिक शत्रु"),
    RELATION_ONE_FRIENDLY("One Friendly", "एक मैत्रीपूर्ण"),
    RELATION_ONE_INIMICAL("One Inimical", "एक शत्रुतापूर्ण"),

    // ============================================
    // MUHURTA - EVENT TYPES
    // ============================================
    MUHURTA_EVENT_MARRIAGE("Marriage", "विवाह"),
    MUHURTA_EVENT_ENGAGEMENT("Engagement", "सगाई"),
    MUHURTA_EVENT_GRIHA_PRAVESH("Griha Pravesh", "गृह प्रवेश"),
    MUHURTA_EVENT_BUSINESS_START("Business Start", "व्यापार शुरुआत"),
    MUHURTA_EVENT_TRAVEL("Travel", "यात्रा"),
    MUHURTA_EVENT_VEHICLE_PURCHASE("Vehicle Purchase", "सवारी खरिद"),
    MUHURTA_EVENT_EDUCATION_START("Education Start", "शिक्षा शुरुआत"),
    MUHURTA_EVENT_MEDICAL_TREATMENT("Medical Treatment", "चिकित्सा उपचार"),
    MUHURTA_EVENT_CONSTRUCTION("Construction", "निर्माण"),
    MUHURTA_EVENT_INVESTMENT("Investment", "लगानी"),
    MUHURTA_EVENT_NAME_CEREMONY("Name Ceremony", "नामकरण"),
    MUHURTA_EVENT_MUNDAN("Mundan", "चौलकर्म"),
    MUHURTA_EVENT_UPANAYANA("Upanayana", "ब्रतबन्ध"),

    // ============================================
    // VARSHAPHALA - SAHAMS
    // ============================================
    SAHAM_KARYASIDDHI("Karyasiddhi Saham", "कार्यसिद्धि सहम"),
    SAHAM_SUCCESS("Success", "सफलता"),
    SAHAM_FORTUNE("Fortune Saham", "भाग्य सहम"),
    SAHAM_WEALTH("Wealth Saham", "धन सहम"),
    SAHAM_MARRIAGE("Marriage Saham", "विवाह सहम"),
    SAHAM_CHILDREN("Children Saham", "सन्तान सहम"),
    SAHAM_HEALTH("Health Saham", "स्वास्थ्य सहम"),
    SAHAM_EDUCATION("Education Saham", "शिक्षा सहम"),
    SAHAM_TRAVEL("Travel Saham", "यात्रा सहम"),
    SAHAM_PROFESSION("Profession Saham", "पेशा सहम"),

    // ============================================
    // HOUSE SIGNIFICATIONS
    // ============================================
    HOUSE_DHARMA("Dharma (1, 5, 9)", "धर्म (१, ५, ९)"),
    HOUSE_ARTHA("Artha (2, 6, 10)", "अर्थ (२, ६, १०)"),
    HOUSE_KAMA("Kama (3, 7, 11)", "काम (३, ७, ११)"),
    HOUSE_MOKSHA("Moksha (4, 8, 12)", "मोक्ष (४, ८, १२)"),

    // ============================================
    // PRASHNA CALCULATOR
    // ============================================
    PRASHNA_CAT_GENERAL("General", "सामान्य"),
    PRASHNA_CAT_CAREER("Career & Profession", "क्यारियर र पेशा"),
    PRASHNA_CAT_RELATIONSHIP("Relationships", "सम्बन्धहरू"),
    PRASHNA_CAT_HEALTH("Health", "स्वास्थ्य"),
    PRASHNA_CAT_FINANCE("Finance", "वित्त"),
    PRASHNA_CAT_TRAVEL("Travel", "यात्रा"),
    PRASHNA_CAT_EDUCATION("Education", "शिक्षा"),
    PRASHNA_CAT_LEGAL("Legal Matters", "कानुनी मामिलाहरू"),
    PRASHNA_CAT_PROPERTY("Property", "सम्पत्ति"),
    PRASHNA_CAT_LOST_ITEM("Lost Items", "हराएका वस्तुहरू"),

    // ============================================
    // RAJJU ARUDHA
    // ============================================
    RAJJU_ASCENDING("Aarohana (Ascending)", "आरोहण"),
    RAJJU_DESCENDING("Avarohana (Descending)", "अवरोहण"),

    // ============================================
    // MATCHMAKING - ADDITIONAL FACTORS
    // ============================================
    MAHENDRA_FAVORABLE("Mahendra favorable at position {count} - promotes longevity and progeny", "महेन्द्र स्थिति {count} मा अनुकूल - दीर्घायु र सन्तान प्रवर्धन गर्छ"),
    MAHENDRA_NOT_APPLICABLE("Mahendra position not in favorable sequence", "महेन्द्र स्थिति अनुकूल क्रममा छैन"),
    VEDHA_PRESENT("Vedha (obstruction) present between {nak1} and {nak2} - may cause obstacles", "वेध (बाधा) {nak1} र {nak2} बीच उपस्थित - अवरोधहरू हुन सक्छ"),
    VEDHA_NOT_PRESENT("No Vedha between the nakshatras - favorable", "नक्षत्रहरू बीच वेध छैन - अनुकूल"),
    RAJJU_COMPATIBLE("Different Rajju types - compatible, no concerns related to body part compatibility", "फरक रज्जु प्रकार - अनुकूल, शरीर भाग अनुकूलतासम्बन्धी कुनै चिन्ता छैन"),
    RAJJU_SAME_DIFF_ARUDHA("Same {rajju} but different Arudha ({arudha1} vs {arudha2}) - partially compatible, reduced concern", "उही {rajju} तर फरक अरुढा ({arudha1} बनाम {arudha2}) - आंशिक अनुकूल, कम चिन्ता"),
    RAJJU_SAME_SAME_ARUDHA("Same {rajju} ({body}) and same {arudha} - Rajju Dosha present. {warning}", "उही {rajju} ({body}) र उही {arudha} - रज्जु दोष उपस्थित। {warning}"),

    // ============================================
    // MATCHMAKING - SPECIAL CONSIDERATIONS
    // ============================================
    SPECIAL_NADI_DOSHA("NADI DOSHA: Same Nadi can affect health and progeny. Consider remedies before proceeding.", "नाडी दोष: उही नाडीले स्वास्थ्य र सन्तानमा असर पार्न सक्छ। अगाडि बढ्नु अघि उपायहरू विचार गर्नुहोस्।"),
    SPECIAL_BHAKOOT_DOSHA("BHAKOOT DOSHA present: {analysis}. May affect love, finances, or health.", "भकुट दोष उपस्थित: {analysis}। प्रेम, वित्त, वा स्वास्थ्यमा असर पर्न सक्छ।"),
    SPECIAL_BRIDE_MANGLIK("MANGLIK IMBALANCE: Bride has {dosha} while Groom is non-Manglik. Kumbh Vivah or remedies strongly advised.", "मांगलिक असन्तुलन: दुलहीमा {dosha} छ जबकि दुलाहा गैर-मांगलिक छन्। कुम्भ विवाह वा उपायहरू दृढतापूर्वक सल्लाह दिइएको।"),
    SPECIAL_GROOM_MANGLIK("MANGLIK IMBALANCE: Groom has {dosha} while Bride is non-Manglik. Remedies strongly recommended.", "मांगलिक असन्तुलन: दुलाहामा {dosha} छ जबकि दुलही गैर-मांगलिक छिन्। उपायहरू दृढतापूर्वक सिफारिस गरिएको।"),
    SPECIAL_GANA_INCOMPAT("GANA INCOMPATIBILITY: Opposite temperaments (Deva-Rakshasa). May cause frequent conflicts without conscious effort.", "गण असंगतता: विपरीत स्वभाव (देव-राक्षस)। सचेत प्रयास बिना बारम्बार द्वन्द्व हुन सक्छ।"),
    SPECIAL_YONI_INCOMPAT("YONI INCOMPATIBILITY: Enemy Yonis present. Physical and instinctual harmony may require extra effort.", "योनि असंगतता: शत्रु योनि उपस्थित। शारीरिक र सहज सामञ्जस्यको लागि अतिरिक्त प्रयास आवश्यक पर्न सक्छ।"),
    SPECIAL_VEDHA("VEDHA PRESENT: {details}. Nakshatra obstruction may cause challenges in specific life areas.", "वेध उपस्थित: {details}। नक्षत्र बाधाले जीवनका विशेष क्षेत्रहरूमा चुनौतीहरू ल्याउन सक्छ।"),
    SPECIAL_RAJJU("RAJJU DOSHA: {details}. Related body part may face health concerns in marriage.", "रज्जु दोष: {details}। सम्बन्धित शरीर भागमा विवाहमा स्वास्थ्य चिन्ता हुन सक्छ।"),
    SPECIAL_STREE_DEERGHA("STREE DEERGHA not satisfied: Nakshatra difference is {diff} (requires 13+). Bride's prosperity may need attention.", "स्त्री दीर्घ सन्तुष्ट छैन: नक्षत्र भिन्नता {diff} छ (१३+ आवश्यक)। दुलहीको समृद्धिमा ध्यान दिनुपर्ने हुन सक्छ।"),
    SPECIAL_MULTIPLE_LOW("MULTIPLE CONCERNS: {count} Gunas scored below threshold. Overall compatibility requires attention.", "बहु चिन्ताहरू: {count} गुणले न्यूनतम भन्दा कम अंक पाए। समग्र अनुकूलतामा ध्यान आवश्यक।"),
    SPECIAL_7TH_LORDS_ENEMY("7TH HOUSE LORDS INCOMPATIBLE: {lord1} and {lord2} are mutual enemies. Marriage house lords in conflict.", "७औं भाव स्वामी असंगत: {lord1} र {lord2} पारस्परिक शत्रु हुन्। विवाह भाव स्वामीहरू द्वन्द्वमा।"),
    SPECIAL_NO_ISSUES("No significant special concerns noted. The match appears harmonious across additional factors.", "कुनै महत्त्वपूर्ण विशेष चिन्ता छैन। मिलान अतिरिक्त कारकहरूमा सामञ्जस्यपूर्ण देखिन्छ।"),

    // ============================================
    // MATCHMAKING - REMEDIES
    // ============================================
    REMEDY_NADI_1("Nadi Dosha: Donate grains, gold, or cow on an auspicious day after consulting a priest", "नाडी दोष: पुजारीसँग परामर्श पछि शुभ दिनमा अन्न, सुन, वा गाई दान गर्नुहोस्"),
    REMEDY_NADI_2("Nadi Dosha: Perform Maha Mrityunjaya Jaap (108 times daily for 40 days)", "नाडी दोष: महा मृत्युञ्जय जाप गर्नुहोस् (४० दिन दैनिक १०८ पटक)"),
    REMEDY_NADI_3("Nadi Dosha: Worship Lord Shiva and Goddess Parvati together on Mondays", "नाडी दोष: सोमबारमा भगवान शिव र देवी पार्वतीको सँगै पूजा गर्नुहोस्"),
    REMEDY_BHAKOOT_1("Bhakoot Dosha: Perform Graha Shanti puja for Moon sign lords of both partners", "भकुट दोष: दुवै साझेदारको चन्द्र राशि स्वामीको ग्रह शान्ति पूजा गर्नुहोस्"),
    REMEDY_BHAKOOT_2("Bhakoot Dosha: Chant Vishnu Sahasranama on Thursdays for 21 weeks", "भकुट दोष: २१ हप्ता बिहीबारमा विष्णु सहस्रनाम जाप गर्नुहोस्"),
    REMEDY_SHADASHTAK("Shadashtak (6-8) Dosha: Perform Rudrabhishek and donate medicines to the needy", "षडाष्टक (६-८) दोष: रुद्राभिषेक गर्नुहोस् र जरुरतमन्दलाई औषधि दान गर्नुहोस्"),
    REMEDY_MANGLIK_1("Manglik Dosha: Perform Kumbh Vivah (symbolic marriage to a pot or banana tree) before actual marriage", "मांगलिक दोष: वास्तविक विवाह अघि कुम्भ विवाह (घैंटो वा केराको बोटसँग प्रतीकात्मक विवाह) गर्नुहोस्"),
    REMEDY_MANGLIK_2("Manglik Dosha: Chant Mangal Mantra 'Om Kraam Kreem Kraum Sah Bhaumaya Namah' 108 times on Tuesdays", "मांगलिक दोष: मंगलबारमा मंगल मन्त्र 'ॐ क्रां क्रीं क्रौं सः भौमाय नमः' १०८ पटक जाप गर्नुहोस्"),
    REMEDY_MANGLIK_3("Manglik Dosha: Wear a Red Coral (Moonga) after proper energization and astrological consultation", "मांगलिक दोष: उचित ऊर्जावान र ज्योतिषीय परामर्श पछि मुंगा (प्रवाल) धारण गर्नुहोस्"),
    REMEDY_MANGLIK_BRIDE("For Bride's Manglik: Visit Hanuman temple on Tuesdays and offer vermilion and jasmine oil", "दुलहीको मांगलिकको लागि: मंगलबारमा हनुमान मन्दिरमा जानुहोस् र सिन्दूर र चमेलीको तेल चढाउनुहोस्"),
    REMEDY_MANGLIK_GROOM("For Groom's Manglik: Perform Mars-related charity on Tuesdays (donate red lentils, jaggery, copper)", "दुलाहाको मांगलिकको लागि: मंगलबारमा मंगल-सम्बन्धित दान गर्नुहोस् (रातो दाल, गुड, तामा दान गर्नुहोस्)"),
    REMEDY_DOUBLE_MANGLIK("Double Manglik: Requires extended Kumbh Vivah ritual and 11 Rudrabhisheks over 11 consecutive Mondays", "दोहोरो मांगलिक: विस्तारित कुम्भ विवाह विधि र लगातार ११ सोमबारमा ११ रुद्राभिषेक आवश्यक"),
    REMEDY_GANA_1("Gana Incompatibility: Chant Ganapati Atharvasheersham daily for 41 days", "गण असंगतता: ४१ दिन दैनिक गणपति अथर्वशीर्षम् जाप गर्नुहोस्"),
    REMEDY_GANA_2("Gana Incompatibility: Perform Navgraha Shanti puja together before marriage", "गण असंगतता: विवाह अघि सँगै नवग्रह शान्ति पूजा गर्नुहोस्"),
    REMEDY_GANA_3("Gana Incompatibility: Practice mutual respect and conscious communication daily", "गण असंगतता: दैनिक पारस्परिक सम्मान र सचेत सञ्चार अभ्यास गर्नुहोस्"),
    REMEDY_GRAHA_MAITRI_1("Graha Maitri: Strengthen Mercury through green charity and Budh mantra chanting on Wednesdays", "ग्रह मैत्री: बुधबारमा हरियो दान र बुध मन्त्र जापद्वारा बुध बलियो बनाउनुहोस्"),
    REMEDY_GRAHA_MAITRI_2("Graha Maitri: Both partners should meditate together daily to improve mental harmony", "ग्रह मैत्री: मानसिक सामञ्जस्य सुधार गर्न दुवै साझेदारले दैनिक सँगै ध्यान गर्नुपर्छ"),
    REMEDY_YONI_1("Yoni Incompatibility: Worship Kamadeva (God of Love) and offer flowers on Fridays", "योनि असंगतता: शुक्रबारमा कामदेव (प्रेमको देवता) पूजा गर्नुहोस् र फूल चढाउनुहोस्"),
    REMEDY_YONI_2("Yoni Incompatibility: Perform Ashwamedha or Gajamedha symbolic puja to neutralize animal enmity", "योनि असंगतता: पशु वैमनस्य निष्क्रिय गर्न अश्वमेध वा गजमेध प्रतीकात्मक पूजा गर्नुहोस्"),
    REMEDY_VEDHA_1("Vedha Dosha: Perform Nakshatra Shanti puja for both birth stars", "वेध दोष: दुवै जन्म नक्षत्रको लागि नक्षत्र शान्ति पूजा गर्नुहोस्"),
    REMEDY_VEDHA_2("Vedha Dosha: Donate black sesame and urad dal on Saturdays", "वेध दोष: शनिबारमा कालो तिल र उडदको दाल दान गर्नुहोस्"),
    REMEDY_RAJJU_SIRO("Siro Rajju Dosha (Head): Perform Ayushya Homa and worship Lord Mrityunjaya Shiva", "शिरो रज्जु दोष (शिर): आयुष्य होम गर्नुहोस् र भगवान मृत्युञ्जय शिवको पूजा गर्नुहोस्"),
    REMEDY_RAJJU_KANTHA("Kantha Rajju Dosha (Neck): Wear Rudraksha mala and chant Vishnu mantras", "कण्ठ रज्जु दोष (घाँटी): रुद्राक्ष माला लगाउनुहोस् र विष्णु मन्त्र जाप गर्नुहोस्"),
    REMEDY_RAJJU_NABHI("Nabhi Rajju Dosha (Navel): Perform Santan Gopal puja and donate to orphanages", "नाभि रज्जु दोष (नाभि): सन्तान गोपाल पूजा गर्नुहोस् र अनाथाश्रममा दान गर्नुहोस्"),
    REMEDY_RAJJU_KATI("Kati Rajju Dosha (Waist): Perform Lakshmi puja and donate to poverty relief", "कटि रज्जु दोष (कम्मर): लक्ष्मी पूजा गर्नुहोस् र गरिबी उन्मूलनमा दान गर्नुहोस्"),
    REMEDY_RAJJU_PADA("Pada Rajju Dosha (Feet): Worship at a pilgrimage site together before marriage", "पाद रज्जु दोष (पाउ): विवाह अघि सँगै तीर्थस्थलमा पूजा गर्नुहोस्"),
    REMEDY_GENERAL_1("General: Perform Satyanarayan Puja together on Purnima (full moon)", "सामान्य: पूर्णिमामा सँगै सत्यनारायण पूजा गर्नुहोस्"),
    REMEDY_GENERAL_2("General: Chant Swayamvara Parvati Mantra for marital harmony", "सामान्य: वैवाहिक सामञ्जस्यको लागि स्वयंवर पार्वती मन्त्र जाप गर्नुहोस्"),
    REMEDY_GENERAL_3("General: Donate to couples in need or contribute to marriage funds for the poor", "सामान्य: जरुरतमन्द जोडीहरूलाई दान गर्नुहोस् वा गरिबको विवाह कोषमा योगदान गर्नुहोस्"),
    REMEDY_GENERAL_4("General: Both partners should observe Monday fasts for Lord Shiva", "सामान्य: दुवै साझेदारले भगवान शिवको लागि सोमबार व्रत गर्नुपर्छ"),
    REMEDY_SERIOUS_1("Serious Concerns: Consult a qualified Vedic astrologer for personalized guidance", "गम्भीर चिन्ताहरू: व्यक्तिगत मार्गदर्शनको लागि योग्य वैदिक ज्योतिषीसँग परामर्श गर्नुहोस्"),
    REMEDY_SERIOUS_2("Serious Concerns: Consider Maha Mrityunjaya Homa for overall protection", "गम्भीर चिन्ताहरू: समग्र सुरक्षाको लागि महा मृत्युञ्जय होम विचार गर्नुहोस्"),
    REMEDY_NONE_NEEDED("Excellent compatibility - no specific remedies required.", "उत्कृष्ट अनुकूलता - कुनै विशेष उपाय आवश्यक छैन।"),
    REMEDY_SATYANARAYAN("For auspiciousness: Perform Satyanarayan Katha together on the first Purnima after marriage", "शुभताको लागि: विवाह पछिको पहिलो पूर्णिमामा सँगै सत्यनारायण कथा गर्नुहोस्"),

    // ============================================
    // MATCHMAKING - SUMMARY & ANALYSIS STRINGS
    // ============================================
    SUMMARY_TITLE("KUNDLI MILAN SUMMARY", "कुण्डली मिलान सारांश"),
    SUMMARY_OVERALL_SCORE("Overall Score", "समग्र अंक"),
    SUMMARY_RATING("Rating", "मूल्यांकन"),
    SUMMARY_STRENGTHS("Strengths", "शक्तिहरू"),
    SUMMARY_CONCERNS("Areas of Concern", "चिन्ताका क्षेत्रहरू"),
    SUMMARY_MANGLIK("Manglik Status", "मांगलिक स्थिति"),
    SUMMARY_ADDITIONAL("Additional Factors", "अतिरिक्त कारकहरू"),
    SUMMARY_RECOMMENDATION("Recommendation", "सिफारिस"),
    DETAILED_TITLE("DETAILED MATCHMAKING ANALYSIS", "विस्तृत मिलान विश्लेषण"),
    BIRTH_DATA_SUMMARY("Birth Data Summary", "जन्म डाटा सारांश"),
    MOON_SIGN("Moon Sign", "चन्द्र राशि"),
    NAKSHATRA_LABEL("Nakshatra", "नक्षत्र"),
    PADA_LABEL("Pada", "पाद"),
    MOON_LONGITUDE("Moon Longitude", "चन्द्र देशान्तर"),
    PURPOSE("Purpose", "उद्देश्य"),
    ANALYSIS_LABEL("Analysis", "विश्लेषण"),
    ADDITIONAL_FACTORS_TITLE("Additional Compatibility Factors", "अतिरिक्त अनुकूलता कारकहरू"),
    OBSTRUCTION("Obstruction", "बाधा"),
    COSMIC_BOND("Cosmic Bond", "ब्रह्माण्डीय बन्धन"),
    WARNING_LABEL("Warning", "चेतावनी"),
    WIFE_PROSPERITY("Wife's Prosperity", "पत्नीको समृद्धि"),
    NAKSHATRA_DIFF("Nakshatra Difference", "नक्षत्र भिन्नता"),
    SATISFIED("Satisfied", "सन्तुष्ट"),
    NOT_SATISFIED("Not Satisfied", "सन्तुष्ट छैन"),
    LONGEVITY_PROSPERITY("Longevity & Prosperity", "दीर्घायु र समृद्धि"),
    NOT_APPLICABLE("Not Applicable", "लागू हुँदैन"),

    // ============================================
    // MATCHMAKING - COMMON STATUS STRINGS
    // ============================================
    BRIDE("Bride", "दुलही"),
    GROOM("Groom", "दुलाहा"),
    STATUS("Status", "स्थिति"),
    COMPATIBLE("Compatible", "अनुकूल"),
    NEEDS_ATTENTION("Needs Attention", "ध्यान आवश्यक"),
    PRESENT("Present", "उपस्थित"),
    NOT_PRESENT("Not Present", "उपस्थित छैन"),
    FAVORABLE("Favorable", "अनुकूल"),
    SAME_RAJJU("Same Rajju", "उही रज्जु"),
    DETAILS("Details", "विवरण"),

    // ============================================
    // MATCHMAKING - GUNA DESCRIPTIONS
    // ============================================
    GUNA_DESC_VARNA("Varna indicates spiritual compatibility based on the Moon sign's element. It measures ego harmony and how partners relate on a spiritual level. Higher compatibility suggests natural understanding of values.", "वर्णले चन्द्र राशिको तत्वमा आधारित आध्यात्मिक अनुकूलता संकेत गर्दछ। यसले अहंकार सामञ्जस्य र साझेदारहरू आध्यात्मिक स्तरमा कसरी सम्बन्धित छन् मापन गर्दछ।"),
    GUNA_DESC_VASHYA("Vashya measures the mutual attraction and influence between partners. It indicates who can influence whom and the power dynamics in the relationship.", "वश्यले साझेदारहरू बीचको पारस्परिक आकर्षण र प्रभाव मापन गर्दछ। यसले कसले कसलाई प्रभाव पार्न सक्छ र सम्बन्धमा शक्ति गतिशीलता संकेत गर्दछ।"),
    GUNA_DESC_TARA("Tara analyzes destiny compatibility through the birth stars (Nakshatras). It determines the auspiciousness of the couple's combined destiny path.", "तारा जन्म तारा (नक्षत्र) मार्फत भाग्य अनुकूलता विश्लेषण गर्दछ। यसले जोडीको संयुक्त भाग्य मार्गको शुभता निर्धारण गर्दछ।"),
    GUNA_DESC_YONI("Yoni represents physical and sexual compatibility based on the animal nature assigned to each Nakshatra. Same or friendly animals indicate better physical harmony.", "योनीले प्रत्येक नक्षत्रलाई तोकिएको पशु प्रकृतिमा आधारित शारीरिक र यौन अनुकूलता प्रतिनिधित्व गर्दछ। उही वा मित्र पशुहरूले राम्रो शारीरिक सामञ्जस्य संकेत गर्दछ।"),
    GUNA_DESC_GRAHA_MAITRI("Graha Maitri analyzes mental compatibility through the friendship of Moon sign lords. It indicates how well the couple can understand each other intellectually.", "ग्रह मैत्रीले चन्द्र राशि स्वामीहरूको मित्रता मार्फत मानसिक अनुकूलता विश्लेषण गर्दछ। यसले जोडीले बौद्धिक रूपमा एकअर्कालाई कत्तिको राम्रोसँग बुझ्न सक्छ संकेत गर्दछ।"),
    GUNA_DESC_GANA("Gana measures temperament compatibility through Deva (divine), Manushya (human), or Rakshasa (demon) classification based on Nakshatra.", "गणले नक्षत्रमा आधारित देव, मनुष्य, वा राक्षस वर्गीकरण मार्फत स्वभाव अनुकूलता मापन गर्दछ।"),
    GUNA_DESC_BHAKOOT("Bhakoot indicates love, health, and financial compatibility based on the Moon sign positions. It's crucial for long-term marital harmony.", "भकुटले चन्द्र राशि स्थितिहरूमा आधारित प्रेम, स्वास्थ्य र आर्थिक अनुकूलता संकेत गर्दछ। यो दीर्घकालीन वैवाहिक सामञ्जस्यको लागि महत्त्वपूर्ण छ।"),
    GUNA_DESC_NADI("Nadi is the most important factor (8 points), indicating health and progeny compatibility. Same Nadi can cause health issues and affect children.", "नाडी सबैभन्दा महत्त्वपूर्ण कारक (८ अंक) हो, स्वास्थ्य र सन्तान अनुकूलता संकेत गर्दछ। उही नाडीले स्वास्थ्य समस्या र सन्तानमा असर पार्न सक्छ।"),
    GUNA_DESC_NOT_AVAILABLE("Detailed description not available for this Guna.", "यस गुणको विस्तृत विवरण उपलब्ध छैन।"),

    // ============================================
    // MATCHMAKING - SCORE INTERPRETATIONS
    // ============================================
    SCORE_EXCELLENT("Excellent match! Score above 28 indicates highly favorable compatibility across all dimensions.", "उत्कृष्ट मिलान! २८ माथिको अंकले सबै आयामहरूमा अत्यन्त अनुकूल अनुकूलता संकेत गर्दछ।"),
    SCORE_GOOD("Good match. Score of 21-27 suggests strong compatibility with minor areas to work on.", "राम्रो मिलान। २१-२७ को अंकले काम गर्नुपर्ने सानातिना क्षेत्रहरूसहित बलियो अनुकूलता सुझाव दिन्छ।"),
    SCORE_AVERAGE("Average compatibility. Score of 18-20 requires attention to problem areas and remedies.", "औसत अनुकूलता। १८-२० को अंकले समस्या क्षेत्रहरू र उपायहरूमा ध्यान आवश्यक पर्छ।"),
    SCORE_BELOW_AVERAGE("Below average compatibility. Score of 14-17 indicates significant challenges requiring serious consideration.", "औसतमुनि अनुकूलता। १४-१७ को अंकले गम्भीर विचार आवश्यक पर्ने महत्त्वपूर्ण चुनौतीहरू संकेत गर्दछ।"),
    SCORE_POOR("Poor compatibility. Score below 14 suggests major challenges. Marriage not recommended without extensive remedies.", "कमजोर अनुकूलता। १४ मुनिको अंकले प्रमुख चुनौतीहरू सुझाव दिन्छ। व्यापक उपायहरू बिना विवाह सिफारिस गरिएको छैन।"),

    // ============================================
    // YOGA CALCULATOR - CATEGORIES & STRENGTH
    // ============================================
    YOGA_CAT_RAJA("Raja Yoga", "राज योग"),
    YOGA_CAT_RAJA_DESC("Power, authority, and leadership combinations", "शक्ति, अधिकार र नेतृत्व संयोजनहरू"),
    YOGA_CAT_DHANA("Dhana Yoga", "धन योग"),
    YOGA_CAT_DHANA_DESC("Wealth and prosperity combinations", "धन र समृद्धि संयोजनहरू"),
    YOGA_CAT_PANCHA_MAHAPURUSHA("Pancha Mahapurusha Yoga", "पञ्च महापुरुष योग"),
    YOGA_CAT_PANCHA_MAHAPURUSHA_DESC("Five great person combinations", "पाँच महान व्यक्ति संयोजनहरू"),
    YOGA_CAT_NABHASA("Nabhasa Yoga", "नाभस योग"),
    YOGA_CAT_NABHASA_DESC("Pattern-based planetary combinations", "ढाँचामा आधारित ग्रह संयोजनहरू"),
    YOGA_CAT_CHANDRA("Chandra Yoga", "चन्द्र योग"),
    YOGA_CAT_CHANDRA_DESC("Moon-based combinations", "चन्द्रमामा आधारित संयोजनहरू"),
    YOGA_CAT_SOLAR("Solar Yoga", "सूर्य योग"),
    YOGA_CAT_SOLAR_DESC("Sun-based combinations", "सूर्यमा आधारित संयोजनहरू"),
    YOGA_CAT_NEGATIVE("Negative Yoga", "नकारात्मक योग"),
    YOGA_CAT_NEGATIVE_DESC("Challenging combinations", "चुनौतीपूर्ण संयोजनहरू"),
    YOGA_CAT_SPECIAL("Special Yoga", "विशेष योग"),
    YOGA_CAT_SPECIAL_DESC("Other significant combinations", "अन्य महत्त्वपूर्ण संयोजनहरू"),

    YOGA_STRENGTH_EXTREMELY_STRONG("Extremely Strong", "अत्यन्त बलियो"),
    YOGA_STRENGTH_STRONG("Strong", "बलियो"),
    YOGA_STRENGTH_MODERATE("Moderate", "मध्यम"),
    YOGA_STRENGTH_WEAK("Weak", "कमजोर"),
    YOGA_STRENGTH_VERY_WEAK("Very Weak", "धेरै कमजोर"),

    // ============================================
    // REMEDIES CALCULATOR - CATEGORIES & STRENGTH
    // ============================================
    REMEDY_CAT_GEMSTONE("Gemstone", "रत्न"),
    REMEDY_CAT_MANTRA("Mantra", "मन्त्र"),
    REMEDY_CAT_YANTRA("Yantra", "यन्त्र"),
    REMEDY_CAT_CHARITY("Charity", "दान"),
    REMEDY_CAT_FASTING("Fasting", "उपवास"),
    REMEDY_CAT_COLOR("Color Therapy", "रंग चिकित्सा"),
    REMEDY_CAT_METAL("Metal", "धातु"),
    REMEDY_CAT_RUDRAKSHA("Rudraksha", "रुद्राक्ष"),
    REMEDY_CAT_DEITY("Deity Worship", "देवता पूजा"),
    REMEDY_CAT_LIFESTYLE("Lifestyle", "जीवनशैली"),

    REMEDY_PRIORITY_ESSENTIAL("Essential", "आवश्यक"),
    REMEDY_PRIORITY_HIGHLY_RECOMMENDED("Highly Recommended", "अत्यधिक सिफारिस"),
    REMEDY_PRIORITY_RECOMMENDED("Recommended", "सिफारिस गरिएको"),
    REMEDY_PRIORITY_OPTIONAL("Optional", "वैकल्पिक"),

    PLANETARY_STRENGTH_VERY_STRONG("Very Strong", "धेरै बलियो"),
    PLANETARY_STRENGTH_STRONG("Strong", "बलियो"),
    PLANETARY_STRENGTH_MODERATE("Moderate", "मध्यम"),
    PLANETARY_STRENGTH_WEAK("Weak", "कमजोर"),
    PLANETARY_STRENGTH_VERY_WEAK("Very Weak", "धेरै कमजोर"),
    PLANETARY_STRENGTH_AFFLICTED("Afflicted", "पीडित"),

    // Shadbala StrengthRating
    SHADBALA_EXTREMELY_WEAK("Extremely Weak", "अत्यन्त कमजोर"),
    SHADBALA_WEAK("Weak", "कमजोर"),
    SHADBALA_BELOW_AVERAGE("Below Average", "औसतभन्दा तल"),
    SHADBALA_AVERAGE("Average", "औसत"),
    SHADBALA_ABOVE_AVERAGE("Above Average", "औसतभन्दा माथि"),
    SHADBALA_STRONG("Strong", "बलियो"),
    SHADBALA_VERY_STRONG("Very Strong", "धेरै बलियो"),
    SHADBALA_EXTREMELY_STRONG("Extremely Strong", "अत्यन्त बलियो"),

    // ============================================
    // HOUSE SIGNIFICATIONS (Localized)
    // ============================================
    HOUSE_1_SIGNIFICATION("self-effort and personality", "आत्म-प्रयास र व्यक्तित्व"),
    HOUSE_2_SIGNIFICATION("family wealth and speech", "पारिवारिक धन र वाणी"),
    HOUSE_3_SIGNIFICATION("courage and communication", "साहस र सञ्चार"),
    HOUSE_4_SIGNIFICATION("property and domestic comfort", "सम्पत्ति र घरेलु सुविधा"),
    HOUSE_5_SIGNIFICATION("speculation and creative ventures", "अनुमान र सिर्जनात्मक उद्यमहरू"),
    HOUSE_6_SIGNIFICATION("service and defeating competition", "सेवा र प्रतिस्पर्धा जित्नु"),
    HOUSE_7_SIGNIFICATION("partnership and business", "साझेदारी र व्यापार"),
    HOUSE_8_SIGNIFICATION("inheritance and unexpected gains", "विरासत र अप्रत्याशित लाभ"),
    HOUSE_9_SIGNIFICATION("fortune and higher pursuits", "भाग्य र उच्च खोजीहरू"),
    HOUSE_10_SIGNIFICATION("career and public recognition", "क्यारियर र सार्वजनिक मान्यता"),
    HOUSE_11_SIGNIFICATION("gains and social networks", "लाभ र सामाजिक सञ्जालहरू"),
    HOUSE_12_SIGNIFICATION("foreign connections and spiritual pursuits", "विदेशी सम्बन्ध र आध्यात्मिक खोजीहरू"),
    VARIOUS_ACTIVITIES("various activities", "विभिन्न गतिविधिहरू"),

    // ============================================
    // REPORT HEADERS & SECTIONS
    // ============================================
    REPORT_YOGA_ANALYSIS("YOGA ANALYSIS REPORT", "योग विश्लेषण रिपोर्ट"),
    REPORT_TOTAL_YOGAS("Total Yogas Found", "कुल योगहरू फेला परेको"),
    REPORT_OVERALL_STRENGTH("Overall Yoga Strength", "समग्र योग बल"),
    REPORT_DOMINANT_CATEGORY("Dominant Category", "प्रमुख वर्ग"),
    REPORT_PLANETS("Planets", "ग्रहहरू"),
    REPORT_HOUSES("Houses", "भावहरू"),
    REPORT_EFFECTS("Effects", "प्रभावहरू"),
    REPORT_ACTIVATION("Activation", "सक्रियता"),
    REPORT_PATTERN("Pattern", "ढाँचा"),
    REPORT_CANCELLATION_FACTORS("Cancellation Factors", "रद्द गर्ने कारकहरू"),
    REPORT_AUSPICIOUS("Auspicious", "शुभ"),
    REPORT_INAUSPICIOUS("Inauspicious", "अशुभ"),

    REPORT_REMEDIES("VEDIC ASTROLOGY REMEDIES REPORT", "वैदिक ज्योतिष उपाय रिपोर्ट"),
    REPORT_PLANETARY_STRENGTH_ANALYSIS("PLANETARY STRENGTH ANALYSIS", "ग्रह बल विश्लेषण"),
    REPORT_PLANETS_REQUIRING_ATTENTION("PLANETS REQUIRING ATTENTION", "ध्यान आवश्यक ग्रहहरू"),
    REPORT_RECOMMENDED_REMEDIES("RECOMMENDED REMEDIES", "सिफारिस गरिएका उपायहरू"),
    REPORT_GENERAL_RECOMMENDATIONS("GENERAL RECOMMENDATIONS", "सामान्य सिफारिसहरू"),
    REPORT_SUMMARY("SUMMARY", "सारांश"),
    REPORT_GENERATED_BY("Generated by AstroStorm - Ultra-Precision Vedic Astrology", "AstroStorm द्वारा उत्पन्न - अति-सटीक वैदिक ज्योतिष"),
    REPORT_CATEGORY("Category", "वर्ग"),
    REPORT_PLANET("Planet", "ग्रह"),
    REPORT_METHOD("Method", "विधि"),
    REPORT_TIMING("Timing", "समय"),

    REPORT_MATCHMAKING("KUNDLI MILAN (MATCHMAKING) REPORT", "कुण्डली मिलान रिपोर्ट"),
    REPORT_MATCHMAKING_TITLE("KUNDLI MILAN REPORT", "कुण्डली मिलान रिपोर्ट"),
    REPORT_ASTROSTORM_ANALYSIS("AstroStorm Analysis", "AstroStorm विश्लेषण"),
    REPORT_PROFILES("PROFILES", "प्रोफाइलहरू"),
    REPORT_BRIDE("BRIDE", "वधू"),
    REPORT_BRIDE_LABEL("Bride:", "वधू:"),
    REPORT_GROOM("GROOM", "वर"),
    REPORT_GROOM_LABEL("Groom:", "वर:"),
    REPORT_MOON_SIGN_LABEL("Moon Sign:", "चन्द्र राशि:"),
    REPORT_NAKSHATRA_LABEL("Nakshatra:", "नक्षत्र:"),
    REPORT_COMPATIBILITY_SCORE("COMPATIBILITY SCORE", "अनुकूलता अंक"),
    REPORT_TOTAL_POINTS("Total Points:", "कुल अंक:"),
    REPORT_PERCENTAGE("Percentage:", "प्रतिशत:"),
    REPORT_RATING_LABEL("Rating:", "मूल्याङ्कन:"),
    REPORT_ASHTAKOOTA("ASHTAKOOTA ANALYSIS", "अष्टकूट विश्लेषण"),
    REPORT_ASHTAKOOTA_8_GUNA("ASHTAKOOTA (8 GUNA) ANALYSIS", "अष्टकूट (८ गुण) विश्लेषण"),
    REPORT_SCORE_LABEL("Score:", "अंक:"),
    REPORT_GUNA("GUNA", "गुण"),
    REPORT_MAX("MAX", "अधिकतम"),
    REPORT_OBTAINED("OBTAINED", "प्राप्त"),
    REPORT_STATUS("STATUS", "स्थिति"),
    REPORT_TOTAL("TOTAL", "कुल"),
    REPORT_OVERALL_RATING("OVERALL RATING", "समग्र मूल्याङ्कन"),
    REPORT_ADDITIONAL_FACTORS("ADDITIONAL FACTORS", "थप कारकहरू"),
    REPORT_MANGLIK_ANALYSIS("MANGLIK ANALYSIS", "मांगलिक विश्लेषण"),
    REPORT_MANGLIK_DOSHA_ANALYSIS("MANGLIK DOSHA ANALYSIS", "मांगलिक दोष विश्लेषण"),
    REPORT_MARS_IN_HOUSE("Mars in House %d", "भाव %d मा मंगल"),
    REPORT_CANCELLATION("(Cancellation)", "(रद्द)"),
    REPORT_SPECIAL_CONSIDERATIONS("SPECIAL CONSIDERATIONS", "विशेष विचारहरू"),
    REPORT_SUGGESTED_REMEDIES("SUGGESTED REMEDIES", "सुझाव गरिएका उपायहरू"),
    REPORT_COMPATIBILITY("Compatibility", "अनुकूलता"),
    REPORT_COMPATIBILITY_LABEL("Compatibility:", "अनुकूलता:"),
    REPORT_NOT_PRESENT("Not Present", "उपस्थित छैन"),
    REPORT_COMPATIBLE("Compatible", "अनुकूल"),
    REPORT_SATISFIED("Satisfied", "सन्तुष्ट"),
    REPORT_NOT_SATISFIED("Not satisfied", "सन्तुष्ट छैन"),
    REPORT_NOT_APPLICABLE("Not applicable", "लागू हुँदैन"),
    REPORT_NA("N/A", "उपलब्ध छैन"),
    REPORT_ASTROSTORM_VEDIC("Vedic Astrology • Ultra-Precision", "वैदिक ज्योतिष • अति-सटीक"),
    REPORT_KUNDLI_MILAN_SUMMARY("KUNDLI MILAN SUMMARY", "कुण्डली मिलान सारांश"),
    REPORT_MANGLIK_LABEL("Manglik:", "मांगलिक:"),
    REPORT_ASHTAKOOTA_GUNA_SCORES("ASHTAKOOTA GUNA SCORES", "अष्टकूट गुण अंकहरू"),

    // ============================================
    // SPECIFIC YOGA NAMES (For display)
    // ============================================
    YOGA_KENDRA_TRIKONA("Kendra-Trikona Raja Yoga", "केन्द्र-त्रिकोण राज योग"),
    YOGA_PARIVARTANA("Parivartana Raja Yoga", "परिवर्तन राज योग"),
    YOGA_VIPARITA("Viparita Raja Yoga", "विपरीत राज योग"),
    YOGA_NEECHA_BHANGA("Neecha Bhanga Raja Yoga", "नीच भंग राज योग"),
    YOGA_MAHA_RAJA("Maha Raja Yoga", "महा राज योग"),
    YOGA_LAKSHMI("Lakshmi Yoga", "लक्ष्मी योग"),
    YOGA_KUBERA("Kubera Yoga", "कुबेर योग"),
    YOGA_CHANDRA_MANGALA("Chandra-Mangala Yoga", "चन्द्र-मंगल योग"),
    YOGA_LABHA("Labha Yoga", "लाभ योग"),
    YOGA_RUCHAKA("Ruchaka Yoga", "रुचक योग"),
    YOGA_BHADRA("Bhadra Yoga", "भद्र योग"),
    YOGA_HAMSA("Hamsa Yoga", "हंस योग"),
    YOGA_MALAVYA("Malavya Yoga", "मालव्य योग"),
    YOGA_SASA("Sasa Yoga", "शश योग"),
    YOGA_SUNAFA("Sunafa Yoga", "सुनफा योग"),
    YOGA_ANAFA("Anafa Yoga", "अनफा योग"),
    YOGA_DURUDHARA("Durudhara Yoga", "दुरुधरा योग"),
    YOGA_GAJA_KESARI("Gaja-Kesari Yoga", "गज-केसरी योग"),
    YOGA_ADHI("Adhi Yoga", "अधि योग"),
    YOGA_VESI("Vesi Yoga", "वेशी योग"),
    YOGA_VOSI("Vosi Yoga", "वोशी योग"),
    YOGA_UBHAYACHARI("Ubhayachari Yoga", "उभयचारी योग"),
    YOGA_KEMADRUMA("Kemadruma Yoga", "केमद्रुम योग"),
    YOGA_DARIDRA("Daridra Yoga", "दरिद्र योग"),
    YOGA_SHAKATA("Shakata Yoga", "शकट योग"),
    YOGA_GURU_CHANDAL("Guru-Chandal Yoga", "गुरु-चांडाल योग"),
    YOGA_BUDHA_ADITYA("Budha-Aditya Yoga", "बुध-आदित्य योग"),
    YOGA_AMALA("Amala Yoga", "अमला योग"),
    YOGA_SARASWATI("Saraswati Yoga", "सरस्वती योग"),
    YOGA_PARVATA("Parvata Yoga", "पर्वत योग"),
    YOGA_KAHALA("Kahala Yoga", "कहल योग"),
    YOGA_YAVA("Yava Yoga", "यव योग"),
    YOGA_SHRINGATAKA("Shringataka Yoga", "शृंगाटक योग"),
    YOGA_GADA("Gada Yoga", "गदा योग"),
    YOGA_RAJJU("Rajju Yoga", "रज्जु योग"),
    YOGA_MUSALA("Musala Yoga", "मुसल योग"),
    YOGA_NALA("Nala Yoga", "नल योग"),
    YOGA_KEDARA("Kedara Yoga", "केदार योग"),
    YOGA_SHOOLA("Shoola Yoga", "शूल योग"),
    YOGA_YUGA("Yuga Yoga", "युग योग"),
    YOGA_GOLA("Gola Yoga", "गोल योग"),
    YOGA_VEENA("Veena Yoga", "वीणा योग"),
    YOGA_DASA_MULA("Dasa-Mula Yoga", "दश-मूल योग"),
    YOGA_VARGOTTAMA_STRENGTH("Vargottama Strength", "वर्गोत्तम बल"),

    // New Yogas - Grahan and Nodal Combinations
    YOGA_SURYA_GRAHAN("Surya Grahan Yoga", "सूर्य ग्रहण योग"),
    YOGA_SURYA_KETU_GRAHAN("Surya-Ketu Grahan Yoga", "सूर्य-केतु ग्रहण योग"),
    YOGA_CHANDRA_GRAHAN("Chandra Grahan Yoga", "चन्द्र ग्रहण योग"),
    YOGA_CHANDRA_KETU("Chandra-Ketu Yoga", "चन्द्र-केतु योग"),
    YOGA_ANGARAK("Angarak Yoga", "अङ्गारक योग"),
    YOGA_SHRAPIT("Shrapit Yoga", "शापित योग"),
    YOGA_KALA_SARPA("Kala Sarpa Yoga", "कालसर्प योग"),
    YOGA_PAPAKARTARI("Papakartari Yoga", "पापकर्तरी योग"),
    YOGA_SHUBHAKARTARI("Shubhakartari Yoga", "शुभकर्तरी योग"),
    YOGA_SANYASA("Sanyasa Yoga", "सन्यास योग"),
    YOGA_CHAMARA("Chamara Yoga", "चामर योग"),
    YOGA_DHARMA_KARMADHIPATI("Dharma-Karmadhipati Yoga", "धर्म-कर्माधिपति योग"),

    // New Yoga Effects
    YOGA_EFFECT_SURYA_GRAHAN("Father-related troubles, ego issues, government problems, health issues with head/eyes", "पिता सम्बन्धी समस्याहरू, अहंकार समस्याहरू, सरकारी समस्याहरू, टाउको/आँखामा स्वास्थ्य समस्याहरू"),
    YOGA_EFFECT_SURYA_KETU_GRAHAN("Spiritual detachment, low self-esteem, father troubles, past-life karmic issues", "आध्यात्मिक विरक्ति, कम आत्मसम्मान, पिता समस्याहरू, पूर्वजन्म कर्म समस्याहरू"),
    YOGA_EFFECT_CHANDRA_GRAHAN("Mental restlessness, mother troubles, emotional instability, obsessive tendencies", "मानसिक अशान्ति, आमा समस्याहरू, भावनात्मक अस्थिरता, जुनूनी प्रवृत्तिहरू"),
    YOGA_EFFECT_CHANDRA_KETU("Detachment from emotions, past-life memories, psychic sensitivity, mother karma", "भावनाहरूबाट विरक्ति, पूर्वजन्म स्मृतिहरू, मनोवैज्ञानिक संवेदनशीलता, आमा कर्म"),
    YOGA_EFFECT_ANGARAK("Accidents, surgery, aggression, sibling troubles, litigation, sudden events", "दुर्घटनाहरू, शल्यक्रिया, आक्रामकता, भाइबहिनी समस्याहरू, मुद्दा, अचानक घटनाहरू"),
    YOGA_EFFECT_SHRAPIT("Past-life karma manifesting as chronic obstacles, delays, fear, ancestral issues", "पूर्वजन्म कर्म दीर्घकालीन बाधाहरू, ढिलाइ, डर, पुर्खौली समस्याहरूको रूपमा प्रकट"),
    YOGA_EFFECT_KALA_SARPA("Karmic life patterns, sudden ups and downs, spiritual transformation potential", "कर्मजन्य जीवन ढाँचाहरू, अचानक उतार-चढाव, आध्यात्मिक रूपान्तरण सम्भावना"),
    YOGA_EFFECT_PAPAKARTARI("Obstacles in self-expression, health challenges, restricted opportunities", "आत्म-अभिव्यक्तिमा बाधाहरू, स्वास्थ्य चुनौतीहरू, सीमित अवसरहरू"),
    YOGA_EFFECT_SHUBHAKARTARI("Protected life, good health, success in endeavors, helpful people around", "सुरक्षित जीवन, राम्रो स्वास्थ्य, प्रयासहरूमा सफलता, वरपर मद्दतगर्ने मानिसहरू"),
    YOGA_EFFECT_SANYASA("Renunciation tendencies, spiritual inclinations, detachment from worldly matters", "त्याग प्रवृत्तिहरू, आध्यात्मिक झुकाव, सांसारिक कुराहरूबाट विरक्ति"),
    YOGA_EFFECT_CHAMARA("Royal honors, fame, eloquence, learned, respected by rulers", "राजकीय सम्मान, प्रसिद्धि, वाक्पटुता, पढेलेखेको, शासकहरूद्वारा सम्मानित"),
    YOGA_EFFECT_DHARMA_KARMADHIPATI("Highly successful career, fortune through profession, fame, authority positions", "अत्यन्त सफल करियर, पेशाबाट भाग्य, प्रसिद्धि, अधिकार पदहरू"),

    // Yoga Effects Translations
    YOGA_EFFECT_RUCHAKA("Commander, army chief, valorous, muscular body, red complexion, successful in conflicts, skilled in warfare, leader of thieves or soldiers, wealth through martial arts or defense", "सेनापति, सेना प्रमुख, वीर, बलियो शरीर, रातो रंग, द्वन्द्वमा सफल, युद्ध कलामा दक्ष, चोर वा सैनिकहरूको नेता, युद्ध कला वा रक्षाबाट धन"),
    YOGA_EFFECT_BHADRA("Intelligent, eloquent speaker, skilled in arts and sciences, long-lived, wealthy through intellect, respected in assemblies, lion-like face, broad chest", "बुद्धिमान, वाक्पटु वक्ता, कला र विज्ञानमा दक्ष, दीर्घायु, बुद्धिबाट धनी, सभामा सम्मानित, सिंह जस्तो मुख, फराकिलो छाती"),
    YOGA_EFFECT_HAMSA("Righteous king, fair complexion, elevated nose, beautiful face, devoted to gods and brahmins, fond of water sports, walks like a swan, respected by rulers, spiritual inclinations", "धार्मिक राजा, गोरो रंग, उठेको नाक, सुन्दर मुख, देवता र ब्राह्मणहरूप्रति भक्त, जल क्रीडाको शौकीन, हंस जस्तो हिँड्ने, शासकहरूद्वारा सम्मानित, आध्यात्मिक झुकाव"),
    YOGA_EFFECT_MALAVYA("Wealthy, enjoys all comforts, beautiful spouse, strong limbs, attractive face, blessed with vehicles and servants, learned in scriptures, lives up to 77 years", "धनी, सबै सुविधा भोग्ने, सुन्दर पति/पत्नी, बलियो अंगहरू, आकर्षक मुख, वाहन र सेवकहरूले आशीर्वादित, शास्त्रमा पढेको, ७७ वर्षसम्म बाँच्ने"),
    YOGA_EFFECT_SASA("Head of village/town/city, wicked disposition but good servants, intriguing nature, knows others' weaknesses, commands over masses, wealth through iron or labor", "गाउँ/शहर/नगरको प्रमुख, दुष्ट स्वभाव तर राम्रा सेवकहरू, षड्यन्त्रकारी स्वभाव, अरूको कमजोरी जान्ने, जनतामाथि आदेश, फलाम वा श्रमबाट धन"),
    YOGA_EFFECT_GAJA_KESARI("Destroyer of enemies like lion, eloquent speaker, virtuous, long-lived, famous", "सिंह जस्तो शत्रु विनाशक, वाक्पटु वक्ता, सद्गुणी, दीर्घायु, प्रसिद्ध"),
    YOGA_EFFECT_SUNAFA("Self-made wealth, intelligent, good status, praised by kings", "स्व-निर्मित धन, बुद्धिमान, राम्रो स्थिति, राजाहरूद्वारा प्रशंसित"),
    YOGA_EFFECT_ANAFA("Good reputation, health, happiness, self-respect", "राम्रो प्रतिष्ठा, स्वास्थ्य, खुशी, आत्म-सम्मान"),
    YOGA_EFFECT_DURUDHARA("Highly fortunate, wealthy, vehicles, servants, charitable, enjoys life", "अत्यधिक भाग्यशाली, धनी, वाहनहरू, सेवकहरू, दानशील, जीवनको आनन्द लिने"),
    YOGA_EFFECT_ADHI("Commander, minister, or king; polite, trustworthy, healthy, wealthy, defeats enemies", "सेनापति, मन्त्री, वा राजा; विनम्र, विश्वसनीय, स्वस्थ, धनी, शत्रुहरूलाई हराउने"),
    YOGA_EFFECT_BUDHA_ADITYA("Intelligence, skilled in many arts, famous, sweet speech, scholarly", "बुद्धि, धेरै कलाहरूमा दक्ष, प्रसिद्ध, मीठो बोली, विद्वान"),
    YOGA_EFFECT_SARASWATI("Highly learned, poet, prose writer, famous speaker, skilled in all arts", "अत्यधिक पढेलेखेको, कवि, गद्य लेखक, प्रसिद्ध वक्ता, सबै कलाहरूमा दक्ष"),
    YOGA_EFFECT_PARVATA("King or minister, famous, generous, wealthy, charitable, mountain-like stability", "राजा वा मन्त्री, प्रसिद्ध, उदार, धनी, दानशील, पहाड जस्तो स्थिरता"),
    YOGA_EFFECT_LAKSHMI("Blessed by Goddess Lakshmi, abundant wealth, luxury, beauty, artistic success", "देवी लक्ष्मीको आशीर्वाद, प्रचुर धन, विलासिता, सौन्दर्य, कलात्मक सफलता"),
    YOGA_EFFECT_MAHA_RAJA("Exceptional fortune, royal status, widespread fame, great wealth and power", "असाधारण भाग्य, राजकीय स्थिति, व्यापक प्रसिद्धि, ठूलो धन र शक्ति"),
    YOGA_EFFECT_KENDRA_TRIKONA("Rise to power and authority, leadership position, recognition from government", "शक्ति र अधिकारमा उदय, नेतृत्व पद, सरकारबाट मान्यता"),
    YOGA_EFFECT_PARIVARTANA("Strong Raja Yoga through mutual exchange, stable rise to power, lasting authority", "आपसी आदानप्रदानबाट बलियो राज योग, शक्तिमा स्थिर उदय, दिगो अधिकार"),
    YOGA_EFFECT_VIPARITA("Rise through fall of enemies, sudden fortune from unexpected sources, gains through others' losses", "शत्रुहरूको पतनबाट उदय, अप्रत्याशित स्रोतहरूबाट अचानक भाग्य, अरूको हानिबाट लाभ"),
    YOGA_EFFECT_NEECHA_BHANGA("Rise from humble beginnings, success after initial struggles, respected leader", "साधारण सुरुवातबाट उदय, प्रारम्भिक संघर्षपछि सफलता, सम्मानित नेता"),
    YOGA_EFFECT_KEMADRUMA("Poverty, suffering, struggles, lack of support, lonely, menial work", "गरिबी, दुख, संघर्ष, समर्थनको अभाव, एक्लो, तल्लो काम"),
    YOGA_EFFECT_KEMADRUMA_CANCELLED("Kemadruma effects significantly reduced due to cancellation factors", "रद्द कारकहरूको कारण केमद्रुम प्रभाव उल्लेखनीय रूपमा कम भयो"),
    YOGA_EFFECT_DARIDRA("Obstacles to gains, financial struggles, unfulfilled desires", "लाभमा बाधाहरू, आर्थिक संघर्ष, अपूर्ण इच्छाहरू"),
    YOGA_EFFECT_SHAKATA("Fluctuating fortune, periods of poverty alternating with wealth", "उतार-चढाव भाग्य, गरिबीको अवधि धनसँग पालैपालो"),
    YOGA_EFFECT_GURU_CHANDAL("Unorthodox beliefs, breaks from tradition, possible disgrace through teachers/religion", "अपरम्परागत विश्वासहरू, परम्परा तोड्ने, गुरु/धर्मबाट सम्भावित अपमान"),
    YOGA_EFFECT_VESI("Wealth through hard work, truthful, balanced life, comfortable old age", "मेहनतबाट धन, सत्यवादी, सन्तुलित जीवन, आरामदायक बुढेसकाल"),
    YOGA_EFFECT_VOSI("Famous, generous, skilled in service, gains through associations", "प्रसिद्ध, उदार, सेवामा दक्ष, संगतबाट लाभ"),
    YOGA_EFFECT_UBHAYACHARI("Eloquent speaker, wealthy, influential, respected by rulers", "वाक्पटु वक्ता, धनी, प्रभावशाली, शासकहरूद्वारा सम्मानित"),
    YOGA_EFFECT_LABHA("Gains from multiple sources, profitable ventures, wealth accumulation", "विभिन्न स्रोतबाट लाभ, लाभदायक उद्यमहरू, धन संचय"),
    YOGA_EFFECT_KUBERA("Immense wealth like lord of wealth, treasure finder, banking success", "धनको देवता जस्तो अपार धन, खजाना फेला पार्ने, बैंकिङमा सफलता"),
    YOGA_EFFECT_CHANDRA_MANGALA("Wealth through business, enterprise, real estate, aggressive financial pursuits", "व्यापारबाट धन, उद्यम, घर जग्गा, आक्रामक आर्थिक प्रयासहरू"),
    YOGA_EFFECT_DASA_MULA("Obstacles in undertakings, needs remedial measures, struggle with finances", "कार्यहरूमा बाधाहरू, उपचारात्मक उपायहरू आवश्यक, आर्थिक संघर्ष"),
    YOGA_EFFECT_KAHALA("Brave but stubborn, military success, leadership through conflict", "बहादुर तर हठी, सैन्य सफलता, द्वन्द्वबाट नेतृत्व"),

    // Yoga Descriptions
    YOGA_DESC_KENDRA_LORD("Kendra lord", "केन्द्र अधिपति"),
    YOGA_DESC_TRIKONA_LORD("Trikona lord", "त्रिकोण अधिपति"),
    YOGA_DESC_IN_CONJUNCTION("in conjunction", "संयोग मा"),
    YOGA_DESC_IN_ASPECT("in aspect", "दृष्टि मा"),
    YOGA_DESC_OWN_SIGN("own sign", "आफ्नो राशि"),
    YOGA_DESC_EXALTED("exalted sign", "उच्च राशि"),
    YOGA_DESC_IN_KENDRA("in Kendra", "केन्द्रमा"),
    YOGA_DESC_DUSTHANA("Dusthana", "दुस्थान"),
    YOGA_DESC_DEBILITATED("debilitated", "नीच"),
    YOGA_DESC_COMBUST("combust", "अस्त"),

    // Nabhasa Yoga Effects
    YOGA_EFFECT_YAVA("Medium wealth initially, prosperity in middle age, decline in old age", "सुरुमा मध्यम धन, मध्य उमेरमा समृद्धि, बुढेसकालमा पतन"),
    YOGA_EFFECT_SHRINGATAKA("Fond of quarrels initially, happiness in middle age, wandering in old age", "सुरुमा झगडा मनपर्ने, मध्य उमेरमा खुशी, बुढेसकालमा भौंतारिने"),
    YOGA_EFFECT_GADA("Wealthy through ceremonies, always engaged in auspicious activities", "संस्कारहरूबाट धनी, सधैं शुभ कार्यहरूमा संलग्न"),
    YOGA_EFFECT_SHAKATA_NABHASA("Fluctuating fortune, poverty followed by wealth in cycles", "उतार-चढाव भाग्य, गरिबीपछि धन चक्रमा"),
    YOGA_EFFECT_RAJJU("Fond of travel, living in foreign lands, restless nature", "यात्राको शौकीन, विदेशी भूमिमा बस्ने, अशान्त स्वभाव"),
    YOGA_EFFECT_MUSALA("Proud, wealthy, learned, famous, many children", "गर्विलो, धनी, पढेलेखेको, प्रसिद्ध, धेरै सन्तान"),
    YOGA_EFFECT_NALA("Handsome, skilled in arts, wealthy through multiple sources", "सुन्दर, कलामा दक्ष, विभिन्न स्रोतबाट धनी"),
    YOGA_EFFECT_KEDARA("Agricultural wealth, helpful to others, truthful", "कृषि धन, अरूलाई मद्दतगर्ने, सत्यवादी"),
    YOGA_EFFECT_SHOOLA("Sharp intellect, quarrelsome, cruel, poor", "तीक्ष्ण बुद्धि, झगडालु, निर्दयी, गरिब"),
    YOGA_EFFECT_YUGA("Heretic, poor, rejected by family", "विधर्मी, गरिब, परिवारद्वारा त्यागिएको"),
    YOGA_EFFECT_GOLA("Poor, dirty, ignorant, idle", "गरिब, फोहोरी, अज्ञानी, अल्छी"),
    YOGA_EFFECT_VEENA("Fond of music, dance, leader, wealthy, happy", "संगीत र नृत्यको शौकीन, नेता, धनी, खुशी"),

    // ============================================
    // CHOGHADIYA NAMES
    // ============================================
    CHOGHADIYA_AMRIT("Amrit", "अमृत"),
    CHOGHADIYA_SHUBH("Shubh", "शुभ"),
    CHOGHADIYA_LABH("Labh", "लाभ"),
    CHOGHADIYA_CHAR("Char", "चर"),
    CHOGHADIYA_ROG("Rog", "रोग"),
    CHOGHADIYA_KAAL("Kaal", "काल"),
    CHOGHADIYA_UDVEG("Udveg", "उद्वेग"),

    // ============================================
    // ADDITIONAL UI STRINGS
    // ============================================
    UI_CONJUNCTION("conjunction", "युति"),
    UI_ASPECT("aspect", "दृष्टि"),
    UI_EXCHANGE("exchange", "परिवर्तन"),
    UI_THROUGHOUT_LIFE("Throughout life", "जीवनभर"),
    UI_NONE("None", "कुनै पनि छैन"),
    UI_PRESENT("Present", "उपस्थित"),
    UI_ASCENDING("Ascending", "आरोही"),
    UI_DESCENDING("Descending", "अवरोही"),
    UI_NAKSHATRAS("nakshatras", "नक्षत्रहरू"),

    // ============================================
    // SCREEN TAB NAMES
    // ============================================
    TAB_OVERVIEW("Overview", "सिंहावलोकन"),
    TAB_REMEDIES("Remedies", "उपायहरू"),
    TAB_PLANETS("Planets", "ग्रहहरू"),
    TAB_TODAY("Today", "आज"),
    TAB_FIND_MUHURTA("Find Muhurta", "मुहूर्त खोज्नुहोस्"),
    TAB_TAJIKA("Tajika", "ताजिक"),
    TAB_SAHAMS("Sahams", "सहमहरू"),
    TAB_DASHA("Dasha", "दशा"),
    TAB_HOUSES("Houses", "भावहरू"),
    TAB_ANALYSIS("Analysis", "विश्लेषण"),
    TAB_DETAILS("Details", "विवरणहरू"),
    TAB_TRANSITS("Transits", "गोचर"),
    TAB_ASPECTS("Aspects", "दृष्टि"),
    TAB_STRENGTH("Strength", "बल"),
    TAB_DIGNITIES("Dignities", "मर्यादाहरू"),

    // ============================================
    // REMEDIES SCREEN SPECIFIC
    // ============================================
    REMEDY_TITLE("Remedies", "उपायहरू"),
    REMEDY_SEARCH("Search remedies", "उपायहरू खोज्नुहोस्"),
    REMEDY_CALCULATION_FAILED("Failed to calculate remedies: %s", "उपायहरू गणना गर्न असफल: %s"),
    REMEDY_COPY("Copy", "कपी गर्नुहोस्"),
    REMEDY_SHARE("Share", "साझा गर्नुहोस्"),
    REMEDY_PLANETARY_ANALYSIS("Planetary Analysis", "ग्रह विश्लेषण"),
    REMEDY_ESSENTIAL_COUNT("%d Essential Remedies", "%d आवश्यक उपायहरू"),
    REMEDY_ALL_STRONG("All planets are in good condition", "सबै ग्रह राम्रो अवस्थामा छन्"),

    // ============================================
    // MUHURTA SCREEN SPECIFIC
    // ============================================
    MUHURTA_CHOGHADIYA("Choghadiya", "चौघडिया"),
    MUHURTA_RAHU_KAAL("Rahu Kaal", "राहुकाल"),
    MUHURTA_YAMA_GHANTAKA("Yama Ghantaka", "यम घण्टक"),
    MUHURTA_GULIKA_KAAL("Gulika Kaal", "गुलिका काल"),
    MUHURTA_ABHIJIT("Abhijit Muhurta", "अभिजित मुहूर्त"),
    MUHURTA_BRAHMA("Brahma Muhurta", "ब्रह्म मुहूर्त"),
    MUHURTA_SELECT_DATE("Select Date", "मिति चयन गर्नुहोस्"),
    MUHURTA_SEARCH_RESULTS("Search Results", "खोज परिणामहरू"),

    // ============================================
    // VARSHAPHALA SCREEN SPECIFIC
    // ============================================
    VARSHAPHALA_TITLE("Varshaphala", "वर्षफल"),
    VARSHAPHALA_ANNUAL_CHART("Annual Chart", "वार्षिक चार्ट"),
    VARSHAPHALA_SAHAMS("Sahams", "सहमहरू"),
    VARSHAPHALA_TAJIKA("Tajika Aspects", "ताजिक दृष्टि"),
    VARSHAPHALA_YOGAS("Tajika Yogas", "ताजिक योग"),
    VARSHAPHALA_PREDICTIONS("Year Predictions", "वर्ष भविष्यवाणी"),
    VARSHAPHALA_SELECT_YEAR("Select Year", "वर्ष चयन गर्नुहोस्"),
    VARSHAPHALA_YEAR_OF_LIFE("Year %d of life", "जीवनको वर्ष %d"),

    // ============================================
    // PANCHANGA DETAILS
    // ============================================
    PANCHANGA_TITHI_SHUKLA("Shukla", "शुक्ल"),
    PANCHANGA_TITHI_KRISHNA("Krishna", "कृष्ण"),
    PANCHANGA_PAKSHA("Paksha", "पक्ष"),
    PANCHANGA_MASA("Masa", "मास"),
    PANCHANGA_LUNAR_PHASE("Lunar Phase", "चन्द्र कला"),
    PANCHANGA_NEW_MOON("New Moon", "अमावस्या"),
    PANCHANGA_FULL_MOON("Full Moon", "पूर्णिमा"),
    PANCHANGA_WAXING("Waxing", "बढ्दो"),
    PANCHANGA_WANING("Waning", "घट्दो"),
    PANCHANGA_FAVORABLE("Favorable", "अनुकूल"),
    PANCHANGA_UNFAVORABLE("Unfavorable", "प्रतिकूल"),
    PANCHANGA_ACTIVITIES("Favorable Activities", "अनुकूल गतिविधिहरू"),

    // ============================================
    // TITHI NAMES
    // ============================================
    TITHI_PRATIPADA("Pratipada", "प्रतिपदा"),
    TITHI_DWITIYA("Dwitiya", "द्वितीया"),
    TITHI_TRITIYA("Tritiya", "तृतीया"),
    TITHI_CHATURTHI("Chaturthi", "चतुर्थी"),
    TITHI_PANCHAMI("Panchami", "पञ्चमी"),
    TITHI_SHASHTHI("Shashthi", "षष्ठी"),
    TITHI_SAPTAMI("Saptami", "सप्तमी"),
    TITHI_ASHTAMI("Ashtami", "अष्टमी"),
    TITHI_NAVAMI("Navami", "नवमी"),
    TITHI_DASHAMI("Dashami", "दशमी"),
    TITHI_EKADASHI("Ekadashi", "एकादशी"),
    TITHI_DWADASHI("Dwadashi", "द्वादशी"),
    TITHI_TRAYODASHI("Trayodashi", "त्रयोदशी"),
    TITHI_CHATURDASHI("Chaturdashi", "चतुर्दशी"),
    TITHI_PURNIMA("Purnima", "पूर्णिमा"),
    TITHI_AMAVASYA("Amavasya", "अमावस्या"),

    // ============================================
    // KARANA NAMES
    // ============================================
    KARANA_BAVA("Bava", "बव"),
    KARANA_BALAVA("Balava", "बालव"),
    KARANA_KAULAVA("Kaulava", "कौलव"),
    KARANA_TAITILA("Taitila", "तैतिल"),
    KARANA_GARA("Gara", "गर"),
    KARANA_VANIJA("Vanija", "वणिज"),
    KARANA_VISHTI("Vishti (Bhadra)", "विष्टि (भद्रा)"),
    KARANA_SHAKUNI("Shakuni", "शकुनि"),
    KARANA_CHATUSHPADA("Chatushpada", "चतुष्पद"),
    KARANA_NAGA("Naga", "नाग"),
    KARANA_KIMSTUGHNA("Kimstughna", "किंस्तुघ्न"),

    // ============================================
    // DAILY YOGA NAMES (PANCHANGA)
    // ============================================
    DAILY_YOGA_VISHKUMBHA("Vishkumbha", "विष्कम्भ"),
    DAILY_YOGA_PRITI("Priti", "प्रीति"),
    DAILY_YOGA_AYUSHMAN("Ayushman", "आयुष्मान"),
    DAILY_YOGA_SAUBHAGYA("Saubhagya", "सौभाग्य"),
    DAILY_YOGA_SHOBHANA("Shobhana", "शोभन"),
    DAILY_YOGA_ATIGANDA("Atiganda", "अतिगण्ड"),
    DAILY_YOGA_SUKARMA("Sukarma", "सुकर्म"),
    DAILY_YOGA_DHRITI("Dhriti", "धृति"),
    DAILY_YOGA_SHOOLA("Shoola", "शूल"),
    DAILY_YOGA_GANDA("Ganda", "गण्ड"),
    DAILY_YOGA_VRIDDHI("Vriddhi", "वृद्धि"),
    DAILY_YOGA_DHRUVA("Dhruva", "ध्रुव"),
    DAILY_YOGA_VYAGHATA("Vyaghata", "व्याघात"),
    DAILY_YOGA_HARSHANA("Harshana", "हर्षण"),
    DAILY_YOGA_VAJRA("Vajra", "वज्र"),
    DAILY_YOGA_SIDDHI("Siddhi", "सिद्धि"),
    DAILY_YOGA_VYATIPATA("Vyatipata", "व्यतीपात"),
    DAILY_YOGA_VARIYANA("Variyana", "वरीयान"),
    DAILY_YOGA_PARIGHA("Parigha", "परिघ"),
    DAILY_YOGA_SHIVA("Shiva", "शिव"),
    DAILY_YOGA_SIDDHA("Siddha", "सिद्ध"),
    DAILY_YOGA_SADHYA("Sadhya", "साध्य"),
    DAILY_YOGA_SHUBHA("Shubha", "शुभ"),
    DAILY_YOGA_SHUKLA("Shukla", "शुक्ल"),
    DAILY_YOGA_BRAHMA("Brahma", "ब्रह्म"),
    DAILY_YOGA_INDRA("Indra", "इन्द्र"),
    DAILY_YOGA_VAIDHRITI("Vaidhriti", "वैधृति"),

    // ============================================
    // VARA (WEEKDAY) DESCRIPTIONS
    // ============================================
    VARA_SUNDAY_DESC("Ruled by Sun - Good for government work, authority, spiritual practices", "सूर्य द्वारा शासित - सरकारी काम, अधिकार, आध्यात्मिक अभ्यासको लागि राम्रो"),
    VARA_MONDAY_DESC("Ruled by Moon - Good for travel, public dealings, emotional matters", "चन्द्रमा द्वारा शासित - यात्रा, सार्वजनिक व्यवहार, भावनात्मक मामिलाहरूको लागि राम्रो"),
    VARA_TUESDAY_DESC("Ruled by Mars - Good for property, surgery, competitive activities", "मंगल द्वारा शासित - सम्पत्ति, शल्यक्रिया, प्रतिस्पर्धात्मक गतिविधिहरूको लागि राम्रो"),
    VARA_WEDNESDAY_DESC("Ruled by Mercury - Good for education, communication, business", "बुध द्वारा शासित - शिक्षा, सञ्चार, व्यापारको लागि राम्रो"),
    VARA_THURSDAY_DESC("Ruled by Jupiter - Good for religious ceremonies, marriage, education", "बृहस्पति द्वारा शासित - धार्मिक समारोह, विवाह, शिक्षाको लागि राम्रो"),
    VARA_FRIDAY_DESC("Ruled by Venus - Good for romance, marriage, arts, luxury", "शुक्र द्वारा शासित - प्रेम, विवाह, कला, विलासिताको लागि राम्रो"),
    VARA_SATURDAY_DESC("Ruled by Saturn - Good for property, agriculture, spiritual discipline", "शनि द्वारा शासित - सम्पत्ति, कृषि, आध्यात्मिक अनुशासनको लागि राम्रो"),

    // ============================================
    // COMMON ACTION LABELS
    // ============================================
    ACTION_NEW_BEGINNINGS("New beginnings", "नयाँ सुरुवातहरू"),
    ACTION_STARTING_VENTURES("Starting ventures", "उद्यमहरू सुरु गर्दै"),
    ACTION_TRAVEL("Travel", "यात्रा"),
    ACTION_MARRIAGE("Marriage", "विवाह"),
    ACTION_EDUCATION("Education", "शिक्षा"),
    ACTION_BUSINESS("Business", "व्यापार"),
    ACTION_SPIRITUAL_PRACTICES("Spiritual practices", "आध्यात्मिक अभ्यास"),
    ACTION_WORSHIP("Worship", "पूजा"),
    ACTION_CHARITY("Charity", "दान"),
    ACTION_FASTING("Fasting", "उपवास"),
    ACTION_MEDITATION("Meditation", "ध्यान"),
    ACTION_SURGERY("Surgery", "शल्यक्रिया"),
    ACTION_CREATIVE_WORK("Creative work", "सिर्जनात्मक काम"),
    ACTION_GOVERNMENT_WORK("Government work", "सरकारी काम"),
    ACTION_PROPERTY_MATTERS("Property matters", "सम्पत्ति मामिलाहरू"),
    ACTION_FINANCIAL_MATTERS("Financial matters", "वित्तीय मामिलाहरू"),
    ACTION_LEGAL_MATTERS("Legal matters", "कानुनी मामिलाहरू"),

    // ============================================
    // NAVIGATION & COMMON UI ACTIONS
    // ============================================
    NAV_BACK("Back", "पछाडि"),
    NAV_NAVIGATE_BACK("Navigate back", "पछाडि जानुहोस्"),
    NAV_PREVIOUS("Previous", "अघिल्लो"),
    NAV_NEXT("Next", "अर्को"),
    NAV_PREVIOUS_YEAR("Previous year", "अघिल्लो वर्ष"),
    NAV_NEXT_YEAR("Next year", "अर्को वर्ष"),
    NAV_PREVIOUS_DAY("Previous day", "अघिल्लो दिन"),
    NAV_NEXT_DAY("Next day", "अर्को दिन"),

    ACTION_EXPORT("Export", "निर्यात गर्नुहोस्"),
    ACTION_CLEAR("Clear", "खाली गर्नुहोस्"),
    ACTION_CLEAR_SEARCH("Clear search", "खोज खाली गर्नुहोस्"),
    ACTION_COPY("Copy", "कपी गर्नुहोस्"),
    ACTION_COPY_MANTRA("Copy mantra", "मन्त्र कपी गर्नुहोस्"),
    ACTION_VIEW_DETAILS("View details", "विवरणहरू हेर्नुहोस्"),
    ACTION_VIEW_FULLSCREEN("View fullscreen", "पूर्ण स्क्रिनमा हेर्नुहोस्"),
    ACTION_NEW_QUESTION("New question", "नयाँ प्रश्न"),
    ACTION_SEARCH("Search", "खोज्नुहोस्");
}

/**
 * Transit, Panchanga, Prashna, and Varshaphala string keys
 * Part 3 of 4 split enums to avoid JVM method size limit
 */
enum class StringKeyAnalysis(override val en: String, override val ne: String) : StringKeyInterface {

    // ============================================
    // TRANSITS TAB - ADDITIONAL STRINGS
    // ============================================
    TRANSIT_OVERVIEW("Transit Overview", "गोचर सिंहावलोकन"),
    TRANSIT_FAVORABLE("Favorable", "अनुकूल"),
    TRANSIT_CHALLENGING("Challenging", "चुनौतीपूर्ण"),
    TRANSIT_ASPECTS("Aspects", "दृष्टिहरू"),
    TRANSIT_OVERALL_SCORE("Overall Transit Score", "समग्र गोचर अंक"),
    TRANSIT_CURRENT_POSITIONS("Current Planetary Positions", "हालको ग्रह स्थितिहरू"),
    TRANSIT_GOCHARA_ANALYSIS("Gochara Analysis (From Moon)", "गोचर विश्लेषण (चन्द्रबाट)"),
    TRANSIT_ASPECTS_TO_NATAL("Transit Aspects to Natal", "जन्म कुण्डलीमा गोचर दृष्टि"),
    TRANSIT_SIGNIFICANT_PERIODS("Significant Periods", "महत्त्वपूर्ण अवधिहरू"),
    TRANSIT_APPLYING("Applying", "समीप आउँदै"),
    TRANSIT_SEPARATING("Separating", "टाढा हुँदै"),
    TRANSIT_ORB("Orb: %s°", "कोणान्तर: %s°"),
    TRANSIT_VEDHA_FROM("Vedha from %s", "%s बाट वेध"),
    TRANSIT_INTENSITY("Intensity %d/5", "तीव्रता %d/५"),
    TRANSIT_HOUSE_FROM_MOON("House %d", "भाव %d"),

    // ============================================
    // PANCHANGA TAB - ADDITIONAL STRINGS
    // ============================================
    PANCHANGA_AT_BIRTH("Panchanga at Birth", "जन्मको समयको पञ्चाङ्ग"),
    PANCHANGA_ABOUT("About Panchanga", "पञ्चाङ्गको बारेमा"),
    PANCHANGA_ABOUT_INTRO("पञ्चाङ्ग परिचय", "पञ्चाङ्ग परिचय"),
    PANCHANGA_SANSKRIT("पञ्चाङ्ग", "पञ्चाङ्ग"),
    PANCHANGA_LUNAR_DAY("Lunar Day", "चन्द्र दिन"),
    PANCHANGA_LUNAR_DAY_SANSKRIT("तिथि", "तिथि"),
    PANCHANGA_LUNAR_MANSION("Lunar Mansion", "चन्द्र नक्षत्र"),
    PANCHANGA_LUNAR_MANSION_SANSKRIT("नक्षत्र", "नक्षत्र"),
    PANCHANGA_LUNISOLAR("Luni-Solar Combination", "चन्द्र-सूर्य संयोजन"),
    PANCHANGA_LUNISOLAR_SANSKRIT("योग", "योग"),
    PANCHANGA_HALF_LUNAR_DAY("Half Lunar Day", "अर्ध चन्द्र दिन"),
    PANCHANGA_HALF_LUNAR_DAY_SANSKRIT("करण", "करण"),
    PANCHANGA_WEEKDAY("Weekday", "वार"),
    PANCHANGA_WEEKDAY_SANSKRIT("वार", "वार"),
    PANCHANGA_NUMBER("Number", "संख्या"),
    PANCHANGA_NUMBER_OF("of", "मध्ये"),
    PANCHANGA_DEITY("Deity", "देवता"),
    PANCHANGA_LORD("Lord", "स्वामी"),
    PANCHANGA_NATURE("Nature", "प्रकृति"),
    PANCHANGA_PROGRESS("Progress", "प्रगति"),
    PANCHANGA_SYMBOL("Symbol", "चिन्ह"),
    PANCHANGA_GANA("Gana", "गण"),
    PANCHANGA_GUNA("Guna", "गुण"),
    PANCHANGA_ANIMAL("Animal", "पशु"),
    PANCHANGA_MEANING("Meaning", "अर्थ"),
    PANCHANGA_TYPE("Type", "प्रकार"),
    PANCHANGA_ELEMENT("Element", "तत्व"),
    PANCHANGA_DIRECTION("Direction", "दिशा"),
    PANCHANGA_RULING_PLANET("Ruling Planet", "शासक ग्रह"),
    PANCHANGA_SIGNIFICANCE("Significance", "महत्त्व"),
    PANCHANGA_CHARACTERISTICS("Characteristics", "विशेषताहरू"),
    PANCHANGA_EFFECTS("Effects", "प्रभावहरू"),
    PANCHANGA_FAVORABLE_ACTIVITIES("Favorable Activities", "अनुकूल गतिविधिहरू"),
    PANCHANGA_AVOID("Activities to Avoid", "टाढा रहनु पर्ने गतिविधिहरू"),
    PANCHANGA_TITHI_SUBTITLE("Lunar Day • तिथि", "चन्द्र दिन • तिथि"),
    PANCHANGA_NAKSHATRA_SUBTITLE("Lunar Mansion • नक्षत्र", "चन्द्र नक्षत्र • नक्षत्र"),
    PANCHANGA_YOGA_SUBTITLE("Luni-Solar Combination • योग", "चन्द्र-सूर्य संयोजन • योग"),
    PANCHANGA_KARANA_SUBTITLE("Half Lunar Day • करण", "अर्ध चन्द्र दिन • करण"),
    PANCHANGA_VARA_SUBTITLE("Weekday • वार", "वार • वार"),
    PANCHANGA_SANSKRIT_LABEL("Sanskrit", "संस्कृत"),
    PANCHANGA_PADA("Pada", "पद"),
    PANCHANGA_RULER("Ruler", "स्वामी"),
    PANCHANGA_ABOUT_SUBTITLE("पञ्चाङ्ग परिचय", "पञ्चाङ्ग परिचय"),
    PANCHANGA_ABOUT_DESCRIPTION("Panchanga (Sanskrit: पञ्चाङ्ग, \"five limbs\") is the traditional Hindu calendar and almanac. It tracks five fundamental elements of Vedic time-keeping, essential for determining auspicious moments (muhurta) for important activities.", "पञ्चाङ्ग (संस्कृत: पञ्चाङ्ग, \"पाँच अंग\") परम्परागत हिन्दू पात्रो र पंचांग हो। यसले वैदिक समय-गणनाको पाँच मौलिक तत्वहरूको ट्र्याक गर्दछ, जुन महत्त्वपूर्ण कार्यहरूको लागि शुभ क्षण (मुहूर्त) निर्धारण गर्न आवश्यक छ।"),
    PANCHANGA_TITHI_DESC("Based on the angular distance between Sun and Moon. Each tithi spans 12° of lunar elongation. There are 30 tithis in a lunar month.", "सूर्य र चन्द्रमाबीचको कोणीय दूरीमा आधारित। प्रत्येक तिथि १२° चन्द्र विस्तार समेट्छ। एक चान्द्र महिनामा ३० तिथि हुन्छन्।"),
    PANCHANGA_NAKSHATRA_DESC("The Moon's position among 27 stellar constellations, each spanning 13°20'. Determines the Moon's influence on consciousness.", "२७ नक्षत्र तारामण्डलहरू बीच चन्द्रमाको स्थिति, प्रत्येक १३°२०' समेट्छ। चेतनामा चन्द्रमाको प्रभाव निर्धारण गर्दछ।"),
    PANCHANGA_YOGA_DESC("Derived from the sum of Sun and Moon longitudes divided into 27 equal parts. Indicates the overall quality of time.", "सूर्य र चन्द्रमाको देशान्तरको योगफललाई २७ बराबर भागमा विभाजित गरी व्युत्पन्न। समयको समग्र गुणस्तर संकेत गर्दछ।"),
    PANCHANGA_KARANA_DESC("Each tithi has two karanas. There are 11 karanas (4 fixed, 7 repeating) cycling through the month.", "प्रत्येक तिथिमा दुई करण हुन्छन्। ११ करण (४ स्थिर, ७ दोहोरिने) महिनाभर चक्रित हुन्छन्।"),
    PANCHANGA_VARA_DESC("Each day is ruled by a planet, influencing the day's energy and suitable activities.", "प्रत्येक दिन एक ग्रहले शासन गर्दछ, दिनको ऊर्जा र उपयुक्त गतिविधिहरूलाई प्रभाव पार्दछ।"),
    PANCHANGA_BIRTH_INSIGHT("The Panchanga at birth reveals the cosmic influences active at the moment of incarnation, providing insights into one's inherent nature, tendencies, and life patterns.", "जन्मको समयको पञ्चाङ्गले अवतारको क्षणमा सक्रिय ब्रह्माण्डीय प्रभावहरू प्रकट गर्दछ, व्यक्तिको स्वभाविक प्रकृति, प्रवृत्ति र जीवन ढाँचामा अन्तर्दृष्टि प्रदान गर्दछ।"),

    // Panchanga Info Card Element Titles (with Sanskrit)
    PANCHANGA_INFO_TITHI_TITLE("Tithi (तिथि)", "तिथि (तिथि)"),
    PANCHANGA_INFO_NAKSHATRA_TITLE("Nakshatra (नक्षत्र)", "नक्षत्र (नक्षत्र)"),
    PANCHANGA_INFO_YOGA_TITLE("Yoga (योग)", "योग (योग)"),
    PANCHANGA_INFO_KARANA_TITLE("Karana (करण)", "करण (करण)"),
    PANCHANGA_INFO_VARA_TITLE("Vara (वार)", "वार (वार)"),
    PANCHANGA_INFO_TITHI_LABEL("Lunar Day", "चन्द्र दिन"),
    PANCHANGA_INFO_NAKSHATRA_LABEL("Lunar Mansion", "चन्द्र नक्षत्र"),
    PANCHANGA_INFO_YOGA_LABEL("Luni-Solar Combination", "चन्द्र-सूर्य संयोजन"),
    PANCHANGA_INFO_KARANA_LABEL("Half Tithi", "अर्ध तिथि"),
    PANCHANGA_INFO_VARA_LABEL("Weekday", "वार"),

    // Quality Indicators
    QUALITY_EXCELLENT("Excellent", "उत्कृष्ट"),
    QUALITY_GOOD("Good", "राम्रो"),
    QUALITY_NEUTRAL("Neutral", "तटस्थ"),
    QUALITY_CHALLENGING("Challenging", "चुनौतीपूर्ण"),
    QUALITY_INAUSPICIOUS("Inauspicious", "अशुभ"),

    // ============================================
    // CHART TAB - LEGEND & UI STRINGS
    // ============================================
    CHART_LEGEND_RETRO("Retro", "वक्री"),
    CHART_LEGEND_COMBUST("Combust", "अस्त"),
    CHART_LEGEND_VARGOTTAMA("Vargottama", "वर्गोत्तम"),
    CHART_LEGEND_EXALTED("Exalted", "उच्च"),
    CHART_LEGEND_DEBILITATED("Debilitated", "नीच"),
    CHART_LEGEND_OWN_SIGN("Own Sign", "स्वराशि"),
    CHART_LEGEND_MOOL_TRI("Mool Tri.", "मूलत्रि."),
    CHART_BIRTH_DETAILS("Birth Details", "जन्म विवरण"),
    CHART_PLANETARY_POSITIONS("Planetary Positions", "ग्रह स्थितिहरू"),
    CHART_TAP_FOR_DETAILS("Tap for details", "विवरणको लागि ट्याप गर्नुहोस्"),
    CHART_TAP_TO_EXPAND("Tap to expand", "विस्तार गर्न ट्याप गर्नुहोस्"),
    CHART_TAP_HOUSE_FOR_DETAILS("Tap house for details", "विवरणको लागि भाव ट्याप गर्नुहोस्"),
    CHART_ASCENDANT_LAGNA("Ascendant (Lagna)", "लग्न"),
    CHART_HOUSE_CUSPS("House Cusps", "भाव सन्धि"),
    CHART_ASTRONOMICAL_DATA("Astronomical Data", "खगोलीय तथ्याङ्क"),
    CHART_JULIAN_DAY("Julian Day", "जुलियन दिन"),
    CHART_MIDHEAVEN("Midheaven", "मध्याकाश"),
    CHART_HOUSE_SYSTEM("House System", "भाव पद्धति"),
    CHART_TAP_FULLSCREEN("Tap chart to view fullscreen", "पूर्ण स्क्रिनमा हेर्न चार्ट ट्याप गर्नुहोस्"),
    CHART_DATE("Date", "मिति"),
    CHART_TIME("Time", "समय"),
    CHART_AYANAMSA("Ayanamsa", "अयनांश"),
    CHART_LOCATION("Location", "स्थान"),

    // Common Birth Data Labels
    HOUSE("House", "भाव"),
    LOCATION("Location", "स्थान"),
    ASCENDANT("Ascendant", "लग्न"),

    // Chart Type Labels
    CHART_LAGNA("Lagna", "लग्न"),
    CHART_RASHI("Rashi Chart (D1)", "राशि कुण्डली (D1)"),
    CHART_NAVAMSA("Navamsa Chart (D9)", "नवांश कुण्डली (D9)"),

    // ============================================
    // PLANET DIALOG - SECTION HEADERS
    // ============================================
    DIALOG_POSITION_DETAILS("Position Details", "स्थिति विवरण"),
    DIALOG_ZODIAC_SIGN("Zodiac Sign", "राशि"),
    DIALOG_DEGREE("Degree", "अंश"),
    DIALOG_HOUSE("House", "भाव"),
    DIALOG_NAKSHATRA("Nakshatra", "नक्षत्र"),
    DIALOG_NAKSHATRA_LORD("Nakshatra Lord", "नक्षत्र स्वामी"),
    DIALOG_NAKSHATRA_DEITY("Nakshatra Deity", "नक्षत्र देवता"),
    DIALOG_MOTION("Motion", "गति"),
    DIALOG_RETROGRADE("Retrograde", "वक्री"),
    DIALOG_STRENGTH_ANALYSIS("Strength Analysis (Shadbala)", "बल विश्लेषण (षड्बल)"),
    DIALOG_STRENGTH_BREAKDOWN("Strength Breakdown (Virupas)", "बल विभाजन (विरुपा)"),
    DIALOG_STHANA_BALA("Sthana Bala (Positional)", "स्थान बल"),
    DIALOG_DIG_BALA("Dig Bala (Directional)", "दिग्बल"),
    DIALOG_KALA_BALA("Kala Bala (Temporal)", "काल बल"),
    DIALOG_CHESTA_BALA("Chesta Bala (Motional)", "चेष्टा बल"),
    DIALOG_NAISARGIKA_BALA("Naisargika Bala (Natural)", "नैसर्गिक बल"),
    DIALOG_DRIK_BALA("Drik Bala (Aspectual)", "दृग्बल"),
    DIALOG_BENEFIC("Benefic", "शुभ"),
    DIALOG_MALEFIC("Malefic", "पापी"),
    DIALOG_ELEMENT("Element", "तत्व"),
    DIALOG_REPRESENTS("Represents:", "प्रतिनिधित्व:"),
    DIALOG_BODY_PARTS("Body Parts:", "शरीर अंग:"),
    DIALOG_PROFESSIONS("Professions:", "पेशाहरू:"),
    DIALOG_HOUSE_PLACEMENT("House %d Placement", "भाव %d स्थिति"),
    DIALOG_STATUS_CONDITIONS("Status & Conditions", "स्थिति र अवस्था"),
    DIALOG_DIGNITY("Dignity", "मर्यादा"),
    DIALOG_COMBUSTION("Combustion", "अस्त"),
    COMBUSTION_NOT_COMBUST("Not Combust", "अस्त छैन"),
    COMBUSTION_APPROACHING("Approaching Combustion", "अस्त नजिक"),
    COMBUSTION_COMBUST("Combust", "अस्त"),
    COMBUSTION_DEEP_COMBUST("Deep Combustion", "गहिरो अस्त"),
    COMBUSTION_CAZIMI("Cazimi", "काजिमी"),
    COMBUSTION_SEPARATING("Separating", "विभाजन"),
    DIALOG_PLANETARY_WAR("Planetary War", "ग्रहयुद्ध"),
    DIALOG_AT_WAR_WITH("At war with %s", "%s सँग युद्धमा"),
    DIALOG_INSIGHTS_PREDICTIONS("Insights & Predictions", "अन्तर्दृष्टि र भविष्यवाणी"),
    DIALOG_OVERALL("Overall: %s / %s Rupas", "समग्र: %s / %s रुपा"),
    DIALOG_REQUIRED_STRENGTH("Required", "आवश्यक"),
    DIALOG_PERCENT_OF_REQUIRED("%s%% of required strength", "आवश्यक बलको %s%%"),
    DIALOG_TOTAL_VIRUPAS("Total Virupas", "कुल विरुपा"),

    // House Names
    HOUSE_1_NAME("First House (Lagna)", "पहिलो भाव (लग्न)"),
    HOUSE_2_NAME("Second House (Dhana)", "दोस्रो भाव (धन)"),
    HOUSE_3_NAME("Third House (Sahaja)", "तेस्रो भाव (सहज)"),
    HOUSE_4_NAME("Fourth House (Sukha)", "चौथो भाव (सुख)"),
    HOUSE_5_NAME("Fifth House (Putra)", "पाँचौं भाव (पुत्र)"),
    HOUSE_6_NAME("Sixth House (Ripu)", "छैटौं भाव (रिपु)"),
    HOUSE_7_NAME("Seventh House (Kalatra)", "सातौं भाव (कलत्र)"),
    HOUSE_8_NAME("Eighth House (Ayur)", "आठौं भाव (आयु)"),
    HOUSE_9_NAME("Ninth House (Dharma)", "नवौं भाव (धर्म)"),
    HOUSE_10_NAME("Tenth House (Karma)", "दसौं भाव (कर्म)"),
    HOUSE_11_NAME("Eleventh House (Labha)", "एघारौं भाव (लाभ)"),
    HOUSE_12_NAME("Twelfth House (Vyaya)", "बाह्रौं भाव (व्यय)"),

    // House Signification Descriptions
    HOUSE_1_SIG("Self, Body, Personality", "आत्म, शरीर, व्यक्तित्व"),
    HOUSE_2_SIG("Wealth, Family, Speech", "धन, परिवार, वाणी"),
    HOUSE_3_SIG("Siblings, Courage, Communication", "भाइबहिनी, साहस, सञ्चार"),
    HOUSE_4_SIG("Home, Mother, Happiness", "घर, आमा, सुख"),
    HOUSE_5_SIG("Children, Intelligence, Romance", "सन्तान, बुद्धि, प्रेम"),
    HOUSE_6_SIG("Enemies, Health, Service", "शत्रु, स्वास्थ्य, सेवा"),
    HOUSE_7_SIG("Marriage, Partnerships, Business", "विवाह, साझेदारी, व्यापार"),
    HOUSE_8_SIG("Longevity, Transformation, Occult", "आयु, रूपान्तरण, तन्त्र"),
    HOUSE_9_SIG("Fortune, Dharma, Father", "भाग्य, धर्म, पिता"),
    HOUSE_10_SIG("Career, Status, Public Image", "क्यारियर, पद, सार्वजनिक छवि"),
    HOUSE_11_SIG("Gains, Income, Desires", "लाभ, आय, इच्छाहरू"),
    HOUSE_12_SIG("Losses, Expenses, Liberation", "हानि, खर्च, मोक्ष"),

    // House Types
    HOUSE_TYPE_KENDRA("Kendra (Angular)", "केन्द्र"),
    HOUSE_TYPE_TRIKONA("Trikona (Trine)", "त्रिकोण"),
    HOUSE_TYPE_DUSTHANA("Dusthana (Malefic)", "दुःस्थान"),
    HOUSE_TYPE_UPACHAYA("Upachaya (Growth)", "उपचय"),
    HOUSE_TYPE_MARAKA("Maraka (Death-inflicting)", "मारक"),
    HOUSE_TYPE_PANAPARA("Panapara", "पणफर"),
    HOUSE_TYPE_APOKLIMA("Apoklima", "आपोक्लिम"),

    // Dialog Buttons
    DIALOG_CLOSE("Close", "बन्द गर्नुहोस्"),
    DIALOG_RESET("Reset", "रिसेट"),
    DIALOG_ZOOM_IN("Zoom In", "ठूलो पार्नुहोस्"),
    DIALOG_ZOOM_OUT("Zoom Out", "सानो पार्नुहोस्"),
    DIALOG_DOWNLOAD("Download", "डाउनलोड"),
    DIALOG_SAVING("Saving...", "सेभ गर्दै..."),
    DIALOG_CHART_SAVED("Chart saved to gallery!", "चार्ट ग्यालेरीमा सेभ भयो!"),
    DIALOG_SAVE_FAILED("Failed to save chart", "चार्ट सेभ गर्न असफल"),

    // Nakshatra Dialog
    DIALOG_BASIC_INFO("Basic Information", "आधारभूत जानकारी"),
    DIALOG_NAKSHATRA_NATURE("Nakshatra Nature", "नक्षत्र प्रकृति"),
    DIALOG_PADA_CHARACTERISTICS("Pada %d Characteristics", "पाद %d विशेषताहरू"),
    DIALOG_GENERAL_CHARACTERISTICS("General Characteristics", "सामान्य विशेषताहरू"),
    DIALOG_CAREER_INDICATIONS("Career Indications", "क्यारियर संकेतहरू"),
    DIALOG_NUMBER("Number", "क्रमांक"),
    DIALOG_DEGREE_RANGE("Degree Range", "अंश दायरा"),

    DIALOG_NAVAMSA_SIGN("Navamsa Sign", "नवांश राशि"),
    DIALOG_GENDER("Gender", "लिङ्ग"),

    // House Dialog
    DIALOG_HOUSE_INFO("House Information", "भाव जानकारी"),
    DIALOG_SIGNIFICATIONS("Significations & Nature", "करकत्व र प्रकृति"),
    DIALOG_NATURE("Nature", "प्रकृति"),
    DIALOG_PLANETS_IN_HOUSE("Planets in House", "भावमा ग्रहहरू"),
    DIALOG_DETAILED_INTERPRETATION("Detailed Interpretation", "विस्तृत व्याख्या"),
    DIALOG_CUSP_DEGREE("Cusp Degree", "सन्धि अंश"),
    DIALOG_SIGN_LORD("Sign Lord", "राशि स्वामी"),
    DIALOG_HOUSE_TYPE("House Type", "भाव प्रकार"),

    // Shadbala Dialog
    DIALOG_SHADBALA_ANALYSIS("Shadbala Analysis", "षड्बल विश्लेषण"),
    DIALOG_SIXFOLD_STRENGTH("Six-fold Planetary Strength", "छवटा ग्रह बल"),
    DIALOG_OVERALL_SUMMARY("Overall Summary", "समग्र सारांश"),
    DIALOG_CHART_STRENGTH("Chart Strength", "कुण्डली बल"),
    DIALOG_STRONGEST("Strongest", "सबैभन्दा बलियो"),
    DIALOG_WEAKEST("Weakest", "सबैभन्दा कमजोर"),

    // ============================================
    // DEBUG ACTIVITY - ERROR SCREEN
    // ============================================
    DEBUG_UNHANDLED_EXCEPTION("Unhandled Exception", "अप्रत्याशित त्रुटि"),
    DEBUG_ERROR_OCCURRED("An unexpected error occurred.", "एउटा अप्रत्याशित त्रुटि भयो।"),
    DEBUG_COPY_LOG("Copy Log", "लग कपी गर्नुहोस्"),
    DEBUG_RESTART_APP("Restart App", "एप पुनः सुरु गर्नुहोस्"),
    DEBUG_CRASH_LOG("Crash Log", "क्र्यास लग"),

    // ============================================
    // CHART EXPORTER - PDF STRINGS
    // ============================================
    EXPORT_VEDIC_REPORT("VEDIC BIRTH CHART REPORT", "वैदिक जन्म कुण्डली रिपोर्ट"),
    EXPORT_NAME("Name:", "नाम:"),
    EXPORT_DATE_TIME("Date & Time:", "मिति र समय:"),
    EXPORT_LOCATION("Location:", "स्थान:"),
    EXPORT_COORDINATES("Coordinates:", "निर्देशांक:"),
    EXPORT_PLANETARY_POSITIONS("PLANETARY POSITIONS", "ग्रह स्थितिहरू"),
    EXPORT_ASTRONOMICAL_DATA("ASTRONOMICAL DATA", "खगोलीय तथ्याङ्क"),
    EXPORT_HOUSE_CUSPS("HOUSE CUSPS", "भाव सन्धिहरू"),
    EXPORT_YOGA_ANALYSIS("YOGA ANALYSIS", "योग विश्लेषण"),
    EXPORT_TOTAL_YOGAS("Total Yogas Found:", "कुल योगहरू फेला परेको:"),
    EXPORT_OVERALL_YOGA_STRENGTH("Overall Yoga Strength:", "समग्र योग बल:"),
    EXPORT_KEY_YOGAS("Key Yogas:", "प्रमुख योगहरू:"),
    EXPORT_CHALLENGING_YOGAS("Challenging Yogas:", "चुनौतीपूर्ण योगहरू:"),
    EXPORT_MITIGATED_BY("Mitigated by:", "न्यूनीकरण:"),
    EXPORT_PLANETARY_ASPECTS("PLANETARY ASPECTS", "ग्रह दृष्टिहरू"),
    EXPORT_SHADBALA_ANALYSIS("SHADBALA ANALYSIS", "षड्बल विश्लेषण"),
    EXPORT_OVERALL_CHART_STRENGTH("Overall Chart Strength:", "समग्र कुण्डली बल:"),
    EXPORT_STRONGEST_PLANET("Strongest Planet:", "सबैभन्दा बलियो ग्रह:"),
    EXPORT_WEAKEST_PLANET("Weakest Planet:", "सबैभन्दा कमजोर ग्रह:"),
    EXPORT_STRENGTH_BREAKDOWN("Strength Breakdown:", "बल विभाजन:"),
    EXPORT_ASHTAKAVARGA_ANALYSIS("ASHTAKAVARGA ANALYSIS", "अष्टकवर्ग विश्लेषण"),
    EXPORT_SARVASHTAKAVARGA("Sarvashtakavarga (Combined Strength)", "सर्वाष्टकवर्ग (संयुक्त बल)"),
    EXPORT_BHINNASHTAKAVARGA("Bhinnashtakavarga (Individual Planet Bindus)", "भिन्नाष्टकवर्ग (व्यक्तिगत ग्रह बिन्दु)"),
    EXPORT_TRANSIT_GUIDE("Transit Interpretation Guide:", "गोचर व्याख्या गाइड:"),
    EXPORT_SAV_EXCELLENT("SAV 30+ bindus: Excellent for transits - major positive events", "SAV ३०+ बिन्दु: गोचरको लागि उत्कृष्ट - प्रमुख सकारात्मक घटनाहरू"),
    EXPORT_SAV_GOOD("SAV 28-29 bindus: Good for transits - favorable outcomes", "SAV २८-२९ बिन्दु: गोचरको लागि राम्रो - अनुकूल परिणामहरू"),
    EXPORT_SAV_AVERAGE("SAV 25-27 bindus: Average - mixed results", "SAV २५-२७ बिन्दु: औसत - मिश्रित परिणामहरू"),
    EXPORT_SAV_CHALLENGING("SAV below 25: Challenging - caution advised during transits", "SAV २५ भन्दा कम: चुनौतीपूर्ण - गोचरमा सावधानी आवश्यक"),
    EXPORT_BAV_EXCELLENT("BAV 5+ bindus: Planet transit through this sign is highly beneficial", "BAV ५+ बिन्दु: यस राशिमा ग्रह गोचर अत्यन्त लाभदायक"),
    EXPORT_BAV_GOOD("BAV 4 bindus: Good results from planet transit", "BAV ४ बिन्दु: ग्रह गोचरबाट राम्रो परिणामहरू"),
    EXPORT_BAV_AVERAGE("BAV 3 bindus: Average results", "BAV ३ बिन्दु: औसत परिणामहरू"),
    EXPORT_BAV_CHALLENGING("BAV 0-2 bindus: Difficult transit period for that planet", "BAV ०-२ बिन्दु: त्यो ग्रहको लागि कठिन गोचर अवधि"),
    EXPORT_GENERATED_BY("Generated by AstroStorm - Ultra-Precision Vedic Astrology", "AstroStorm द्वारा उत्पन्न - अति-सटीक वैदिक ज्योतिष"),
    EXPORT_PAGE("Page %d", "पृष्ठ %d"),
    EXPORT_PLANET("Planet", "ग्रह"),
    EXPORT_SIGN("Sign", "राशि"),
    EXPORT_DEGREE("Degree", "अंश"),
    EXPORT_NAKSHATRA("Nakshatra", "नक्षत्र"),
    EXPORT_PADA("Pada", "पाद"),
    EXPORT_HOUSE("House", "भाव"),
    EXPORT_STATUS("Status", "स्थिति"),
    EXPORT_TOTAL_RUPAS("Total Rupas", "कुल रुपा"),
    EXPORT_REQUIRED("Required", "आवश्यक"),
    EXPORT_PERCENT("%", "प्रतिशत"),
    EXPORT_RATING("Rating", "मूल्याङ्कन"),
    EXPORT_VIRUPAS("virupas", "विरुपा"),

    // Birth Chart Summary Labels
    EXPORT_BIRTH_INFO("BIRTH INFORMATION", "जन्म जानकारी"),
    EXPORT_CHART_SUMMARY("CHART SUMMARY", "कुण्डली सारांश"),
    EXPORT_ASCENDANT_LAGNA("Ascendant (Lagna):", "लग्न:"),
    EXPORT_MOON_SIGN_RASHI("Moon Sign (Rashi):", "चन्द्र राशि:"),
    EXPORT_SUN_SIGN("Sun Sign:", "सूर्य राशि:"),
    EXPORT_BIRTH_NAKSHATRA("Birth Nakshatra:", "जन्म नक्षत्र:"),
    EXPORT_TIMEZONE("Timezone:", "समय क्षेत्र:"),

    // Text Report Footer
    EXPORT_CALC_ENGINE("Calculation Engine: Swiss Ephemeris (JPL Mode)", "गणना इन्जिन: स्विस ईफेमेरिस (JPL मोड)"),
    EXPORT_ULTRA_PRECISION("Ultra-Precision Vedic Astrology Software", "अति-सटीक वैदिक ज्योतिष सफ्टवेयर"),
    EXPORT_GENERATED_BY_SHORT("Generated by AstroStorm", "AstroStorm द्वारा उत्पन्न"),
    EXPORT_GENERATED("Generated", "उत्पन्न"),
    EXPORT_MOON_SIGN("Moon Sign (Rashi)", "चन्द्र राशि"),

    // ============================================
    // PREDICTION TYPES
    // ============================================
    PREDICTION_STRONG_PLANET("Strong %s", "बलियो %s"),
    PREDICTION_STRONG_DESC("This planet has sufficient strength to deliver positive results. Its significations will manifest more easily in your life.", "यो ग्रहसँग सकारात्मक परिणामहरू दिनको लागि पर्याप्त बल छ। यसको करकत्वहरू तपाईंको जीवनमा सजिलैसँग प्रकट हुनेछन्।"),
    PREDICTION_WEAK_PLANET("Weak %s", "कमजोर %s"),
    PREDICTION_WEAK_DESC("This planet lacks sufficient strength. You may face challenges in areas it governs. Remedial measures may help.", "यो ग्रहमा पर्याप्त बल छैन। तपाईंले यसले शासन गर्ने क्षेत्रहरूमा चुनौतीहरू सामना गर्न सक्नुहुन्छ। उपाय उपायहरूले मद्दत गर्न सक्छ।"),
    PREDICTION_EXALTED("Exalted Planet", "उच्च ग्रह"),
    PREDICTION_EXALTED_DESC("%s is in its sign of exaltation, giving exceptional results in its significations.", "%s आफ्नो उच्च राशिमा छ, यसको करकत्वमा असाधारण परिणामहरू दिँदै।"),
    PREDICTION_DEBILITATED("Debilitated Planet", "नीच ग्रह"),
    PREDICTION_DEBILITATED_DESC("%s is in its fall. Its positive significations may be reduced or delayed.", "%s आफ्नो नीच राशिमा छ। यसको सकारात्मक करकत्वहरू कम वा ढिलो हुन सक्छन्।"),
    PREDICTION_OWN_SIGN("Planet in Own Sign", "स्वराशिमा ग्रह"),
    PREDICTION_OWN_SIGN_DESC("%s is comfortable in its own sign, giving stable and reliable results.", "%s आफ्नै राशिमा सहज छ, स्थिर र भरपर्दो परिणामहरू दिँदै।"),
    PREDICTION_RETROGRADE("Retrograde Motion", "वक्री गति"),
    PREDICTION_RETROGRADE_DESC("Retrograde planets work on an internal level. Results may be delayed but often more profound.", "वक्री ग्रहहरू आन्तरिक स्तरमा काम गर्छन्। परिणामहरू ढिलो हुन सक्छन् तर प्रायः अधिक गहिरो हुन्छन्।"),
    PREDICTION_TRIKONA("Trikona Placement", "त्रिकोण स्थिति"),
    PREDICTION_TRIKONA_DESC("%s in house %d (Trikona) is auspicious for fortune and dharma.", "भाव %d (त्रिकोण) मा %s भाग्य र धर्मको लागि शुभ छ।"),
    PREDICTION_DUSTHANA("Dusthana Placement", "दुःस्थान स्थिति"),
    PREDICTION_DUSTHANA_DESC("%s in house %d may face obstacles but can also give transformative experiences.", "भाव %d मा %s ले बाधाहरू सामना गर्न सक्छ तर रूपान्तरणकारी अनुभवहरू पनि दिन सक्छ।"),
    PREDICTION_KENDRA("Kendra Placement", "केन्द्र स्थिति"),
    PREDICTION_KENDRA_DESC("%s in house %d (Kendra) gains strength and visibility.", "भाव %d (केन्द्र) मा %s ले बल र दृश्यता प्राप्त गर्दछ।"),

    // ============================================
    // VARSHAPHALA - TAJIKA ASPECT TYPES
    // ============================================
    TAJIKA_ITHASALA("Ithasala", "इतशाल"),
    TAJIKA_ITHASALA_DESC("Applying aspect - promises fulfillment of matters", "निकटवर्ती पक्ष - कार्यहरू पूर्ण हुने वाचा"),
    TAJIKA_EASARAPHA("Easarapha", "इसराफ"),
    TAJIKA_EASARAPHA_DESC("Separating aspect - event has passed or is fading", "विलग पक्ष - घटना बितिसकेको वा क्षीण हुँदैछ"),
    TAJIKA_NAKTA("Nakta", "नक्त"),
    TAJIKA_NAKTA_DESC("Transmission of light with reception - indirect completion", "ग्रहणसहित प्रकाश प्रसारण - अप्रत्यक्ष पूर्णता"),
    TAJIKA_YAMAYA("Yamaya", "यमाया"),
    TAJIKA_YAMAYA_DESC("Translation of light - third planet connects significators", "प्रकाश अनुवाद - तेस्रो ग्रहले कारकहरू जोड्दछ"),
    TAJIKA_MANAU("Manau", "मनौ"),
    TAJIKA_MANAU_DESC("Reverse application - slower planet applies to faster", "उल्टो प्रयोग - ढिलो ग्रहले छिटोमा प्रयोग गर्छ"),
    TAJIKA_KAMBOOLA("Kamboola", "कम्बूल"),
    TAJIKA_KAMBOOLA_DESC("Powerful Ithasala with angular placement", "केन्द्र स्थानसहित शक्तिशाली इतशाल"),
    TAJIKA_GAIRI_KAMBOOLA("Gairi-Kamboola", "गैरी-कम्बूल"),
    TAJIKA_GAIRI_KAMBOOLA_DESC("Weaker form of Kamboola", "कम्बूलको कमजोर रूप"),
    TAJIKA_KHALASARA("Khalasara", "खलासर"),
    TAJIKA_KHALASARA_DESC("Mutual separation - dissolution of matters", "पारस्परिक विलगता - कार्यहरूको विघटन"),
    TAJIKA_RADDA("Radda", "रद्द"),
    TAJIKA_RADDA_DESC("Refranation - retrograde breaks the aspect", "भंग - वक्री गतिले पक्ष तोड्छ"),
    TAJIKA_DUHPHALI_KUTTHA("Duhphali-Kuttha", "दुःफली-कुट्ठ"),
    TAJIKA_DUHPHALI_KUTTHA_DESC("Malefic intervention prevents completion", "पापग्रह हस्तक्षेपले पूर्णता रोक्छ"),
    TAJIKA_TAMBIRA("Tambira", "तम्बीर"),
    TAJIKA_TAMBIRA_DESC("Indirect aspect through intermediary", "मध्यस्थ मार्फत अप्रत्यक्ष पक्ष"),
    TAJIKA_KUTTHA("Kuttha", "कुट्ठ"),
    TAJIKA_KUTTHA_DESC("Impediment to aspect completion", "पक्ष पूर्णतामा बाधा"),
    TAJIKA_DURAPHA("Durapha", "दुराफ"),
    TAJIKA_DURAPHA_DESC("Hard aspect causing difficulties", "कठिनाइहरू निम्त्याउने कडा पक्ष"),
    TAJIKA_MUTHASHILA("Muthashila", "मुथशिल"),
    TAJIKA_MUTHASHILA_DESC("Mutual application between planets", "ग्रहहरू बीच पारस्परिक प्रयोग"),
    TAJIKA_IKKABALA("Ikkabala", "इक्कबल"),
    TAJIKA_IKKABALA_DESC("Unity of strength between planets", "ग्रहहरू बीच बलको एकता"),

    // ============================================
    // VARSHAPHALA - ASPECT STRENGTH
    // ============================================
    ASPECT_VERY_STRONG("Very Strong", "अति बलियो"),
    ASPECT_STRONG("Strong", "बलियो"),
    ASPECT_MODERATE("Moderate", "मध्यम"),
    ASPECT_WEAK("Weak", "कमजोर"),
    ASPECT_VERY_WEAK("Very Weak", "अति कमजोर"),

    // ============================================
    // VARSHAPHALA - SAHAM TYPES
    // ============================================
    SAHAM_PUNYA("Fortune", "पुण्य"),
    SAHAM_PUNYA_SANSKRIT("Punya Saham", "पुण्य सहम"),
    SAHAM_PUNYA_DESC("Overall luck and prosperity", "समग्र भाग्य र समृद्धि"),
    SAHAM_VIDYA("Education", "विद्या"),
    SAHAM_VIDYA_SANSKRIT("Vidya Saham", "विद्या सहम"),
    SAHAM_VIDYA_DESC("Learning and knowledge", "सिकाइ र ज्ञान"),
    SAHAM_YASHAS("Fame", "यश"),
    SAHAM_YASHAS_SANSKRIT("Yashas Saham", "यश सहम"),
    SAHAM_YASHAS_DESC("Reputation and recognition", "प्रतिष्ठा र मान्यता"),
    SAHAM_MITRA("Friends", "मित्र"),
    SAHAM_MITRA_SANSKRIT("Mitra Saham", "मित्र सहम"),
    SAHAM_MITRA_DESC("Friendship and alliances", "मित्रता र गठबन्धन"),
    SAHAM_MAHATMYA("Greatness", "महात्म्य"),
    SAHAM_MAHATMYA_SANSKRIT("Mahatmya Saham", "महात्म्य सहम"),
    SAHAM_MAHATMYA_DESC("Spiritual achievement", "आध्यात्मिक उपलब्धि"),
    SAHAM_ASHA("Hope", "आशा"),
    SAHAM_ASHA_SANSKRIT("Asha Saham", "आशा सहम"),
    SAHAM_ASHA_DESC("Aspirations and wishes", "आकांक्षा र इच्छाहरू"),
    SAHAM_SAMARTHA("Capability", "समर्थ"),
    SAHAM_SAMARTHA_SANSKRIT("Samartha Saham", "समर्थ सहम"),
    SAHAM_SAMARTHA_DESC("Ability and competence", "क्षमता र योग्यता"),
    SAHAM_BHRATRI("Siblings", "भ्रातृ"),
    SAHAM_BHRATRI_SANSKRIT("Bhratri Saham", "भ्रातृ सहम"),
    SAHAM_BHRATRI_DESC("Brothers and sisters", "दाजुभाइ र दिदीबहिनी"),
    SAHAM_PITRI("Father", "पितृ"),
    SAHAM_PITRI_SANSKRIT("Pitri Saham", "पितृ सहम"),
    SAHAM_PITRI_DESC("Father's welfare", "बुबाको कल्याण"),
    SAHAM_MATRI("Mother", "मातृ"),
    SAHAM_MATRI_SANSKRIT("Matri Saham", "मातृ सहम"),
    SAHAM_MATRI_DESC("Mother's welfare", "आमाको कल्याण"),
    SAHAM_PUTRA("Children", "पुत्र"),
    SAHAM_PUTRA_SANSKRIT("Putra Saham", "पुत्र सहम"),
    SAHAM_PUTRA_DESC("Offspring and progeny", "सन्तान"),
    SAHAM_VIVAHA("Marriage", "विवाह"),
    SAHAM_VIVAHA_SANSKRIT("Vivaha Saham", "विवाह सहम"),
    SAHAM_VIVAHA_DESC("Matrimony and partnership", "विवाह र साझेदारी"),
    SAHAM_KARMA("Career", "कर्म"),
    SAHAM_KARMA_SANSKRIT("Karma Saham", "कर्म सहम"),
    SAHAM_KARMA_DESC("Profession and livelihood", "पेशा र जीविका"),
    SAHAM_ROGA("Disease", "रोग"),
    SAHAM_ROGA_SANSKRIT("Roga Saham", "रोग सहम"),
    SAHAM_ROGA_DESC("Health challenges", "स्वास्थ्य चुनौतीहरू"),
    SAHAM_MRITYU("Longevity", "मृत्यु"),
    SAHAM_MRITYU_SANSKRIT("Mrityu Saham", "मृत्यु सहम"),
    SAHAM_MRITYU_DESC("Life span indicators", "जीवन अवधि संकेतकहरू"),
    SAHAM_PARADESA("Foreign", "परदेश"),
    SAHAM_PARADESA_SANSKRIT("Paradesa Saham", "परदेश सहम"),
    SAHAM_PARADESA_DESC("Travel and foreign lands", "यात्रा र विदेश"),
    SAHAM_DHANA("Wealth", "धन"),
    SAHAM_DHANA_SANSKRIT("Dhana Saham", "धन सहम"),
    SAHAM_DHANA_DESC("Financial prosperity", "आर्थिक समृद्धि"),
    SAHAM_RAJA("Power", "राज"),
    SAHAM_RAJA_SANSKRIT("Raja Saham", "राज सहम"),
    SAHAM_RAJA_DESC("Authority and position", "अधिकार र पद"),
    SAHAM_BANDHANA("Bondage", "बन्धन"),
    SAHAM_BANDHANA_SANSKRIT("Bandhana Saham", "बन्धन सहम"),
    SAHAM_BANDHANA_DESC("Restrictions and obstacles", "प्रतिबन्ध र बाधाहरू"),
    SAHAM_KARYASIDDHI_TYPE("Success", "कार्यसिद्धि"),
    SAHAM_KARYASIDDHI_TYPE_SANSKRIT("Karyasiddhi Saham", "कार्यसिद्धि सहम"),
    SAHAM_KARYASIDDHI_TYPE_DESC("Accomplishment of goals", "लक्ष्य प्राप्ति"),

    // ============================================
    // VARSHAPHALA - KEY DATE TYPES
    // ============================================
    KEY_DATE_FAVORABLE("Favorable", "अनुकूल"),
    KEY_DATE_CHALLENGING("Challenging", "चुनौतीपूर्ण"),
    KEY_DATE_IMPORTANT("Important", "महत्त्वपूर्ण"),
    KEY_DATE_TRANSIT("Transit", "गोचर"),

    // ============================================
    // VARSHAPHALA - UI STRINGS
    // ============================================
    VARSHAPHALA_NO_CHART("No Chart Selected", "कुनै कुण्डली छानिएको छैन"),
    VARSHAPHALA_NO_CHART_DESC("Select a birth chart to view Varshaphala", "वर्षफल हेर्न जन्म कुण्डली छान्नुहोस्"),
    VARSHAPHALA_SUN_RETURNS("Sun returns to natal position", "सूर्य जन्मकालीन स्थानमा फर्कन्छ"),
    VARSHAPHALA_RETURN_DATE("Return Date", "प्रतिफल मिति"),
    VARSHAPHALA_AGE_FORMAT("Age %d", "उमेर %d"),
    VARSHAPHALA_IN_HOUSE("House %d", "भाव %d"),
    VARSHAPHALA_FIVEFOLD_STRENGTH("Five-fold Planetary Strength", "पञ्चवर्गीय ग्रह बल"),
    VARSHAPHALA_THREE_FLAG("Three-flag Diagram", "त्रिपताकी चक्र"),
    VARSHAPHALA_IMPORTANT_DATES("%d important dates", "%d महत्त्वपूर्ण मितिहरू"),
    VARSHAPHALA_CHART_LEGEND_ASC("Asc", "लग्न"),
    VARSHAPHALA_CHART_LEGEND_MUNTHA("Muntha", "मुन्थ"),
    VARSHAPHALA_CHART_LEGEND_BENEFIC("Benefic", "शुभ"),
    VARSHAPHALA_CHART_LEGEND_MALEFIC("Malefic", "पाप"),
    VARSHAPHALA_CHALLENGING("Challenging", "चुनौतीपूर्ण"),
    VARSHAPHALA_TOTAL("Total", "कुल"),
    VARSHAPHALA_ACTIVE("Active", "सक्रिय"),
    VARSHAPHALA_CURRENT("CURRENT", "वर्तमान"),
    VARSHAPHALA_DAYS_FORMAT("%d days", "%d दिन"),
    VARSHAPHALA_HOUSE_SIGN("House %d - %s", "भाव %d - %s"),
    VARSHAPHALA_LORD_IN_HOUSE("Lord: %s in H%d", "स्वामी: %s भाव%d मा"),
    VARSHAPHALA_RULES_HOUSES("Rules Houses: %s", "भावहरू शासन: %s"),
    VARSHAPHALA_PLANETS("Planets: ", "ग्रहहरू: "),
    VARSHAPHALA_NO_PLANETS("No planets", "कुनै ग्रह छैन"),
    VARSHAPHALA_HOUSES_PREFIX("Houses: ", "भावहरू: "),
    VARSHAPHALA_SPECIFIC_INDICATIONS("Specific Indications:", "विशिष्ट संकेतहरू:"),
    VARSHAPHALA_LORD_PREFIX("Lord: %s", "स्वामी: %s"),

    // ============================================
    // VARSHAPHALA - MUNTHA THEMES
    // ============================================
    MUNTHA_PERSONAL_GROWTH("Personal Growth", "व्यक्तिगत विकास"),
    MUNTHA_NEW_BEGINNINGS("New Beginnings", "नयाँ शुरुवातहरू"),
    MUNTHA_HEALTH_FOCUS("Health Focus", "स्वास्थ्य फोकस"),
    MUNTHA_FINANCIAL_GAINS("Financial Gains", "आर्थिक लाभ"),
    MUNTHA_FAMILY_MATTERS("Family Matters", "पारिवारिक मामिलाहरू"),
    MUNTHA_SPEECH("Speech", "वाणी"),
    MUNTHA_COMMUNICATION("Communication", "सञ्चार"),
    MUNTHA_SHORT_TRAVELS("Short Travels", "छोटो यात्राहरू"),
    MUNTHA_SIBLINGS("Siblings", "भाइबहिनी"),
    MUNTHA_HOME_AFFAIRS("Home Affairs", "घरेलु मामिलाहरू"),
    MUNTHA_PROPERTY("Property", "सम्पत्ति"),
    MUNTHA_INNER_PEACE("Inner Peace", "आन्तरिक शान्ति"),
    MUNTHA_CREATIVITY("Creativity", "सिर्जनशीलता"),
    MUNTHA_ROMANCE("Romance", "रोमान्स"),
    MUNTHA_CHILDREN("Children", "सन्तान"),
    MUNTHA_SERVICE("Service", "सेवा"),
    MUNTHA_HEALTH_ISSUES("Health Issues", "स्वास्थ्य समस्याहरू"),
    MUNTHA_COMPETITION("Competition", "प्रतिस्पर्धा"),
    MUNTHA_PARTNERSHIPS("Partnerships", "साझेदारीहरू"),
    MUNTHA_MARRIAGE("Marriage", "विवाह"),
    MUNTHA_BUSINESS("Business", "व्यापार"),
    MUNTHA_TRANSFORMATION("Transformation", "रूपान्तरण"),
    MUNTHA_RESEARCH("Research", "अनुसन्धान"),
    MUNTHA_INHERITANCE("Inheritance", "विरासत"),
    MUNTHA_FORTUNE("Fortune", "भाग्य"),
    MUNTHA_LONG_TRAVEL("Long Travel", "लामो यात्रा"),
    MUNTHA_HIGHER_LEARNING("Higher Learning", "उच्च शिक्षा"),
    MUNTHA_CAREER_ADVANCEMENT("Career Advancement", "क्यारियर प्रगति"),
    MUNTHA_RECOGNITION("Recognition", "मान्यता"),
    MUNTHA_AUTHORITY("Authority", "अधिकार"),
    MUNTHA_GAINS("Gains", "लाभ"),
    MUNTHA_FRIENDS("Friends", "मित्रहरू"),
    MUNTHA_FULFILLED_WISHES("Fulfilled Wishes", "पूरा भएका इच्छाहरू"),
    MUNTHA_SPIRITUALITY("Spirituality", "आध्यात्मिकता"),
    MUNTHA_FOREIGN_LANDS("Foreign Lands", "विदेश"),
    MUNTHA_EXPENSES("Expenses", "खर्चहरू"),
    MUNTHA_GENERAL_GROWTH("General Growth", "सामान्य विकास"),

    // ============================================
    // VARSHAPHALA - HOUSE SIGNIFICATIONS
    // ============================================
    VARSHA_HOUSE_1_SIG("personal development and health", "व्यक्तिगत विकास र स्वास्थ्य"),
    VARSHA_HOUSE_2_SIG("finances and family", "वित्त र परिवार"),
    VARSHA_HOUSE_3_SIG("communication and siblings", "सञ्चार र भाइबहिनी"),
    VARSHA_HOUSE_4_SIG("home and property", "घर र सम्पत्ति"),
    VARSHA_HOUSE_5_SIG("creativity and children", "सिर्जनशीलता र सन्तान"),
    VARSHA_HOUSE_6_SIG("health and service", "स्वास्थ्य र सेवा"),
    VARSHA_HOUSE_7_SIG("partnerships and marriage", "साझेदारी र विवाह"),
    VARSHA_HOUSE_8_SIG("transformation and inheritance", "रूपान्तरण र विरासत"),
    VARSHA_HOUSE_9_SIG("fortune and higher learning", "भाग्य र उच्च शिक्षा"),
    VARSHA_HOUSE_10_SIG("career and status", "क्यारियर र स्थिति"),
    VARSHA_HOUSE_11_SIG("gains and friendships", "लाभ र मित्रता"),
    VARSHA_HOUSE_12_SIG("spirituality and foreign matters", "आध्यात्मिकता र विदेशी मामिलाहरू"),
    VARSHA_HOUSE_VARIOUS("various life areas", "विभिन्न जीवन क्षेत्रहरू"),

    // ============================================
    // VARSHAPHALA - PLANET PERIOD PREDICTIONS
    // ============================================
    VARSHA_SUN_NATURE("vitality, authority, and self-expression", "जीवनशक्ति, अधिकार र आत्म-अभिव्यक्ति"),
    VARSHA_MOON_NATURE("emotions, nurturing, and public connections", "भावनाहरू, पालनपोषण र सार्वजनिक सम्बन्धहरू"),
    VARSHA_MARS_NATURE("energy, initiative, and competitive drive", "ऊर्जा, पहल र प्रतिस्पर्धात्मक उत्प्रेरणा"),
    VARSHA_MERCURY_NATURE("communication, learning, and business", "सञ्चार, सिकाइ र व्यापार"),
    VARSHA_JUPITER_NATURE("wisdom, expansion, and good fortune", "बुद्धि, विस्तार र सुभाग्य"),
    VARSHA_VENUS_NATURE("relationships, creativity, and pleasures", "सम्बन्धहरू, सिर्जनशीलता र आनन्द"),
    VARSHA_SATURN_NATURE("discipline, responsibility, and long-term goals", "अनुशासन, जिम्मेवारी र दीर्घकालीन लक्ष्यहरू"),
    VARSHA_RAHU_NATURE("ambition, innovation, and unconventional paths", "महत्त्वाकांक्षा, नवीनता र अपरम्परागत मार्गहरू"),
    VARSHA_KETU_NATURE("spirituality, detachment, and past karma", "आध्यात्मिकता, वैराग्य र पूर्व कर्म"),
    VARSHA_GENERAL_NATURE("general influences", "सामान्य प्रभावहरू"),

    // ============================================
    // VARSHAPHALA - STRENGTH DESCRIPTIONS
    // ============================================
    VARSHA_STRENGTH_EXALTED("Exalted", "उच्च"),
    VARSHA_STRENGTH_STRONG("Strong", "बलियो"),
    VARSHA_STRENGTH_MODERATE("Moderate", "मध्यम"),
    VARSHA_STRENGTH_ANGULAR("Angular", "केन्द्रीय"),
    VARSHA_STRENGTH_RETROGRADE("Retrograde", "वक्री"),
    VARSHA_STRENGTH_DEBILITATED("Debilitated", "नीच"),
    VARSHA_STRENGTH_UNKNOWN("Unknown", "अज्ञात"),

    // ============================================
    // VARSHAPHALA - PREDICTION PHRASES
    // ============================================
    VARSHA_PERIOD_EXCEPTIONAL("This period promises exceptional results", "यो अवधिले असाधारण परिणामहरूको वाचा गर्छ"),
    VARSHA_PERIOD_WELL_SUPPORTED("This period is well-supported for success", "यो अवधि सफलताको लागि राम्ररी समर्थित छ"),
    VARSHA_PERIOD_EXTRA_EFFORT("This period requires extra effort and patience", "यो अवधिमा थप प्रयास र धैर्य चाहिन्छ"),
    VARSHA_PERIOD_MIXED("This period brings mixed but manageable influences", "यो अवधिले मिश्रित तर व्यवस्थापनयोग्य प्रभावहरू ल्याउँछ"),
    VARSHA_DURING_PERIOD("During this %s period, focus shifts to %s, particularly affecting %s. %s.", "यो %s अवधिमा, %s मा फोकस सर्छ, विशेष गरी %s लाई असर गर्दछ। %s।"),

    // ============================================
    // VARSHAPHALA - YEAR LORD DESCRIPTIONS
    // ============================================
    VARSHA_YEARLORD_SUN("Year Lord Sun brings focus on leadership, authority, and self-expression.", "वर्ष स्वामी सूर्यले नेतृत्व, अधिकार र आत्म-अभिव्यक्तिमा फोकस ल्याउँछ।"),
    VARSHA_YEARLORD_MOON("Year Lord Moon emphasizes emotional wellbeing and public connections.", "वर्ष स्वामी चन्द्रमाले भावनात्मक कल्याण र सार्वजनिक सम्बन्धहरूमा जोड दिन्छ।"),
    VARSHA_YEARLORD_MARS("Year Lord Mars energizes initiatives and competitive endeavors.", "वर्ष स्वामी मंगलले पहलहरू र प्रतिस्पर्धात्मक प्रयासहरूलाई ऊर्जित गर्छ।"),
    VARSHA_YEARLORD_MERCURY("Year Lord Mercury enhances communication and business activities.", "वर्ष स्वामी बुधले सञ्चार र व्यापारिक गतिविधिहरू बढाउँछ।"),
    VARSHA_YEARLORD_JUPITER("Year Lord Jupiter bestows wisdom, expansion, and good fortune.", "वर्ष स्वामी बृहस्पतिले बुद्धि, विस्तार र सुभाग्य प्रदान गर्छ।"),
    VARSHA_YEARLORD_VENUS("Year Lord Venus brings harmony to relationships and creativity.", "वर्ष स्वामी शुक्रले सम्बन्धहरू र सिर्जनशीलतामा सामञ्जस्य ल्याउँछ।"),
    VARSHA_YEARLORD_SATURN("Year Lord Saturn teaches discipline and responsibility.", "वर्ष स्वामी शनिले अनुशासन र जिम्मेवारी सिकाउँछ।"),
    VARSHA_YEARLORD_GENERIC("The Year Lord influences various aspects with balanced energy.", "वर्ष स्वामीले सन्तुलित ऊर्जासहित विभिन्न पक्षहरूलाई प्रभाव पार्छ।"),

    // ============================================
    // VARSHAPHALA - TRI-PATAKI CHAKRA
    // ============================================
    TRIPATAKI_DHARMA("Dharma (1, 5, 9)", "धर्म (१, ५, ९)"),
    TRIPATAKI_ARTHA("Artha (2, 6, 10)", "अर्थ (२, ६, १०)"),
    TRIPATAKI_KAMA("Kama (3, 7, 11)", "काम (३, ७, ११)"),
    TRIPATAKI_DHARMA_DOMINANT("Spiritual growth and righteous pursuits dominate", "आध्यात्मिक वृद्धि र धार्मिक खोजीहरू प्रभावी"),
    TRIPATAKI_ARTHA_DOMINANT("Material prosperity and career emphasis", "भौतिक समृद्धि र क्यारियर जोड"),
    TRIPATAKI_KAMA_DOMINANT("Relationships and desires take center stage", "सम्बन्धहरू र इच्छाहरू केन्द्रमा"),
    TRIPATAKI_BALANCED("Balanced influences across all areas", "सबै क्षेत्रहरूमा सन्तुलित प्रभाव"),
    TRIPATAKI_NO_PLANETS("No planets in %s sector - quieter year for these matters.", "%s क्षेत्रमा कुनै ग्रह छैन - यी मामिलाहरूको लागि शान्त वर्ष।"),
    TRIPATAKI_BENEFIC_INFLUENCE("Benefic %s bring favorable influences.", "शुभ %s ले अनुकूल प्रभावहरू ल्याउँछ।"),
    TRIPATAKI_MALEFIC_INFLUENCE("Malefic %s bring challenges requiring effort.", "पाप %s ले प्रयास चाहिने चुनौतीहरू ल्याउँछ।"),
    TRIPATAKI_MIXED_INFLUENCE("Mixed influences suggest variable results.", "मिश्रित प्रभावहरूले परिवर्तनशील परिणामहरू सुझाव दिन्छ।"),
    TRIPATAKI_DHARMA_AREA("righteousness, fortune, and higher learning", "धार्मिकता, भाग्य र उच्च शिक्षा"),
    TRIPATAKI_ARTHA_AREA("wealth, career, and practical achievements", "धन, क्यारियर र व्यावहारिक उपलब्धिहरू"),
    TRIPATAKI_KAMA_AREA("relationships, desires, and social connections", "सम्बन्धहरू, इच्छाहरू र सामाजिक सम्बन्धहरू"),
    TRIPATAKI_PLANETS_IN_TRIKONA("%d planet(s) in %s trikona emphasizes %s.", "%s त्रिकोणमा %d ग्रह(हरू)ले %s मा जोड दिन्छ।"),
    TRIPATAKI_BALANCED_DISTRIBUTION("Balanced distribution of planetary energies across all life sectors.", "सबै जीवन क्षेत्रहरूमा ग्रह ऊर्जाहरूको सन्तुलित वितरण।"),

    // ============================================
    // VARSHAPHALA - OVERALL PREDICTION TONES
    // ============================================
    VARSHA_TONE_EXCELLENT("excellent", "उत्कृष्ट"),
    VARSHA_TONE_FAVORABLE("favorable", "अनुकूल"),
    VARSHA_TONE_POSITIVE("positive", "सकारात्मक"),
    VARSHA_TONE_CHALLENGING("challenging but growth-oriented", "चुनौतीपूर्ण तर विकासोन्मुख"),
    VARSHA_TONE_BALANCED("balanced", "सन्तुलित"),

    // ============================================
    // VARSHAPHALA - PANCHA VARGIYA BALA CATEGORIES
    // ============================================
    PANCHA_EXCELLENT("Excellent", "उत्कृष्ट"),
    PANCHA_GOOD("Good", "राम्रो"),
    PANCHA_AVERAGE("Average", "औसत"),
    PANCHA_BELOW_AVERAGE("Below Average", "औसतमुनि"),
    PANCHA_WEAK("Weak", "कमजोर"),

    // ============================================
    // VARSHAPHALA - DASHA KEYWORDS
    // ============================================
    DASHA_KW_LEADERSHIP("Leadership", "नेतृत्व"),
    DASHA_KW_VITALITY("Vitality", "जीवनशक्ति"),
    DASHA_KW_FATHER("Father", "बुबा"),
    DASHA_KW_EMOTIONS("Emotions", "भावनाहरू"),
    DASHA_KW_MOTHER("Mother", "आमा"),
    DASHA_KW_PUBLIC("Public", "सार्वजनिक"),
    DASHA_KW_ACTION("Action", "कार्य"),
    DASHA_KW_ENERGY("Energy", "ऊर्जा"),
    DASHA_KW_COURAGE("Courage", "साहस"),
    DASHA_KW_COMMUNICATION("Communication", "सञ्चार"),
    DASHA_KW_LEARNING("Learning", "सिकाइ"),
    DASHA_KW_WISDOM("Wisdom", "बुद्धि"),
    DASHA_KW_GROWTH("Growth", "वृद्धि"),
    DASHA_KW_LOVE("Love", "प्रेम"),
    DASHA_KW_ART("Art", "कला"),
    DASHA_KW_COMFORT("Comfort", "सुविधा"),
    DASHA_KW_DISCIPLINE("Discipline", "अनुशासन"),
    DASHA_KW_KARMA("Karma", "कर्म"),
    DASHA_KW_DELAYS("Delays", "ढिलाइ"),
    DASHA_KW_AMBITION("Ambition", "महत्त्वाकांक्षा"),
    DASHA_KW_INNOVATION("Innovation", "नवीनता"),
    DASHA_KW_FOREIGN("Foreign", "विदेशी"),
    DASHA_KW_DETACHMENT("Detachment", "वैराग्य"),
    DASHA_KW_PAST("Past", "भूतकाल"),
    DASHA_KW_GENERAL("General", "सामान्य"),
    DASHA_KW_SELF("Self", "आत्म"),
    DASHA_KW_BODY("Body", "शरीर"),
    DASHA_KW_WEALTH("Wealth", "धन"),
    DASHA_KW_SPEECH("Speech", "वाणी"),
    DASHA_KW_HOME("Home", "घर"),
    DASHA_KW_PEACE("Peace", "शान्ति"),
    DASHA_KW_CHILDREN("Children", "सन्तान"),
    DASHA_KW_HEALTH("Health", "स्वास्थ्य"),
    DASHA_KW_SERVICE("Service", "सेवा"),
    DASHA_KW_MARRIAGE("Marriage", "विवाह"),
    DASHA_KW_BUSINESS("Business", "व्यापार"),
    DASHA_KW_TRANSFORMATION("Transformation", "रूपान्तरण"),
    DASHA_KW_RESEARCH("Research", "अनुसन्धान"),
    DASHA_KW_LUCK("Luck", "भाग्य"),
    DASHA_KW_TRAVEL("Travel", "यात्रा"),
    DASHA_KW_CAREER("Career", "क्यारियर"),
    DASHA_KW_STATUS("Status", "स्थिति"),
    DASHA_KW_GAINS("Gains", "लाभ"),
    DASHA_KW_FRIENDS("Friends", "मित्रहरू"),
    DASHA_KW_LOSSES("Losses", "हानि"),

    // ============================================
    // VARSHAPHALA - HOUSE PREDICTION PHRASES
    // ============================================
    VARSHA_LORD_EXCELLENT("is excellently placed for positive outcomes.", "सकारात्मक परिणामहरूको लागि उत्कृष्ट रूपमा स्थित छ।"),
    VARSHA_LORD_WELL_POSITIONED("is well-positioned for success.", "सफलताको लागि राम्ररी स्थित छ।"),
    VARSHA_LORD_MODERATE_SUPPORT("provides moderate support.", "मध्यम समर्थन प्रदान गर्दछ।"),
    VARSHA_LORD_CHALLENGES("faces challenges requiring attention.", "ध्यान चाहिने चुनौतीहरूको सामना गर्दछ।"),
    VARSHA_LORD_VARIABLE("influences results variably.", "परिणामहरूलाई परिवर्तनशील रूपमा प्रभाव पार्छ।"),
    VARSHA_BENEFICS_ENHANCE(" %s enhance positive outcomes.", " %s ले सकारात्मक परिणामहरू बढाउँछ।"),
    VARSHA_MALEFICS_CHALLENGE(" %s may bring challenges.", " %s ले चुनौतीहरू ल्याउन सक्छ।"),
    VARSHA_MIXED_PLANETS(" Mixed influences from %s.", " %s बाट मिश्रित प्रभावहरू।"),
    VARSHA_DEPENDS_LORD(" Results depend primarily on the lord's position.", " परिणामहरू मुख्यतया स्वामीको स्थितिमा निर्भर छन्।"),
    VARSHA_MUNTHA_EMPHASIS(" Muntha emphasizes these matters this year.", " मुन्थले यस वर्ष यी मामिलाहरूमा जोड दिन्छ।"),
    VARSHA_YEARLORD_RULES(" Year Lord rules this house - significant developments expected.", " वर्ष स्वामीले यो भाव शासन गर्छ - महत्त्वपूर्ण विकासहरू अपेक्षित।"),
    VARSHA_HOUSE_GOVERNS("House %d in %s governs %s.", "भाव %d %s मा %s शासन गर्दछ।"),
    VARSHA_LORD_IN_HOUSE("The lord %s in house %d ", "स्वामी %s भाव %d मा "),

    // ============================================
    // VARSHAPHALA - SPECIFIC EVENTS
    // ============================================
    VARSHA_EVENT_VITALITY("Increased vitality and personal confidence", "बढेको जीवनशक्ति र व्यक्तिगत आत्मविश्वास"),
    VARSHA_EVENT_NEW_VENTURES("Favorable for starting new ventures", "नयाँ उद्यमहरू सुरु गर्न अनुकूल"),
    VARSHA_EVENT_SPIRITUAL("Spiritual growth and wisdom", "आध्यात्मिक वृद्धि र बुद्धि"),
    VARSHA_EVENT_ACCIDENTS("Increased energy - watch for accidents", "बढेको ऊर्जा - दुर्घटनाबाट सावधान"),
    VARSHA_EVENT_FINANCIAL("Financial gains and wealth accumulation", "आर्थिक लाभ र धन संचय"),
    VARSHA_EVENT_FAMILY("Improvement in family relationships", "पारिवारिक सम्बन्धमा सुधार"),
    VARSHA_EVENT_LUXURY("Acquisition of luxury items", "विलासी वस्तुहरूको प्राप्ति"),
    VARSHA_EVENT_CREATIVE("Creative success and recognition", "सिर्जनात्मक सफलता र मान्यता"),
    VARSHA_EVENT_CHILDREN_MATTERS("Favorable for children's matters", "सन्तान सम्बन्धी मामिलाहरूको लागि अनुकूल"),
    VARSHA_EVENT_ACADEMIC("Academic success or childbirth possible", "शैक्षिक सफलता वा सन्तान जन्म सम्भव"),
    VARSHA_EVENT_ROMANTIC("Romantic happiness", "रोमान्टिक खुशी"),
    VARSHA_EVENT_PARTNERSHIPS("Strengthening of partnerships", "साझेदारीहरूको सुदृढीकरण"),
    VARSHA_EVENT_MARRIAGE_BUSINESS("Favorable for marriage or business", "विवाह वा व्यापारको लागि अनुकूल"),
    VARSHA_EVENT_ROMANTIC_FULFILL("Romantic fulfillment", "रोमान्टिक पूर्णता"),
    VARSHA_EVENT_CAREER_ADVANCE("Career advancement or promotion", "क्यारियर प्रगति वा पदोन्नति"),
    VARSHA_EVENT_RECOGNITION("Recognition from authorities", "अधिकारीहरूबाट मान्यता"),
    VARSHA_EVENT_GOVT_FAVOR("Government favor or leadership role", "सरकारी कृपा वा नेतृत्व भूमिका"),
    VARSHA_EVENT_DESIRES("Fulfillment of desires and wishes", "इच्छाहरू र कामनाहरूको पूर्ति"),
    VARSHA_EVENT_MULTIPLE_GAINS("Gains from multiple sources", "विभिन्न स्रोतहरूबाट लाभ"),

    // ============================================
    // VARSHAPHALA - DIGNITY DESCRIPTIONS
    // ============================================
    DIGNITY_EXALTED("exalted in %s", "%s मा उच्च"),
    DIGNITY_OWN_SIGN("in its own sign of %s", "आफ्नै राशि %s मा"),
    DIGNITY_DEBILITATED("debilitated in %s", "%s मा नीच"),
    DIGNITY_FRIENDLY("in the friendly sign of %s", "%s मित्र राशिमा"),
    DIGNITY_NEUTRAL("in the neutral sign of %s", "%s तटस्थ राशिमा"),
    DIGNITY_ENEMY("in the enemy sign of %s", "%s शत्रु राशिमा"),
    DIGNITY_KENDRA("in an angular house (Kendra)", "केन्द्र भावमा"),
    DIGNITY_TRIKONA("in a trine house (Trikona)", "त्रिकोण भावमा"),
    DIGNITY_GAINS("in a house of gains", "लाभ भावमा"),
    DIGNITY_UPACHAYA("in an upachaya house", "उपचय भावमा"),
    DIGNITY_DUSTHANA("in a challenging house (Dusthana)", "दुःस्थान भावमा"),
    DIGNITY_RETROGRADE("and is retrograde", "र वक्री छ"),
    DIGNITY_YEARLORD_DESC("The Year Lord %s is %s. This suggests its influence will be potent and its results will manifest clearly throughout the year.", "वर्ष स्वामी %s %s छ। यसले संकेत गर्छ कि यसको प्रभाव शक्तिशाली हुनेछ र यसको परिणामहरू वर्षभर स्पष्ट रूपमा प्रकट हुनेछन्।"),

    // ============================================
    // VARSHAPHALA - OVERALL PREDICTION TEMPLATE
    // ============================================
    VARSHA_OVERALL_TEMPLATE("This Varshaphala year presents an overall %s outlook. %s %s The Tajika aspects show %d favorable and %d challenging configurations. By understanding these influences, the year's potential can be maximized.", "यो वर्षफल वर्षले समग्र %s दृष्टिकोण प्रस्तुत गर्दछ। %s %s ताजिक पक्षहरूले %d अनुकूल र %d चुनौतीपूर्ण विन्यासहरू देखाउँछन्। यी प्रभावहरूलाई बुझेर, वर्षको सम्भावनालाई अधिकतम बनाउन सकिन्छ।"),
    VARSHA_MUNTHA_DIRECTS("Muntha in House %d (%s) directs attention to %s.", "भाव %d (%s) मा मुन्थले %s मा ध्यान केन्द्रित गर्दछ।"),

    // ============================================
    // VARSHAPHALA - KEY DATES
    // ============================================
    KEY_DATE_SOLAR_RETURN("Solar Return", "सौर प्रतिफल"),
    KEY_DATE_SOLAR_RETURN_DESC("Beginning of the annual horoscope year", "वार्षिक राशिफल वर्षको शुरुवात"),
    KEY_DATE_DASHA_BEGINS("%s Dasha Begins", "%s दशा शुरु"),
    KEY_DATE_DASHA_DESC("Start of %s period (%d days)", "%s अवधिको शुरुवात (%d दिन)"),

    // ============================================
    // VARSHAPHALA - HOUSE STRENGTH
    // ============================================
    HOUSE_STRENGTH_EXCELLENT("Excellent", "उत्कृष्ट"),
    HOUSE_STRENGTH_STRONG("Strong", "बलियो"),
    HOUSE_STRENGTH_MODERATE("Moderate", "मध्यम"),
    HOUSE_STRENGTH_WEAK("Weak", "कमजोर"),
    HOUSE_STRENGTH_CHALLENGED("Challenged", "चुनौतीपूर्ण"),

    // ============================================
    // VARSHAPHALA - ASPECT EFFECT DESCRIPTIONS
    // ============================================
    ASPECT_ITHASALA_EFFECT("%s applying to %s promises fulfillment", "%s %s मा निकटवर्ती हुँदा पूर्तिको वाचा"),
    ASPECT_EASARAPHA_EFFECT("Separating aspect suggests matters are concluding", "विलग पक्षले मामिलाहरू समाप्त हुँदैछन् भनी सुझाव दिन्छ"),
    ASPECT_KAMBOOLA_EFFECT("Powerful angular conjunction promises prominent success", "शक्तिशाली केन्द्रीय युतिले प्रमुख सफलताको वाचा गर्छ"),
    ASPECT_RADDA_EFFECT("Retrograde motion causes delays or reversals", "वक्री गतिले ढिलाइ वा उल्टो परिणाम ल्याउँछ"),
    ASPECT_DURAPHA_EFFECT("Hard aspect creates challenges that strengthen through difficulty", "कडा पक्षले कठिनाइद्वारा बलियो बनाउने चुनौतीहरू सिर्जना गर्छ"),
    ASPECT_GENERIC_EFFECT("%s influences matters with %s energy", "%s ले %s ऊर्जासहित मामिलाहरूलाई प्रभाव पार्छ"),
    ASPECT_SUPPORTIVE("supportive", "समर्थनात्मक"),
    ASPECT_PREDICTION_TEMPLATE("The %s between %s and %s is %s for matters of %s.", "%s र %s बीचको %s %s को मामिलाहरूको लागि %s छ।"),
    ASPECT_FAVORABLE_FOR("favorable", "अनुकूल"),
    ASPECT_REQUIRING_ATTENTION("requiring attention", "ध्यान चाहिने"),

    // ============================================
    // VARSHAPHALA - SAHAM INTERPRETATION
    // ============================================
    SAHAM_INTERPRETATION_TEMPLATE("The %s Saham in %s (House %d) relates to %s this year. Its lord %s in House %d is %s.", "%s सहम %s मा (भाव %d) यस वर्ष %s सँग सम्बन्धित छ। यसको स्वामी %s भाव %d मा %s छ।"),
    SAHAM_LORD_WELL_PLACED("well-placed, promising positive outcomes", "राम्ररी स्थित, सकारात्मक परिणामहरूको वाचा"),
    SAHAM_LORD_REASONABLE("providing reasonable support", "उचित समर्थन प्रदान गर्दै"),
    SAHAM_LORD_ATTENTION("requiring attention and effort", "ध्यान र प्रयास चाहिने"),
    SAHAM_LORD_VARIABLE("influencing matters variably", "मामिलाहरूलाई परिवर्तनशील रूपमा प्रभाव पार्दै"),
    SAHAM_INDICATES("indicates specific life areas", "विशिष्ट जीवन क्षेत्रहरू संकेत गर्दछ"),

    // ============================================
    // VARSHAPHALA - REPORT SECTIONS
    // ============================================
    VARSHA_REPORT_TITLE("VARSHAPHALA (ANNUAL HOROSCOPE) REPORT", "वर्षफल (वार्षिक राशिफल) रिपोर्ट"),
    VARSHA_REPORT_NAME("Name: %s", "नाम: %s"),
    VARSHA_REPORT_YEAR("Year: %d (Age: %d)", "वर्ष: %d (उमेर: %d)"),
    VARSHA_REPORT_SOLAR_RETURN("Solar Return: %s", "सौर प्रतिफल: %s"),
    VARSHA_REPORT_YEAR_RATING("Year Rating: %s/5.0", "वर्ष मूल्याङ्कन: %s/५.०"),
    VARSHA_REPORT_SECTION_YEARLORD("YEAR LORD", "वर्ष स्वामी"),
    VARSHA_REPORT_YEARLORD_LINE("Year Lord: %s (%s)", "वर्ष स्वामी: %s (%s)"),
    VARSHA_REPORT_POSITION("Position: House %d", "स्थिति: भाव %d"),
    VARSHA_REPORT_SECTION_MUNTHA("MUNTHA", "मुन्थ"),
    VARSHA_REPORT_MUNTHA_POSITION("Muntha Position: %s° %s", "मुन्थ स्थिति: %s° %s"),
    VARSHA_REPORT_MUNTHA_HOUSE("Muntha House: %d", "मुन्थ भाव: %d"),
    VARSHA_REPORT_MUNTHA_LORD("Muntha Lord: %s in House %d", "मुन्थ स्वामी: %s भाव %d मा"),
    VARSHA_REPORT_SECTION_THEMES("MAJOR THEMES", "मुख्य विषयहरू"),
    VARSHA_REPORT_SECTION_MUDDA("MUDDA DASHA PERIODS", "मुद्द दशा अवधिहरू"),
    VARSHA_REPORT_DASHA_LINE("%s: %s to %s (%d days)%s", "%s: %s देखि %s (%d दिन)%s"),
    VARSHA_REPORT_CURRENT_MARKER(" [CURRENT]", " [वर्तमान]"),
    VARSHA_REPORT_FAVORABLE_MONTHS("FAVORABLE MONTHS: %s", "अनुकूल महिनाहरू: %s"),
    VARSHA_REPORT_CHALLENGING_MONTHS("CHALLENGING MONTHS: %s", "चुनौतीपूर्ण महिनाहरू: %s"),
    VARSHA_REPORT_SECTION_PREDICTION("OVERALL PREDICTION", "समग्र भविष्यवाणी"),
    VARSHA_REPORT_FOOTER("Generated by AstroStorm - Ultra-Precision Vedic Astrology", "AstroStorm द्वारा उत्पन्न - अति-सटीक वैदिक ज्योतिष"),

    // ============================================
    // VARSHAPHALA - MONTH NAMES (SHORT)
    // ============================================
    MONTH_JAN("Jan", "जनवरी"),
    MONTH_FEB("Feb", "फेब्रुअरी"),
    MONTH_MAR("Mar", "मार्च"),
    MONTH_APR("Apr", "अप्रिल"),
    MONTH_MAY("May", "मे"),
    MONTH_JUN("Jun", "जुन"),
    MONTH_JUL("Jul", "जुलाई"),
    MONTH_AUG("Aug", "अगस्ट"),
    MONTH_SEP("Sep", "सेप्टेम्बर"),
    MONTH_OCT("Oct", "अक्टोबर"),
    MONTH_NOV("Nov", "नोभेम्बर"),
    MONTH_DEC("Dec", "डिसेम्बर"),

    // ============================================
    // VARSHAPHALA - ORDINAL SUFFIXES
    // ============================================
    ORDINAL_ST("st", "औं"),
    ORDINAL_ND("nd", "औं"),
    ORDINAL_RD("rd", "औं"),
    ORDINAL_TH("th", "औं"),

    // ============================================
    // VARSHAPHALA - MUNTHA INTERPRETATION TEMPLATE
    // ============================================
    MUNTHA_INTERPRETATION_TEMPLATE("Muntha in %s in the %d%s house focuses the year's energy on %s. The Muntha lord %s in house %d provides %s support for these matters.", "%d%s भावमा %s मा मुन्थले वर्षको ऊर्जालाई %s मा केन्द्रित गर्दछ। मुन्थ स्वामी %s भाव %d मा यी मामिलाहरूको लागि %s समर्थन प्रदान गर्दछ।"),
    MUNTHA_SUPPORT_EXCELLENT("excellent", "उत्कृष्ट"),
    MUNTHA_SUPPORT_FAVORABLE("favorable", "अनुकूल"),
    MUNTHA_SUPPORT_CHALLENGING("challenging but growth-oriented", "चुनौतीपूर्ण तर विकासोन्मुख"),
    MUNTHA_SUPPORT_VARIABLE("variable", "परिवर्तनशील"),

    // ============================================
    // DASHA MAHADASHA INTERPRETATIONS
    // ============================================
    DASHA_INTERP_MAHADASHA_SUN(
        "A period of heightened self-expression, authority, and recognition. Focus turns to career advancement, leadership roles, government dealings, and matters related to father. Soul purpose becomes clearer. Health of heart and vitality gains prominence. Good for developing confidence and establishing one's identity in the world.",
        "आत्म-अभिव्यक्ति, अधिकार र मान्यताको उच्च अवधि। क्यारियर प्रगति, नेतृत्व भूमिका, सरकारी व्यवहार र बुबासँग सम्बन्धित मामिलाहरूमा ध्यान केन्द्रित हुन्छ। आत्माको उद्देश्य स्पष्ट हुन्छ। हृदय र जीवनशक्तिको स्वास्थ्यले प्रमुखता पाउँछ। आत्मविश्वास विकास गर्न र संसारमा आफ्नो पहिचान स्थापित गर्न राम्रो।"
    ),
    DASHA_INTERP_MAHADASHA_MOON(
        "An emotionally rich and intuitive period emphasizing mental peace, nurturing, and receptivity. Focus on mother, home life, public image, travel across water, and emotional well-being. Creativity and imagination flourish. Memory and connection to the past strengthen. Relationships with women and the public become significant.",
        "भावनात्मक रूपले समृद्ध र अन्तर्ज्ञानात्मक अवधि जसले मानसिक शान्ति, पालनपोषण र ग्रहणशीलतामा जोड दिन्छ। आमा, घरेलु जीवन, सार्वजनिक छवि, पानी पार यात्रा र भावनात्मक कल्याणमा ध्यान केन्द्रित। सिर्जनशीलता र कल्पना फस्टाउँछ। स्मृति र भूतकालसँगको सम्बन्ध बलियो हुन्छ। महिलाहरू र जनतासँगको सम्बन्ध महत्त्वपूर्ण हुन्छ।"
    ),
    DASHA_INTERP_MAHADASHA_MARS(
        "A period of heightened energy, courage, initiative, and competitive drive. Focus on property matters, real estate, siblings, technical and engineering pursuits, sports, and surgery. Decisive action is favored. Physical vitality increases. Good for tackling challenges requiring strength and determination.",
        "ऊर्जा, साहस, पहल र प्रतिस्पर्धात्मक उत्प्रेरणाको उच्च अवधि। सम्पत्ति मामिला, घरजग्गा, भाइबहिनी, प्राविधिक र इन्जिनियरिङ, खेलकुद र शल्यक्रियामा ध्यान केन्द्रित। निर्णायक कार्यलाई प्राथमिकता दिइन्छ। शारीरिक जीवनशक्ति बढ्छ। बल र दृढ संकल्प चाहिने चुनौतीहरू सामना गर्न राम्रो।"
    ),
    DASHA_INTERP_MAHADASHA_MERCURY(
        "A period of enhanced learning, communication, analytical thinking, and commerce. Focus on education, writing, publishing, accounting, trade, and intellectual pursuits. Social connections expand through skillful communication. Good for developing skills, starting businesses, and mastering information.",
        "सिकाइ, सञ्चार, विश्लेषणात्मक सोच र वाणिज्यको उन्नत अवधि। शिक्षा, लेखन, प्रकाशन, लेखा, व्यापार र बौद्धिक खोजमा ध्यान केन्द्रित। दक्ष सञ्चार मार्फत सामाजिक सम्बन्ध विस्तार हुन्छ। सीप विकास, व्यवसाय सुरु गर्न र जानकारीमा दक्षता हासिल गर्न राम्रो।"
    ),
    DASHA_INTERP_MAHADASHA_JUPITER(
        "A period of wisdom, expansion, prosperity, and divine grace (Guru's blessings). Focus on spirituality, higher learning, teaching, children, law, and philosophical pursuits. Fortune favors righteous endeavors. Faith and optimism increase. Excellent for marriage, progeny, and spiritual advancement.",
        "बुद्धि, विस्तार, समृद्धि र दैवी कृपाको अवधि (गुरुको आशीर्वाद)। आध्यात्मिकता, उच्च शिक्षा, शिक्षण, सन्तान, कानुन र दार्शनिक खोजमा ध्यान केन्द्रित। भाग्यले धार्मिक प्रयासहरूलाई साथ दिन्छ। विश्वास र आशावाद बढ्छ। विवाह, सन्तान र आध्यात्मिक प्रगतिको लागि उत्कृष्ट।"
    ),
    DASHA_INTERP_MAHADASHA_VENUS(
        "A period of luxury, beauty, relationships, artistic expression, and material comforts. Focus on marriage, partnerships, arts, music, dance, vehicles, jewelry, and sensory pleasures. Creativity and romance blossom. Refinement in all areas of life. Good for enhancing beauty, wealth, and experiencing life's pleasures.",
        "विलासिता, सौन्दर्य, सम्बन्ध, कलात्मक अभिव्यक्ति र भौतिक सुविधाहरूको अवधि। विवाह, साझेदारी, कला, सङ्गीत, नृत्य, सवारीसाधन, गहना र इन्द्रिय आनन्दमा ध्यान केन्द्रित। सिर्जनशीलता र रोमान्स फस्टाउँछ। जीवनका सबै क्षेत्रमा परिष्करण। सौन्दर्य, सम्पत्ति बढाउन र जीवनका आनन्दहरू अनुभव गर्न राम्रो।"
    ),
    DASHA_INTERP_MAHADASHA_SATURN(
        "A period of discipline, karmic lessons, perseverance, and structural growth. Focus on service, responsibility, hard work, long-term projects, and lessons through patience. Delays and obstacles ultimately lead to lasting success and maturity. Time to build solid foundations and pay karmic debts.",
        "अनुशासन, कार्मिक पाठ, दृढता र संरचनात्मक विकासको अवधि। सेवा, जिम्मेवारी, कठिन परिश्रम, दीर्घकालीन परियोजना र धैर्य मार्फत पाठहरूमा ध्यान केन्द्रित। ढिलाइ र बाधाहरूले अन्ततः स्थायी सफलता र परिपक्वता ल्याउँछ। ठोस आधार निर्माण गर्ने र कार्मिक ऋण चुक्ता गर्ने समय।"
    ),
    DASHA_INTERP_MAHADASHA_RAHU(
        "A period of intense worldly ambition, unconventional paths, and material desires. Focus on foreign connections, technology, innovation, and breaking traditional boundaries. Sudden opportunities and unexpected changes arise. Material gains through unusual or non-traditional means. Beware of illusions.",
        "तीव्र सांसारिक महत्त्वाकांक्षा, अपरम्परागत मार्ग र भौतिक इच्छाको अवधि। विदेशी सम्बन्ध, प्रविधि, नवीनता र परम्परागत सीमाहरू तोड्नमा ध्यान केन्द्रित। अचानक अवसरहरू र अप्रत्याशित परिवर्तनहरू आउँछन्। असामान्य वा गैर-परम्परागत माध्यमबाट भौतिक लाभ। भ्रमबाट सावधान रहनुहोस्।"
    ),
    DASHA_INTERP_MAHADASHA_KETU(
        "A period of spirituality, detachment, and profound inner transformation. Focus on liberation (moksha), occult research, healing practices, and resolving past-life karma. Deep introspection yields spiritual insights. Material attachments may dissolve. Excellent for meditation, research, and spiritual practices.",
        "आध्यात्मिकता, वैराग्य र गहिरो आन्तरिक रूपान्तरणको अवधि। मुक्ति (मोक्ष), तान्त्रिक अनुसन्धान, उपचार अभ्यास र पूर्वजन्मको कर्म समाधानमा ध्यान केन्द्रित। गहिरो आत्मनिरीक्षणले आध्यात्मिक अन्तर्दृष्टि दिन्छ। भौतिक आसक्तिहरू विलीन हुन सक्छन्। ध्यान, अनुसन्धान र आध्यात्मिक अभ्यासको लागि उत्कृष्ट।"
    ),
    DASHA_INTERP_MAHADASHA_DEFAULT(
        "A period of transformation and karmic unfolding according to planetary influences.",
        "ग्रहीय प्रभाव अनुसार रूपान्तरण र कार्मिक विकासको अवधि।"
    ),

    // ============================================
    // DASHA ANTARDASHA INTERPRETATIONS
    // ============================================
    DASHA_INTERP_ANTARDASHA_SUN(
        "Current sub-period (Bhukti) activates themes of authority, self-confidence, recognition, and dealings with father figures or government. Leadership opportunities may arise.",
        "हालको उप-अवधि (भुक्ति) ले अधिकार, आत्मविश्वास, मान्यता र बुबाका व्यक्ति वा सरकारसँगको व्यवहारका विषयहरू सक्रिय गर्छ। नेतृत्व अवसरहरू आउन सक्छन्।"
    ),
    DASHA_INTERP_ANTARDASHA_MOON(
        "Current sub-period emphasizes emotional matters, mental peace, mother, public image, domestic affairs, and connection with women. Intuition heightens.",
        "हालको उप-अवधिले भावनात्मक मामिला, मानसिक शान्ति, आमा, सार्वजनिक छवि, घरेलु मामिला र महिलाहरूसँगको सम्बन्धमा जोड दिन्छ। अन्तर्ज्ञान तीव्र हुन्छ।"
    ),
    DASHA_INTERP_ANTARDASHA_MARS(
        "Current sub-period brings increased energy, drive for action, courage, and matters involving property, siblings, competition, or technical endeavors.",
        "हालको उप-अवधिले बढेको ऊर्जा, कार्यको लागि उत्प्रेरणा, साहस र सम्पत्ति, भाइबहिनी, प्रतिस्पर्धा वा प्राविधिक प्रयासका मामिलाहरू ल्याउँछ।"
    ),
    DASHA_INTERP_ANTARDASHA_MERCURY(
        "Current sub-period emphasizes communication, learning, business transactions, intellectual activities, and connections with younger people or merchants.",
        "हालको उप-अवधिले सञ्चार, सिकाइ, व्यापारिक लेनदेन, बौद्धिक गतिविधिहरू र साना मानिसहरू वा व्यापारीहरूसँगको सम्बन्धमा जोड दिन्छ।"
    ),
    DASHA_INTERP_ANTARDASHA_JUPITER(
        "Current sub-period brings wisdom, expansion, good fortune, and focus on spirituality, teachers, children, higher education, or legal matters.",
        "हालको उप-अवधिले बुद्धि, विस्तार, सुभाग्य र आध्यात्मिकता, शिक्षकहरू, सन्तान, उच्च शिक्षा वा कानुनी मामिलाहरूमा ध्यान केन्द्रित गर्छ।"
    ),
    DASHA_INTERP_ANTARDASHA_VENUS(
        "Current sub-period emphasizes relationships, romance, creativity, luxury, artistic pursuits, material comforts, and partnership matters.",
        "हालको उप-अवधिले सम्बन्ध, रोमान्स, सिर्जनशीलता, विलासिता, कलात्मक खोज, भौतिक सुविधा र साझेदारी मामिलाहरूमा जोड दिन्छ।"
    ),
    DASHA_INTERP_ANTARDASHA_SATURN(
        "Current sub-period brings discipline, responsibility, hard work, delays, and lessons requiring patience. Focus on service and long-term efforts.",
        "हालको उप-अवधिले अनुशासन, जिम्मेवारी, कठिन परिश्रम, ढिलाइ र धैर्य चाहिने पाठहरू ल्याउँछ। सेवा र दीर्घकालीन प्रयासमा ध्यान केन्द्रित।"
    ),
    DASHA_INTERP_ANTARDASHA_RAHU(
        "Current sub-period emphasizes worldly ambitions, unconventional approaches, foreign matters, technology, and sudden changes or opportunities.",
        "हालको उप-अवधिले सांसारिक महत्त्वाकांक्षा, अपरम्परागत दृष्टिकोण, विदेशी मामिला, प्रविधि र अचानक परिवर्तन वा अवसरहरूमा जोड दिन्छ।"
    ),
    DASHA_INTERP_ANTARDASHA_KETU(
        "Current sub-period brings spiritual insights, detachment, introspection, research, and resolution of past karmic patterns. Material concerns recede.",
        "हालको उप-अवधिले आध्यात्मिक अन्तर्दृष्टि, वैराग्य, आत्मनिरीक्षण, अनुसन्धान र पूर्व कार्मिक ढाँचाको समाधान ल्याउँछ। भौतिक चिन्ताहरू पछाडि हट्छन्।"
    ),
    DASHA_INTERP_ANTARDASHA_DEFAULT(
        "Current sub-period brings mixed planetary influences requiring careful navigation.",
        "हालको उप-अवधिले सावधानीपूर्वक मार्गदर्शन चाहिने मिश्रित ग्रहीय प्रभावहरू ल्याउँछ।"
    ),

    // ============================================
    // CHART ANALYSIS SCREEN - TABS & UI
    // ============================================
    ANALYSIS_CHART_ANALYSIS("Chart Analysis", "कुण्डली विश्लेषण"),
    ANALYSIS_TAB_CHART("Chart", "कुण्डली"),
    ANALYSIS_TAB_PLANETS("Planets", "ग्रहहरू"),
    ANALYSIS_TAB_YOGAS("Yogas", "योगहरू"),
    ANALYSIS_TAB_DASHAS("Dashas", "दशाहरू"),
    ANALYSIS_TAB_TRANSITS("Transits", "गोचरहरू"),
    ANALYSIS_TAB_ASHTAKAVARGA("Ashtakavarga", "अष्टकवर्ग"),
    ANALYSIS_TAB_PANCHANGA("Panchanga", "पञ्चाङ्ग"),

    // ============================================
    // DIVISIONAL CHARTS - NAMES & DESCRIPTIONS
    // ============================================
    VARGA_D1_NAME("Lagna Chart (Rashi)", "लग्न कुण्डली (राशि)"),
    VARGA_D2_NAME("Hora Chart", "होरा कुण्डली"),
    VARGA_D3_NAME("Drekkana Chart", "द्रेक्काण कुण्डली"),
    VARGA_D4_NAME("Chaturthamsa Chart", "चतुर्थांश कुण्डली"),
    VARGA_D7_NAME("Saptamsa Chart", "सप्तांश कुण्डली"),
    VARGA_D9_NAME("Navamsa Chart", "नवांश कुण्डली"),
    VARGA_D10_NAME("Dasamsa Chart", "दशांश कुण्डली"),
    VARGA_D12_NAME("Dwadasamsa Chart", "द्वादशांश कुण्डली"),
    VARGA_D16_NAME("Shodasamsa Chart", "षोडशांश कुण्डली"),
    VARGA_D20_NAME("Vimsamsa Chart", "विंशांश कुण्डली"),
    VARGA_D24_NAME("Siddhamsa Chart", "चतुर्विंशांश कुण्डली"),
    VARGA_D27_NAME("Bhamsa Chart", "सप्तविंशांश कुण्डली"),
    VARGA_D30_NAME("Trimsamsa Chart", "त्रिंशांश कुण्डली"),
    VARGA_D60_NAME("Shashtiamsa Chart", "षष्टिांश कुण्डली"),

    VARGA_D3_DESC_FULL("Siblings, Courage, Vitality", "भाइबहिनी, साहस, जीवनशक्ति"),
    VARGA_D9_DESC_FULL("Marriage, Dharma, Fortune", "विवाह, धर्म, भाग्य"),
    VARGA_D10_DESC_FULL("Career, Profession", "क्यारियर, पेशा"),
    VARGA_D12_DESC_FULL("Parents, Ancestry", "आमाबुबा, पुर्खा"),
    VARGA_D16_DESC_FULL("Vehicles, Pleasures", "सवारी, आनन्द"),
    VARGA_D20_DESC_FULL("Spiritual Life", "आध्यात्मिक जीवन"),
    VARGA_D24_DESC_FULL("Education, Learning", "शिक्षा, सिकाइ"),
    VARGA_D27_DESC_FULL("Strength, Weakness", "बल, कमजोरी"),
    VARGA_D30_DESC_FULL("Evils, Misfortunes", "दुर्भाग्य, विपत्ति"),
    VARGA_D60_DESC_FULL("Past Life Karma", "पूर्वजन्मको कर्म"),

    // Divisional chart selector labels
    VARGA_LAGNA("Lagna", "लग्न"),
    VARGA_HORA("Hora", "होरा"),
    VARGA_DREKKANA("Drekkana", "द्रेक्काण"),
    VARGA_SAPTAMSA("Saptamsa", "सप्तांश"),
    VARGA_NAVAMSA("Navamsa", "नवांश"),
    VARGA_DASAMSA("Dasamsa", "दशांश"),
    VARGA_BHAMSA("Bhamsa", "भांश"),

    // ============================================
    // PLANETS TAB - HARDCODED STRINGS
    // ============================================
    PLANETS_CONDITIONS("Planetary Conditions", "ग्रह अवस्थाहरू"),
    PLANETS_RETROGRADE("Retrograde", "वक्री"),
    PLANETS_COMBUST("Combust", "अस्त"),
    PLANETS_AT_WAR("At War", "युद्धमा"),
    PLANETS_PLANETARY_WAR("Planetary War", "ग्रहयुद्ध"),
    PLANETS_SHADBALA_SUMMARY("Shadbala Summary", "षड्बल सारांश"),
    PLANETS_OVERALL("Overall", "समग्र"),
    PLANETS_VIEW_DETAILS("View Details", "विवरण हेर्नुहोस्"),
    PLANETS_TAP_FOR_DETAILS("Tap for details", "विवरणको लागि ट्याप गर्नुहोस्"),
    PLANETS_SHADBALA("Shadbala", "षड्बल"),
    PLANETS_RUPAS("%s / %s rupas (%s%%)", "%s / %s रुपा (%s%%)"),
    PLANETS_HOUSE_FORMAT("House %d", "भाव %d"),

    // Dignity status
    DIGNITY_EXALTED_STATUS("Exalted", "उच्च"),
    DIGNITY_DEBILITATED_STATUS("Debilitated", "नीच"),
    DIGNITY_OWN_SIGN_STATUS("Own Sign", "स्वराशि"),
    DIGNITY_NEUTRAL_STATUS("Neutral", "तटस्थ"),

    // ============================================
    // ASHTAKAVARGA TAB - HARDCODED STRINGS
    // ============================================
    ASHTAK_SUMMARY("Ashtakavarga Summary", "अष्टकवर्ग सारांश"),
    ASHTAK_TOTAL_SAV("Total SAV", "कुल SAV"),
    ASHTAK_STRONGEST("Strongest", "सबैभन्दा बलियो"),
    ASHTAK_WEAKEST("Weakest", "सबैभन्दा कमजोर"),
    ASHTAK_QUICK_ANALYSIS("Quick Analysis", "द्रुत विश्लेषण"),
    ASHTAK_FAVORABLE_SIGNS("Favorable Signs (28+):", "अनुकूल राशिहरू (२८+):"),
    ASHTAK_CHALLENGING_SIGNS("Challenging Signs (<25):", "चुनौतीपूर्ण राशिहरू (<२५):"),
    ASHTAK_SIGNS_COUNT("%d signs", "%d राशिहरू"),

    // Sarvashtakavarga
    ASHTAK_SAV_TITLE("Sarvashtakavarga (SAV)", "सर्वाष्टकवर्ग (SAV)"),
    ASHTAK_SAV_COMBINED_DESC("Combined strength of all planets in each sign", "प्रत्येक राशिमा सबै ग्रहहरूको संयुक्त बल"),

    // Bhinnashtakavarga
    ASHTAK_BAV_TITLE("Bhinnashtakavarga (BAV)", "भिन्नाष्टकवर्ग (BAV)"),
    ASHTAK_BAV_INDIVIDUAL_DESC("Individual planet strength in each sign (0-8 bindus)", "प्रत्येक राशिमा व्यक्तिगत ग्रहको बल (०-८ बिन्दु)"),
    ASHTAK_TOTAL("Total", "कुल"),

    // SAV Legend
    ASHTAK_SAV_EXCELLENT("30+ (Excellent)", "३०+ (उत्कृष्ट)"),
    ASHTAK_SAV_GOOD("28-29 (Good)", "२८-२९ (राम्रो)"),
    ASHTAK_SAV_AVERAGE("25-27 (Average)", "२५-२७ (औसत)"),
    ASHTAK_SAV_WEAK("<25 (Weak)", "<२५ (कमजोर)"),

    // BAV Legend
    ASHTAK_BAV_STRONG("5+ (Strong)", "५+ (बलियो)"),
    ASHTAK_BAV_GOOD("4 (Good)", "४ (राम्रो)"),
    ASHTAK_BAV_AVERAGE("3 (Average)", "३ (औसत)"),
    ASHTAK_BAV_WEAK("0-2 (Weak)", "०-२ (कमजोर)"),

    // Interpretation Guide
    ASHTAK_GUIDE_TITLE("Interpretation Guide", "व्याख्या गाइड"),
    ASHTAK_GUIDE_SAV_TITLE("Sarvashtakavarga (SAV)", "सर्वाष्टकवर्ग (SAV)"),
    ASHTAK_GUIDE_SAV_30("30+ bindus: Excellent for transits - major positive events", "३०+ बिन्दु: गोचरको लागि उत्कृष्ट - प्रमुख सकारात्मक घटनाहरू"),
    ASHTAK_GUIDE_SAV_28("28-29 bindus: Good for transits - favorable outcomes", "२८-२९ बिन्दु: गोचरको लागि राम्रो - अनुकूल परिणामहरू"),
    ASHTAK_GUIDE_SAV_25("25-27 bindus: Average - mixed results expected", "२५-२७ बिन्दु: औसत - मिश्रित परिणामहरू अपेक्षित"),
    ASHTAK_GUIDE_SAV_BELOW("Below 25: Challenging - caution during transits", "२५ भन्दा कम: चुनौतीपूर्ण - गोचरमा सावधानी"),
    ASHTAK_GUIDE_BAV_TITLE("Bhinnashtakavarga (BAV)", "भिन्नाष्टकवर्ग (BAV)"),
    ASHTAK_GUIDE_BAV_5("5+ bindus: Planet transit highly beneficial", "५+ बिन्दु: ग्रह गोचर अत्यधिक लाभदायक"),
    ASHTAK_GUIDE_BAV_4("4 bindus: Good results from transit", "४ बिन्दु: गोचरबाट राम्रो परिणाम"),
    ASHTAK_GUIDE_BAV_3("3 bindus: Average, neutral results", "३ बिन्दु: औसत, तटस्थ परिणामहरू"),
    ASHTAK_GUIDE_BAV_02("0-2 bindus: Difficult transit period", "०-२ बिन्दु: कठिन गोचर अवधि"),
    ASHTAK_GUIDE_TRANSIT_TITLE("Transit Application", "गोचर अनुप्रयोग"),
    ASHTAK_GUIDE_TRANSIT_1("Check SAV of the sign a planet transits", "ग्रहले गोचर गर्ने राशिको SAV जाँच गर्नुहोस्"),
    ASHTAK_GUIDE_TRANSIT_2("Check BAV score of that planet in transited sign", "गोचर गरिएको राशिमा त्यो ग्रहको BAV स्कोर जाँच गर्नुहोस्"),
    ASHTAK_GUIDE_TRANSIT_3("High combined scores = favorable transit", "उच्च संयुक्त स्कोर = अनुकूल गोचर"),
    ASHTAK_GUIDE_TRANSIT_4("Use for timing important decisions", "महत्त्वपूर्ण निर्णयहरूको समयको लागि प्रयोग गर्नुहोस्"),

    // ============================================
    // ASPECT TYPES (For AspectCalculator)
    // ============================================
    ASPECT_TYPE_CONJUNCTION("Conjunction", "युति"),
    ASPECT_TYPE_7TH("7th Aspect", "सप्तम दृष्टि"),
    ASPECT_TYPE_MARS_4TH("Mars 4th Aspect", "मंगलको चतुर्थ दृष्टि"),
    ASPECT_TYPE_MARS_8TH("Mars 8th Aspect", "मंगलको अष्टम दृष्टि"),
    ASPECT_TYPE_JUPITER_5TH("Jupiter 5th Aspect", "गुरुको पञ्चम दृष्टि"),
    ASPECT_TYPE_JUPITER_9TH("Jupiter 9th Aspect", "गुरुको नवम दृष्टि"),
    ASPECT_TYPE_SATURN_3RD("Saturn 3rd Aspect", "शनिको तृतीय दृष्टि"),
    ASPECT_TYPE_SATURN_10TH("Saturn 10th Aspect", "शनिको दशम दृष्टि"),

    // Aspect Strength Descriptions (Drishti Bala)
    ASPECT_STRENGTH_EXACT("Exact (Purna)", "पूर्ण (एकदम सटीक)"),
    ASPECT_STRENGTH_ADHIKA("Strong (Adhika)", "अधिक (बलियो)"),
    ASPECT_STRENGTH_MADHYA("Medium (Madhya)", "मध्यम"),
    ASPECT_STRENGTH_ALPA("Weak (Alpa)", "अल्प (कमजोर)"),
    ASPECT_STRENGTH_SUNYA("Negligible (Sunya)", "शून्य (नगण्य)"),

    // Aspect descriptions
    ASPECT_CASTS_ON("%s casts %s on %s", "%s ले %s मा %s दृष्टि राख्छ"),
    ASPECT_APPLYING("Applying", "समीप आउँदै"),
    ASPECT_SEPARATING("Separating", "टाढा हुँदै"),
    ASPECT_DRISHTI_BALA("Drishti Bala", "दृष्टि बल"),

    // ============================================
    // TRANSIT QUALITY (For AshtakavargaCalculator)
    // ============================================
    TRANSIT_QUALITY_EXCELLENT("Excellent", "उत्कृष्ट"),
    TRANSIT_QUALITY_GOOD("Good", "राम्रो"),
    TRANSIT_QUALITY_AVERAGE("Average", "औसत"),
    TRANSIT_QUALITY_BELOW_AVG("Below Average", "औसतभन्दा कम"),
    TRANSIT_QUALITY_CHALLENGING("Challenging", "चुनौतीपूर्ण"),
    TRANSIT_QUALITY_DIFFICULT("Difficult", "कठिन"),
    TRANSIT_QUALITY_UNKNOWN("Unknown", "अज्ञात"),

    // Transit interpretations
    TRANSIT_INTERP_EXCELLENT("Excellent - Highly favorable transit", "उत्कृष्ट - अत्यन्त अनुकूल गोचर"),
    TRANSIT_INTERP_GOOD("Good - Favorable results expected", "राम्रो - अनुकूल परिणामहरू अपेक्षित"),
    TRANSIT_INTERP_AVERAGE("Average - Mixed results", "औसत - मिश्रित परिणामहरू"),
    TRANSIT_INTERP_BELOW_AVG("Below Average - Some challenges", "औसतभन्दा कम - केही चुनौतीहरू"),
    TRANSIT_INTERP_DIFFICULT("Difficult - Careful navigation needed", "कठिन - सावधानीपूर्ण व्यवहार आवश्यक"),
    TRANSIT_ANALYSIS_NOT_AVAILABLE("Transit analysis not available for this planet.", "यस ग्रहको लागि गोचर विश्लेषण उपलब्ध छैन।"),

    // ============================================
    // ELEMENTS (Additional)
    // ============================================
    ELEMENT_ETHER("Ether", "आकाश"),

    // ============================================
    // PLANET SIGNIFICATIONS (For PlanetDetailDialog)
    // ============================================
    // Sun Significations
    PLANET_SUN_NATURE("Malefic", "पापी"),
    PLANET_SUN_ELEMENT("Fire", "अग्नि"),
    PLANET_SUN_REPRESENTS_1("Soul, Self, Ego", "आत्मा, स्वयं, अहंकार"),
    PLANET_SUN_REPRESENTS_2("Father, Authority Figures", "पिता, अधिकारीहरू"),
    PLANET_SUN_REPRESENTS_3("Government, Power", "सरकार, शक्ति"),
    PLANET_SUN_REPRESENTS_4("Health, Vitality", "स्वास्थ्य, जीवनशक्ति"),
    PLANET_SUN_REPRESENTS_5("Fame, Recognition", "प्रसिद्धि, मान्यता"),
    PLANET_SUN_BODY_PARTS("Heart, Spine, Right Eye, Bones", "हृदय, मेरुदण्ड, दाहिने आँखा, हड्डी"),
    PLANET_SUN_PROFESSIONS("Government jobs, Politics, Medicine, Administration, Leadership roles", "सरकारी जागिर, राजनीति, चिकित्सा, प्रशासन, नेतृत्व भूमिकाहरू"),

    // Moon Significations
    PLANET_MOON_NATURE("Benefic", "शुभ"),
    PLANET_MOON_ELEMENT("Water", "जल"),
    PLANET_MOON_REPRESENTS_1("Mind, Emotions", "मन, भावनाहरू"),
    PLANET_MOON_REPRESENTS_2("Mother, Nurturing", "आमा, पालनपोषण"),
    PLANET_MOON_REPRESENTS_3("Public, Masses", "जनता, समुदाय"),
    PLANET_MOON_REPRESENTS_4("Comforts, Happiness", "आराम, खुशी"),
    PLANET_MOON_REPRESENTS_5("Memory, Imagination", "स्मृति, कल्पना"),
    PLANET_MOON_BODY_PARTS("Mind, Left Eye, Breast, Blood, Fluids", "मन, बायाँ आँखा, स्तन, रगत, तरल पदार्थ"),
    PLANET_MOON_PROFESSIONS("Nursing, Hotel industry, Shipping, Agriculture, Psychology", "नर्सिङ, होटल उद्योग, जहाजरानी, कृषि, मनोविज्ञान"),

    // Mars Significations
    PLANET_MARS_NATURE("Malefic", "पापी"),
    PLANET_MARS_ELEMENT("Fire", "अग्नि"),
    PLANET_MARS_REPRESENTS_1("Energy, Action, Courage", "ऊर्जा, कार्य, साहस"),
    PLANET_MARS_REPRESENTS_2("Siblings, Younger Brothers", "भाइबहिनी, सानो भाइ"),
    PLANET_MARS_REPRESENTS_3("Property, Land", "सम्पत्ति, जमिन"),
    PLANET_MARS_REPRESENTS_4("Competition, Sports", "प्रतिस्पर्धा, खेलकुद"),
    PLANET_MARS_REPRESENTS_5("Technical Skills", "प्राविधिक सीपहरू"),
    PLANET_MARS_BODY_PARTS("Blood, Muscles, Marrow, Head injuries", "रगत, मांसपेशी, मज्जा, टाउकोमा चोटपटक"),
    PLANET_MARS_PROFESSIONS("Military, Police, Surgery, Engineering, Sports, Real Estate", "सेना, प्रहरी, शल्यक्रिया, इन्जिनियरिङ, खेलकुद, घरजग्गा"),

    // Mercury Significations
    PLANET_MERCURY_NATURE("Benefic", "शुभ"),
    PLANET_MERCURY_ELEMENT("Earth", "पृथ्वी"),
    PLANET_MERCURY_REPRESENTS_1("Intelligence, Communication", "बुद्धि, सञ्चार"),
    PLANET_MERCURY_REPRESENTS_2("Learning, Education", "सिकाइ, शिक्षा"),
    PLANET_MERCURY_REPRESENTS_3("Business, Trade", "व्यापार, व्यवसाय"),
    PLANET_MERCURY_REPRESENTS_4("Writing, Speech", "लेखन, वाणी"),
    PLANET_MERCURY_REPRESENTS_5("Siblings, Friends", "भाइबहिनी, साथीहरू"),
    PLANET_MERCURY_BODY_PARTS("Nervous system, Skin, Speech, Hands", "स्नायु प्रणाली, छाला, वाणी, हातहरू"),
    PLANET_MERCURY_PROFESSIONS("Writing, Teaching, Accounting, Trading, IT, Media", "लेखन, शिक्षण, लेखा, व्यापार, आईटी, मिडिया"),

    // Jupiter Significations
    PLANET_JUPITER_NATURE("Benefic", "शुभ"),
    PLANET_JUPITER_ELEMENT("Ether", "आकाश"),
    PLANET_JUPITER_REPRESENTS_1("Wisdom, Knowledge", "ज्ञान, विद्या"),
    PLANET_JUPITER_REPRESENTS_2("Teachers, Gurus", "शिक्षकहरू, गुरुहरू"),
    PLANET_JUPITER_REPRESENTS_3("Fortune, Luck", "भाग्य, किस्मत"),
    PLANET_JUPITER_REPRESENTS_4("Children, Dharma", "सन्तान, धर्म"),
    PLANET_JUPITER_REPRESENTS_5("Expansion, Growth", "विस्तार, वृद्धि"),
    PLANET_JUPITER_BODY_PARTS("Liver, Fat tissue, Ears, Thighs", "कलेजो, बोसो, कान, जाँघ"),
    PLANET_JUPITER_PROFESSIONS("Teaching, Law, Priesthood, Banking, Counseling", "शिक्षण, कानून, पुरोहित, बैंकिङ, परामर्श"),

    // Venus Significations
    PLANET_VENUS_NATURE("Benefic", "शुभ"),
    PLANET_VENUS_ELEMENT("Water", "जल"),
    PLANET_VENUS_REPRESENTS_1("Love, Beauty, Art", "प्रेम, सौन्दर्य, कला"),
    PLANET_VENUS_REPRESENTS_2("Marriage, Relationships", "विवाह, सम्बन्धहरू"),
    PLANET_VENUS_REPRESENTS_3("Luxuries, Comforts", "विलासिता, आराम"),
    PLANET_VENUS_REPRESENTS_4("Vehicles, Pleasures", "सवारी, आनन्द"),
    PLANET_VENUS_REPRESENTS_5("Creativity", "सिर्जनशीलता"),
    PLANET_VENUS_BODY_PARTS("Reproductive system, Face, Skin, Throat", "प्रजनन प्रणाली, अनुहार, छाला, घाँटी"),
    PLANET_VENUS_PROFESSIONS("Entertainment, Fashion, Art, Hospitality, Beauty industry", "मनोरञ्जन, फेसन, कला, आतिथ्य, सौन्दर्य उद्योग"),

    // Saturn Significations
    PLANET_SATURN_NATURE("Malefic", "पापी"),
    PLANET_SATURN_ELEMENT("Air", "वायु"),
    PLANET_SATURN_REPRESENTS_1("Discipline, Hard work", "अनुशासन, कडा परिश्रम"),
    PLANET_SATURN_REPRESENTS_2("Karma, Delays", "कर्म, ढिलाइ"),
    PLANET_SATURN_REPRESENTS_3("Longevity, Service", "दीर्घायु, सेवा"),
    PLANET_SATURN_REPRESENTS_4("Laborers, Servants", "श्रमिकहरू, सेवकहरू"),
    PLANET_SATURN_REPRESENTS_5("Chronic issues", "दीर्घकालीन समस्याहरू"),
    PLANET_SATURN_BODY_PARTS("Bones, Teeth, Knees, Joints, Nerves", "हड्डी, दाँत, घुँडा, जोर्नीहरू, स्नायु"),
    PLANET_SATURN_PROFESSIONS("Mining, Agriculture, Labor, Judiciary, Real Estate", "खनन, कृषि, श्रम, न्यायपालिका, घरजग्गा"),

    // Rahu Significations
    PLANET_RAHU_NATURE("Malefic", "पापी"),
    PLANET_RAHU_ELEMENT("Air", "वायु"),
    PLANET_RAHU_REPRESENTS_1("Obsession, Illusion", "जुनून, भ्रम"),
    PLANET_RAHU_REPRESENTS_2("Foreign lands, Travel", "विदेश, यात्रा"),
    PLANET_RAHU_REPRESENTS_3("Technology, Innovation", "प्रविधि, नवीनता"),
    PLANET_RAHU_REPRESENTS_4("Unconventional paths", "अपरंपरागत मार्गहरू"),
    PLANET_RAHU_REPRESENTS_5("Material desires", "भौतिक इच्छाहरू"),
    PLANET_RAHU_BODY_PARTS("Skin diseases, Nervous disorders", "छालाका रोगहरू, स्नायु विकारहरू"),
    PLANET_RAHU_PROFESSIONS("Technology, Foreign affairs, Aviation, Politics, Research", "प्रविधि, विदेशी मामिला, उड्डयन, राजनीति, अनुसन्धान"),

    // Ketu Significations
    PLANET_KETU_NATURE("Malefic", "पापी"),
    PLANET_KETU_ELEMENT("Fire", "अग्नि"),
    PLANET_KETU_REPRESENTS_1("Spirituality, Liberation", "आध्यात्मिकता, मोक्ष"),
    PLANET_KETU_REPRESENTS_2("Past life karma", "पूर्वजन्मको कर्म"),
    PLANET_KETU_REPRESENTS_3("Detachment, Isolation", "वैराग्य, एकान्त"),
    PLANET_KETU_REPRESENTS_4("Occult, Mysticism", "गुप्त विद्या, रहस्यवाद"),
    PLANET_KETU_REPRESENTS_5("Healing abilities", "उपचार क्षमता"),
    PLANET_KETU_BODY_PARTS("Skin, Spine, Nervous system", "छाला, मेरुदण्ड, स्नायु प्रणाली"),
    PLANET_KETU_PROFESSIONS("Spirituality, Research, Healing, Astrology, Philosophy", "आध्यात्मिकता, अनुसन्धान, उपचार, ज्योतिष, दर्शन"),

    // ============================================
    // CHART LEGEND LABELS (For ChartRenderer)
    // ============================================
    CHART_LEGEND_RETRO_SHORT("Retro", "वक्री"),
    CHART_LEGEND_COMBUST_SHORT("Comb", "अस्त"),
    CHART_LEGEND_VARGO_SHORT("Vargo", "वर्गो"),
    CHART_LEGEND_EXALT_SHORT("Exalt", "उच्च"),
    CHART_LEGEND_DEB_SHORT("Deb", "नीच"),
    CHART_LEGEND_OWN_SHORT("Own", "स्व"),
    CHART_ASC_ABBR("Asc", "ल"),

    // ============================================
    // ASHTAKAVARGA ANALYSIS HEADERS
    // ============================================
    ASHTAK_ANALYSIS_HEADER("ASHTAKAVARGA ANALYSIS", "अष्टकवर्ग विश्लेषण"),
    ASHTAK_SAV_HEADER("SARVASHTAKAVARGA (Combined Strength)", "सर्वाष्टकवर्ग (संयुक्त बल)"),
    ASHTAK_BAV_HEADER("BHINNASHTAKAVARGA (Individual Planet Strengths)", "भिन्नाष्टकवर्ग (व्यक्तिगत ग्रह बल)"),
    ASHTAK_TOTAL_SAV_BINDUS("Total SAV Bindus:", "कुल SAV बिन्दुहरू:"),
    ASHTAK_AVG_PER_SIGN("Average per Sign:", "प्रति राशि औसत:"),
    ASHTAK_NOT_APPLICABLE("Ashtakavarga not applicable for %s", "%s को लागि अष्टकवर्ग लागू हुँदैन"),

    // Ashtakavarga Planet Effects
    ASHTAK_SUN_EFFECTS("authority, father, health, government, career", "अधिकार, पिता, स्वास्थ्य, सरकार, क्यारियर"),
    ASHTAK_MOON_EFFECTS("mind, emotions, mother, public image", "मन, भावना, आमा, सार्वजनिक छवि"),
    ASHTAK_MARS_EFFECTS("energy, siblings, property, courage", "ऊर्जा, भाइबहिनी, सम्पत्ति, साहस"),
    ASHTAK_MERCURY_EFFECTS("communication, intellect, business, education", "सञ्चार, बुद्धि, व्यापार, शिक्षा"),
    ASHTAK_JUPITER_EFFECTS("wisdom, children, fortune, spirituality", "ज्ञान, सन्तान, भाग्य, आध्यात्मिकता"),
    ASHTAK_VENUS_EFFECTS("relationships, luxury, arts, vehicles", "सम्बन्ध, विलासिता, कला, सवारी"),
    ASHTAK_SATURN_EFFECTS("career, longevity, discipline, challenges", "क्यारियर, दीर्घायु, अनुशासन, चुनौतीहरू"),
    ASHTAK_GENERAL_EFFECTS("general matters", "सामान्य मामिलाहरू"),

    // House matters for transit interpretation
    ASHTAK_HOUSE_1_MATTERS("self, personality, health", "आफू, व्यक्तित्व, स्वास्थ्य"),
    ASHTAK_HOUSE_2_MATTERS("wealth, family, speech", "धन, परिवार, वाणी"),
    ASHTAK_HOUSE_3_MATTERS("courage, siblings, communication", "साहस, भाइबहिनी, सञ्चार"),
    ASHTAK_HOUSE_4_MATTERS("home, mother, comfort", "घर, आमा, आराम"),
    ASHTAK_HOUSE_5_MATTERS("children, intelligence, romance", "सन्तान, बुद्धि, प्रेम"),
    ASHTAK_HOUSE_6_MATTERS("enemies, health issues, service", "शत्रु, स्वास्थ्य समस्या, सेवा"),
    ASHTAK_HOUSE_7_MATTERS("partnership, marriage, business", "साझेदारी, विवाह, व्यापार"),
    ASHTAK_HOUSE_8_MATTERS("transformation, inheritance, occult", "रूपान्तरण, सम्पत्ति, गुप्त विद्या"),
    ASHTAK_HOUSE_9_MATTERS("luck, father, higher learning", "भाग्य, पिता, उच्च शिक्षा"),
    ASHTAK_HOUSE_10_MATTERS("career, status, authority", "क्यारियर, हैसियत, अधिकार"),
    ASHTAK_HOUSE_11_MATTERS("gains, friends, aspirations", "लाभ, साथीहरू, आकांक्षाहरू"),
    ASHTAK_HOUSE_12_MATTERS("losses, spirituality, foreign", "हानि, आध्यात्मिकता, विदेश"),

    // Transit interpretation templates
    TRANSIT_EXCELLENT_TEMPLATE("Transit through %s with %d BAV bindus and %d SAV bindus indicates excellent results. Matters related to %s will flourish. Areas of %s receive strong positive influence.", "%s मा %d BAV बिन्दु र %d SAV बिन्दुसँगको गोचरले उत्कृष्ट परिणाम देखाउँछ। %s सम्बन्धी मामिलाहरू फस्टाउनेछन्। %s को क्षेत्रमा बलियो सकारात्मक प्रभाव पर्नेछ।"),
    TRANSIT_GOOD_TEMPLATE("Transit through %s brings favorable results with %d BAV and %d SAV bindus. Good progress expected in %s. %s areas are positively influenced.", "%s मा %d BAV र %d SAV बिन्दुसँगको गोचरले अनुकूल परिणामहरू ल्याउँछ। %s मा राम्रो प्रगति अपेक्षित छ। %s क्षेत्रमा सकारात्मक प्रभाव पर्नेछ।"),
    TRANSIT_AVERAGE_TEMPLATE("Transit through %s (%d BAV, %d SAV) brings mixed results. Some progress in %s with occasional challenges. Balance needed in %s.", "%s मा (%d BAV, %d SAV) गोचरले मिश्रित परिणामहरू ल्याउँछ। %s मा केही प्रगति तर कहिलेकाहीँ चुनौतीहरू। %s मा सन्तुलन आवश्यक।"),
    TRANSIT_BELOW_AVG_TEMPLATE("Transit through %s (%d BAV, %d SAV) suggests caution needed. %s matters may face delays. Extra effort required in %s areas.", "%s मा (%d BAV, %d SAV) गोचरले सावधानी आवश्यक देखाउँछ। %s मामिलाहरूमा ढिलाइ हुन सक्छ। %s क्षेत्रमा थप प्रयास आवश्यक।"),
    TRANSIT_CHALLENGING_TEMPLATE("Challenging transit through %s with only %d BAV and %d SAV bindus. Difficulties possible in %s. Patience needed for %s matters.", "%s मा केवल %d BAV र %d SAV बिन्दुसँगको चुनौतीपूर्ण गोचर। %s मा कठिनाइहरू सम्भव। %s मामिलाहरूमा धैर्य आवश्यक।"),
    TRANSIT_DIFFICULT_TEMPLATE("Difficult transit period through %s (%d BAV, %d SAV). Significant challenges in %s areas. Careful handling of %s required.", "%s मा (%d BAV, %d SAV) कठिन गोचर अवधि। %s क्षेत्रमा महत्त्वपूर्ण चुनौतीहरू। %s को सावधानीपूर्वक व्यवस्थापन आवश्यक।"),

    // ============================================
    // NAKSHATRA DETAILS (For ChartDialogs)
    // ============================================
    NAKSHATRA_SYMBOL("Symbol", "प्रतीक"),
    NAKSHATRA_DEITY("Deity", "देवता"),
    NAKSHATRA_PADA("Pada", "पद"),
    NAKSHATRA_GUNA("Guna", "गुण"),
    NAKSHATRA_GANA("Gana", "गण"),
    NAKSHATRA_YONI("Yoni", "योनि"),
    NAKSHATRA_ANIMAL("Animal", "पशु"),
    NAKSHATRA_BIRD("Bird", "पक्षी"),
    NAKSHATRA_TREE("Tree", "वृक्ष"),
    NAKSHATRA_NATURE("Nature", "प्रकृति"),
    NAKSHATRA_GENDER("Gender", "लिङ्ग"),
    NAKSHATRA_MALE("Male", "पुरुष"),
    NAKSHATRA_FEMALE("Female", "महिला"),
    NAKSHATRA_CAREERS("Careers", "क्यारियर"),

    // Nakshatra Nature types
    NAKSHATRA_NATURE_SWIFT("Swift (Kshipra)", "क्षिप्र (छिटो)"),
    NAKSHATRA_NATURE_FIERCE("Fierce (Ugra)", "उग्र (तीव्र)"),
    NAKSHATRA_NATURE_MIXED("Mixed (Mishra)", "मिश्र"),
    NAKSHATRA_NATURE_FIXED("Fixed (Dhruva)", "ध्रुव (स्थिर)"),
    NAKSHATRA_NATURE_SOFT("Soft (Mridu)", "मृदु (कोमल)"),
    NAKSHATRA_NATURE_SHARP("Sharp (Tikshna)", "तीक्ष्ण"),
    NAKSHATRA_NATURE_MOVABLE("Movable (Chara)", "चर"),
    NAKSHATRA_NATURE_LIGHT("Light (Laghu)", "लघु"),

    // Guna types
    GUNA_RAJAS("Rajas", "रजस्"),
    GUNA_TAMAS("Tamas", "तमस्");
}

/**
 * Divisional charts, Shadbala, and Dosha (Sade Sati, Manglik, Pitra) string keys
 * Part 4 of 4 split enums to avoid JVM method size limit
 */
enum class StringKeyDosha(override val en: String, override val ne: String) : StringKeyInterface {
    GUNA_SATTVA("Sattva", "सत्त्व"),

    // ============================================
    // DIVISIONAL CHART TITLES (For DivisionalChartCalculator)
    // ============================================
    VARGA_D1_TITLE("Rashi (D1)", "राशि (D1)"),
    VARGA_D1_DESC("Physical Body, General Life", "भौतिक शरीर, सामान्य जीवन"),
    VARGA_D2_TITLE("Hora (D2)", "होरा (D2)"),
    VARGA_D2_DESC("Wealth, Prosperity", "धन, समृद्धि"),
    VARGA_D3_TITLE("Drekkana (D3)", "द्रेक्काण (D3)"),
    VARGA_D3_DESC("Siblings, Courage", "भाइबहिनी, साहस"),
    VARGA_D4_TITLE("Chaturthamsa (D4)", "चतुर्थांश (D4)"),
    VARGA_D4_DESC("Fortune, Property", "भाग्य, सम्पत्ति"),
    VARGA_D7_TITLE("Saptamsa (D7)", "सप्तमांश (D7)"),
    VARGA_D7_DESC("Children, Progeny", "सन्तान"),
    VARGA_D9_TITLE("Navamsa (D9)", "नवमांश (D9)"),
    VARGA_D9_DESC("Marriage, Dharma", "विवाह, धर्म"),
    VARGA_D10_TITLE("Dasamsa (D10)", "दशमांश (D10)"),
    VARGA_D10_DESC("Career, Profession", "क्यारियर, पेशा"),
    VARGA_D12_TITLE("Dwadasamsa (D12)", "द्वादशांश (D12)"),
    VARGA_D12_DESC("Parents, Ancestry", "आमाबुवा, पुर्खा"),
    VARGA_D16_TITLE("Shodasamsa (D16)", "षोडशांश (D16)"),
    VARGA_D16_DESC("Vehicles, Pleasures", "सवारी, आनन्द"),
    VARGA_D20_TITLE("Vimsamsa (D20)", "विंशांश (D20)"),
    VARGA_D20_DESC("Spiritual Life", "आध्यात्मिक जीवन"),
    VARGA_D24_TITLE("Siddhamsa (D24)", "सिद्धांश (D24)"),
    VARGA_D24_DESC("Education, Learning", "शिक्षा, सिकाइ"),
    VARGA_D27_TITLE("Bhamsa (D27)", "भांश (D27)"),
    VARGA_D27_DESC("Strength, Weakness", "बल, कमजोरी"),
    VARGA_D30_TITLE("Trimsamsa (D30)", "त्रिंशांश (D30)"),
    VARGA_D30_DESC("Evils, Misfortunes", "अशुभ, दुर्भाग्य"),
    VARGA_D40_TITLE("Khavedamsa (D40)", "खवेदांश (D40)"),
    VARGA_D40_DESC("Auspicious/Inauspicious Effects", "शुभाशुभ प्रभावहरू"),
    VARGA_D45_TITLE("Akshavedamsa (D45)", "अक्षवेदांश (D45)"),
    VARGA_D45_DESC("General Indications", "सामान्य सङ्केतहरू"),
    VARGA_D60_TITLE("Shashtiamsa (D60)", "षष्ट्यंश (D60)"),
    VARGA_D60_DESC("Past Life Karma", "पूर्वजन्मको कर्म"),
    VARGA_PLANETARY_POSITIONS("PLANETARY POSITIONS", "ग्रह स्थितिहरू"),

    // ============================================
    // HOROSCOPE CALCULATOR (Life Areas & Themes)
    // ============================================
    LIFE_AREA_SPIRITUAL("Spiritual Growth", "आध्यात्मिक वृद्धि"),

    // Weekly Themes
    THEME_BALANCE("Balance", "सन्तुलन"),
    THEME_DYNAMIC_ACTION("Dynamic Action", "गतिशील कार्य"),
    THEME_PRACTICAL_PROGRESS("Practical Progress", "व्यावहारिक प्रगति"),
    THEME_SOCIAL_CONNECTIONS("Social Connections", "सामाजिक सम्बन्धहरू"),
    THEME_EMOTIONAL_INSIGHT("Emotional Insight", "भावनात्मक अन्तर्दृष्टि"),
    THEME_SELF_EXPRESSION("Self-Expression", "आत्म-अभिव्यक्ति"),
    THEME_TRANSFORMATION("Transformation", "रूपान्तरण"),
    THEME_SPIRITUAL_LIBERATION("Spiritual Liberation", "आध्यात्मिक मुक्ति"),

    // Element-based advice
    ADVICE_FIRE_ELEMENT("Take bold action and express yourself confidently.", "साहसिक कदम चाल्नुहोस् र आत्मविश्वासका साथ आफूलाई अभिव्यक्त गर्नुहोस्।"),
    ADVICE_EARTH_ELEMENT("Focus on practical matters and material progress.", "व्यावहारिक मामिलाहरू र भौतिक प्रगतिमा ध्यान दिनुहोस्।"),
    ADVICE_AIR_ELEMENT("Engage in social activities and intellectual pursuits.", "सामाजिक क्रियाकलापहरू र बौद्धिक खोजमा संलग्न हुनुहोस्।"),
    ADVICE_WATER_ELEMENT("Trust your intuition and honor your emotions.", "आफ्नो अन्तर्ज्ञानमा विश्वास गर्नुहोस् र भावनाहरूलाई सम्मान गर्नुहोस्।"),

    // Week types
    WEEK_OPPORTUNITIES("Week of Opportunities", "अवसरहरूको हप्ता"),
    WEEK_STEADY_PROGRESS("Steady Progress", "स्थिर प्रगति"),
    WEEK_MINDFUL_NAVIGATION("Mindful Navigation", "सचेत नेभिगेसन"),

    // Time periods
    TIME_MORNING("Morning hours", "बिहानको समय"),
    TIME_AFTERNOON("Afternoon hours", "दिउँसोको समय"),
    TIME_EVENING("Evening hours", "साँझको समय"),

    // ============================================
    // MATCHMAKING CALCULATOR (Additional Vashya types)
    // ============================================
    VASHYA_QUADRUPED("Quadruped", "चतुष्पाद"),
    VASHYA_HUMAN("Human", "मनुष्य"),
    VASHYA_AQUATIC("Aquatic", "जलचर"),
    VASHYA_WILD("Wild", "वन्य"),
    VASHYA_INSECT("Insect", "कीट"),

    // Compatibility ratings (additional)
    COMPAT_BELOW_AVG("Below Average", "औसतभन्दा कम"),
    COMPAT_BELOW_AVG_DESC("Caution advised. Several compatibility issues that need addressing through remedies and counseling.", "सावधानी सल्लाह दिइएको। उपाय र परामर्शबाट सम्बोधन गर्नुपर्ने धेरै अनुकूलता समस्याहरू।"),

    // Yogini planet associations (additional)
    YOGINI_CHANDRA("Chandra (Moon)", "चन्द्र"),
    YOGINI_SURYA("Surya (Sun)", "सूर्य"),
    YOGINI_GURU("Guru (Jupiter)", "गुरु"),
    YOGINI_MANGAL("Mangal (Mars)", "मंगल"),
    YOGINI_BUDHA("Budha (Mercury)", "बुध"),
    YOGINI_SHANI("Shani (Saturn)", "शनि"),
    YOGINI_SHUKRA("Shukra (Venus)", "शुक्र"),
    YOGINI_RAHU("Rahu", "राहु"),

    // ============================================
    // REPORT HEADERS
    // ============================================
    REPORT_VEDIC_REMEDIES("VEDIC ASTROLOGY REMEDIES REPORT", "वैदिक ज्योतिष उपाय प्रतिवेदन"),
    REPORT_PLANETS_NEEDING_ATTENTION("PLANETS REQUIRING ATTENTION:", "ध्यान आवश्यक ग्रहहरू:"),

    // ============================================
    // ERROR MESSAGES (Additional for GeocodingService)
    // ============================================
    ERROR_QUERY_MIN_CHARS("Query must be at least 2 characters", "खोजी कम्तीमा २ वर्णको हुनुपर्छ"),
    ERROR_CONNECTION_TIMEOUT("Connection timeout. Please check your internet.", "जडान समय समाप्त। कृपया आफ्नो इन्टरनेट जाँच गर्नुहोस्।"),
    ERROR_NO_INTERNET("No internet connection.", "इन्टरनेट जडान छैन।"),
    ERROR_UNKNOWN("Unknown error", "अज्ञात त्रुटि"),

    // ============================================
    // GENERAL UI LABELS
    // ============================================
    LABEL_UNKNOWN("Unknown", "अज्ञात"),
    LABEL_REQUIRED("Required:", "आवश्यक:"),
    LABEL_RUPAS("Rupas", "रूपा"),
    LABEL_PERCENT_REQUIRED("% of required", "आवश्यकको %"),
    LABEL_TODAY("Today", "आज"),
    LABEL_PREVIOUS_MONTH("Previous month", "अघिल्लो महिना"),
    LABEL_NEXT_MONTH("Next month", "अर्को महिना"),
    LABEL_AD("AD", "ई.सं."),
    LABEL_BS("BS", "बि.सं."),

    // Weekday abbreviations
    WEEKDAY_SU("Su", "आ"),
    WEEKDAY_MO("Mo", "सो"),
    WEEKDAY_TU("Tu", "मं"),
    WEEKDAY_WE("We", "बु"),
    WEEKDAY_TH("Th", "बि"),
    WEEKDAY_FR("Fr", "शु"),
    WEEKDAY_SA("Sa", "श"),

    // ============================================
    // COLORS (For Horoscope advice)
    // ============================================
    COLOR_RED_ORANGE_GOLD("Red, Orange, or Gold", "रातो, सुन्तला, वा सुनौलो"),
    COLOR_GREEN_BROWN_WHITE("Green, Brown, or White", "हरियो, खैरो, वा सेतो"),
    COLOR_BLUE_LIGHT_SILVER("Blue, Light Blue, or Silver", "निलो, हल्का निलो, वा चाँदी"),
    COLOR_WHITE_CREAM_SEA("White, Cream, or Sea Green", "सेतो, क्रिम, वा समुद्री हरियो"),

    // ============================================
    // GEMSTONES
    // ============================================
    GEMSTONE_RUBY("Ruby", "माणिक"),
    GEMSTONE_PEARL("Pearl", "मोती"),
    GEMSTONE_RED_CORAL("Red Coral", "मूंगा"),
    GEMSTONE_EMERALD("Emerald", "पन्ना"),
    GEMSTONE_YELLOW_SAPPHIRE("Yellow Sapphire", "पुखराज"),
    GEMSTONE_DIAMOND("Diamond/White Sapphire", "हीरा/सेतो नीलम"),
    GEMSTONE_BLUE_SAPPHIRE("Blue Sapphire", "नीलम"),
    GEMSTONE_HESSONITE("Hessonite", "गोमेद"),
    GEMSTONE_CATS_EYE("Cat's Eye", "लहसुनिया"),

    // ============================================
    // MOON PHASES
    // ============================================
    MOON_FIRST_QUARTER("First Quarter Moon", "पहिलो चौथाई चन्द्रमा"),
    MOON_FIRST_QUARTER_DESC("Good for taking action", "कार्य गर्नको लागि राम्रो"),
    MOON_FULL("Full Moon", "पूर्णिमा"),
    MOON_FULL_DESC("Emotional peak - completion energy", "भावनात्मक शिखर - पूर्णता ऊर्जा"),

    // ============================================
    // PRASHNA HOUSE SIGNIFICATIONS
    // ============================================
    PRASHNA_HOUSE_1_NAME("Lagna/Querent", "लग्न/प्रश्नकर्ता"),
    PRASHNA_HOUSE_2_NAME("Dhana", "धन"),
    PRASHNA_HOUSE_3_NAME("Sahaja", "सहज"),
    PRASHNA_HOUSE_4_NAME("Sukha", "सुख"),
    PRASHNA_HOUSE_5_NAME("Putra", "पुत्र"),
    PRASHNA_HOUSE_6_NAME("Ripu", "रिपु"),
    PRASHNA_HOUSE_7_NAME("Kalatra", "कलत्र"),
    PRASHNA_HOUSE_8_NAME("Ayu", "आयु"),
    PRASHNA_HOUSE_9_NAME("Dharma", "धर्म"),
    PRASHNA_HOUSE_10_NAME("Karma", "कर्म"),
    PRASHNA_HOUSE_11_NAME("Labha", "लाभ"),
    PRASHNA_HOUSE_12_NAME("Vyaya", "व्यय"),

    // Body parts for Prashna
    BODY_PART_HEAD("Head", "टाउको"),
    BODY_PART_FACE("Face/Mouth", "अनुहार/मुख"),
    BODY_PART_ARMS("Arms/Shoulders", "पाखुरा/काँध"),
    BODY_PART_CHEST("Chest", "छाती"),
    BODY_PART_UPPER_ABDOMEN("Upper Abdomen", "माथिल्लो पेट"),
    BODY_PART_LOWER_ABDOMEN("Lower Abdomen", "तल्लो पेट"),
    BODY_PART_BELOW_NAVEL("Below Navel", "नाभिमुनि"),
    BODY_PART_REPRODUCTIVE("Reproductive organs", "प्रजनन अंगहरू"),
    BODY_PART_THIGHS("Thighs", "जाँघ"),
    BODY_PART_KNEES("Knees", "घुँडा"),
    BODY_PART_CALVES("Calves/Shins", "पिँडौला"),
    BODY_PART_FEET("Feet", "खुट्टा"),

    // Prashna terms
    PRASHNA_MOOK("Mook (Dumb) Prashna", "मूक प्रश्न"),
    PRASHNA_ARUDHA("Arudha Lagna", "आरूढ लग्न"),

    // ============================================
    // REMEDIES REPORT LABELS
    // ============================================
    REPORT_NAME_LABEL("Name:", "नाम:"),
    REPORT_ASCENDANT_LABEL("Ascendant:", "लग्न:"),
    REPORT_MANTRA_LABEL("Mantra:", "मन्त्र:"),

    // ============================================
    // PLANET LIFE AREAS (For remedies)
    // ============================================
    PLANET_LIFE_AREA_SUN("authority, career, government favor, father's health, self-confidence", "अधिकार, करियर, सरकारी कृपा, बुबाको स्वास्थ्य, आत्मविश्वास"),
    PLANET_LIFE_AREA_MOON("mental peace, mother's health, emotional stability, public relations", "मानसिक शान्ति, आमाको स्वास्थ्य, भावनात्मक स्थिरता, जनसम्पर्क"),
    PLANET_LIFE_AREA_MARS("courage, siblings, property matters, physical strength, competition", "साहस, भाइबहिनी, सम्पत्ति मामिला, शारीरिक शक्ति, प्रतिस्पर्धा"),
    PLANET_LIFE_AREA_MERCURY("intellect, communication, business, education, nervous system", "बुद्धि, सञ्चार, व्यापार, शिक्षा, स्नायु प्रणाली"),
    PLANET_LIFE_AREA_JUPITER("wisdom, children, wealth, spirituality, teachers, dharma", "ज्ञान, सन्तान, धन, आध्यात्मिकता, गुरु, धर्म"),
    PLANET_LIFE_AREA_VENUS("marriage, love, luxury, art, vehicles, pleasure", "विवाह, प्रेम, विलासिता, कला, सवारी, आनन्द"),
    PLANET_LIFE_AREA_SATURN("longevity, service, discipline, karma, delays, chronic issues", "दीर्घायु, सेवा, अनुशासन, कर्म, ढिलाइ, दीर्घकालीन समस्या"),
    PLANET_LIFE_AREA_RAHU("foreign connections, unconventional success, material desires", "विदेशी सम्बन्ध, अपरम्परागत सफलता, भौतिक इच्छा"),
    PLANET_LIFE_AREA_KETU("spirituality, liberation, past karma, psychic abilities", "आध्यात्मिकता, मोक्ष, पूर्व कर्म, मानसिक क्षमता"),

    // ============================================
    // PLANETARY WEEKDAYS
    // ============================================
    PLANET_DAY_SUN("Sunday", "आइतबार"),
    PLANET_DAY_MOON("Monday", "सोमबार"),
    PLANET_DAY_MARS("Tuesday", "मंगलबार"),
    PLANET_DAY_MERCURY("Wednesday", "बुधबार"),
    PLANET_DAY_JUPITER("Thursday", "बिहिबार"),
    PLANET_DAY_VENUS("Friday", "शुक्रबार"),
    PLANET_DAY_SATURN("Saturday", "शनिबार"),
    PLANET_DAY_RAHU("Saturday", "शनिबार"),
    PLANET_DAY_KETU("Tuesday", "मंगलबार"),

    // ============================================
    // SYNASTRY / CHART COMPARISON
    // ============================================
    SYNASTRY_TITLE("Chart Comparison", "कुण्डली तुलना"),
    SYNASTRY_SUBTITLE("Synastry Analysis", "सिनेस्ट्री विश्लेषण"),
    SYNASTRY_SELECT_CHARTS("Select Charts to Compare", "तुलना गर्न कुण्डलीहरू छान्नुहोस्"),
    SYNASTRY_CHART_1("Chart 1", "कुण्डली १"),
    SYNASTRY_CHART_2("Chart 2", "कुण्डली २"),
    SYNASTRY_OVERVIEW("Overview", "अवलोकन"),
    SYNASTRY_ASPECTS("Inter-Aspects", "अन्तर-दृष्टिहरू"),
    SYNASTRY_HOUSES("House Overlays", "भाव ओभरले"),
    SYNASTRY_COMPATIBILITY("Compatibility", "अनुकूलता"),
    SYNASTRY_NO_ASPECTS("No significant aspects found", "कुनै महत्त्वपूर्ण दृष्टि फेला परेन"),
    SYNASTRY_OVERALL_SCORE("Overall Compatibility Score", "समग्र अनुकूलता स्कोर"),
    SYNASTRY_HARMONIOUS("Harmonious Aspects", "सामञ्जस्यपूर्ण दृष्टिहरू"),
    SYNASTRY_CHALLENGING("Challenging Aspects", "चुनौतीपूर्ण दृष्टिहरू"),
    SYNASTRY_PLANET_CONNECTIONS("Planetary Connections", "ग्रह सम्बन्धहरू"),
    SYNASTRY_HOUSE_INFLUENCE("House Influences", "भाव प्रभावहरू"),
    SYNASTRY_PLANET_IN_HOUSE("%s in %s's House %d", "%s को भाव %d मा %s"),
    SYNASTRY_ASPECT_ORB("Orb: %.1f°", "ओर्ब: %.1f°"),
    SYNASTRY_APPLYING("Applying", "निकट आउँदै"),
    SYNASTRY_SEPARATING("Separating", "टाढा जाँदै"),
    SYNASTRY_CONJUNCTION("Conjunction", "युति"),
    SYNASTRY_OPPOSITION("Opposition", "प्रतिपक्ष"),
    SYNASTRY_TRINE("Trine", "त्रिकोण"),
    SYNASTRY_SQUARE("Square", "चतुर्थांश"),
    SYNASTRY_SEXTILE("Sextile", "षड्भाग"),
    SYNASTRY_STRONG("Strong", "बलियो"),
    SYNASTRY_MODERATE("Moderate", "मध्यम"),
    SYNASTRY_WEAK("Weak", "कमजोर"),
    SYNASTRY_KEY_ASPECTS("Key Synastry Aspects", "मुख्य सिनेस्ट्री दृष्टिहरू"),
    SYNASTRY_EMOTIONAL_BOND("Emotional Bond", "भावनात्मक बन्धन"),
    SYNASTRY_COMMUNICATION("Communication", "सञ्चार"),
    SYNASTRY_ROMANCE("Romance & Attraction", "रोमान्स र आकर्षण"),
    SYNASTRY_STABILITY("Long-term Stability", "दीर्घकालीन स्थिरता"),
    SYNASTRY_GROWTH("Growth & Evolution", "वृद्धि र विकास"),
    SYNASTRY_INFO_TITLE("About Synastry", "सिनेस्ट्रीको बारेमा"),
    SYNASTRY_INFO_DESC("Synastry compares two birth charts to analyze relationship dynamics, compatibility, and areas of harmony or challenge between individuals.", "सिनेस्ट्रीले दुई जन्म कुण्डलीहरू तुलना गरेर व्यक्तिहरू बीचको सम्बन्ध गतिशीलता, अनुकूलता, र सामञ्जस्य वा चुनौतीका क्षेत्रहरू विश्लेषण गर्दछ।"),
    SYNASTRY_SWAP("Swap Charts", "कुण्डलीहरू स्वाप गर्नुहोस्"),
    SYNASTRY_CLEAR("Clear Selection", "छनौट खाली गर्नुहोस्"),
    SYNASTRY_CALCULATE("Analyze Synastry", "सिनेस्ट्री विश्लेषण गर्नुहोस्"),
    SYNASTRY_ANALYZING("Analyzing synastry...", "सिनेस्ट्री विश्लेषण गर्दै..."),
    SYNASTRY_SUN_MOON("Sun-Moon Aspects", "सूर्य-चन्द्र दृष्टिहरू"),
    SYNASTRY_VENUS_MARS("Venus-Mars Aspects", "शुक्र-मंगल दृष्टिहरू"),
    SYNASTRY_ASCENDANT("Ascendant Connections", "लग्न सम्बन्धहरू"),
    SYNASTRY_MUTUAL_ASPECTS("Mutual Aspects", "पारस्परिक दृष्टिहरू"),
    SYNASTRY_SELECT_BOTH("Please select both charts to compare", "कृपया तुलना गर्न दुवै कुण्डली छान्नुहोस्"),

    // ============================================
    // NAKSHATRA ANALYSIS
    // ============================================
    NAKSHATRA_TITLE("Nakshatra Analysis", "नक्षत्र विश्लेषण"),
    NAKSHATRA_SUBTITLE("Lunar Mansion Analysis", "चन्द्र भवन विश्लेषण"),
    NAKSHATRA_OVERVIEW("Overview", "अवलोकन"),
    NAKSHATRA_DETAILS("Details", "विवरणहरू"),
    NAKSHATRA_COMPATIBILITY("Compatibility", "अनुकूलता"),
    NAKSHATRA_REMEDIES("Remedies", "उपायहरू"),
    NAKSHATRA_BIRTH_STAR("Birth Nakshatra", "जन्म नक्षत्र"),
    NAKSHATRA_MOON_POSITION("Moon Nakshatra", "चन्द्र नक्षत्र"),
    NAKSHATRA_RULER("Ruling Planet", "स्वामी ग्रह"),
    NAKSHATRA_ELEMENT("Element", "तत्व"),
    NAKSHATRA_QUALITY("Quality", "गुण"),
    NAKSHATRA_CASTE("Caste", "वर्ण"),
    NAKSHATRA_DIRECTION("Direction", "दिशा"),
    NAKSHATRA_BODY_PART("Body Part", "शरीरको अंग"),
    NAKSHATRA_DOSHA("Dosha", "दोष"),
    NAKSHATRA_FAVORABLE_DAYS("Favorable Days", "अनुकूल दिनहरू"),
    NAKSHATRA_LUCKY_NUMBERS("Lucky Numbers", "भाग्यशाली अंकहरू"),
    NAKSHATRA_LUCKY_COLORS("Lucky Colors", "भाग्यशाली रंगहरू"),
    NAKSHATRA_LUCKY_STONES("Lucky Gemstones", "भाग्यशाली रत्नहरू"),
    NAKSHATRA_CHARACTERISTICS("Characteristics", "विशेषताहरू"),
    NAKSHATRA_STRENGTHS("Strengths", "शक्तिहरू"),
    NAKSHATRA_WEAKNESSES("Weaknesses", "कमजोरीहरू"),
    NAKSHATRA_CAREER("Career Aptitude", "क्यारियर योग्यता"),
    NAKSHATRA_HEALTH("Health Tendencies", "स्वास्थ्य प्रवृत्तिहरू"),
    NAKSHATRA_RELATIONSHIP("Relationship Style", "सम्बन्ध शैली"),
    NAKSHATRA_SPIRITUAL("Spiritual Path", "आध्यात्मिक मार्ग"),
    NAKSHATRA_MANTRA("Nakshatra Mantra", "नक्षत्र मन्त्र"),
    NAKSHATRA_INFO_TITLE("About Nakshatras", "नक्षत्रहरूको बारेमा"),
    NAKSHATRA_INFO_DESC("Nakshatras are the 27 lunar mansions in Vedic astrology, each spanning 13°20' of the zodiac. They reveal deeper psychological patterns and spiritual tendencies.", "नक्षत्रहरू वैदिक ज्योतिषमा २७ चन्द्र भवनहरू हुन्, प्रत्येक राशिचक्रको १३°२०' फैलिएको। तिनीहरूले गहिरो मनोवैज्ञानिक ढाँचा र आध्यात्मिक प्रवृत्तिहरू प्रकट गर्छन्।"),
    NAKSHATRA_ALL_PLANETS("Planetary Nakshatras", "ग्रह नक्षत्रहरू"),
    NAKSHATRA_DASHA_LORD("Dasha Lord", "दशा स्वामी"),
    NAKSHATRA_SPAN("Nakshatra Span", "नक्षत्र विस्तार"),
    NAKSHATRA_DEGREE_IN("Degree in Nakshatra", "नक्षत्रमा अंश"),
    NAKSHATRA_TARABALA("Tarabala Analysis", "ताराबल विश्लेषण"),
    NAKSHATRA_CHANDRABALA("Chandrabala", "चन्द्रबल"),
    NAKSHATRA_COMPATIBLE_WITH("Compatible with", "अनुकूल"),
    NAKSHATRA_INCOMPATIBLE_WITH("Incompatible with", "अनुकूल छैन"),
    NAKSHATRA_VEDHA_PAIRS("Vedha Pairs", "वेध जोडीहरू"),
    NAKSHATRA_RAJJU_TYPE("Rajju Type", "रज्जु प्रकार"),

    // ============================================
    // SHADBALA ANALYSIS
    // ============================================
    SHADBALA_TITLE("Shadbala", "षड्बल"),
    SHADBALA_SUBTITLE("Six-fold Planetary Strength", "छवटा ग्रह शक्ति"),
    SHADBALA_OVERVIEW("Overview", "अवलोकन"),
    SHADBALA_DETAILS("Detailed Analysis", "विस्तृत विश्लेषण"),
    SHADBALA_COMPARISON("Comparison", "तुलना"),
    SHADBALA_TOTAL_STRENGTH("Total Strength", "कुल शक्ति"),
    SHADBALA_RUPAS("Rupas", "रूपा"),
    SHADBALA_VIRUPAS("Virupas", "विरूपा"),
    SHADBALA_REQUIRED("Required", "आवश्यक"),
    SHADBALA_PERCENTAGE("Percentage", "प्रतिशत"),
    SHADBALA_RATING("Rating", "मूल्यांकन"),
    SHADBALA_STRONGEST_PLANET("Strongest Planet", "सबैभन्दा बलियो ग्रह"),
    SHADBALA_WEAKEST_PLANET("Weakest Planet", "सबैभन्दा कमजोर ग्रह"),
    SHADBALA_OVERALL_STRENGTH("Overall Chart Strength", "समग्र कुण्डली शक्ति"),
    SHADBALA_STRONG_COUNT("%d planets above required", "%d ग्रह आवश्यकताभन्दा माथि"),
    SHADBALA_WEAK_COUNT("%d planets below required", "%d ग्रह आवश्यकताभन्दा तल"),
    SHADBALA_STHANA_BALA("Sthana Bala", "स्थान बल"),
    SHADBALA_STHANA_BALA_DESC("Positional Strength", "स्थितिगत शक्ति"),
    SHADBALA_DIG_BALA("Dig Bala", "दिग् बल"),
    SHADBALA_DIG_BALA_DESC("Directional Strength", "दिशागत शक्ति"),
    SHADBALA_KALA_BALA("Kala Bala", "काल बल"),
    SHADBALA_KALA_BALA_DESC("Temporal Strength", "समयगत शक्ति"),
    SHADBALA_CHESTA_BALA("Chesta Bala", "चेष्टा बल"),
    SHADBALA_CHESTA_BALA_DESC("Motional Strength", "गतिशील शक्ति"),
    SHADBALA_NAISARGIKA_BALA("Naisargika Bala", "नैसर्गिक बल"),
    SHADBALA_NAISARGIKA_BALA_DESC("Natural Strength", "प्राकृतिक शक्ति"),
    SHADBALA_DRIK_BALA("Drik Bala", "दृक् बल"),
    SHADBALA_DRIK_BALA_DESC("Aspectual Strength", "दृष्टिगत शक्ति"),
    SHADBALA_UCCHA_BALA("Uccha Bala", "उच्च बल"),
    SHADBALA_SAPTAVARGAJA_BALA("Saptavargaja Bala", "सप्तवर्गज बल"),
    SHADBALA_OJHAYUGMA_BALA("Ojhayugma Bala", "ओझायुग्म बल"),
    SHADBALA_KENDRADI_BALA("Kendradi Bala", "केन्द्रादि बल"),
    SHADBALA_DREKKANA_BALA("Drekkana Bala", "द्रेक्काण बल"),
    SHADBALA_NATHONNATHA_BALA("Nathonnatha Bala", "नथोन्नथ बल"),
    SHADBALA_PAKSHA_BALA("Paksha Bala", "पक्ष बल"),
    SHADBALA_TRIBHAGA_BALA("Tribhaga Bala", "त्रिभाग बल"),
    SHADBALA_HORA_BALA("Hora/Dina/Masa/Varsha", "होरा/दिन/मास/वर्ष"),
    SHADBALA_AYANA_BALA("Ayana Bala", "अयन बल"),
    SHADBALA_YUDDHA_BALA("Yuddha Bala", "युद्ध बल"),
    SHADBALA_INFO_TITLE("About Shadbala", "षड्बलको बारेमा"),
    SHADBALA_INFO_DESC("Shadbala is the six-fold strength calculation system in Vedic astrology that determines a planet's capacity to deliver its significations. Each planet needs to meet a minimum threshold to be considered functionally strong.", "षड्बल वैदिक ज्योतिषमा छवटा शक्ति गणना प्रणाली हो जसले ग्रहको आफ्नो संकेतहरू प्रदान गर्ने क्षमता निर्धारण गर्दछ। प्रत्येक ग्रहलाई कार्यात्मक रूपमा बलियो मानिनको लागि न्यूनतम सीमा पूरा गर्नुपर्छ।"),
    SHADBALA_INTERPRETATION("Interpretation", "व्याख्या"),
    SHADBALA_PLANET_ANALYSIS("%s Analysis", "%s विश्लेषण"),
    SHADBALA_MEETS_REQUIREMENT("Meets required strength", "आवश्यक शक्ति पूरा गर्दछ"),
    SHADBALA_BELOW_REQUIREMENT("Below required strength", "आवश्यक शक्तिभन्दा तल"),
    SHADBALA_BREAKDOWN("Strength Breakdown", "शक्ति विवरण"),
    SHADBALA_CALCULATING("Calculating Shadbala...", "षड्बल गणना गर्दै..."),
    SHADBALA_CHART_ANALYSIS("Chart Strength Analysis", "कुण्डली शक्ति विश्लेषण"),

    // ============================================
    // SADE SATI ANALYSIS
    // ============================================
    SADE_SATI_TITLE("Sade Sati Analysis", "साढेसाती विश्लेषण"),
    SADE_SATI_SUBTITLE("Saturn's 7.5 Year Transit", "शनिको ७.५ वर्षे गोचर"),
    SADE_SATI_ACTIVE("Sade Sati Active", "साढेसाती सक्रिय"),
    SADE_SATI_NOT_ACTIVE("Sade Sati is not currently active", "साढेसाती हाल सक्रिय छैन"),
    SADE_SATI_PHASE_RISING("Rising Phase", "उदय चरण"),
    SADE_SATI_PHASE_PEAK("Peak Phase", "शिखर चरण"),
    SADE_SATI_PHASE_SETTING("Setting Phase", "अस्त चरण"),
    SADE_SATI_RISING_DESC("Saturn transiting 12th from Moon - Beginning of Sade Sati", "शनि चन्द्रबाट १२औं राशिमा गोचर - साढेसातीको शुरुआत"),
    SADE_SATI_PEAK_DESC("Saturn transiting over natal Moon - Most intense phase", "शनि जन्म चन्द्रमाथि गोचर - सबैभन्दा तीव्र चरण"),
    SADE_SATI_SETTING_DESC("Saturn transiting 2nd from Moon - Final phase of Sade Sati", "शनि चन्द्रबाट २औं राशिमा गोचर - साढेसातीको अन्तिम चरण"),
    SADE_SATI_ACTIVE_SUMMARY("{phase} phase active with {severity} intensity", "{phase} चरण {severity} तीव्रताका साथ सक्रिय"),
    SMALL_PANOTI_FOURTH("Kantak Shani (4th from Moon)", "कण्टक शनि (चन्द्रबाट ४औं)"),
    SMALL_PANOTI_EIGHTH("Ashtama Shani (8th from Moon)", "अष्टम शनि (चन्द्रबाट ८औं)"),
    SMALL_PANOTI_ACTIVE_SUMMARY("{type} is active", "{type} सक्रिय छ"),
    SEVERITY_MILD("Mild", "हल्का"),
    SEVERITY_MODERATE("Moderate", "मध्यम"),
    SEVERITY_SIGNIFICANT("Significant", "महत्त्वपूर्ण"),
    SEVERITY_INTENSE("Intense", "तीव्र"),
    SADE_SATI_DAYS_REMAINING("Days Remaining", "बाँकी दिनहरू"),
    SADE_SATI_PROGRESS("Progress in Phase", "चरणमा प्रगति"),
    SADE_SATI_MOON_SIGN("Natal Moon Sign", "जन्म चन्द्र राशि"),
    SADE_SATI_SATURN_SIGN("Transit Saturn Sign", "गोचर शनि राशि"),

    // Sade Sati Remedies
    REMEDY_SHANI_MANTRA_TITLE("Shani Mantra", "शनि मन्त्र"),
    REMEDY_SHANI_MANTRA_DESC("Recite 'Om Sham Shanaishcharaya Namah' 108 times daily", "'ॐ शं शनैश्चराय नमः' दैनिक १०८ पटक जप गर्नुहोस्"),
    REMEDY_SATURDAY_CHARITY_TITLE("Saturday Charity", "शनिबार दान"),
    REMEDY_SATURDAY_CHARITY_DESC("Donate black sesame, mustard oil, or iron items to the needy", "कालो तिल, सर्स्यूको तेल, वा फलामका सामानहरू गरिबलाई दान गर्नुहोस्"),
    REMEDY_SATURDAY_FAST_TITLE("Saturday Fasting", "शनिबार व्रत"),
    REMEDY_SATURDAY_FAST_DESC("Observe fast on Saturdays and eat only after sunset", "शनिबार व्रत राख्नुहोस् र सूर्यास्त पछि मात्र खानुहोस्"),
    REMEDY_HANUMAN_WORSHIP_TITLE("Hanuman Worship", "हनुमान पूजा"),
    REMEDY_HANUMAN_WORSHIP_DESC("Recite Hanuman Chalisa daily, especially on Tuesdays and Saturdays", "हनुमान चालीसा दैनिक पाठ गर्नुहोस्, विशेष गरी मंगलबार र शनिबार"),
    REMEDY_BLUE_SAPPHIRE_TITLE("Blue Sapphire (Neelam)", "नीलम रत्न"),
    REMEDY_BLUE_SAPPHIRE_DESC("Wear after proper testing and astrologer consultation", "उचित परीक्षण र ज्योतिषी परामर्श पछि लगाउनुहोस्"),

    // ============================================
    // MANGLIK DOSHA ANALYSIS
    // ============================================
    MANGLIK_TITLE("Manglik Dosha Analysis", "मांगलिक दोष विश्लेषण"),
    MANGLIK_SUBTITLE("Mars Placement Analysis for Marriage", "विवाहको लागि मंगल स्थिति विश्लेषण"),
    MANGLIK_NONE_LEVEL("No Manglik Dosha", "मांगलिक दोष छैन"),
    MANGLIK_MILD("Mild Manglik", "हल्का मांगलिक"),
    MANGLIK_PARTIAL_LEVEL("Partial Manglik", "आंशिक मांगलिक"),
    MANGLIK_FULL_LEVEL("Full Manglik", "पूर्ण मांगलिक"),
    MANGLIK_SEVERE("Severe Manglik", "गम्भीर मांगलिक"),
    MANGLIK_SUMMARY_PRESENT("{level} present with {intensity}% intensity", "{level} {intensity}% तीव्रताका साथ उपस्थित"),
    MANGLIK_SUMMARY_ABSENT("No Manglik Dosha in this chart", "यस कुण्डलीमा मांगलिक दोष छैन"),
    MANGLIK_FROM_LAGNA("From Lagna", "लग्नबाट"),
    MANGLIK_FROM_MOON("From Moon", "चन्द्रबाट"),
    MANGLIK_FROM_VENUS("From Venus", "शुक्रबाट"),
    MANGLIK_CANCELLATIONS("Cancellation Factors", "रद्द गर्ने कारकहरू"),
    MANGLIK_EFFECTIVE_LEVEL("Effective Level", "प्रभावी स्तर"),

    // Manglik Cancellation Factors
    MANGLIK_CANCEL_OWN_SIGN_TITLE("Mars in Own Sign", "मंगल स्वराशिमा"),
    MANGLIK_CANCEL_OWN_SIGN_DESC("Mars in Aries or Scorpio reduces Manglik effects", "मंगल मेष वा वृश्चिकमा मांगलिक प्रभाव कम गर्छ"),
    MANGLIK_CANCEL_EXALTED_TITLE("Mars Exalted", "मंगल उच्च"),
    MANGLIK_CANCEL_EXALTED_DESC("Mars in Capricorn cancels Manglik Dosha completely", "मंगल मकरमा मांगलिक दोष पूर्ण रूपमा रद्द गर्छ"),
    MANGLIK_CANCEL_JUPITER_CONJUNCT_TITLE("Jupiter Conjunction", "गुरु युति"),
    MANGLIK_CANCEL_JUPITER_CONJUNCT_DESC("Jupiter conjunct Mars cancels Manglik effects", "गुरुले मंगलसँग युतिले मांगलिक प्रभाव रद्द गर्छ"),
    MANGLIK_CANCEL_VENUS_CONJUNCT_TITLE("Venus Conjunction", "शुक्र युति"),
    MANGLIK_CANCEL_VENUS_CONJUNCT_DESC("Venus conjunct Mars significantly reduces effects", "शुक्रले मंगलसँग युतिले प्रभाव उल्लेखनीय रूपमा कम गर्छ"),
    MANGLIK_CANCEL_JUPITER_ASPECT_TITLE("Jupiter's Aspect", "गुरुको दृष्टि"),
    MANGLIK_CANCEL_JUPITER_ASPECT_DESC("Jupiter aspecting Mars reduces Manglik effects", "गुरुको मंगलमा दृष्टिले मांगलिक प्रभाव कम गर्छ"),
    MANGLIK_CANCEL_SECOND_MERCURY_TITLE("Mars in 2nd in Mercury Sign", "बुध राशिमा २औं मंगल"),
    MANGLIK_CANCEL_SECOND_MERCURY_DESC("Mars in 2nd house in Gemini/Virgo cancels dosha", "मिथुन/कन्यामा २औं भावमा मंगलले दोष रद्द गर्छ"),
    MANGLIK_CANCEL_FOURTH_OWN_TITLE("Mars in 4th in Own Sign", "स्वराशिमा ४औं मंगल"),
    MANGLIK_CANCEL_FOURTH_OWN_DESC("Mars in 4th house in Aries/Scorpio cancels dosha", "मेष/वृश्चिकमा ४औं भावमा मंगलले दोष रद्द गर्छ"),
    MANGLIK_CANCEL_SEVENTH_SPECIAL_TITLE("Mars in 7th Special", "७औं मंगल विशेष"),
    MANGLIK_CANCEL_SEVENTH_SPECIAL_DESC("Mars in 7th in Cancer/Capricorn reduces effects", "कर्कट/मकरमा ७औं भावमा मंगलले प्रभाव कम गर्छ"),
    MANGLIK_CANCEL_EIGHTH_JUPITER_TITLE("Mars in 8th Jupiter Sign", "गुरु राशिमा ८औं मंगल"),
    MANGLIK_CANCEL_EIGHTH_JUPITER_DESC("Mars in 8th in Sagittarius/Pisces reduces effects", "धनु/मीनमा ८औं भावमा मंगलले प्रभाव कम गर्छ"),
    MANGLIK_CANCEL_TWELFTH_VENUS_TITLE("Mars in 12th Venus Sign", "शुक्र राशिमा १२औं मंगल"),
    MANGLIK_CANCEL_TWELFTH_VENUS_DESC("Mars in 12th in Taurus/Libra cancels dosha", "वृषभ/तुलामा १२औं भावमा मंगलले दोष रद्द गर्छ"),
    MANGLIK_CANCEL_BENEFIC_ASC_TITLE("Benefic Ascendant", "शुभ लग्न"),
    MANGLIK_CANCEL_BENEFIC_ASC_DESC("For Aries/Cancer/Leo/Scorpio ascendants, Mars is benefic", "मेष/कर्कट/सिंह/वृश्चिक लग्नको लागि मंगल शुभ हो"),

    // Manglik Remedies
    REMEDY_KUMBH_VIVAH_TITLE("Kumbh Vivah", "कुम्भ विवाह"),
    REMEDY_KUMBH_VIVAH_DESC("Ceremonial marriage to a clay pot or Peepal tree before actual marriage", "वास्तविक विवाह अघि माटोको घडा वा पीपलको रूखसँग विवाह समारोह"),
    REMEDY_MANGAL_SHANTI_TITLE("Mangal Shanti Puja", "मंगल शान्ति पूजा"),
    REMEDY_MANGAL_SHANTI_DESC("Perform Mars pacification ritual at a temple or home", "मन्दिर वा घरमा मंगल शान्ति विधि गर्नुहोस्"),
    REMEDY_MARS_MANTRA_TITLE("Mars Mantra", "मंगल मन्त्र"),
    REMEDY_MARS_MANTRA_DESC("Recite 'Om Kram Kreem Kroum Sah Bhaumaya Namah' 108 times on Tuesdays", "मंगलबार 'ॐ क्रां क्रीं क्रौं सः भौमाय नमः' १०८ पटक जप गर्नुहोस्"),
    REMEDY_CORAL_TITLE("Red Coral (Moonga)", "मूंगा रत्न"),
    REMEDY_CORAL_DESC("Wear red coral in gold or copper on right hand ring finger", "दाहिने हातको अनामिकामा सुन वा तामामा मूंगा लगाउनुहोस्"),
    REMEDY_TUESDAY_CHARITY_TITLE("Tuesday Charity", "मंगलबार दान"),
    REMEDY_TUESDAY_CHARITY_DESC("Donate red lentils, red cloth, or copper items on Tuesdays", "मंगलबार रातो दाल, रातो कपडा, वा तामाका सामान दान गर्नुहोस्"),

    // ============================================
    // PITRA DOSHA ANALYSIS
    // ============================================
    PITRA_DOSHA_TITLE("Pitra Dosha Analysis", "पितृ दोष विश्लेषण"),
    PITRA_DOSHA_SUBTITLE("Ancestral Karma Assessment", "पुर्खाको कर्म मूल्यांकन"),
    PITRA_DOSHA_NONE("No Pitra Dosha", "पितृ दोष छैन"),
    PITRA_DOSHA_MINOR("Minor Pitra Dosha", "हल्का पितृ दोष"),
    PITRA_DOSHA_MODERATE("Moderate Pitra Dosha", "मध्यम पितृ दोष"),
    PITRA_DOSHA_SIGNIFICANT("Significant Pitra Dosha", "महत्त्वपूर्ण पितृ दोष"),
    PITRA_DOSHA_SEVERE("Severe Pitra Dosha", "गम्भीर पितृ दोष"),
    PITRA_DOSHA_PRESENT_SUMMARY("{level} detected in chart", "कुण्डलीमा {level} पत्ता लाग्यो"),
    PITRA_DOSHA_ABSENT_SUMMARY("No significant Pitra Dosha indicators found", "कुनै महत्त्वपूर्ण पितृ दोष संकेतकहरू फेला परेनन्"),

    // Pitra Dosha Types
    PITRA_TYPE_SURYA_RAHU("Sun-Rahu Conjunction", "सूर्य-राहु युति"),
    PITRA_TYPE_SURYA_KETU("Sun-Ketu Conjunction", "सूर्य-केतु युति"),
    PITRA_TYPE_SURYA_SHANI("Sun-Saturn Affliction", "सूर्य-शनि पीडा"),
    PITRA_TYPE_NINTH_HOUSE("9th House Affliction", "९औं भाव पीडा"),
    PITRA_TYPE_NINTH_LORD("9th Lord Affliction", "९औं भावेश पीडा"),
    PITRA_TYPE_RAHU_NINTH("Rahu in 9th House", "९औं भावमा राहु"),
    PITRA_TYPE_COMBINED("Combined Affliction", "संयुक्त पीडा"),

    // Pitra Dosha Descriptions
    PITRA_DESC_SURYA_RAHU("Primary indicator - eclipsed Sun indicates blocked ancestral blessings", "प्राथमिक संकेतक - ग्रहण लागेको सूर्यले अवरुद्ध पुर्खाको आशीर्वाद संकेत गर्छ"),
    PITRA_DESC_SURYA_KETU("Past-life karmic debts from paternal lineage", "पितृ वंशबाट पूर्व जन्मको कर्म ऋण"),
    PITRA_DESC_SURYA_SHANI("Father-related karmic lessons and delays", "पिता-सम्बन्धित कर्म पाठ र ढिलाइ"),
    PITRA_DESC_NINTH_HOUSE("House of ancestors afflicted by malefics", "पुर्खाको भाव अशुभ ग्रहबाट पीडित"),
    PITRA_DESC_NINTH_LORD("Lord of father and fortune is weakened", "पिता र भाग्यको स्वामी कमजोर छ"),
    PITRA_DESC_RAHU_NINTH("Strong indication of ancestral debts", "पुर्खाको ऋणको बलियो संकेत"),
    PITRA_DESC_COMBINED("Multiple factors indicate significant ancestral karma", "बहु कारकहरूले महत्त्वपूर्ण पुर्खाको कर्म संकेत गर्छन्"),

    // Pitra Dosha Remedies
    REMEDY_PITRA_TARPAN_TITLE("Pitra Tarpan", "पितृ तर्पण"),
    REMEDY_PITRA_TARPAN_DESC("Offer water with sesame seeds to ancestors during Amavasya", "अमावस्यामा पुर्खालाई तिल पानी अर्पण गर्नुहोस्"),
    REMEDY_SHRADDHA_TITLE("Shraddha Ceremony", "श्राद्ध विधि"),
    REMEDY_SHRADDHA_DESC("Perform annual death anniversary rituals for departed ancestors", "दिवंगत पुर्खाको वार्षिक मृत्यु वर्षगाँठ विधि गर्नुहोस्"),
    REMEDY_CROW_FEEDING_TITLE("Crow Feeding", "काग भोजन"),
    REMEDY_CROW_FEEDING_DESC("Feed crows daily as they are considered messengers of ancestors", "कागलाई दैनिक खुवाउनुहोस् किनभने तिनीहरूलाई पुर्खाको दूत मानिन्छ"),
    REMEDY_NARAYAN_BALI_TITLE("Narayan Bali", "नारायण बलि"),
    REMEDY_NARAYAN_BALI_DESC("Special ritual for departed souls performed at Trimbakeshwar", "त्र्यम्बकेश्वरमा दिवंगत आत्माको लागि विशेष विधि"),
    REMEDY_PIND_DAAN_TITLE("Pind Daan", "पिण्ड दान"),
    REMEDY_PIND_DAAN_DESC("Offer rice balls to ancestors at Gaya or other sacred places", "गया वा अन्य पवित्र स्थानमा पुर्खालाई भातको पिण्ड अर्पण गर्नुहोस्"),
    REMEDY_PITRA_GAYATRI_TITLE("Pitra Gayatri Mantra", "पितृ गायत्री मन्त्र"),
    REMEDY_PITRA_GAYATRI_DESC("Recite Pitra Gayatri daily during Brahma Muhurta for ancestral peace", "पुर्खाको शान्तिको लागि ब्रह्म मुहूर्तमा पितृ गायत्री दैनिक जप गर्नुहोस्"),

    // ============================================
    // COMMON DOSHA ANALYSIS STRINGS
    // ============================================
    DOSHA_ANALYSIS("Dosha Analysis", "दोष विश्लेषण"),
    DOSHA_INDICATORS("Indicators Found", "संकेतकहरू फेला परे"),
    DOSHA_AFFECTED_AREAS("Affected Life Areas", "प्रभावित जीवन क्षेत्रहरू"),
    DOSHA_REMEDIES_SECTION("Recommended Remedies", "सिफारिस गरिएका उपायहरू"),
    DOSHA_INTERPRETATION("Interpretation", "व्याख्या"),
    DOSHA_SEVERITY_SCORE("Severity Score", "गम्भीरता अंक"),
    DOSHA_AUSPICIOUS_TIMES("Auspicious Times for Remedies", "उपायहरूको लागि शुभ समय"),

    // ============================================
    // INTERPRETATION SECTION HEADERS
    // ============================================
    INTERP_ANALYSIS_HEADER("ANALYSIS", "विश्लेषण"),
    INTERP_INDICATORS_FOUND("INDICATORS FOUND:", "पाइएका संकेतकहरू:"),
    INTERP_INTERPRETATION("INTERPRETATION:", "व्याख्या:"),
    INTERP_SEVERITY("SEVERITY:", "गम्भीरता:"),
    INTERP_LEVEL("Level:", "स्तर:"),

    // ============================================
    // MANGLIK DOSHA INTERPRETATION
    // ============================================
    MANGLIK_INTERP_NO_DOSHA("NO MANGLIK DOSHA", "मांगलिक दोष छैन"),
    MANGLIK_INTERP_MARS_NOT_PLACED(
        "Mars is not placed in houses 1, 2, 4, 7, 8, or 12 from your Lagna, Moon, or Venus.",
        "मंगल तपाईंको लग्न, चन्द्र वा शुक्रबाट १, २, ४, ७, ८ वा १२ भावमा छैन।"
    ),
    MANGLIK_INTERP_NO_DOSHA_DESC(
        "There is no Manglik Dosha in your chart.",
        "तपाईंको कुण्डलीमा मांगलिक दोष छैन।"
    ),
    MANGLIK_INTERP_HEADER("MANGLIK DOSHA ANALYSIS", "मांगलिक दोष विश्लेषण"),
    MANGLIK_INTERP_MARS_POSITION("Mars Position:", "मंगलको स्थिति:"),
    MANGLIK_INTERP_FROM_REFERENCE("ANALYSIS FROM THREE REFERENCE POINTS:", "तीन सन्दर्भ बिन्दुबाट विश्लेषण:"),
    MANGLIK_INTERP_FROM_LAGNA("From Lagna", "लग्नबाट"),
    MANGLIK_INTERP_FROM_MOON("From Moon", "चन्द्रबाट"),
    MANGLIK_INTERP_FROM_VENUS("From Venus", "शुक्रबाट"),
    MANGLIK_INTERP_MARS_IN_HOUSE("Mars in house", "मंगल भावमा"),
    MANGLIK_INTERP_MANGLIK_YES("YES", "छ"),
    MANGLIK_INTERP_MANGLIK_NO("NO", "छैन"),
    MANGLIK_INTERP_INITIAL_LEVEL("Initial Level:", "प्रारम्भिक स्तर:"),
    MANGLIK_INTERP_CANCELLATION_PRESENT("CANCELLATION FACTORS PRESENT:", "निरसन कारकहरू उपस्थित:"),
    MANGLIK_INTERP_EFFECTIVE_LEVEL("Effective Level After Cancellations:", "निरसन पछिको प्रभावकारी स्तर:"),
    MANGLIK_INTERP_HOUSE_SUFFIX_ST("st", "औं"),
    MANGLIK_INTERP_HOUSE_SUFFIX_ND("nd", "औं"),
    MANGLIK_INTERP_HOUSE_SUFFIX_RD("rd", "औं"),
    MANGLIK_INTERP_HOUSE_SUFFIX_TH("th", "औं"),

    // ============================================
    // MANGLIK MARRIAGE CONSIDERATIONS
    // ============================================
    MANGLIK_MARRIAGE_HEADER("MARRIAGE CONSIDERATIONS", "विवाह विचार"),
    MANGLIK_MARRIAGE_NONE_NO_RESTRICTION(
        "No restrictions based on Manglik Dosha",
        "मांगलिक दोषको आधारमा कुनै प्रतिबन्ध छैन"
    ),
    MANGLIK_MARRIAGE_NONE_COMPATIBLE(
        "Compatible with both Manglik and non-Manglik partners",
        "मांगलिक र गैर-मांगलिक दुवै साझेदारसँग मिल्दो"
    ),
    MANGLIK_MARRIAGE_MILD_EFFECTS(
        "Mild Manglik effects - marriage with non-Manglik is possible",
        "हल्का मांगलिक प्रभाव - गैर-मांगलिकसँग विवाह सम्भव छ"
    ),
    MANGLIK_MARRIAGE_MILD_REMEDIES(
        "Simple remedies recommended before marriage",
        "विवाह अघि सरल उपायहरू सिफारिस गरिन्छ"
    ),
    MANGLIK_MARRIAGE_MILD_MATCHING(
        "Matching with another Manglik is beneficial but not essential",
        "अर्को मांगलिकसँग मिलान लाभदायक तर अनिवार्य होइन"
    ),
    MANGLIK_MARRIAGE_PARTIAL_REMEDIES(
        "Partial Manglik - remedies strongly recommended",
        "आंशिक मांगलिक - उपायहरू जोडदार सिफारिस"
    ),
    MANGLIK_MARRIAGE_PARTIAL_PREFERABLE(
        "Marriage with Manglik partner is preferable",
        "मांगलिक साझेदारसँग विवाह उपयुक्त"
    ),
    MANGLIK_MARRIAGE_PARTIAL_KUMBH(
        "If marrying non-Manglik, perform Kumbh Vivah",
        "गैर-मांगलिकसँग विवाह गर्दा कुम्भ विवाह गर्नुहोस्"
    ),
    MANGLIK_MARRIAGE_FULL_PRESENT(
        "Full Manglik Dosha present",
        "पूर्ण मांगलिक दोष उपस्थित"
    ),
    MANGLIK_MARRIAGE_FULL_RECOMMENDED(
        "Marriage with Manglik partner highly recommended",
        "मांगलिक साझेदारसँग विवाह अत्यधिक सिफारिस"
    ),
    MANGLIK_MARRIAGE_FULL_KUMBH_ESSENTIAL(
        "Kumbh Vivah or equivalent ritual essential before marriage",
        "विवाह अघि कुम्भ विवाह वा समान अनुष्ठान आवश्यक"
    ),
    MANGLIK_MARRIAGE_FULL_PROPITIATION(
        "Regular Mars propitiation recommended",
        "नियमित मंगल शान्ति सिफारिस गरिन्छ"
    ),
    MANGLIK_MARRIAGE_SEVERE_CONSIDERATION(
        "Severe Manglik Dosha - careful consideration required",
        "गम्भीर मांगलिक दोष - सावधानीपूर्ण विचार आवश्यक"
    ),
    MANGLIK_MARRIAGE_SEVERE_ONLY_MANGLIK(
        "Only marry Manglik partner with similar intensity",
        "समान तीव्रता भएको मांगलिक साझेदारसँग मात्र विवाह गर्नुहोस्"
    ),
    MANGLIK_MARRIAGE_SEVERE_MULTIPLE_REMEDIES(
        "Multiple remedies required before and after marriage",
        "विवाह अघि र पछि धेरै उपायहरू आवश्यक"
    ),
    MANGLIK_MARRIAGE_SEVERE_DELAY(
        "Consider delaying marriage until after age 28 (Mars maturity)",
        "मंगल परिपक्वता (२८ वर्ष) सम्म विवाह ढिला गर्ने विचार गर्नुहोस्"
    ),
    MANGLIK_MARRIAGE_FULL_CANCELLATION(
        "NOTE: Full cancellation present - Manglik Dosha effectively nullified",
        "नोट: पूर्ण निरसन उपस्थित - मांगलिक दोष प्रभावकारी रूपमा शून्य"
    ),

    // ============================================
    // MANGLIK COMPATIBILITY
    // ============================================
    MANGLIK_COMPAT_EXCELLENT(
        "Excellent Manglik compatibility - no concerns",
        "उत्कृष्ट मांगलिक मिलान - कुनै चिन्ता छैन"
    ),
    MANGLIK_COMPAT_GOOD(
        "Good compatibility - minor remedies may help",
        "राम्रो मिलान - साना उपायहरू सहायक हुन सक्छ"
    ),
    MANGLIK_COMPAT_AVERAGE(
        "Average compatibility - remedies recommended",
        "औसत मिलान - उपायहरू सिफारिस गरिन्छ"
    ),
    MANGLIK_COMPAT_BELOW_AVERAGE(
        "Below average - significant remedies required",
        "औसतभन्दा कम - महत्वपूर्ण उपायहरू आवश्यक"
    ),
    MANGLIK_COMPAT_POOR(
        "Challenging combination - expert consultation advised",
        "कठिन संयोजन - विशेषज्ञ परामर्श सल्लाह दिइन्छ"
    ),

    // ============================================
    // MANGLIK REMEDY EFFECTIVENESS
    // ============================================
    REMEDY_EFFECTIVENESS_TRADITIONAL(
        "Traditional remedy - highly effective",
        "परम्परागत उपाय - अत्यधिक प्रभावकारी"
    ),
    REMEDY_EFFECTIVENESS_ALL_LEVELS(
        "Recommended for all Manglik levels",
        "सबै मांगलिक स्तरहरूको लागि सिफारिस"
    ),
    REMEDY_EFFECTIVENESS_TUESDAY(
        "Daily recitation on Tuesdays",
        "मंगलबार दैनिक पाठ"
    ),
    REMEDY_EFFECTIVENESS_CONSULT(
        "Consult astrologer before wearing",
        "लगाउनु अघि ज्योतिषीसँग परामर्श गर्नुहोस्"
    ),
    REMEDY_EFFECTIVENESS_EVERY_TUESDAY(
        "Every Tuesday",
        "हरेक मंगलबार"
    ),

    // ============================================
    // MANGLIK REFERENCE POINTS
    // ============================================
    MANGLIK_REF_LAGNA("Lagna", "लग्न"),
    MANGLIK_REF_MOON("Moon", "चन्द्र"),
    MANGLIK_REF_VENUS("Venus", "शुक्र"),
    YES("Yes", "छ"),
    NO("No", "छैन"),

    // ============================================
    // MANGLIK INTERPRETATION KEYS
    // ============================================
    MANGLIK_INTERP_NO_DOSHA_TITLE("NO MANGLIK DOSHA", "मांगलिक दोष छैन"),
    MANGLIK_INTERP_NO_DOSHA_DESC(
        "Mars is not placed in houses 1, 2, 4, 7, 8, or 12 from your Lagna, Moon, or Venus. There is no Manglik Dosha in your chart.",
        "मंगल तपाईंको लग्न, चन्द्र वा शुक्रबाट १, २, ४, ७, ८ वा १२औं भावमा छैन। तपाईंको कुण्डलीमा मांगलिक दोष छैन।"
    ),
    MANGLIK_INTERP_TITLE("MANGLIK DOSHA ANALYSIS", "मांगलिक दोष विश्लेषण"),
    MANGLIK_INTERP_MARS_POSITION("Mars Position: {sign} in House {house}", "मंगल स्थिति: {sign} भाव {house} मा"),
    MANGLIK_INTERP_MARS_RETROGRADE("Note: Mars is retrograde which can intensify or modify its effects.", "नोट: मंगल वक्री छ जसले यसको प्रभावहरू तीव्र वा परिमार्जन गर्न सक्छ।"),
    MANGLIK_INTERP_THREE_REF("ANALYSIS FROM THREE REFERENCE POINTS:", "तीन सन्दर्भ बिन्दुबाट विश्लेषण:"),
    MANGLIK_INTERP_MARS_HOUSE("Mars in House {house}", "मंगल भाव {house} मा"),
    MANGLIK_INTERP_IS_MANGLIK("Manglik", "मांगलिक"),
    MANGLIK_INTERP_INITIAL_LEVEL("Initial Level: {level}", "प्रारम्भिक स्तर: {level}"),

    // ============================================
    // MANGLIK MARRIAGE CONSIDERATIONS
    // ============================================
    MANGLIK_MARRIAGE_TITLE("MARRIAGE CONSIDERATIONS", "विवाह सम्बन्धी विचारहरू"),
    MANGLIK_MARRIAGE_NONE_1("No restrictions based on Manglik Dosha.", "मांगलिक दोषमा आधारित कुनै प्रतिबन्ध छैन।"),
    MANGLIK_MARRIAGE_NONE_2("Compatible with both Manglik and non-Manglik partners.", "मांगलिक र गैर-मांगलिक दुवै साझेदारसँग मिल्दो।"),
    MANGLIK_MARRIAGE_MILD_1("Mild Manglik effects - marriage with non-Manglik is possible.", "हल्का मांगलिक प्रभाव - गैर-मांगलिकसँग विवाह सम्भव।"),
    MANGLIK_MARRIAGE_MILD_2("Simple remedies recommended before marriage.", "विवाह अघि सरल उपायहरू सिफारिस।"),
    MANGLIK_MARRIAGE_MILD_3("Matching with another Manglik is beneficial but not essential.", "अर्को मांगलिकसँग मिलान लाभदायक तर अनिवार्य होइन।"),
    MANGLIK_MARRIAGE_PARTIAL_1("Partial Manglik - remedies strongly recommended.", "आंशिक मांगलिक - उपायहरू जोडदार सिफारिस।"),
    MANGLIK_MARRIAGE_PARTIAL_2("Marriage with Manglik partner is preferable.", "मांगलिक साझेदारसँग विवाह उपयुक्त।"),
    MANGLIK_MARRIAGE_PARTIAL_3("If marrying non-Manglik, perform Kumbh Vivah.", "गैर-मांगलिकसँग विवाह गर्दा कुम्भ विवाह गर्नुहोस्।"),
    MANGLIK_MARRIAGE_FULL_1("Full Manglik Dosha present.", "पूर्ण मांगलिक दोष उपस्थित।"),
    MANGLIK_MARRIAGE_FULL_2("Marriage with Manglik partner highly recommended.", "मांगलिक साझेदारसँग विवाह अत्यधिक सिफारिस।"),
    MANGLIK_MARRIAGE_FULL_3("Kumbh Vivah or equivalent ritual essential before marriage.", "विवाह अघि कुम्भ विवाह वा समतुल्य अनुष्ठान अनिवार्य।"),
    MANGLIK_MARRIAGE_FULL_4("Regular Mars propitiation recommended.", "नियमित मंगल शान्ति सिफारिस।"),
    MANGLIK_MARRIAGE_SEVERE_1("Severe Manglik Dosha - careful consideration required.", "गम्भीर मांगलिक दोष - सावधानीपूर्ण विचार आवश्यक।"),
    MANGLIK_MARRIAGE_SEVERE_2("Only marry Manglik partner with similar intensity.", "समान तीव्रता भएको मांगलिक साझेदारसँग मात्र विवाह।"),
    MANGLIK_MARRIAGE_SEVERE_3("Multiple remedies required before and after marriage.", "विवाह अघि र पछि बहु उपायहरू आवश्यक।"),
    MANGLIK_MARRIAGE_SEVERE_4("Consider delaying marriage until after age 28 (Mars maturity).", "मंगल परिपक्वता (२८ वर्ष) सम्म विवाह ढिला गर्ने विचार गर्नुहोस्।"),
    MANGLIK_MARRIAGE_FULL_CANCEL_NOTE("NOTE: Full cancellation present - Manglik Dosha effectively nullified.", "नोट: पूर्ण निरसन उपस्थित - मांगलिक दोष प्रभावकारी रूपमा शून्य।"),

    // ============================================
    // PITRA DOSHA INTERPRETATION
    // ============================================
    PITRA_INTERP_NO_DOSHA("NO SIGNIFICANT PITRA DOSHA", "कुनै महत्वपूर्ण पित्र दोष छैन"),
    PITRA_INTERP_NO_DOSHA_DESC(
        "Your chart does not show significant indicators of Pitra Dosha.",
        "तपाईंको कुण्डलीमा पित्र दोषको महत्वपूर्ण संकेतकहरू देखिँदैनन्।"
    ),
    PITRA_INTERP_SUPPORTIVE(
        "The ancestral lineage appears supportive of your life journey.",
        "पैतृक वंश तपाईंको जीवन यात्रामा सहायक देखिन्छ।"
    ),
    PITRA_INTERP_BENEFICIAL(
        "However, performing regular ancestral offerings (Shraddha) is always beneficial for maintaining positive ancestral blessings.",
        "तथापि, सकारात्मक पैतृक आशीर्वाद कायम राख्न नियमित श्राद्ध गर्नु सधैं लाभदायक हुन्छ।"
    ),
    PITRA_INTERP_HEADER("PITRA DOSHA ANALYSIS", "पित्र दोष विश्लेषण"),
    PITRA_INTERP_NINTH_HOUSE("9TH HOUSE ANALYSIS (House of Ancestors):", "९औं भाव विश्लेषण (पूर्वजको भाव):"),
    PITRA_INTERP_NINTH_LORD("9th Lord:", "९औं स्वामी:"),
    PITRA_INTERP_NINTH_LORD_POSITION("9th Lord Position:", "९औं स्वामीको स्थिति:"),
    PITRA_INTERP_LORD_AFFLICTED("9th Lord Afflicted:", "९औं स्वामी पीडित:"),
    PITRA_INTERP_HOUSE_AFFLICTED("9th House Afflicted:", "९औं भाव पीडित:"),
    PITRA_INTERP_BENEFIC_INFLUENCE("Benefic Influence:", "शुभ प्रभाव:"),
    PITRA_INTERP_YES_MITIGATING("Yes - Mitigating", "छ - न्यूनीकरण गर्दै"),

    // Pitra Dosha indicator descriptions
    PITRA_DESC_SUN_RAHU_HOUSE(
        "Sun conjunct Rahu in House %d - Primary Pitra Dosha indicator",
        "भाव %d मा सूर्य-राहु युति - प्रमुख पित्र दोष संकेतक"
    ),
    PITRA_DESC_SUN_KETU_HOUSE(
        "Sun conjunct Ketu in House %d - Indicates past-life ancestral karma",
        "भाव %d मा सूर्य-केतु युति - पूर्वजन्मको पैतृक कर्म संकेत"
    ),
    PITRA_DESC_SUN_SATURN_CONJUNCT(
        "Sun conjunct Saturn in House %d - Father-related karmic issues",
        "भाव %d मा सूर्य-शनि युति - पितृ-सम्बन्धी कार्मिक मुद्दाहरू"
    ),
    PITRA_DESC_SATURN_ASPECT(
        "Saturn aspects Sun from House %d - Delayed results due to ancestral karma",
        "शनिले भाव %d बाट सूर्यलाई हेर्छ - पैतृक कर्मले गर्दा ढिलो परिणाम"
    ),
    PITRA_DESC_MALEFICS_NINTH(
        "Malefics in 9th house - Ancestral blessings blocked",
        "९औं भावमा पाप ग्रह - पैतृक आशीर्वाद अवरुद्ध"
    ),
    PITRA_DESC_NINTH_LORD_AFFLICTED(
        "9th lord %s is afflicted - Ancestral lineage karma",
        "९औं स्वामी %s पीडित छ - पैतृक वंश कर्म"
    ),

    // Pitra interpretation levels
    PITRA_LEVEL_MINOR_DESC(
        "Minor ancestral karma is indicated. This may manifest as occasional obstacles or delays that seem unexplained. Regular ancestral prayers and offerings during Pitru Paksha should be sufficient.",
        "साना पैतृक कर्म संकेत गरिएको छ। यसले अव्याख्येय देखिने कहिलेकाहीं अवरोधहरू वा ढिलाइको रूपमा प्रकट हुन सक्छ। पितृ पक्षमा नियमित पैतृक प्रार्थना र अर्पण पर्याप्त हुनुपर्छ।"
    ),
    PITRA_LEVEL_MODERATE_DESC(
        "Moderate Pitra Dosha suggests unresolved ancestral obligations. You may experience recurring challenges in life that feel karmic. Regular Tarpan and Shraddha ceremonies are recommended.",
        "मध्यम पित्र दोषले अपूर्ण पैतृक दायित्वहरू संकेत गर्दछ। तपाईंले कार्मिक जस्तो लाग्ने जीवनमा आवर्ती चुनौतीहरू अनुभव गर्न सक्नुहुन्छ। नियमित तर्पण र श्राद्ध समारोहहरू सिफारिस गरिन्छ।"
    ),
    PITRA_LEVEL_SIGNIFICANT_DESC(
        "Significant ancestral karma is present. This may manifest as: delayed marriage or relationship issues, difficulties with children or progeny, career obstacles despite qualifications, and family disharmony. Comprehensive remedies including Narayan Bali may be beneficial.",
        "महत्वपूर्ण पैतृक कर्म उपस्थित छ। यसले विवाहमा ढिलाइ वा सम्बन्ध समस्या, सन्तान वा सन्ततिमा कठिनाइहरू, योग्यता भएता पनि क्यारियरमा अवरोधहरू, र पारिवारिक विसंगतिको रूपमा प्रकट हुन सक्छ। नारायण बलि सहित व्यापक उपायहरू लाभदायक हुन सक्छ।"
    ),
    PITRA_LEVEL_SEVERE_DESC(
        "Severe Pitra Dosha indicates deep ancestral karma that requires serious attention and remedial measures. This level of dosha often indicates: ancestors who departed with unfulfilled wishes, interrupted or improper last rites in the lineage, and significant karmic debts carried forward. Consult a qualified priest for Narayan Bali/Nagbali and Pind Daan at sacred places like Gaya.",
        "गम्भीर पित्र दोषले गहिरो पैतृक कर्म संकेत गर्दछ जसलाई गम्भीर ध्यान र उपचारात्मक उपायहरू आवश्यक छ। यस स्तरको दोषले प्रायः संकेत गर्दछ: अपूर्ण इच्छाहरूसँग गएका पूर्वजहरू, वंशमा अवरुद्ध वा अनुचित अन्तिम संस्कार, र अगाडि लगिएको महत्वपूर्ण कार्मिक ऋणहरू। गयामा नारायण बलि/नागबलि र पिण्डदान को लागि योग्य पुजारीसँग परामर्श गर्नुहोस्।"
    ),

    // Pitra life areas
    PITRA_AREA_FATHER("Father and paternal lineage", "पिता र पैतृक वंश"),
    PITRA_AREA_SPIRITUAL("Spiritual progress and dharma", "आध्यात्मिक प्रगति र धर्म"),
    PITRA_AREA_SELF("Self, health, and overall life direction", "आत्म, स्वास्थ्य, र समग्र जीवन दिशा"),
    PITRA_AREA_FAMILY_WEALTH("Family wealth and accumulated assets", "पारिवारिक सम्पत्ति र संचित सम्पत्ति"),
    PITRA_AREA_SIBLINGS("Siblings and communication", "भाइबहिनी र संचार"),
    PITRA_AREA_MOTHER("Mother, property, and domestic peace", "आमा, सम्पत्ति, र घरेलु शान्ति"),
    PITRA_AREA_CHILDREN("Children, education, and creativity", "बच्चाहरू, शिक्षा, र सृजनात्मकता"),
    PITRA_AREA_HEALTH("Health, debts, and service", "स्वास्थ्य, ऋण, र सेवा"),
    PITRA_AREA_MARRIAGE("Marriage and partnerships", "विवाह र साझेदारी"),
    PITRA_AREA_LONGEVITY("Longevity and inherited wealth", "दीर्घायु र विरासत सम्पत्ति"),
    PITRA_AREA_FORTUNE("Fortune, higher learning, and spirituality", "भाग्य, उच्च शिक्षा, र आध्यात्मिकता"),
    PITRA_AREA_CAREER("Career and public reputation", "क्यारियर र सार्वजनिक प्रतिष्ठा"),
    PITRA_AREA_GAINS("Gains and social network", "लाभ र सामाजिक नेटवर्क"),
    PITRA_AREA_LIBERATION("Spiritual liberation and foreign lands", "आध्यात्मिक मुक्ति र विदेश"),

    // Pitra remedy timings
    PITRA_TIMING_AMAVASYA("Amavasya (New Moon) or Pitru Paksha", "औंसी वा पितृ पक्ष"),
    PITRA_TIMING_DEATH_ANNIVERSARY("Father's death anniversary or Pitru Paksha", "पिताको पुण्यतिथि वा पितृ पक्ष"),
    PITRA_TIMING_DAILY_PITRU("Daily, especially during Pitru Paksha", "दैनिक, विशेष गरी पितृ पक्षमा"),
    PITRA_TIMING_NARAYAN_BALI("Once in lifetime at Trimbakeshwar or Gaya", "जीवनकालमा एकपटक त्र्यम्बकेश्वर वा गयामा"),
    PITRA_TIMING_PIND_DAAN("Pitru Paksha at Gaya", "गयामा पितृ पक्ष"),
    PITRA_TIMING_BRAHMA_MUHURTA("Daily during Brahma Muhurta", "ब्रह्म मुहूर्तमा दैनिक"),

    // Pitra auspicious periods
    PITRA_PERIOD_PITRU_PAKSHA("Pitru Paksha (15-day period in Bhadrapada month)", "पितृ पक्ष (भाद्र महिनामा १५ दिनको अवधि)"),
    PITRA_PERIOD_AMAVASYA("Amavasya (New Moon days)", "औंसी (अमावस्या दिनहरू)"),
    PITRA_PERIOD_ECLIPSE("Solar/Lunar eclipses", "सूर्य/चन्द्र ग्रहण"),
    PITRA_PERIOD_DEATH_ANNIV("Father's death anniversary", "पिताको पुण्यतिथि"),
    PITRA_PERIOD_MAHALAYA("Mahalaya Amavasya", "महालय अमावस्या"),
    PITRA_PERIOD_AKSHAYA("Akshaya Tritiya", "अक्षय तृतीया"),
    PITRA_PERIOD_GAYA("Gaya Shraddha periods", "गया श्राद्ध अवधिहरू"),

    // ============================================
    // SADE SATI INTERPRETATION
    // ============================================
    SADE_SATI_ACTIVE_HEADER("SADE SATI ACTIVE - %s PHASE", "साढ़े साती सक्रिय - %s चरण"),
    SADE_SATI_TRANSIT_DESC(
        "Saturn is currently transiting %s, which is the %s from your natal Moon in %s.",
        "शनि हाल %s मा गोचर गर्दैछ, जुन तपाईंको जन्म चन्द्र %s बाट %s हो।"
    ),
    SADE_SATI_12TH_HOUSE("12th house", "१२औं भाव"),
    SADE_SATI_SAME_SIGN("same sign", "उही राशि"),
    SADE_SATI_2ND_HOUSE("2nd house", "२औं भाव"),
    SADE_SATI_RISING_HEADER("RISING PHASE CHARACTERISTICS:", "उदय चरण विशेषताहरू:"),
    SADE_SATI_RISING_BEGIN("Beginning of Sade Sati period", "साढ़े साती अवधिको सुरुवात"),
    SADE_SATI_RISING_EXPENSES("Focus on expenses and losses (12th house)", "खर्च र हानिमा केन्द्रित (१२औं भाव)"),
    SADE_SATI_RISING_SLEEP("Sleep disturbances possible", "निद्रामा गडबडी सम्भव"),
    SADE_SATI_RISING_ENEMIES("Hidden enemies may become active", "लुकेका शत्रुहरू सक्रिय हुन सक्छन्"),
    SADE_SATI_RISING_SPIRITUAL("Spiritual growth opportunities", "आध्यात्मिक वृद्धिका अवसरहरू"),
    SADE_SATI_PEAK_HEADER("PEAK PHASE CHARACTERISTICS:", "शिखर चरण विशेषताहरू:"),
    SADE_SATI_PEAK_INTENSE("Most intense phase of Sade Sati", "साढ़े साती को सबैभन्दा तीव्र चरण"),
    SADE_SATI_PEAK_MIND("Direct impact on mind and emotions", "मन र भावनाहरूमा प्रत्यक्ष प्रभाव"),
    SADE_SATI_PEAK_HEALTH("Health may need attention", "स्वास्थ्यमा ध्यान आवश्यक हुन सक्छ"),
    SADE_SATI_PEAK_SELF("Self-image transformation", "आत्म-छवि रूपान्तरण"),
    SADE_SATI_PEAK_RESTRUCTURE("Major life restructuring possible", "प्रमुख जीवन पुनर्संरचना सम्भव"),
    SADE_SATI_SETTING_HEADER("SETTING PHASE CHARACTERISTICS:", "अस्त चरण विशेषताहरू:"),
    SADE_SATI_SETTING_FINAL("Final phase of Sade Sati", "साढ़े साती को अन्तिम चरण"),
    SADE_SATI_SETTING_FINANCES("Focus on finances and family (2nd house)", "वित्त र परिवारमा केन्द्रित (२औं भाव)"),
    SADE_SATI_SETTING_SPEECH("Speech and communication impacted", "वाणी र संचार प्रभावित"),
    SADE_SATI_SETTING_WEALTH("Accumulated wealth may fluctuate", "संचित सम्पत्तिमा उतारचढाव"),
    SADE_SATI_SETTING_LESSONS("Integration of lessons learned", "सिकेका पाठहरूको एकीकरण"),
    SADE_SATI_NOT_ACTIVE_HEADER("SADE SATI NOT ACTIVE", "साढ़े साती सक्रिय छैन"),
    SADE_SATI_NOT_ACTIVE_DESC(
        "Saturn is currently transiting %s, which does not form Sade Sati or Small Panoti with your natal Moon in %s.",
        "शनि हाल %s मा गोचर गर्दैछ, जसले तपाईंको जन्म चन्द्र %s सँग साढ़े साती वा सानो पनोती बनाउँदैन।"
    ),
    SADE_SATI_FAVORABLE_PERIOD(
        "This is generally a favorable period regarding Saturn's influence on emotional and mental well-being.",
        "भावनात्मक र मानसिक कल्याणमा शनिको प्रभाव सम्बन्धमा यो सामान्यतया अनुकूल अवधि हो।"
    ),
    SADE_SATI_SMALL_PANOTI_HEADER("SMALL PANOTI (DHAIYA) ACTIVE", "सानो पनोती (ढैया) सक्रिय"),
    SADE_SATI_FOURTH_TRANSIT("Saturn is transiting the 4th house from your Moon.", "शनि तपाईंको चन्द्रबाट ४औं भावमा गोचर गर्दैछ।"),
    SADE_SATI_FOURTH_DOMESTIC("Domestic peace may be disturbed", "घरेलु शान्ति अशान्त हुन सक्छ"),
    SADE_SATI_FOURTH_MOTHER("Mother's health may need attention", "आमाको स्वास्थ्यमा ध्यान आवश्यक"),
    SADE_SATI_FOURTH_PROPERTY("Property matters require caution", "सम्पत्ति मामिलाहरूमा सावधानी आवश्यक"),
    SADE_SATI_FOURTH_MENTAL("Mental peace may fluctuate", "मानसिक शान्तिमा उतारचढाव"),
    SADE_SATI_ASHTAMA_HEADER("ASHTAMA SHANI - Saturn in 8th from Moon", "अष्टम शनि - चन्द्रबाट ८औं मा शनि"),
    SADE_SATI_ASHTAMA_CHALLENGING(
        "This is considered one of the most challenging Saturn transits.",
        "यो शनिको सबैभन्दा चुनौतीपूर्ण गोचर मध्ये एक मानिन्छ।"
    ),
    SADE_SATI_ASHTAMA_CHANGES("Sudden changes and transformations", "अचानक परिवर्तन र रूपान्तरणहरू"),
    SADE_SATI_ASHTAMA_HEALTH("Health requires vigilance", "स्वास्थ्यमा सतर्कता आवश्यक"),
    SADE_SATI_ASHTAMA_OBSTACLES("Obstacles in ventures", "उद्यमहरूमा अवरोधहरू"),
    SADE_SATI_ASHTAMA_PSYCHOLOGICAL("Deep psychological transformation", "गहिरो मनोवैज्ञानिक रूपान्तरण"),

    // Sade Sati favorable/challenging factors
    SADE_SATI_FACTOR_EXALTED("Saturn is exalted in transit - effects significantly reduced", "शनि गोचरमा उच्च - प्रभावहरू उल्लेखनीय कम"),
    SADE_SATI_FACTOR_OWN_SIGN("Saturn is in own sign - effects well-managed", "शनि स्वराशिमा - प्रभावहरू राम्रोसँग व्यवस्थित"),
    SADE_SATI_FACTOR_YOGAKARAKA("Saturn is Yogakaraka for your ascendant - may bring positive results", "शनि तपाईंको लग्नको लागि योगकारक - सकारात्मक परिणाम ल्याउन सक्छ"),
    SADE_SATI_FACTOR_NATAL_STRONG("Natal Saturn is strong - better equipped to handle transit", "जन्म शनि बलियो - गोचर सम्हाल्न राम्रो सुसज्जित"),
    SADE_SATI_FACTOR_DEBILITATED("Saturn is debilitated in transit - effects may be more challenging", "शनि गोचरमा नीच - प्रभावहरू थप चुनौतीपूर्ण हुन सक्छ"),
    SADE_SATI_FACTOR_WEAK_MOON("Natal Moon is weak - emotional resilience may be tested", "जन्म चन्द्र कमजोर - भावनात्मक लचिलोपन परीक्षण हुन सक्छ"),
    SADE_SATI_FACTOR_NATAL_WEAK("Natal Saturn is weak - transit effects may be more pronounced", "जन्म शनि कमजोर - गोचर प्रभाव थप उच्चारित हुन सक्छ"),
    SADE_SATI_FACTOR_RETROGRADE("Natal Saturn is retrograde - internal processing of karmic lessons", "जन्म शनि वक्री - कार्मिक पाठहरूको आन्तरिक प्रशोधन"),

    // Sade Sati remedy timings
    SADE_SATI_TIMING_SATURN_HORA("Saturday during Saturn Hora", "शनि होरामा शनिबार"),
    SADE_SATI_TIMING_EVERY_SATURDAY("Every Saturday", "हरेक शनिबार"),
    SADE_SATI_TIMING_TUE_SAT("Tuesday and Saturday", "मंगलबार र शनिबार"),

    // Error messages
    ERROR_MOON_NOT_FOUND("Unable to calculate - Moon position not found", "गणना गर्न असमर्थ - चन्द्र स्थिति फेला परेन"),
    ERROR_SADE_SATI_CALC("Unable to calculate Sade Sati - Moon position not available in chart.", "साढ़े साती गणना गर्न असमर्थ - कुण्डलीमा चन्द्र स्थिति उपलब्ध छैन।"),
    ERROR_BAV_NOT_FOUND("Bhinnashtakavarga not found for %s", "%s को लागि भिन्नाष्टकवर्ग फेला परेन"),
    ERROR_ASHTAKAVARGA_NOT_APPLICABLE("Ashtakavarga not applicable for %s", "%s को लागि अष्टकवर्ग लागू हुँदैन"),

    // ============================================
    // SYNASTRY SCREEN STRINGS
    // ============================================
    SYNASTRY_LAGNA("Lagna", "लग्न"),
    SYNASTRY_MOON("Moon", "चन्द्र"),
    SYNASTRY_VENUS("Venus", "शुक्र"),
    SYNASTRY_HOUSE_IN("in House", "भावमा");
}

/**
 * Helper object to find string keys by English value across all enums
 */
object StringKeyFinder {
    /**
     * Find key by English value across all string key enums
     */
    fun findByEnglish(value: String): StringKeyInterface? {
        return StringKey.entries.find { it.en.equals(value, ignoreCase = true) }
            ?: StringKeyMatch.entries.find { it.en.equals(value, ignoreCase = true) }
            ?: StringKeyAnalysis.entries.find { it.en.equals(value, ignoreCase = true) }
            ?: StringKeyDosha.entries.find { it.en.equals(value, ignoreCase = true) }
    }
}
