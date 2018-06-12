//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.05.07 um 06:13:43 PM CEST 
//


package de.foellix.aql.datastructure;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.foellix.aql.datastructure package. 
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

    private final static QName _File_QNAME = new QName("", "file");
    private final static QName _Method_QNAME = new QName("", "method");
    private final static QName _Classname_QNAME = new QName("", "classname");
    private final static QName _Name_QNAME = new QName("", "name");
    private final static QName _Value_QNAME = new QName("", "value");
    private final static QName _Statementfull_QNAME = new QName("", "statementfull");
    private final static QName _Statementgeneric_QNAME = new QName("", "statementgeneric");
    private final static QName _Type_QNAME = new QName("", "type");
    private final static QName _Scheme_QNAME = new QName("", "scheme");
    private final static QName _Ssp_QNAME = new QName("", "ssp");
    private final static QName _Host_QNAME = new QName("", "host");
    private final static QName _Port_QNAME = new QName("", "port");
    private final static QName _Path_QNAME = new QName("", "path");
    private final static QName _Action_QNAME = new QName("", "action");
    private final static QName _Category_QNAME = new QName("", "category");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.foellix.aql.datastructure
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Hash }
     * 
     */
    public Hash createHash() {
        return new Hash();
    }

    /**
     * Create an instance of {@link Hashes }
     * 
     */
    public Hashes createHashes() {
        return new Hashes();
    }

    /**
     * Create an instance of {@link App }
     * 
     */
    public App createApp() {
        return new App();
    }

    /**
     * Create an instance of {@link Attribute }
     * 
     */
    public Attribute createAttribute() {
        return new Attribute();
    }

    /**
     * Create an instance of {@link Reference }
     * 
     */
    public Reference createReference() {
        return new Reference();
    }

    /**
     * Create an instance of {@link Statement }
     * 
     */
    public Statement createStatement() {
        return new Statement();
    }

    /**
     * Create an instance of {@link Parameters }
     * 
     */
    public Parameters createParameters() {
        return new Parameters();
    }

    /**
     * Create an instance of {@link Parameter }
     * 
     */
    public Parameter createParameter() {
        return new Parameter();
    }

    /**
     * Create an instance of {@link Attributes }
     * 
     */
    public Attributes createAttributes() {
        return new Attributes();
    }

    /**
     * Create an instance of {@link Permission }
     * 
     */
    public Permission createPermission() {
        return new Permission();
    }

    /**
     * Create an instance of {@link Data }
     * 
     */
    public Data createData() {
        return new Data();
    }

    /**
     * Create an instance of {@link Target }
     * 
     */
    public Target createTarget() {
        return new Target();
    }

    /**
     * Create an instance of {@link Intentsource }
     * 
     */
    public Intentsource createIntentsource() {
        return new Intentsource();
    }

    /**
     * Create an instance of {@link Intentsink }
     * 
     */
    public Intentsink createIntentsink() {
        return new Intentsink();
    }

    /**
     * Create an instance of {@link Intent }
     * 
     */
    public Intent createIntent() {
        return new Intent();
    }

    /**
     * Create an instance of {@link Intentfilter }
     * 
     */
    public Intentfilter createIntentfilter() {
        return new Intentfilter();
    }

    /**
     * Create an instance of {@link Flow }
     * 
     */
    public Flow createFlow() {
        return new Flow();
    }

    /**
     * Create an instance of {@link Permissions }
     * 
     */
    public Permissions createPermissions() {
        return new Permissions();
    }

    /**
     * Create an instance of {@link Intentsources }
     * 
     */
    public Intentsources createIntentsources() {
        return new Intentsources();
    }

    /**
     * Create an instance of {@link Intentsinks }
     * 
     */
    public Intentsinks createIntentsinks() {
        return new Intentsinks();
    }

    /**
     * Create an instance of {@link Intents }
     * 
     */
    public Intents createIntents() {
        return new Intents();
    }

    /**
     * Create an instance of {@link Intentfilters }
     * 
     */
    public Intentfilters createIntentfilters() {
        return new Intentfilters();
    }

    /**
     * Create an instance of {@link Flows }
     * 
     */
    public Flows createFlows() {
        return new Flows();
    }

    /**
     * Create an instance of {@link Answer }
     * 
     */
    public Answer createAnswer() {
        return new Answer();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "file")
    public JAXBElement<String> createFile(String value) {
        return new JAXBElement<String>(_File_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "method")
    public JAXBElement<String> createMethod(String value) {
        return new JAXBElement<String>(_Method_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "classname")
    public JAXBElement<String> createClassname(String value) {
        return new JAXBElement<String>(_Classname_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "value")
    public JAXBElement<String> createValue(String value) {
        return new JAXBElement<String>(_Value_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "statementfull")
    public JAXBElement<String> createStatementfull(String value) {
        return new JAXBElement<String>(_Statementfull_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "statementgeneric")
    public JAXBElement<String> createStatementgeneric(String value) {
        return new JAXBElement<String>(_Statementgeneric_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "type")
    public JAXBElement<String> createType(String value) {
        return new JAXBElement<String>(_Type_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "scheme")
    public JAXBElement<String> createScheme(String value) {
        return new JAXBElement<String>(_Scheme_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ssp")
    public JAXBElement<String> createSsp(String value) {
        return new JAXBElement<String>(_Ssp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "host")
    public JAXBElement<String> createHost(String value) {
        return new JAXBElement<String>(_Host_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "port")
    public JAXBElement<String> createPort(String value) {
        return new JAXBElement<String>(_Port_QNAME, String.class, null, value);
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
    @XmlElementDecl(namespace = "", name = "action")
    public JAXBElement<String> createAction(String value) {
        return new JAXBElement<String>(_Action_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "category")
    public JAXBElement<String> createCategory(String value) {
        return new JAXBElement<String>(_Category_QNAME, String.class, null, value);
    }

}
