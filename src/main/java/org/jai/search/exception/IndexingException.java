package org.jai.search.exception;

/**
 * Exception thrown in case of issues in indexing the document.
 * 
 * @author malikj
 * 
 */
@SuppressWarnings("serial")
public class IndexingException extends Exception
{
    public IndexingException()
    {
    }

    public IndexingException(final String s)
    {
        super(s);
    }

    public IndexingException(final String s, final Throwable throwable)
    {
        super(s, throwable);
    }

    public IndexingException(final Throwable throwable)
    {
        super(throwable);
    }
}
