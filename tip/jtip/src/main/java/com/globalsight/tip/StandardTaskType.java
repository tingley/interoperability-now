package com.globalsight.tip;

import java.util.HashSet;
import java.util.Set;

import com.globalsight.tip.StandardTaskTypeConstants.PrepareSpecifications;
import com.globalsight.tip.StandardTaskTypeConstants.TranslateGenericBitext;
import com.globalsight.tip.StandardTaskTypeConstants.TranslateNativeFormat;
import com.globalsight.tip.StandardTaskTypeConstants.TranslateStrictBitext;

import static com.globalsight.tip.StandardTaskTypeConstants.*;

@SuppressWarnings("serial")
public enum StandardTaskType implements TIPPTaskType {
	
	TRANSLATE_STRICT_BITEXT(TRANSLATE_STRICT_BITEXT_URI, new HashSet<String>() {{
				add(TranslateStrictBitext.BILINGUAL);
				add(TranslateStrictBitext.PREVIEW);
				add(TranslateStrictBitext.STS);
				add(TranslateStrictBitext.TMX);
				add(TranslateStrictBitext.REFERENCE); 
			}}),
	TRANSLATE_GENERIC_BITEXT(TRANSLATE_GENERIC_BITEXT_URI, new HashSet<String>() {{
				add(TranslateGenericBitext.BILINGUAL);
				add(TranslateGenericBitext.STS);
				add(TranslateGenericBitext.TMX);
				add(TranslateGenericBitext.REFERENCE);
			}}),
	TRANSLATE_NATIVE_FORMAT(TRANSLATE_NATIVE_FORMAT_URI, new HashSet<String>() {{
				add(TranslateNativeFormat.INPUT);
				add(TranslateNativeFormat.OUTPUT);
				add(TranslateNativeFormat.STS);
				add(TranslateNativeFormat.TMX);
				add(TranslateNativeFormat.REFERENCE);
			}}),
	PREPARE_SPECIFICATIONS(PREPARE_SPECIFICATIONS_URI, new HashSet<String>() {{
				add(PrepareSpecifications.CONTENT);
				add(PrepareSpecifications.STS);
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
	private Set<String> sectionUris;
	
	StandardTaskType(String typeUri, Set<String> sectionUris) {
		this.uri = typeUri;
		this.sectionUris = sectionUris;
	}

	public String getType() {
		return uri;
	}

	public Set<String> getSupportedSectionTypes() {
		return sectionUris;
	}
	

}
