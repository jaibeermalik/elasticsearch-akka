package org.jai.search.exception;

/**
 * Exception thrown in case of issues generating the products etc. data which needs to be indexed.
 * 
 * @author malikj
 * 
 */
@SuppressWarnings("serial")
public class DataGenerationException extends Exception
{
    public DataGenerationException()
    {
    }

    public DataGenerationException(final String s)
    {
        super(s);
    }

    public DataGenerationException(final String s, final Throwable throwable)
    {
        super(s, throwable);
    }

    public DataGenerationException(final Throwable throwable)
    {
        super(throwable);
    }
}
