package org.saiku.reporting.backend.util;

import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;

	public class FileExtensionFilter implements IBasicFileFilter 
	{
	    private char[][] extensions;

	    public FileExtensionFilter(String[] extensions)
	    {
	        int length = extensions.length;
	        this.extensions = new char[length][];
	        for (String s : extensions)
	        {
	            this.extensions[--length] = s.toCharArray();
	        }
	    }

	    @Override
	    public boolean accept(IBasicFile file)
	    {
	        char[] path = file.getPath().toCharArray();
	        for (char[] extension : extensions)
	        {
	            if (extension.length > path.length)
	            {
	                continue;
	            }
	            int pStart = path.length - 1;
	            int eStart = extension.length - 1;
	            boolean success = true;
	            for (int i = 0; i <= eStart; i++)
	            {
	                if ((path[pStart - i] | 0x20) != (extension[eStart - i] | 0x20))
	                {
	                    success = false;
	                    break;
	                }
	            }
	            if (success)
	                return true;
	        }
	        return false;
	    }
	}