package com.globalsight.tip;

public class StandardTaskType {

    public static final String TRANSLATE = 
            "http://interoperability-now.org/TIPP/schema/tasks/v1/translate";
    public static class Translate {
        public static final String BILINGUAL =
                "http://interoperability-now.org/TIPP/schema/tasks/v1/translate/bilingual";
        public static final String STS =
                "http://interoperability-now.org/TIPP/schema/tasks/v1/translate/sts";
        public static final String PREVIEW =
                "http://interoperability-now.org/TIPP/schema/tasks/v1/translate/preview";
        public static final String TMX =
                "http://interoperability-now.org/TIPP/schema/tasks/v1/translate/tmx";
        public static final String REFERENCE =
                "http://interoperability-now.org/TIPP/schema/tasks/v1/translate/reference";
    }
}
