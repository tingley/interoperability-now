package com.globalsight.tip;

interface TIPConstants {

    public static final String MANIFEST = "TIPManifest";
    public static final String ATTR_VERSION = "version";
    
    public static final String GLOBAL_DESCRIPTOR = "GlobalDescriptor";
    public static final String PACKAGE_OBJECTS = "PackageObjects";

    // GlobalDescriptor fields
    public static final String UNIQUE_PACKAGE_ID = "UniquePackageID";
    public static final String PACKAGE_CREATOR = "PackageCreator";
    public static final String ORDER_ACTION = "OrderAction";
    
    // Creator fields
    static class Creator {
        public static final String NAME = "CreatorName";
        public static final String ID = "CreatorID";
        public static final String UPDATE = "CreatorUpdate";
        public static final String COMMUNICATION = "Communication";
    }
    
    public static final String CONTRIBUTOR_TOOL = "ContributorTool";
    // ContributorTool fields
    static class ContributorTool {
        public static final String NAME = "ToolName";
        public static final String ID = "ToolID";
        public static final String VERSION = "ToolVersion";
    }

    // OrderAction fields
    public static final String ORDER_TASK = "OrderTask";
    public static final String ORDER_RESPONSE = "OrderResponse";
    
    static class OrderTask {
        public static final String TYPE = "TaskType";
        public static final String SOURCE_LANGUAGE = "SourceLanguage";
        public static final String TARGET_LANGUAGE = "TargetLanguage";
    }
    
    static class OrderResponse {
        public static final String NAME = "ResponseName";
        public static final String ID = "ResponseID";
        public static final String UPDATE = "ResponseUpdate";
        public static final String MESSAGE = "ResponseMessage";
        public static final String COMMENT = "ResponseComment";
    }
    
    // PackageObjects
    public static final String PACKAGE_OBJECT_SECTION = "PackageObjectSection";
    
    // PackageObjectSection
    public static final String ATTR_SECTION_NAME = "sectionname";
    public static final String OBJECT_SEQUENCE = "ObjectSequence";
    public static final String OBJECT_FILE = "ObjectFile";
    
    static class ObjectFile {
        public static final String ATTR_LOCALIZABLE = "localizable";
        public static final String TYPE = "Type";
        public static final String LOCATION_PATH = "LocationPath";
    }
    
    public static final String YES = "yes";
    public static final String NO = "no";
}
