package org.jax.mgi.snpindexer.util.es;

import java.io.IOException;

import org.elasticsearch.xcontent.XContentBuilder;


public abstract class Mapping extends Builder {
	
	public Mapping(Boolean pretty) {
		super(pretty);
	}

	public abstract void buildMapping() throws IOException;


	public static class FieldBuilder {
		XContentBuilder builder;
		String name;
		String type;
		String analyzer;
		boolean index;
		boolean enabled;
		boolean autocomplete;
		boolean classicText;
		boolean htmlSmoosh;
		boolean keyword;
		boolean keywordAutocomplete;
		boolean letterText;
		boolean sort;
		boolean standardBigrams;
		boolean standardText;
		boolean symbol;
		boolean synonym;

		public FieldBuilder(XContentBuilder builder, String name, String type) {
			this.builder = builder;
			this.name = name;
			this.type = type;
			this.index = true;
			this.enabled = true;
		}

		public FieldBuilder analyzer(String analyzer) {
			this.analyzer = analyzer;
			return this;
		}

		public FieldBuilder autocomplete() {
			this.autocomplete = true;
			return this;
		}

		public FieldBuilder classicText() {
			this.classicText = true;
			return this;
		}

		public FieldBuilder htmlSmoosh() {
			this.htmlSmoosh = true;
			return this;
		}

		public FieldBuilder keyword() {
			this.keyword = true;
			return this;
		}

		public FieldBuilder keywordAutocomplete() {
			this.keywordAutocomplete = true;
			return this;
		}

		public FieldBuilder letterText() {
			this.letterText = true;
			return this;
		}

		public FieldBuilder sort() {
			this.sort = true;
			return this;
		}

		public FieldBuilder standardBigrams() {
			this.standardBigrams = true;
			return this;
		}

		public FieldBuilder standardText() {
			this.standardText = true;
			return this;
		}

		public FieldBuilder symbol() {
			this.symbol = true;
			return this;
		}

		public FieldBuilder synonym() {
			this.synonym = true;
			return this;
		}
		
		public FieldBuilder notEnabled() {
			this.enabled = false;
			return this;
		}
		
		public FieldBuilder notIndexed() {
			this.index = false;
			return this;
		}

		protected void buildProperty(String name, String type) throws IOException {
			buildProperty(name, type, null, null, null);
		}

		protected void buildProperty(String name, String type, String analyzer) throws IOException {
			buildProperty(name, type, analyzer, null, null);
		}

		protected void buildProperty(String name, String type, String analyzer, String search_analyzer, String normalizer) throws IOException {
			builder.startObject(name);
			if(type != null) builder.field("type", type);
			if(analyzer != null) builder.field("analyzer", analyzer);
			if(search_analyzer != null) builder.field("search_analyzer", search_analyzer);
			if(normalizer!= null) builder.field("normalizer", normalizer);
			builder.endObject();
		}


		public void build() throws IOException {
			builder.startObject(name);
			if(type != null) builder.field("type", type);
			if(!index) builder.field("index", false);
			if(!enabled) builder.field("enabled", false);
			if(analyzer != null) builder.field("analyzer", analyzer);
			if(symbol || autocomplete || keyword || keywordAutocomplete || synonym || sort || standardText) {
				builder.startObject("fields");
				if(keyword) { buildProperty("keyword", "keyword"); }
				if(keywordAutocomplete) { buildProperty("keywordAutocomplete", "text", "keyword_autocomplete", "keyword_autocomplete_search", null); }
				if(letterText) buildProperty("letterText", "text", "letter_text", "default", null);
				if(symbol) { buildProperty("symbol", "text", "symbols"); }
				if(autocomplete) buildProperty("autocomplete", "text", "autocomplete", "autocomplete_search", null);
				if(classicText) buildProperty("classicText", "text", "classic_text", "default", null);
				if(synonym) buildProperty("synonyms", "text", "generic_synonym", "autocomplete_search", null);
				if(sort) buildProperty("sort", "keyword", null, null, "lowercase");
				if(htmlSmoosh) buildProperty("htmlSmoosh", "text", "html_smoosh");
				if(standardBigrams) buildProperty("standardBigrams", "text", "standard_bigrams");
				if(standardText) buildProperty("standardText", "text", "standard_text", "default", null);
				builder.endObject();
			}
			builder.endObject();
		}
	}

}
