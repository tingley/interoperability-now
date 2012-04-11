package com.globalsight.tip;

public class StandardTaskType {

    public static final String TRANSLATE_STRICT_BITEXT = 
            "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-strict-bitext";
    public static class TranslateStrictBitext {
        public static final String BILINGUAL =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-strict-bitext/bilingual";
        public static final String STS =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-strict-bitext/sts";
        public static final String PREVIEW =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-strict-bitext/preview";
        public static final String TMX =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-strict-bitext/tmx";
        public static final String REFERENCE =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-strict-bitext/reference";
    }

    public static final String TRANSLATE_GENERIC_BITEXT = 
            "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-generic-bitext";    
    public static class TranslateGenericBitext {
        public static final String BILINGUAL =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-generic-bitext/bilingual";
        public static final String STS =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-generic-bitext/sts";
        public static final String TMX =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-generic-bitext/tmx";
        public static final String REFERENCE =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-generic-bitext/reference";
    }

    public static final String TRANSLATE_NATIVE_FORMAT = 
            "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-native-format";
    public static class TranslateNativeFormat {
        public static final String INPUT =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-native-format/input";
        public static final String OUTPUT =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-native-format/output";
        public static final String STS =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-generic-bitext/sts";
        public static final String TMX =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-native-format/tmx";
        public static final String REFERENCE =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/translate-native-format/reference";
    }
    
    public static final String PREPARE_SPECIFICATIIONS = 
            "http://schema.interoperability-now.org/tipp/v1.4/tasks/prepare-specifications";
    public static class PrepareSpecifications {
        public static final String CONTENT =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/prepare-specifications/content";
        public static final String STS =
                "http://schema.interoperability-now.org/tipp/v1.4/tasks/prepare-specifications/sts";
    }

}
