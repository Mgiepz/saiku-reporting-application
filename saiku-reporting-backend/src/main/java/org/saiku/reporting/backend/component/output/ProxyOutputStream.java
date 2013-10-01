/*******************************************************************************
 * Copyright 2013 Marius Giepz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.saiku.reporting.backend.component.output;

import java.io.IOException;
import java.io.OutputStream;

public class ProxyOutputStream extends OutputStream
{
  private OutputStream parent;

  public ProxyOutputStream()
  {
  }

  public OutputStream getParent()
  {
    return parent;
  }

  public void setParent(final OutputStream parent)
  {
    this.parent = parent;
  }

  public void write(final int b) throws IOException
  {
    if (parent != null)
    {
      parent.write(b);
    }
  }

  public void write(final byte[] b) throws IOException
  {
    if (parent != null)
    {
      parent.write(b);
    }
  }

  public void write(final byte[] b, final int off, final int len) throws IOException
  {
    if (parent != null)
    {
      parent.write(b, off, len);
    }
  }

  public void flush() throws IOException
  {
    if (parent != null)
    {
      parent.flush();
    }
  }

  public void close() throws IOException
  {
    if (parent != null)
    {
      parent.close();
    }
  }
}
