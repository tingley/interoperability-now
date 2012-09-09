package com.globalsight.tip;

interface TIPPConstants {

    public static final String SCHEMA_VERSION = "1.5";
    public static final String SCHEMA_LOCATION =
                "http://schema.interoperability-now.org/tipp/TIPPManifest-1_5.xsd";

    public static final String MANIFEST = "TIPPManifest";
    public static final String ATTR_VERSION = "version";
    
    public static final String GLOBAL_DESCRIPTOR = "GlobalDescriptor";
    public static final String PACKAGE_OBJECTS = "PackageObjects";

    // GlobalDescriptor fields
    public static final String UNIQUE_PACKAGE_ID = "UniquePackageID";
    public static final String PACKAGE_CREATOR = "PackageCreator";
    public static final String TASK_REQUEST = "TaskRequest";
    public static final String TASK_RESPONSE = "TaskResponse";
    
    // Creator fields
    static class Creator {
        public static final String NAME = "Name";
        public static final String ID = "ID";
        public static final String UPDATE = "Update";
    }
    
    public static final String TOOL = "Tool";
    // ContributorTool fields
    static class ContributorTool {
        public static final String NAME = "ToolName";
        public static final String ID = "ToolID";
        public static final String VERSION = "ToolVersion";
    }
    
    public static final String TASK = "Task";
    static class Task {
        public static final String TYPE = "TaskType";
        public static final String SOURCE_LANGUAGE = "SourceLanguage";
        public static final String TARGET_LANGUAGE = "TargetLanguage";
    }
    
    static class TaskResponse {
        public static final String IN_RESPONSE_TO = "InResponseTo";
        public static final String MESSAGE = "ResponseMessage";
        public static final String COMMENT = "ResponseComment";
    }
    
    // PackageObjects
    public static final String PACKAGE_OBJECT_SECTION = "PackageObjectSection";
    
    // PackageObjectSection
    public static final String ATTR_SECTION_NAME = "name";
    public static final String ATTR_SECTION_TYPE = "type";
    public static final String OBJECT_FILE = "ObjectFile";
    
    static class ObjectFile {
        public static final String LOCATION = "Location";
        public static final String NAME = "Name";
        public static final String ATTR_SEQUENCE = "sequence";
    }
}
