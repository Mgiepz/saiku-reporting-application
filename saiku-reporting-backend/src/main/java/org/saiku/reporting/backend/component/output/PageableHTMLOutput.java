package org.saiku.reporting.backend.component.output;

import java.io.IOException;
import java.io.OutputStream;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.DisplayAllFlowSelector;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.SinglePageFlowSelector;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.PageableHtmlOutputProcessor;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.stream.StreamRepository;


public class PageableHTMLOutput implements ReportOutputHandler
{
  private String contentHandlerPattern;
  private ProxyOutputStream proxyOutputStream;
  private PageableReportProcessor proc;
  private AllItemsHtmlPrinter printer;

  public PageableHTMLOutput(final String contentHandlerPattern)
  {
    this.contentHandlerPattern = contentHandlerPattern;
  }

  public Object getReportLock()
  {
    return this;
  }

  public String getContentHandlerPattern()
  {
    return contentHandlerPattern;
  }

  public ProxyOutputStream getProxyOutputStream()
  {
    return proxyOutputStream;
  }

  public void setProxyOutputStream(final ProxyOutputStream proxyOutputStream)
  {
    this.proxyOutputStream = proxyOutputStream;
  }

  public HtmlPrinter getPrinter()
  {
    return printer;
  }

  public void setPrinter(AllItemsHtmlPrinter printer)
  {
    this.printer = printer;
  }  
  
  public PageableReportProcessor getReportProcessor() {
    return proc;
  }

  public void setReportProcessor(PageableReportProcessor proc) {
    this.proc = proc;
  }  
  
  protected PageableReportProcessor createReportProcessor(final MasterReport report, final int yieldRate)
      throws ReportProcessingException
  {

    proxyOutputStream = new ProxyOutputStream();

    printer = new AllItemsHtmlPrinter(report.getResourceManager());

	final StreamRepository targetRepository = new StreamRepository(proxyOutputStream);
	final ContentLocation targetRoot = targetRepository.getRoot();
    printer.setContentWriter(targetRoot, new DefaultNameGenerator(targetRoot, "index", "html"));
    
//   printer.setUrlRewriter(new PentahoURLRewriter(contentHandlerPattern, false));

    final PageableHtmlOutputProcessor outputProcessor = new PageableHtmlOutputProcessor(report.getConfiguration());
    outputProcessor.setPrinter(printer);
    proc = new PageableReportProcessor(report, outputProcessor);

    if (yieldRate > 0)
    {
      proc.addReportProgressListener(new YieldReportListener(yieldRate));
    }

    return proc;
  }

  protected void reinitOutputTarget() throws ReportProcessingException, ContentIOException
  {
    //TOTO: nothing for now!
  }

  public int paginate(final MasterReport report,
                      final int yieldRate) throws ReportProcessingException, IOException, ContentIOException
  {
    if (proc == null)
    {
      proc = createReportProcessor(report, yieldRate);
    }
    reinitOutputTarget();
    try
    {
      if (proc.isPaginated() == false)
      {
        proc.paginate();
      }
    }
    finally
    {
      printer.setContentWriter(null, null);
      printer.setDataWriter(null, null);
    }

    return proc.getLogicalPageCount();
  }

  public int generate(final MasterReport report,
                          final int acceptedPage,
                          final OutputStream outputStream,
                          final int yieldRate)
      throws ReportProcessingException, IOException, ContentIOException
  {
    if (proc == null)
    {
      proc = createReportProcessor(report, yieldRate);
    }
    final PageableHtmlOutputProcessor outputProcessor = (PageableHtmlOutputProcessor) proc.getOutputProcessor();
    if (acceptedPage >= 0)
    {
      outputProcessor.setFlowSelector(new SinglePageFlowSelector(acceptedPage));
    }
    else
    {
      outputProcessor.setFlowSelector(new DisplayAllFlowSelector());
    }
    proxyOutputStream.setParent(outputStream);
    reinitOutputTarget();
    try
    {
      proc.processReport();
      return proc.getLogicalPageCount();
    }
    finally
    {
      outputStream.flush();
      printer.setContentWriter(null, null);
      printer.setDataWriter(null, null);
    }
  }

  public boolean supportsPagination() {
    return true;
  }

  public void close()
  {
    if (proc != null)
    {
      proc.close();
      proxyOutputStream = null;
    }

  }
}
