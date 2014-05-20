package org.saiku.reporting.backend.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.metadata.model.IMetadataQueryExec;
import org.pentaho.metadata.query.model.Parameter;
import org.pentaho.metadata.query.model.Query;
import org.pentaho.metadata.query.model.util.QueryXmlHelper;
import org.saiku.reporting.backend.server.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Properties;
/**
 * Created by bugg on 12/05/14.
 */
public class MetadataQueryComponent {

    @Autowired
    private MetadataRepository metadataRepository;

    public void setMetadataRepository(MetadataRepository metadataRepository){
        this.metadataRepository = metadataRepository;
    }
    static final Log logger = LogFactory.getLog( MetadataQueryComponent.class );
    Query queryObject; // An optional query model to execute instead of the string query
    String query;
    Integer maxRows; // -1;
    Integer timeout; // -1;
    boolean readOnly; // false;

    boolean live = false;
    boolean useForwardOnlyResultSet = false;
    boolean logSql = false;
    boolean forceDbDialect = false;
    IPentahoResultSet resultSet = null;

    String xmlHelperClass = "org.pentaho.metadata.query.model.util.QueryXmlHelper"; //$NON-NLS-1$
    String sqlGeneratorClass = null;

    Map<String, Object> inputs = null;

    /*
     * The list of inputs to this component, used when resolving parameter values.
     *
     * @param inputs map of inputs
     */
    public void setInputs( Map<String, Object> inputs ) {
        this.inputs = inputs;
    }

    public void setLogSql( boolean logSql ) {
        this.logSql = logSql;
    }

    public void setQuery( String query ) {
        this.query = query;
    }

    /**
     * Sets the query to be executed. This is a query model that will be executed in preference to a string-based query.
     *
     * @param queryObject
     */
    public void setQueryObject( Query queryObject ) {
        this.queryObject = queryObject;
    }

    public void setMaxRows( Integer maxRows ) {
        this.maxRows = maxRows;
    }

    public void setTimeout( Integer timeout ) {
        this.timeout = timeout;
    }

    public void setLive( boolean live ) {
        this.live = live;
    }

    /**
     * This sets the read only property in the Pentaho SQLConnection API
     *
     * @param readOnly
     *          true if read only
     */
    public void setReadOnly( Boolean readOnly ) {
        this.readOnly = readOnly;
    }

    public void setUseForwardOnlyResultSet( boolean useForwardOnlyResultSet ) {
        this.useForwardOnlyResultSet = useForwardOnlyResultSet;
    }

    public void setQueryModelXmlHelper( String xmlHelperClass ) {
        this.xmlHelperClass = xmlHelperClass;
    }

  /*
   * TODO handle these generically public void setQueryModelSqlGenerator(String sqlGeneratorClass) {
   * this.sqlGeneratorClass = sqlGeneratorClass; }
   *
   * public void setForceDbDialect(boolean forceDbDialect) { this.forceDbDialect = forceDbDialect; }
   */

    @SuppressWarnings( "unchecked" )
    private QueryXmlHelper createQueryXmlHelper() throws Exception {
        Class clazz = Class.forName( xmlHelperClass );
        return (QueryXmlHelper) clazz.getConstructor( new Class[] {} ).newInstance( new Object[] {} );
    }

    public boolean execute() {

        // get the xml parser
        QueryXmlHelper helper = null;
        try {
            helper = createQueryXmlHelper();
        } catch ( Exception e ) {
            logger.error( "error", e ); //$NON-NLS-1$
            return false;
        }

        // parse the metadata query
        try {
            queryObject = helper.fromXML( metadataRepository.getMetadataDomainRepository(), query );
        } catch ( Exception e ) {
            logger.error( "error", e ); //$NON-NLS-1$
            return false;
        }



        if ( queryObject == null ) {
            logger.error( "error query object null" ); //$NON-NLS-1$
            return false;
        }

        // Read metadata for new timeout/max_rows and set in superclass
        // Can still be overridden in the action sequence
        if ( timeout == null ) {
            Object timeoutProperty = queryObject.getLogicalModel().getProperty( "timeout" ); //$NON-NLS-1$
            if ( timeoutProperty != null && timeoutProperty instanceof Number ) {
                int timeoutVal = ( (Number) timeoutProperty ).intValue();
                this.setTimeout( timeoutVal );
            }
        }

        if ( maxRows == null ) {
            Object maxRowsProperty = queryObject.getLogicalModel().getProperty( "max_rows" ); //$NON-NLS-1$
            if ( maxRowsProperty != null && maxRowsProperty instanceof Number ) {
                int maxRowsVal = ( (Number) maxRowsProperty ).intValue();
                this.setMaxRows( maxRowsVal );
            }
        }


        // determine parameter values
        IMetadataQueryExec executor = (IMetadataQueryExec) metadataRepository.getExecutor(queryObject, inputs, metadataRepository);



        try {
            executor.setDoQueryLog( logSql );
            executor.setForwardOnly( this.useForwardOnlyResultSet );
            executor.setMaxRows( this.maxRows );
            executor.setMetadataDomainRepository( metadataRepository.getMetadataDomainRepository() );
            executor.setReadOnly( this.readOnly );
            executor.setTimeout( this.timeout );
            if ( this.inputs != null ) {
                executor.setInputs( this.inputs );
            }

            resultSet = executor.executeQuery( queryObject );
            if ( resultSet != null && !live && executor.isLive() ) {
                // read the results and cache them
                MemoryResultSet cachedResultSet = new MemoryResultSet( resultSet.getMetaData() );
                Object[] rowObjects = resultSet.next();
                while ( rowObjects != null ) {
                    cachedResultSet.addRow( rowObjects );
                    rowObjects = resultSet.next();
                }
                resultSet.close();
                resultSet.closeConnection();
                resultSet = cachedResultSet;
            }

            return resultSet != null;
        } catch ( Exception e ) {
            logger.error( "error", e ); //$NON-NLS-1$
            throw new RuntimeException( e.getLocalizedMessage(), e );
        }

    }

    public boolean validate() {
        if ( query == null ) {
            logger.error( "no query specified" ); //$NON-NLS-1$
            return false;
        }

        return true;
    }

    public IPentahoResultSet getResultSet() {
        return resultSet;
    }

}
