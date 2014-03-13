package org.jai.search.exception;

/**
 * Exception thrown in case of issues in indexing the document.
 * 
 * @author malikj
 * 
 */
@SuppressWarnings("serial")
public class DocumentTypeIndexingException extends Exception
{
    public DocumentTypeIndexingException()
    {
    }

    public DocumentTypeIndexingException(final String s)
    {
        super(s);
    }

    public DocumentTypeIndexingException(final String s, final Throwable throwable)
    {
        super(s, throwable);
    }

    public DocumentTypeIndexingException(final Throwable throwable)
    {
        super(throwable);
    }
}
