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
    YOGAS_COUNT_DETECTED("%d yogas detected in %s", "%s मा %d योगहरू पत्ता लागेको"),

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

    // ============================================
    // PROFILE SWITCHER
    // ============================================
    PROFILE_SWITCH("Switch Profile", "प्रोफाइल बदल्नुहोस्"),
    PROFILE_ADD_NEW("Add New Profile", "नयाँ प्रोफाइल थप्नुहोस्"),
    PROFILE_CURRENT("Current", "हालको"),

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
    MISC_LESS("Less", "कम");

    companion object {
        /**
         * Find key by English value
         */
        fun findByEnglish(value: String): StringKey? {
            return entries.find { it.en.equals(value, ignoreCase = true) }
        }
    }
}
