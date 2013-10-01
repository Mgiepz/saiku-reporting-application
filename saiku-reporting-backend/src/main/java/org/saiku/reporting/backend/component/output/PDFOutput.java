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
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfOutputProcessor;
import org.pentaho.reporting.libraries.repository.ContentIOException;

public class PDFOutput implements ReportOutputHandler
{
  public PDFOutput()
  {
  }

  public Object getReportLock()
  {
    return this;
  }

  private PageableReportProcessor createProcessor(final MasterReport report,
                                                  final int yieldRate,
                                                  final OutputStream outputStream) throws ReportProcessingException
  {
    final PdfOutputProcessor outputProcessor = new PdfOutputProcessor(report.getConfiguration(), outputStream);
    final PageableReportProcessor proc = new PageableReportProcessor(report, outputProcessor);
    if (yieldRate > 0)
    {
      proc.addReportProgressListener(new YieldReportListener(yieldRate));
    }
    return proc;
  }

  public int paginate(MasterReport report, int yieldRate) throws ReportProcessingException, IOException, ContentIOException {
    return 0;
  }
  
  public int generate(final MasterReport report,
                          final int acceptedPage,
                          final OutputStream outputStream,
                          final int yieldRate) throws ReportProcessingException, IOException
  {
    final PageableReportProcessor proc = createProcessor(report, yieldRate, outputStream);
    try
    {
      proc.processReport();
      return 0;
    }
    finally
    {
      proc.close();
    }
  }

  public boolean supportsPagination() {
    return false;
  }

  public void close()
  {
  }
}
