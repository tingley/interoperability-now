package com.globalsight.tip;

interface TIPPConstants {

    public static final String SCHEMA_VERSION = "1.5";
    public static final String SCHEMA_LOCATION =
                "http://schema.interoperability-now.org/tipp/1_5/TIPPManifest.xsd";
    public static final String COMMON_SCHEMA_LOCATION =
                "http://schema.interoperability-now.org/tipp/1_5/TIPPCommon.xsd";

    public static final String MANIFEST = "TIPPManifest";
    public static final String ATTR_VERSION = "version";
    
    public static final String GLOBAL_DESCRIPTOR = "GlobalDescriptor";
    public static final String PACKAGE_OBJECTS = "Resources";

    // GlobalDescriptor fields
    public static final String UNIQUE_PACKAGE_ID = "UniquePackageID";
    public static final String PACKAGE_CREATOR = "Creator";
    
    // Creator fields
    static class Creator {
        public static final String NAME = "Name";
        public static final String ID = "ID";
        public static final String UPDATE = "Date";
    }
    
    public static final String TOOL = "Tool";
    // ContributorTool fields
    static class ContributorTool {
        public static final String NAME = "ToolName";
        public static final String ID = "ToolID";
        public static final String VERSION = "ToolVersion";
    }
    
    public static final String TASK_REQUEST = "TaskRequest";
    public static final String TASK_RESPONSE = "TaskResponse";
    static class Task {
        public static final String TYPE = "TaskType";
        public static final String SOURCE_LANGUAGE = "SourceLanguage";
        public static final String TARGET_LANGUAGE = "TargetLanguage";
    }
    
    static class TaskResponse {
        public static final String IN_RESPONSE_TO = "InResponseTo";
        public static final String MESSAGE = "Response";
        public static final String COMMENT = "Comment";
    }
    
    // PackageObjectSection
    public static final String ATTR_SECTION_NAME = "name";
    public static final String FILE_RESOURCE = "File";
    
    // Specialized reference resources
    public static final String REFERENCE_FILE_RESOURCE = "ReferenceFile";
    
    static class ObjectFile {
        public static final String LOCATION = "Location";
        public static final String NAME = "Name";
        public static final String ATTR_SEQUENCE = "sequence";
        
        // Only for REFERENCE_FILE_RESOURCE
        public static final String ATTR_LANGUAGE_CHOICE = "languageChoice"; 
    }
}
