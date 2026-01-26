package org.jax.mgi.snpindexer.util.es;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import co.elastic.clients.elasticsearch._types.mapping.DateProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;

/**
 * Base builder for Elasticsearch mappings, supporting flags for subfields.
 */
public abstract class Mapping {

    protected Map<String, Property> properties = new LinkedHashMap<>();

    // Flags for building special subfields
    protected boolean autocomplete;
    protected boolean keywordAutocomplete;
    protected boolean synonym;
    protected boolean sort;
    protected boolean classicText;
    protected boolean htmlSmoosh;
    protected boolean standardBigrams;
    protected boolean standardText;
    protected boolean symbol;
    protected boolean letterText;

    public abstract void buildMapping() throws IOException;

    /**
     * Adds a keyword field, optionally with special subfields based on flags.
     */
    protected void addKeywordField(String name) {
        Map<String, Property> fields = new LinkedHashMap<>();

        if (keywordAutocomplete) {
            fields.put("keywordAutocomplete", createTextProperty("keyword_autocomplete", "keyword_autocomplete_search"));
        }
        if (autocomplete) {
            fields.put("autocomplete", createTextProperty("autocomplete", "autocomplete_search"));
        }
        if (synonym) {
            fields.put("synonyms", createTextProperty("generic_synonym", "autocomplete_search"));
        }
        if (sort) {
            fields.put("sort", createKeywordPropertyWithNormalizer("lowercase"));
        }
        if (classicText) {
            fields.put("classicText", createTextProperty("classic_text", "default"));
        }
        if (htmlSmoosh) {
            fields.put("htmlSmoosh", createTextProperty("html_smoosh", null));
        }
        if (standardBigrams) {
            fields.put("standardBigrams", createTextProperty("standard_bigrams", null));
        }
        if (standardText) {
            fields.put("standardText", createTextProperty("standard_text", "default"));
        }
        if (symbol) {
            fields.put("symbol", createTextProperty("symbols", null));
        }
        if (letterText) {
            fields.put("letterText", createTextProperty("letter_text", "default"));
        }

        KeywordProperty.Builder keywordBuilder = new KeywordProperty.Builder();
        if (!fields.isEmpty()) {
            keywordBuilder.fields(fields);
        }

        Property property = new Property.Builder()
            .keyword(keywordBuilder.build())
            .build();

        properties.put(name, property);

        // Reset flags after building field to avoid bleed-over
        resetFlags();
    }

    /**
     * Adds a text field with optional analyzer and search_analyzer.
     */
    protected void addTextField(String name, String analyzer, String searchAnalyzer) {
        TextProperty.Builder textBuilder = new TextProperty.Builder();
        if (analyzer != null) {
            textBuilder.analyzer(analyzer);
        }
        if (searchAnalyzer != null) {
            textBuilder.searchAnalyzer(searchAnalyzer);
        }

        Property property = new Property.Builder()
            .text(textBuilder.build())
            .build();

        properties.put(name, property);

        resetFlags();
    }

    /**
     * Adds a date field with optional format.
     */
    protected void addDateField(String name, String format) {
        DateProperty.Builder dateBuilder = new DateProperty.Builder();
        if (format != null) {
            dateBuilder.format(format);
        }
        Property property = new Property.Builder()
            .date(dateBuilder.build())
            .build();

        properties.put(name, property);

        resetFlags();
    }
    
    /**
     * Adds a object field to the mapping.
     *
     * @param name the name of the object field
     * @param enabled boolean 
     */
    protected void addObjectField(String name, boolean enabled) {
        ObjectProperty.Builder objectBuilder = new ObjectProperty.Builder();
        objectBuilder.enabled(enabled);

        Property property = new Property.Builder()
                .object(objectBuilder.build())
                .build();

        properties.put(name, property);
        resetFlags();
    }    

    // Helpers to create Property for subfields

    private Property createTextProperty(String analyzer, String searchAnalyzer) {
        TextProperty.Builder tb = new TextProperty.Builder();
        tb.analyzer(analyzer);
        if (searchAnalyzer != null) {
            tb.searchAnalyzer(searchAnalyzer);
        }
        return new Property.Builder().text(tb.build()).build();
    }

    private Property createKeywordPropertyWithNormalizer(String normalizer) {
        KeywordProperty.Builder kb = new KeywordProperty.Builder();
        if (normalizer != null) {
            kb.normalizer(normalizer);
        }
        return new Property.Builder().keyword(kb.build()).build();
    }

    /**
     * Return the built mapping as a TypeMapping.
     */
    public TypeMapping getMapping() {
        return new TypeMapping.Builder()
            .properties(properties)
            .build();
    }

    /**
     * Reset all flags to false after building a field.
     */
    private void resetFlags() {
        autocomplete = false;
        keywordAutocomplete = false;
        synonym = false;
        sort = false;
        classicText = false;
        htmlSmoosh = false;
        standardBigrams = false;
        standardText = false;
        symbol = false;
        letterText = false;
    }

    // Fluent setters for flags

    public Mapping autocomplete() {
        this.autocomplete = true;
        return this;
    }

    public Mapping keywordAutocomplete() {
        this.keywordAutocomplete = true;
        return this;
    }

    public Mapping synonym() {
        this.synonym = true;
        return this;
    }

    public Mapping sort() {
        this.sort = true;
        return this;
    }

    public Mapping classicText() {
        this.classicText = true;
        return this;
    }

    public Mapping htmlSmoosh() {
        this.htmlSmoosh = true;
        return this;
    }

    public Mapping standardBigrams() {
        this.standardBigrams = true;
        return this;
    }

    public Mapping standardText() {
        this.standardText = true;
        return this;
    }

    public Mapping symbol() {
        this.symbol = true;
        return this;
    }

    public Mapping letterText() {
        this.letterText = true;
        return this;
    }
}

