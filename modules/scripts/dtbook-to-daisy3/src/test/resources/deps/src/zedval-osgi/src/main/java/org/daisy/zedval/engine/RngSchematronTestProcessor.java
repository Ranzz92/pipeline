/*
 * ZedVal - ANSI/NISO Z39.86-2002/Z39.86-2005 DTB Validator
 * Copyright (C) 2003,2004,2005,2006,2007,2008 Daisy Consortium
 *
 * This library is free software; you can
 * redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation;
 * either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the
 * GNU Lesser General Public License along with
 * this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * For information about the Daisy Consortium,
 * visit www.daisy.org or contact info@mail.daisy.org.
 * For development issues, contact
 * markus.gylling@tpb.se.
 */

package org.daisy.zedval.engine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.validation.SchematronMessage;
import org.daisy.util.xml.validation.ValidationException;
import org.daisy.util.xml.validation.jaxp.AbstractSchemaFactory;
import org.daisy.util.xml.validation.jaxp.RelaxNGSchemaFactory;
import org.daisy.util.xml.validation.jaxp.SchematronSchemaFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An <code>RngSchematronTestProcessor</code> object executes tests via
 * RelaxNG schema validation
 * 
 * @author James Pritchett
 * @author Daniel Carlsson
 * @author Markus Gylling
 * @author Piotr Kiernicki
 */

public class RngSchematronTestProcessor extends ZedTestProcessor implements ErrorHandler {
    //protected RelaxngSchematronValidator validator = null;

    protected URL schemaUrl;

    private ZedFile curFile;
    
	private Map<String,ZedTest> tests;
        
    private Schema schema = null;
    
    private static final String UTIL_JAXP_SCHEMAREADERFACTORY = "org.daisy.util.xml.validation.jaxp.SchematronValidator.SchemaReaderFactory";
    
    /**
     * @param id
     *            Id of this ZedTestProcessor (from processor map)
     * @param name
     *            Name for this ZedTestProcessor
     * @param tests
     *            LinkedHashMap of ZedTests that this ZedTestProcessor
     *            implements (key = id)
     * @param files
     *            LinkedHashMap of ZedFiles upon which this ZedTestProcessor is
     *            to be invoked (key = absolute full path)
     * @param c
     *            ZedContext for this run
     * @param schemaName
     *            name of schema against which to validate
     * @param useRng
     *            tells whether to validate against the RNG parts of the schema
     * @param useSchematron
     *            tells whether to validate against the schematron parts of the
     *            schema
     */
    public RngSchematronTestProcessor(String id, String name, Map<String,ZedTest> tests,
    		Map<String,ZedFile> files, ZedContext c, String schemaName,
            boolean useRng, boolean useSchematron) {
    	
        super(id, name, tests, files, c);
        
        this.tests = tests;
                
        if (c != null && c.getProcessorMap() != null) {

        	this.schemaUrl = getSchemaURL(schemaName);
        	
        	if(this.schemaUrl==null) {
        	    try {
                    this.getContext().getReporter().addMessage(new TestProcessorErrMsg("unable to create URL for schema, or could not find file "+ schemaName, "en",false, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: " + zre.getMessage());
                }
        	}else{
        		//this.validator = new RelaxngSchematronValidator(this.schemaUrl, this, useRng, useSchematron);
        		AbstractSchemaFactory factory = null;
        		if(useRng) {
        			factory = new RelaxNGSchemaFactory();        			
        		}else{
        			factory = new SchematronSchemaFactory();
        		}
        		factory.setErrorHandler(this);
        		try {
					factory.setEntityResolver(CatalogEntityResolver.getInstance());
				} catch (CatalogExceptionNotRecoverable e) {
					try{
						this.getContext().getReporter().addMessage(new TestProcessorErrMsg("unable to allocate entityresolver for schema "+ schemaName, "en",false, this));
	                } catch (ZedReporterException zre) {
	                    System.err.println("Exception while writing to reporter: " + zre.getMessage());
	                }
				}
				try {
					this.schema = factory.newSchema(this.schemaUrl);
				} catch (SAXException e) {
					try{						
						this.getContext().getReporter().addMessage(new TestProcessorErrMsg("unable to create Schema for schema "+ schemaName, "en",false, this));
					} catch (ZedReporterException zre) {
	                    System.err.println("Exception while writing to reporter: " + zre.getMessage());
	                }
				}
        	}   
        } // if (c != null && c.getProcessorMap() != null)
        
        
    }

    private URL getSchemaURL(String schemaName) {
    	URL schemaurl = null;
        try{        
	        schemaurl = CatalogEntityResolver.getInstance().resolveEntityToURL(null, schemaName);
	
	        //if url is null, test user.dir
	        //this is for those adventurous people who add their
	        //own testProcessors - 
	        //may the lord be with them.
	        if (schemaurl == null) {
	        	schemaurl = new URL(System.getProperty("user.dir")+File.separator+schemaName);
	        	try {
					//File test = new File(schemaurl.toURI());// URL#toURI only in jre_1.5; same as new URI (this.toString())
	                File test = new File(new URI(schemaurl.toString()));
					if(!test.exists()) {
						schemaurl=null;
					}
				} catch (URISyntaxException e) {
					 schemaurl=null;
				}
	        }
	        
	        //additionally, a full pathname may have been provided by
	        //the adventurer.                
	        if (schemaurl == null) {
	        	schemaurl = new URL(schemaName);
	        	try {
					File test = new File(new URI(schemaurl.toString()));
					if(!test.exists()) {
						schemaurl=null;
					}
				} catch (URISyntaxException e) {
					 schemaurl=null;
				}
	        }
        }catch (Exception e) {
			e.printStackTrace();
			schemaurl = null;
		}
        return schemaurl;
	}

	/**
     * Validates all files against the schema
     */

    public boolean performTests() {
        boolean retVal = true; // return value

        if (this.schema == null) {
            // loadSchema in the constructor failed
            try {
                this.getContext().getReporter().addMessage(new TestProcessorErrMsg("Cannot run testProcessor ["+ this.getLabel()+ "]: schema driver not initialized properly","en", true, null, this));
                retVal = false;
            } catch (ZedReporterException zre) {
                System.err.println("Exception while writing to reporter: " + zre.getMessage());
            }
        } else {
        	
        	//we utilize the org.daisy.util hook to force the use of Saxon 9. See
        	//org.daisy.util.xml.validation.jaxp.SchematronValidator
        	String origFactoryVal = System.getProperty(UTIL_JAXP_SCHEMAREADERFACTORY);
        	System.setProperty(UTIL_JAXP_SCHEMAREADERFACTORY, "org.daisy.util.xml.validation.jaxp.SaxonSchematronSchemaReaderFactory");
        	        	
        	Validator val = this.schema.newValidator();
        	
            for (Iterator<ZedFile> i = this.getFilesTested().values().iterator(); i.hasNext();) {
                try {
                    this.curFile = i.next();
                    retVal = false;
                    // Check to be sure we can actually run the test on this
                    // file
                    try {
                        if (!this.curFile.exists()) {
                            this.getContext().getReporter().addMessage(new TestProcessorErrMsg("Cannot run testProcessor ["+ this.getLabel()+ "] on file "+ this.curFile.getCanonicalPath()+ ": File does not exist","en", false, this.curFile, this));
                        } else if (!this.curFile.canRead()) {
                            this.getContext().getReporter().addMessage(new TestProcessorErrMsg("Cannot run testProcessor ["+ this.getLabel()+ "] on file "+ this.curFile.getCanonicalPath()+ ": File is not readable","en", false, this.curFile, this));
                        } else if (!(this.curFile instanceof XmlFile)) {                         
                            this.getContext().getReporter().addMessage(new TestProcessorErrMsg("Cannot run testProcessor ["+ this.getLabel()+ "] on file "+ this.curFile.getCanonicalPath()+ ": File is not an XML file","en", false, this.curFile,this));
                        } else if (!(((XmlFile) (this.curFile)).isWellFormed())) {
                            this.getContext().getReporter().addMessage(new TestProcessorErrMsg("Cannot run testProcessor ["+ this.getLabel()+ "] on XML file "+ this.curFile.getCanonicalPath()+ ": File is not well-formed","en", false, this.curFile,this));
                        } else {
                            // If file can be tested, validate against the
                            // schema and get the results
                            try {
                                //retVal = this.validator.isValid(this.curFile);
                            	StreamSource ss = new StreamSource(this.curFile);
                            	ss.setSystemId(this.curFile);
                                val.validate(ss);
                            } catch (SAXException e) {
                                this.getContext().getReporter().addMessage(new TestProcessorErrMsg("Validation error while processing "+ this.curFile.getCanonicalPath()+ ": "+ e.getMessage(),"en", true,this.curFile, this));
                                retVal = false;
                            }
                        }
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                } catch (IOException e) {
                    retVal = false;
                } finally {
                	System.setProperty(UTIL_JAXP_SCHEMAREADERFACTORY, origFactoryVal);
                }
            } // for
        }
        return retVal;
    }

    /**
     * Returns the schema file being used by this processor
     * 
     * @return File object for schema
     */
    public File getSchemaFile() {
        try {
            return new File(new URI(this.schemaUrl.toString()));
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public URL getSchemaUrl() {
        return this.schemaUrl;
    }

    /**
     * Receives notification of a recoverable (probably validation) error
     * 
     * @param e
     *            The exception that represents the error
     */
    public void error(SAXParseException e) {
    	report(e ,"error");
    }

    /**
     * Receives notification of a non-recoverable (probably well-formedness) error
     * @param e
     *            The exception that represents the error
     */
    public void fatalError(SAXParseException e) {
        // this shouldnt happen since wellformedness is checked in
        // performTests() above
    	report(e ,"fatal error");
    }

    /**
     * Receives notification of a warning
     * 
     * @param e
     *            The exception that represents the warning
     */
    public void warning(SAXParseException e) {
   		report(e ,"warning");
    }

    private void report(SAXParseException e, String type) {
    	if(!e.getMessage().contains("XSLT 1.0")) {
			//hack: avoid Saxon version warnings that come from inside Jing    	    	
	    	String message = null;    	
	    	ZedTest t = null;
	        try {
	        	if(e.getMessage().indexOf("[sch]")>=0) {
	        		SchematronMessage sm = new SchematronMessage(e.getMessage());
	        		message = getTestMessage(sm.getMessage("zedid"));      
	        		t = (ZedTest)this.tests.get(sm.getMessage("zedid"));
	        	}else{
	        		message = e.getMessage();
	        	}
	        	        	
	            this.getContext().getReporter().addMessage(
	                    new FailureMessage("A validation " + type + " occurred: "
	                            + message, 
	                            "en", 
	                            t, 
	                            e.getLineNumber(), 
	                            e.getColumnNumber(), 
	                            this.curFile, 
	                            this));
	        } catch (ZedReporterException zre) {
	            System.err.println("Exception while writing to reporter: "+ zre.getMessage());
	        } catch (ValidationException ve) {
	        	System.err.println("Validation exception in RngSchematronTestProcessor.report");
			}
    	}
    }
    
    public String toString() {
        if (this.schemaUrl != null) {
            return super.toString() + "\t[uses=" + this.schemaUrl + "]\n";
        }
        return super.toString() + "\t[uses=null]\n";

    }

    private String getTestMessage(String zedTestId) {
		String ret = zedTestId;
		ZedTest t = (ZedTest) this.tests.get(zedTestId);
		if (t != null) {
			ZedMessage mess = (ZedMessage) t.getOnFalseMsgs().get("long");
			ret = mess.getText();
		} else {
			System.err.println("no test found for sch id "
					+ zedTestId);
		}
		return ret;
	}
    
}
