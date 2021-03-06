package org.jai.search.config;

import org.apache.commons.lang.LocaleUtils;

import java.util.Locale;

public enum ElasticSearchIndexConfig
{
    COM_WEBSITE("com", IndexDocumentType.PRODUCT.getText(), SupportedLocale.ENGLISH),
    NL_WEBSITE("nl", IndexDocumentType.PRODUCT.getText(), SupportedLocale.DUTCH);
    private String indexName;

    private String documentType;

    private SupportedLocale supportedLocale;

    private ElasticSearchIndexConfig(final String indexName, final String documentType, final SupportedLocale supportedLocale)
    {
        this.indexName = indexName;
        this.documentType = documentType;
        this.supportedLocale = supportedLocale;
    }

    public String getIndexAliasName()
    {
        return indexName;
    }

    public String getDocumentType()
    {
        return documentType;
    }

    public SupportedLocale getSupportedLocale()
    {
        return supportedLocale;
    }

    public static enum SupportedLocale
    {
        ENGLISH("en_EN", "English"),
        DUTCH("nl_NL", "Dutch");
        private String text;

        private String lang;

        private SupportedLocale(final String text, final String lang)
        {
            this.text = text;
        }

        public String getText()
        {
            return text;
        }

        public String getLang()
        {
            return lang;
        }
    }

    public Locale getLocale()
    {
        return LocaleUtils.toLocale(supportedLocale.getText());
    }

    public String getGroupDocumentType()
    {
        switch (this)
        {
            case COM_WEBSITE:
            case NL_WEBSITE:
                return IndexDocumentType.PRODUCT_GROUP.getText();
            default:
                break;
        }
        return null;
    }

    public String getPropertiesDocumentType()
    {
        switch (this)
        {
            case COM_WEBSITE:
            case NL_WEBSITE:
                return IndexDocumentType.PRODUCT_PROPERTY.getText();
            default:
                break;
        }
        return null;
    }

    public String getSnowballCustomFilterName()
    {
        return "jai_filter_snowball_" + getSupportedLocale().getText();
    }

    public String getStopwordsCustomFilterName()
    {
        return "jai_filter_stopword_" + getSupportedLocale().getText();
    }

    public String getWorddelimiterCustomFilterName()
    {
        return "jai_filter_worddelimiter_" + getSupportedLocale().getText();
    }

    public String getSynonymsCustomFilterName()
    {
        return "jai_filter_synonyms_" + getSupportedLocale().getText();
    }

    public String getCustomFreeTextAnalyzerName()
    {
        return "jai_analyzer_custom_freetext_" + getSupportedLocale().getText();
    }

    public String getCustomFacetAnalyzerName()
    {
        return "jai_analyzer_custom_facet_" + getSupportedLocale().getText();
    }

    public String getAutoSuggestionAnalyzerName()
    {
        return "jai_analyzer_autosuggestion_" + getSupportedLocale().getText();
    }

    public String getShingleTokenFilterName()
    {
        return "jai_shingle_token_filter" + getSupportedLocale().getText();
    }

    public String getNGramTokenFilterName()
    {
        return "jai_ngram_token_filter" + getSupportedLocale().getText();
    }

    public String getStandardTextAnalyzerName()
    {
        return "jai_analyzer_standard_freetext_" + getSupportedLocale().getText();
    }
}
