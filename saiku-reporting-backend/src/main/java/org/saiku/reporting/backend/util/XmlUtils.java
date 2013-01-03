/*
 * Copyright (C) 2011 Marius Giepz
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 */

package org.saiku.reporting.backend.util;


import java.io.StringWriter;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * XML utils, including formatting.
 */
public class XmlUtils
{

	private static final int LINE_LENGTH = 80;
	private static final int INTENTATION = 2;

	private static XmlFormatter formatter = new XmlFormatter(INTENTATION, LINE_LENGTH);

	private static class XmlFormatter
	{
		private int indentNumChars;
		private int lineLength;
		private boolean singleLine;

		public XmlFormatter(int indentNumChars, int lineLength)
		{
			this.indentNumChars = indentNumChars;
			this.lineLength = lineLength;
		}

		public synchronized String format(String s, int initialIndent)
		{
			int indent = initialIndent;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length(); i++)
			{
				char currentChar = s.charAt(i);
				if (currentChar == '<')
				{
					char nextChar = s.charAt(i + 1);
					if (nextChar == '/')
						indent -= indentNumChars;
					if (!singleLine)   // Don't indent before closing element if we're creating opening and closing elements on a single line.
						sb.append(buildWhitespace(indent));
					if (nextChar != '?' && nextChar != '!' && nextChar != '/')
						indent += indentNumChars;
					singleLine = false;  // Reset flag.
				}
				sb.append(currentChar);
				if (currentChar == '>')
				{
					if (s.charAt(i - 1) == '/')
					{
						indent -= indentNumChars;
						sb.append("\n");
					}
					else
					{
						int nextStartElementPos = s.indexOf('<', i);
						if (nextStartElementPos > i + 1)
						{
							String textBetweenElements = s.substring(i + 1, nextStartElementPos);

							// If the space between elements is solely newlines, let them through to preserve additional newlines in source document.
							if (textBetweenElements.replaceAll("\n", "").length() == 0)
							{
								sb.append(textBetweenElements + "\n");
							}
							// Put tags and text on a single line if the text is short.
							//              else if (textBetweenElements.length() <= lineLength * 0.5)
							//              {
							sb.append(textBetweenElements);
							//                singleLine = true;
							//              }
							//              // For larger amounts of text, wrap lines to a maximum line length.
							//              else
							//              {
							//              //  sb.append("\n" + lineWrap(textBetweenElements, lineLength, indent, null) + "\n");
							//              }
							i = nextStartElementPos - 1;
						}
						else
						{
							sb.append("\n");
						}
					}
				}
			}
			return sb.toString();
		}
	}

	private static String buildWhitespace(int numChars)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numChars; i++)
			sb.append(" ");
		return sb.toString();
	}

	/**
	 * Wraps the supplied text to the specified line length.
	 * @lineLength the maximum length of each line in the returned string (not including indent if specified).
	 * @indent optional number of whitespace characters to prepend to each line before the text.
	 * @linePrefix optional string to append to the indent (before the text).
	 * @returns the supplied text wrapped so that no line exceeds the specified line length + indent, optionally with
	 * indent and prefix applied to each line.
	 */
	private static String lineWrap(String s, int lineLength, Integer indent, String linePrefix)
	{
		if (s == null)
			return null;

		StringBuilder sb = new StringBuilder();
		int lineStartPos = 0;
		int lineEndPos;
		boolean firstLine = true;
		while(lineStartPos < s.length())
		{
			if (!firstLine)
				sb.append("\n");
			else
				firstLine = false;

			if (lineStartPos + lineLength > s.length())
				lineEndPos = s.length() - 1;
			else
			{
				lineEndPos = lineStartPos + lineLength - 1;
				while (lineEndPos > lineStartPos && (s.charAt(lineEndPos) != ' ' && s.charAt(lineEndPos) != '\t'))
					lineEndPos--;
			}
			sb.append(buildWhitespace(indent));
			if (linePrefix != null)
				sb.append(linePrefix);

			sb.append(s.substring(lineStartPos, lineEndPos + 1));
			lineStartPos = lineEndPos + 1;
		}
		return sb.toString();
	}

	public static String prettyPrint(final String xml) {
		StringWriter sw = null;

		try {
			final OutputFormat format = new OutputFormat("  ", true);
			//OutputFormat.createPrettyPrint();
			final org.dom4j.Document document = DocumentHelper.parseText(xml);
			sw = new StringWriter();
			final XMLWriter writer = new XMLWriter(sw, format);
			writer.write(document);
		} catch (Exception e) {
			System.out.println("creating beautified xml failed, refer to exc : " + e.getMessage());
		}
		return sw.toString();
	}

}