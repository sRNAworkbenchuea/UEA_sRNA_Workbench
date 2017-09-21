package uk.ac.uea.cmp.srnaworkbench.tools;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;

/**
 * Base class for all Tool parameters class.  Provides basic load and save functionality
 * for all tools.
 * @author ezb11yfu
 */
public abstract class ToolParameters
{
    private LinkedHashMap<String, Parameter<?> > parameters;

    protected ToolParameters()
    {
        this.parameters = new LinkedHashMap<String, Parameter<?> >();
    }

    /**
     * Adds a new Parameter to the map, which is created from the provided ParameterDefinition.
     * The parameter value is set to the default value specified in the definition
     * @param definition The definition of the new parameter to add
     */
    protected <T> void addParameter(ParameterDefinition<T> definition)
    {
        addParameter(new Parameter<T>(definition, definition.getDefaultValue()));
    }

    /**
     * Adds the provided parameter to the map
     * @param param Parameter to add
     */
    protected <T> void addParameter(Parameter<T> param)
    {
        this.parameters.put(param.getName(), param);
    }

    /**
     * Gets the entire parameter map
     * @return The parameter map keyed by parameter name
     */
    protected Map<String, Parameter<?> > getParameterMap()      {return this.parameters;}

    /**
     * Gets a parameter of unknown type given a parameter name
     * @param name The name of the parameter to get
     * @return The parameter object (type unknown)
     */
    protected Parameter<?> getParameter(String name)
    {
//      if(name.equals( "genome") )
//        System.out.println( "here" );
        return this.parameters.get(name);
    }

    /**
     * Gets a parameter value given a parameter name.  Throws an IllegalArgumentException
     * if the requested parameter does not exist in the map.  Assumes the caller
     * knows the correct type for the entry they are requesting.
     * @param name The name of the parameter to get
     * @return The parameter value (as an object of unknown type)
     */
    protected <T> T getParameterValue(Class<T> type, String name)
    {
        Parameter param = this.getParameter(name);

        if (param == null)
            throw new NullPointerException(name + " has not been initialised.");
        else
            return type.cast(param.getValue());
    }

    /**
     * Sets a parameter given a ParameterDefinition and a value of specific type T.
     * If the parameter doesn't already exist in the map it will be created.
     * @param <T> The type of the parameter to set
     * @param definition The parameter definition
     * @param value The value of type T to set
     */
    @SuppressWarnings("unchecked")
    protected <T> void setParameter(ParameterDefinition<T> definition, T value)
    {

        Parameter<?> param = this.getParameter(definition.getName());

        if (param == null)
        {

            this.addParameter(new Parameter<T>(definition, value));
            // CHRIS ADDED: there is no code to check if initial setting of parameter is valid: do it here!
            Parameter<T> paramT = (Parameter<T>)parameters.get( definition.getName() );
            paramT.setValue( value );
        }
        else
        {
            // Parameter returned will always be of type T so this line should be fine
            @SuppressWarnings("unchecked")
            Parameter<T> paramT = (Parameter<T>)parameters.get( definition.getName() );

            paramT.setValue( value );
        }
    }

    /**
     * Sets a parameter given a ParameterDefinition and a value of specific type T.
     * If the parameter doesn't already exist in the map it will be created.
     * @param definition The parameter definition
     * @param value The value of type T to set
     */
    protected <T> void setParameterValue(String name, T value)
    {
        Parameter<?> param = getParameter(name);

        if (param == null)
        {
            throw new NullPointerException(name + " has not been initialised.");
        }
        else
        {
            // Parameter returned will always be of type T so this line should be fine
            @SuppressWarnings("unchecked")
            Parameter<T> paramT = param.getClass().cast(param);
            paramT.setValue( value );
        }
    }

    /**
     * Loads a Map of String key-value pairs from file
     * @param file The file to load
     * @return Map of key-value pairs as String
     * @throws IOException Thrown if there are any problems reading the file
     */
    private static Map<String, String> loadParameters(File file) throws IOException
    {
        BufferedReader br = null;
        HashMap<String,String> map = new HashMap<String,String>();

        br = new BufferedReader(new FileReader(file));

        String line = null;
        while((line = br.readLine()) != null)
        {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#"))
            {
                // This line's a comment or contains whitespace... goto next line
                continue;
            }

            String[] parts = line.split("=");

            if (parts.length < 2)
            {
                throw new IOException("Syntax error in param file: \"" + line + "\"");
            }

            String param = parts[0].trim();
            String value = parts[1].trim();

            map.put(param, value);
        }

        br.close();

        return map;
    }

    /**
     * Loads a ToolParameters object from disk.  Requires a derived ToolParameters object
     * as an argument to understand the parameter map for the given type.
     * @param <T> A type derived from a ToolParameters base class
     * @param params A derived ToolParameters object, normally the default constructor for
     * that type can be used here.
     * @param file The file containing the saved parameters on disk
     * @return The ToolParameters object with all parameters set as found in the file on disk
     * @throws IOException Thrown if there were any problems loading the file.
     */
    public static <T extends ToolParameters> T load( T params, File file ) throws IOException
    {
      // read the file...
      Map< String, String > m = loadParameters( file );

      for ( Map.Entry<String, String> me : m.entrySet() )
      {
        
        String param = me.getKey();
        String value = me.getValue();
//
//        if(param.equals( "genome") )
//          System.out.println( "" );
        Parameter p = params.getParameter( param );

        if ( p == null )
        {
          throw new IOException( "Unknown Parameter: \"" + param + "\"" );
        }
        else
        {
          p.setValueFromString( value );
        }
      }

      return params;
    }

    /**
     * Saves out all parameters to the specified file
     * @param file The file to write to
     * @throws IOException Thrown if something goes wrong writing to the file
     */
    public void save(File file) throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

        for(Parameter p : this.parameters.values())
        {
            out.print(p.getName() + "=" + p.getValue() + "\n");
        }

        out.flush();
        out.close();
    }

    /**
     * Creates a string representation of the parameter map list.
     * @return Parameter list as String
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for(Parameter p : this.getParameterMap().values())
        {
            sb.append(p.toString());
            sb.append(LINE_SEPARATOR);
        }

        return sb.toString();
    }

    /**
     * Creates a descriptive string representation of the parameter map list.
     * @return Parameter list as String
     */
    public String toDescriptiveString()
    {
        StringBuilder sb = new StringBuilder();

        for(Parameter p : this.getParameterMap().values())
        {
            sb.append( p.toDescriptiveString() );
            sb.append( LINE_SEPARATOR );
        }

        return sb.toString();
    }

    /**
     * Required to set the upper and lower boundaries of a numeric parameter
     * @param <K> Any Comparable object
     */
    public static final class NumericLimits<K>
    {
        private K lower;
        private K upper;

        public NumericLimits(K lower, K upper)
        {
            setLower(lower);
            setUpper(upper);
        }

        /**
         * Tests the given value to see if it is within specified limits.  Note:
         * this methods suppresses "unchecked" case warnings.  This shouldn't produce
         * any issues further down the line as lower and upper are both tested for
         * their existence and their type (Comparable) in the constructor.
         * @param value Value to test
         * @return true if value is within defined limits, otherwise false
         */
        @SuppressWarnings("unchecked")
        public boolean valid(K value)
        {
            if (this.lower != null && this.upper != null)
            {
                Comparable c1 = (Comparable)lower;
                Comparable c2 = (Comparable)upper;

                if (c1.compareTo(value) <= 0 &&  c2.compareTo( value ) >= 0 )
                {
                    return true;
                }
            }
            return false;
        }

        public K getLower()                 {return this.lower;}
        public K getUpper()                 {return this.upper;}

        public final void setLower(K lower) {this.lower = lower != null ? lower instanceof Comparable ? lower : null : null;}
        public final void setUpper(K upper) {this.upper = upper != null ? upper instanceof Comparable ? upper : null : null;}
    }

    /**
     * A single Tool parameter, defined by name and default_value
     * @param <K> Any object for default value
     */
    public static final class ParameterDefinition<K>
    {
        private String name;
        private K default_value;
        private NumericLimits<K> limits;

        public ParameterDefinition(String name, K default_value)
        {
            this(name, default_value, null, null);
        }

        public ParameterDefinition(String name, K default_value, K lower, K upper)
        {
            this.name = name;
            this.default_value = default_value;

            if ( ! ( lower == null || upper == null ) )
            {
              this.limits = new NumericLimits<K>(lower, upper);
            }
        }

        public void setName(String name)            {this.name = name;}
        public void setDefault(K default_value)     {this.default_value = default_value;}

        public String   getName()                   {return this.name;}
        public K        getDefaultValue()           {return this.default_value;}

        public NumericLimits<K> getLimits()         {return this.limits;}
    }

    public static final class Parameter<K>
    {
        private ParameterDefinition<K> definition;
        private K value;

        public Parameter(ParameterDefinition<K> definition, K value)
        {
            this.definition = definition;
            this.value = value;
        }

        public String                   getName()           {return this.definition.getName();}
        public ParameterDefinition<K>   getDefinition()     {return this.definition;}
        public K                        getValue()          {return this.value;}


        /**
         * Sets the value of this parameter.  Will automatically test if the
         * value is within defined limits and throws an IllegalArgumentException
         * if not.
         * @param value Numeric value to set
         */
        public void setValue(K value)
        {
            if (value == null)
                this.value = null;
            else
            {
                if (this.definition.getLimits() != null)
                {
                    if (!this.definition.getLimits().valid(value))
                        throw new IllegalArgumentException("Illegal " + this.definition.getName() + " parameter value.  Valid values: "
                                + this.definition.getLimits().getLower() + " <= " + this.definition.getName() + " <= " + this.definition.getLimits().getUpper() + ".");
                }

                this.value = value;
            }
        }

        /**
         * Sets the current parameter's value from a String representation.
         * In the case of numeric parameters, the appropriate "parse" method is called.
         * The type of this parameter is known, and all possibilities are
         * checked in the method, so the "unchecked" case warning suppression should
         * not have any adverse impact.
         *
         * @param strValue String representation of the parameter.
         *                 Boolean values are allowed to be 'yes' and 'no' as well as 'true' and 'false'.
         */
        @SuppressWarnings("unchecked")
        public void setValueFromString(String strValue)
        {
            K temp = null;

            if (this.value instanceof Integer)
            {
                temp = (K)new Integer(Integer.parseInt(strValue));
            }
            else if (this.value instanceof Double)
            {
                temp = (K)new Double(Double.parseDouble(strValue));
            }
            else if (this.value instanceof Float)
            {
                temp = (K)new Float(Float.parseFloat(strValue));
            }
            else if (this.value instanceof Long)
            {
                temp = (K)new Long(Long.parseLong(strValue));
            }
            else if (this.value instanceof Byte)
            {
                temp = (K)new Byte(Byte.parseByte(strValue));
            }
            else if (this.value instanceof Short)
            {
                temp = (K)new Short(Short.parseShort(strValue));
            }
            else if (this.value instanceof Boolean)
            {
                if ( "yes".equalsIgnoreCase( strValue ) || "no".equalsIgnoreCase( strValue ) )
                {
                    temp = (K)( "yes".equalsIgnoreCase( strValue ) ? Boolean.TRUE : Boolean.FALSE );
                }
                else
                {
                    temp = (K)Boolean.valueOf(strValue);
                }
            }
            else if (this.value instanceof File)
            {
                // Trim surrounding quotes if found.
                String file = strValue;
                if (strValue.startsWith("\"") && strValue.endsWith("\""))
                {
                    file = strValue.substring(1, strValue.length() - 1);
                }
                temp = (K)new File(file);
            }
            else if (this.value instanceof String)
            {
                temp = (K)strValue;
            }

            setValue(temp);
        }


        /**
         * Returns a String representation of the Parameter as follows ("key: value").
         * @return String representation of the Parameter
         */
        @Override
        public String toString()
        {
            return StringUtils.nullSafeConcatenation( getName(), ": ", getValue() );
        }

        public String toDescriptiveString()
        {
            String val = String.valueOf( getValue() );

            if ( value instanceof Boolean )
            {
              val = ((Boolean)value).booleanValue() ? "yes" : "no";
            }

            return StringUtils.nullSafeConcatenation( getName(), ": ", val );
        }
    }
}
