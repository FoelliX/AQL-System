//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.05.16 um 08:45:07 AM CEST 
//


package de.foellix.aql.config;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.foellix.aql.config package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Path_QNAME = new QName("", "path");
    private final static QName _Run_QNAME = new QName("", "run");
    private final static QName _RunOnExit_QNAME = new QName("", "runOnExit");
    private final static QName _RunOnSuccess_QNAME = new QName("", "runOnSuccess");
    private final static QName _RunOnFail_QNAME = new QName("", "runOnFail");
    private final static QName _RunOnAbort_QNAME = new QName("", "runOnAbort");
    private final static QName _Result_QNAME = new QName("", "result");
    private final static QName _Questions_QNAME = new QName("", "questions");
    private final static QName _Instances_QNAME = new QName("", "instances");
    private final static QName _MemoryPerInstance_QNAME = new QName("", "memoryPerInstance");
    private final static QName _AndroidPlatforms_QNAME = new QName("", "androidPlatforms");
    private final static QName _MaxMemory_QNAME = new QName("", "maxMemory");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.foellix.aql.config
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Priority }
     * 
     */
    public Priority createPriority() {
        return new Priority();
    }

    /**
     * Create an instance of {@link Tool }
     * 
     */
    public Tool createTool() {
        return new Tool();
    }

    /**
     * Create an instance of {@link Tools }
     * 
     */
    public Tools createTools() {
        return new Tools();
    }

    /**
     * Create an instance of {@link Preprocessors }
     * 
     */
    public Preprocessors createPreprocessors() {
        return new Preprocessors();
    }

    /**
     * Create an instance of {@link Operators }
     * 
     */
    public Operators createOperators() {
        return new Operators();
    }

    /**
     * Create an instance of {@link Converters }
     * 
     */
    public Converters createConverters() {
        return new Converters();
    }

    /**
     * Create an instance of {@link Config }
     * 
     */
    public Config createConfig() {
        return new Config();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "path")
    public JAXBElement<String> createPath(String value) {
        return new JAXBElement<String>(_Path_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "run")
    public JAXBElement<String> createRun(String value) {
        return new JAXBElement<String>(_Run_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "runOnExit")
    public JAXBElement<String> createRunOnExit(String value) {
        return new JAXBElement<String>(_RunOnExit_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "runOnSuccess")
    public JAXBElement<String> createRunOnSuccess(String value) {
        return new JAXBElement<String>(_RunOnSuccess_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "runOnFail")
    public JAXBElement<String> createRunOnFail(String value) {
        return new JAXBElement<String>(_RunOnFail_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "runOnAbort")
    public JAXBElement<String> createRunOnAbort(String value) {
        return new JAXBElement<String>(_RunOnAbort_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "result")
    public JAXBElement<String> createResult(String value) {
        return new JAXBElement<String>(_Result_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "questions")
    public JAXBElement<String> createQuestions(String value) {
        return new JAXBElement<String>(_Questions_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "instances")
    public JAXBElement<Integer> createInstances(Integer value) {
        return new JAXBElement<Integer>(_Instances_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "memoryPerInstance")
    public JAXBElement<Integer> createMemoryPerInstance(Integer value) {
        return new JAXBElement<Integer>(_MemoryPerInstance_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "androidPlatforms")
    public JAXBElement<String> createAndroidPlatforms(String value) {
        return new JAXBElement<String>(_AndroidPlatforms_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "maxMemory")
    public JAXBElement<Integer> createMaxMemory(Integer value) {
        return new JAXBElement<Integer>(_MaxMemory_QNAME, Integer.class, null, value);
    }

}
