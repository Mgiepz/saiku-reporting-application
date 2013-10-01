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
package org.saiku.reporting.backend.component;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.parameters.ListParameter;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterAttributeNames;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterContext;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.engine.classic.core.parameters.ValidationMessage;
import org.pentaho.reporting.engine.classic.core.parameters.ValidationResult;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.engine.classic.core.util.beans.BeanException;
import org.pentaho.reporting.engine.classic.core.util.beans.ConverterRegistry;
import org.pentaho.reporting.engine.classic.core.util.beans.ValueConverter;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;

public class ReportContentUtil
{

  /**
   * Apply inputs (if any) to corresponding report parameters, care is taken when
   * checking parameter types to perform any necessary casting and conversion.
   *
   * @param report The report to retrieve parameter definitions and values from.         
   * @param context a ParameterContext for which the parameters will be under
   * @param validationResult the validation result that will hold the warnings. If null, a new one will be created.
   * @return the validation result containing any parameter validation errors.
   * @throws java.io.IOException if the report of this component could not be parsed.
   * @throws ResourceException   if the report of this component could not be parsed.
   */
  public static ValidationResult applyInputsToReportParameters(final MasterReport report, final ParameterContext context,
      final Map<String, Object> inputs, ValidationResult validationResult)
      throws IOException, ResourceException
  {
    if (validationResult == null)
    {
      validationResult = new ValidationResult();
    }
    // apply inputs to report
    if (inputs != null)
    {
      final Log log = LogFactory.getLog(ReportContentUtil.class);
      final ParameterDefinitionEntry[] params = report.getParameterDefinition().getParameterDefinitions();
      final ReportParameterValues parameterValues = report.getParameterValues();
      for (final ParameterDefinitionEntry param : params)
      {
        final String paramName = param.getName();
        try
        {
          final Object computedParameter = ReportContentUtil.computeParameterValue(context, param, inputs.get(paramName));
          parameterValues.put(param.getName(), computedParameter);
          if (log.isInfoEnabled())
          {
            log.info("ReportPlugin.infoParameterValues" +
                paramName + " " + String.valueOf(inputs.get(paramName)) + " " + String.valueOf(computedParameter));
          }
        }
        catch (Exception e)
        {
          if (log.isWarnEnabled())
          {
            log.warn("ReportPlugin.logErrorParametrization" + e.getMessage()); //$NON-NLS-1$
          }
          validationResult.addError(paramName, new ValidationMessage(e.getMessage()));
        }
      }
    }
    return validationResult;
  }

  public static Object computeParameterValue(final ParameterContext parameterContext,
                                             final ParameterDefinitionEntry parameterDefinition,
                                             final Object value)
      throws ReportProcessingException
  {
    if (value == null)
    {
      // there are still buggy report definitions out there ...
      return null;
    }

    final Class valueType = parameterDefinition.getValueType();
    final boolean allowMultiSelect = isAllowMultiSelect(parameterDefinition);
    if (allowMultiSelect && Collection.class.isInstance(value))
    {
      final Collection c = (Collection) value;
      final Class componentType;
      if (valueType.isArray())
      {
        componentType = valueType.getComponentType();
      }
      else
      {
        componentType = valueType;
      }

      final int length = c.size();
      final Object[] sourceArray = c.toArray();
      final Object array = Array.newInstance(componentType, length);
      for (int i = 0; i < length; i++)
      {
        Array.set(array, i, convert(parameterContext, parameterDefinition, componentType, sourceArray[i]));
      }
      return array;
    }
    else if (value.getClass().isArray())
    {
      final Class componentType;
      if (valueType.isArray())
      {
        componentType = valueType.getComponentType();
      }
      else
      {
        componentType = valueType;
      }

      final int length = Array.getLength(value);
      final Object array = Array.newInstance(componentType, length);
      for (int i = 0; i < length; i++)
      {
        Array.set(array, i, convert(parameterContext, parameterDefinition, componentType, Array.get(value, i)));
      }
      return array;
    }
    else if (allowMultiSelect)
    {
      // if the parameter allows multi selections, wrap this single input in an array
      // and re-call addParameter with it
      final Object[] array = new Object[1];
      array[0] = value;
      return computeParameterValue(parameterContext, parameterDefinition, array);
    }
    else
    {
      return convert(parameterContext, parameterDefinition, parameterDefinition.getValueType(), value);
    }
  }

  private static boolean isAllowMultiSelect(final ParameterDefinitionEntry parameter)
  {
    if (parameter instanceof ListParameter)
    {
      final ListParameter listParameter = (ListParameter) parameter;
      return listParameter.isAllowMultiSelection();
    }
    return false;
  }


  private static Object convert(final ParameterContext context,
                                final ParameterDefinitionEntry parameter,
                                final Class targetType, final Object rawValue)
      throws ReportProcessingException
  {
    if (targetType == null)
    {
      throw new NullPointerException();
    }

    if (rawValue == null)
    {
      return null;
    }
    if (targetType.isInstance(rawValue))
    {
      return rawValue;
    }

//TODO:    
//    if (targetType.isAssignableFrom(TableModel.class) && IPentahoResultSet.class.isAssignableFrom(rawValue.getClass()))
//    {
//      // wrap IPentahoResultSet to simulate TableModel
//      return new PentahoTableModel((IPentahoResultSet) rawValue);
//    }

    final String valueAsString = String.valueOf(rawValue);
    if (StringUtils.isEmpty(valueAsString))
    {
      // none of the converters accept empty strings as valid input. So we can return null instead.
      return null;
    }

    if (targetType.equals(Timestamp.class))
    {
      try
      {
        final Date date = parseDate(parameter, context, valueAsString);
        return new Timestamp(date.getTime());
      }
      catch (ParseException pe)
      {
        // ignore, we try to parse it as real date now ..
      }
    }
    else if (targetType.equals(Time.class))
    {
      try
      {
        final Date date = parseDate(parameter, context, valueAsString);
        return new Time(date.getTime());
      }
      catch (ParseException pe)
      {
        // ignore, we try to parse it as real date now ..
      }
    }
    else if (targetType.equals(java.sql.Date.class))
    {
      try
      {
        final Date date = parseDate(parameter, context, valueAsString);
        return new java.sql.Date(date.getTime());
      }
      catch (ParseException pe)
      {
        // ignore, we try to parse it as real date now ..
      }
    }
    else if (targetType.equals(Date.class))
    {
      try
      {
        final Date date = parseDate(parameter, context, valueAsString);
        return new Date(date.getTime());
      }
      catch (ParseException pe)
      {
        // ignore, we try to parse it as real date now ..
      }
    }

    final String dataFormat = parameter.getParameterAttribute(ParameterAttributeNames.Core.NAMESPACE,
        ParameterAttributeNames.Core.DATA_FORMAT, context);
    if (dataFormat != null)
    {
      try
      {
        if (Number.class.isAssignableFrom(targetType))
        {
          final DecimalFormat format = new DecimalFormat(dataFormat, new DecimalFormatSymbols(Locale.getDefault()));
          format.setParseBigDecimal(true);
          final Number number = format.parse(valueAsString);
          final String asText = ConverterRegistry.toAttributeValue(number);
          return ConverterRegistry.toPropertyValue(asText, targetType);
        }
        else if (Date.class.isAssignableFrom(targetType))
        {
          final SimpleDateFormat format = new SimpleDateFormat(dataFormat, new DateFormatSymbols(Locale.getDefault()));
          format.setLenient(false);
          final Date number = format.parse(valueAsString);
          final String asText = ConverterRegistry.toAttributeValue(number);
          return ConverterRegistry.toPropertyValue(asText, targetType);
        }
      }
      catch (Exception e)
      {
        // again, ignore it .
      }
    }

    final ValueConverter valueConverter = ConverterRegistry.getInstance().getValueConverter(targetType);
    if (valueConverter != null)
    {
      try
      {
        return valueConverter.toPropertyValue(valueAsString);
      }
      catch (BeanException e)
      {
        throw new ReportProcessingException(
        		"ReportPlugin.unableToConvertParameter " + parameter.getName() + " " +  valueAsString); //$NON-NLS-1$
      }
    }
    return rawValue;
  }

  private static Date parseDate(final ParameterDefinitionEntry parameterEntry,
                                final ParameterContext context,
                                final String value) throws ParseException
  {
    try
    {
      return parseDateStrict(parameterEntry, context, value);
    }
    catch (ParseException pe)
    {
      //
    }

    try
    {
      // parse the legacy format that we used in 3.5.0-GA.
      final Long dateAsLong = Long.parseLong(value);
      return new Date(dateAsLong);
    }
    catch (NumberFormatException nfe)
    {
      // ignored
    }

    try
    {
      final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); // NON-NLS
      return simpleDateFormat.parse(value);
    }
    catch (ParseException pe)
    {
      //
    }
    throw new ParseException("Unable to parse Date", 0);
  }

  private static Date parseDateStrict(final ParameterDefinitionEntry parameterEntry,
                                      final ParameterContext context,
                                      final String value) throws ParseException
  {
    final String timezoneSpec = parameterEntry.getParameterAttribute
        (ParameterAttributeNames.Core.NAMESPACE, ParameterAttributeNames.Core.TIMEZONE, context);
    if (timezoneSpec == null ||
        "server".equals(timezoneSpec)) // NON-NLS
    {
      final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); // NON-NLS
      return simpleDateFormat.parse(value);
    }
    else if ("utc".equals(timezoneSpec)) // NON-NLS
    {
      final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); // NON-NLS
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // NON-NLS
      return simpleDateFormat.parse(value);
    }
    else if ("client".equals(timezoneSpec)) // NON-NLS
    {
      try
      {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); // NON-NLS
        return simpleDateFormat.parse(value);
      }
      catch (ParseException pe)
      {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); // NON-NLS
        return simpleDateFormat.parse(value);
      }
    }
    else
    {
      final TimeZone timeZone = TimeZone.getTimeZone(timezoneSpec);
      // this never returns null, but if the timezone is not understood, we end up with GMT/UTC.
      final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); // NON-NLS
      simpleDateFormat.setTimeZone(timeZone);
      return simpleDateFormat.parse(value);
    }
  }
  
}
