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

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.libraries.repository.ContentIOException;

public interface ReportOutputHandler
{
  /**
   * Returns the number of pages in the report.
   *
   * @param report the report to handle.
   * @param yieldRate the yield rate.
   * @return the number of pages generated. This is ignored if {@link #supportsPagination()} returns false.
   * @throws ReportProcessingException if the report processing fails.
   * @throws IOException if there is an IO error.
   * @throws ContentIOException if there is an IO error.
   */
  public int generate(final MasterReport report,
                                 final int acceptedPage,
                                 final OutputStream outputStream,
                                 final int yieldRate)
      throws ReportProcessingException, IOException, ContentIOException;

  public int paginate(final MasterReport report,
      final int yieldRate)
      throws ReportProcessingException, IOException, ContentIOException;

  public void close();

  /**
   * Does the output target support pagination?
   */
  public boolean supportsPagination();

  public Object getReportLock();
}
