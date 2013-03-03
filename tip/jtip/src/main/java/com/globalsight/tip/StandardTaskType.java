package com.globalsight.tip;

import java.util.HashSet;
import java.util.Set;

import static com.globalsight.tip.TIPPSectionType.*;

@SuppressWarnings("serial")
public enum StandardTaskType implements TIPPTaskType {

	TRANSLATE_STRICT_BITEXT("http://schema.interoperability-now.org/tipp/v1.5/tasks/translate-strict-bitext", 
	        new HashSet<TIPPSectionType>() {{
				add(BILINGUAL);
				add(PREVIEW);
				add(STS);
				add(TM);
				add(REFERENCE);
				add(METRICS);
			}}),
	TRANSLATE_GENERIC_BITEXT("http://schema.interoperability-now.org/tipp/v1.5/tasks/translate-generic-bitext",
	        new HashSet<TIPPSectionType>() {{
				add(BILINGUAL);
				add(STS);
				add(TM);
				add(REFERENCE);
                add(METRICS);
                add(TERMINOLOGY);
			}}),
	TRANSLATE_NATIVE_FORMAT("http://schema.interoperability-now.org/tipp/v1.5/tasks/translate-native-format",
	        new HashSet<TIPPSectionType>() {{
				add(INPUT);
				add(OUTPUT);
				add(STS);
				add(TM);
				add(REFERENCE);
                add(METRICS);
                add(TERMINOLOGY);
			}}),
	PREPARE_SPECIFICATIONS("http://schema.interoperability-now.org/tipp/v1.5/tasks/prepare-specifications",
	        new HashSet<TIPPSectionType>() {{
				add(INPUT);
				add(STS);
			}});
	
	public static TIPPTaskType forTypeUri(String typeUri) {
		for (TIPPTaskType t : values()) {
			if (t.getType().equals(typeUri)) {
				return t;
			}
		}
		return null;
	}
	
	private String uri;
	private Set<TIPPSectionType> sectionUris;
	
	StandardTaskType(String typeUri, Set<TIPPSectionType> sectionUris) {
		this.uri = typeUri;
		this.sectionUris = sectionUris;
	}

	// TODO: rename to getUri()?
	public String getType() {
		return uri;
	}

	public Set<TIPPSectionType> getSupportedSectionTypes() {
		return sectionUris;
	}
	

}
