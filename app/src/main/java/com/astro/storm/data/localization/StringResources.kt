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
    fun get(key: StringKey, language: Language): String {
        return when (language) {
            Language.ENGLISH -> key.en
            Language.NEPALI -> key.ne
        }
    }

    /**
     * Get localized string with format arguments
     */
    fun get(key: StringKey, language: Language, vararg args: Any): String {
        val template = get(key, language)
        return try {
            String.format(template, *args)
        } catch (e: Exception) {
            template
        }
    }
}

/**
 * All translatable string keys with their translations
 *
 * Organized by category for maintainability.
 * Nepali translations are authentic and culturally appropriate for Vedic astrology context.
 */
enum class StringKey(val en: String, val ne: String) {

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
    NAKSHATRA_PADA("Pada", "पद"),

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
    PROFILE_BIRTH_CHART("Birth chart", "जन्म कुण्डली"),

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

    // ============================================
    // MATCHMAKING CALCULATOR - MANGLIK DOSHA
    // ============================================
    MANGLIK_NONE("No Manglik Dosha", "मांगलिक दोष छैन"),
    MANGLIK_PARTIAL("Partial Manglik", "आंशिक मांगलिक"),
    MANGLIK_FULL("Full Manglik", "पूर्ण मांगलिक"),
    MANGLIK_DOUBLE("Double Manglik (Severe)", "दोहोरो मांगलिक (गम्भीर)"),

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
    DASHA_BHUKTI("Bhukti", "भुक्ति"),
    DASHA_SANDHI_NOTE("Dasha Sandhi (junction periods) occur when transitioning between planetary periods and are considered sensitive times requiring careful attention.", "दशा सन्धि (जोड अवधिहरू) ग्रह अवधिहरूबीच सन्क्रमण हुँदा हुन्छ र यी संवेदनशील समयहरू मानिन्छन्।"),
    DASHA_PERCENT_COMPLETE("%s%% complete", "%s%% पूरा"),
    DASHA_YEARS_ABBR("yrs", "वर्ष"),
    DASHA_COLLAPSE("Collapse", "संकुचन गर्नुहोस्"),
    DASHA_EXPAND("Expand", "विस्तार गर्नुहोस्"),

    // ============================================
    // DASHA CALCULATOR - YOGINI DASHAS
    // ============================================
    YOGINI_MANGALA("Mangala", "मंगला"),
    YOGINI_PINGALA("Pingala", "पिंगला"),
    YOGINI_DHANYA("Dhanya", "धान्य"),
    YOGINI_BHRAMARI("Bhramari", "भ्रामरी"),
    YOGINI_BHADRIKA("Bhadrika", "भद्रिका"),
    YOGINI_ULKA("Ulka", "उल्का"),
    YOGINI_SIDDHA("Siddha", "सिद्धा"),
    YOGINI_SANKATA("Sankata", "संकटा"),

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
    // DIVISIONAL CHARTS - DESCRIPTIONS
    // ============================================
    VARGA_D1_DESC("Physical Body, General Life", "शारीरिक शरीर, सामान्य जीवन"),
    VARGA_D2_DESC("Wealth, Prosperity", "धन, समृद्धि"),
    VARGA_D3_DESC("Siblings, Courage", "भाइबहिनी, साहस"),
    VARGA_D4_DESC("Fortune, Property", "भाग्य, सम्पत्ति"),
    VARGA_D7_DESC("Children, Progeny", "सन्तान"),
    VARGA_D9_DESC("Spouse, Dharma", "जीवनसाथी, धर्म"),
    VARGA_D10_DESC("Career, Status", "क्यारियर, स्थिति"),
    VARGA_D12_DESC("Parents, Heritage", "आमाबुबा, विरासत"),
    VARGA_D16_DESC("Vehicles, Happiness", "सवारी, खुशी"),
    VARGA_D20_DESC("Spiritual Progress", "आध्यात्मिक प्रगति"),
    VARGA_D24_DESC("Education, Learning", "शिक्षा, सिकाइ"),
    VARGA_D27_DESC("Strength, Stamina", "बल, सहनशक्ति"),
    VARGA_D30_DESC("Misfortunes, Troubles", "दुर्भाग्य, समस्याहरू"),
    VARGA_D40_DESC("Maternal Legacy", "मातृ विरासत"),
    VARGA_D45_DESC("Paternal Legacy", "पैतृक विरासत"),
    VARGA_D60_DESC("Past Life Karma", "पूर्वजन्मको कर्म"),

    // ============================================
    // HOROSCOPE CALCULATOR - LIFE AREAS
    // ============================================
    HORO_CAREER("Career", "क्यारियर"),
    HORO_LOVE("Love & Relationships", "प्रेम र सम्बन्ध"),
    HORO_HEALTH("Health & Vitality", "स्वास्थ्य र जीवनशक्ति"),
    HORO_FINANCE("Finance & Wealth", "वित्त र धन"),
    HORO_FAMILY("Family & Home", "परिवार र घर"),
    HORO_SPIRITUALITY("Spiritual Growth", "आध्यात्मिक वृद्धि"),

    // ============================================
    // HOROSCOPE CALCULATOR - THEMES
    // ============================================
    THEME_BALANCE("Balance", "सन्तुलन"),
    THEME_DYNAMIC_ACTION("Dynamic Action", "गतिशील कार्य"),
    THEME_PRACTICAL_PROGRESS("Practical Progress", "व्यावहारिक प्रगति"),
    THEME_SOCIAL_CONNECTIONS("Social Connections", "सामाजिक सम्बन्धहरू"),
    THEME_EMOTIONAL_INSIGHT("Emotional Insight", "भावनात्मक अन्तर्दृष्टि"),
    THEME_SELF_EXPRESSION("Self-Expression", "आत्म-अभिव्यक्ति"),
    THEME_TRANSFORMATION("Transformation", "रूपान्तरण"),

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
    ACTION_SEARCH("Search", "खोज्नुहोस्"),

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
    DIALOG_NATURE("Nature", "प्रकृति"),
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
    ASHTAK_GUIDE_TRANSIT_4("Use for timing important decisions", "महत्त्वपूर्ण निर्णयहरूको समयको लागि प्रयोग गर्नुहोस्");

    companion object {
        /**
         * Find key by English value
         */
        fun findByEnglish(value: String): StringKey? {
            return entries.find { it.en.equals(value, ignoreCase = true) }
        }
    }
}
