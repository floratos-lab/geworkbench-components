package org.geworkbench.util;

import org.genepattern.webservice.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nazaire
 * Date: Sep 23, 2009
 */
public class DescriptiveParameter extends Parameter
{
    private boolean isFile = false;

    public DescriptiveParameter(String name, String value, boolean isFile)
    {
        super(name, value);
        this.isFile = isFile;
    }

    public DescriptiveParameter(String name, File value) throws IOException, SecurityException
    {
        super(name, value);
        this.isFile = true;
    }

    public boolean isFile()
    {
        return isFile;
    }
}
