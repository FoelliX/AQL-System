//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.01.10 um 12:57:59 PM CET 
//


package de.foellix.aql.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{}priority" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}execute"/&gt;
 *         &lt;element ref="{}path" minOccurs="0"/&gt;
 *         &lt;element ref="{}runOnEntry" minOccurs="0"/&gt;
 *         &lt;element ref="{}runOnExit" minOccurs="0"/&gt;
 *         &lt;element ref="{}runOnSuccess" minOccurs="0"/&gt;
 *         &lt;element ref="{}runOnFail" minOccurs="0"/&gt;
 *         &lt;element ref="{}runOnAbort" minOccurs="0"/&gt;
 *         &lt;element ref="{}questions"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="external" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "priority",
    "execute",
    "path",
    "runOnEntry",
    "runOnExit",
    "runOnSuccess",
    "runOnFail",
    "runOnAbort",
    "questions"
})
@XmlRootElement(name = "tool")
public class Tool {

    protected List<Priority> priority;
    @XmlElement(required = true)
    protected Execute execute;
    protected String path;
    protected String runOnEntry;
    protected String runOnExit;
    protected String runOnSuccess;
    protected String runOnFail;
    protected String runOnAbort;
    @XmlElement(required = true)
    protected String questions;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "version")
    protected String version;
    @XmlAttribute(name = "external")
    protected Boolean external;

    /**
     * Gets the value of the priority property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the priority property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPriority().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Priority }
     * 
     * 
     */
    public List<Priority> getPriority() {
        if (priority == null) {
            priority = new ArrayList<Priority>();
        }
        return this.priority;
    }

    /**
     * Ruft den Wert der execute-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Execute }
     *     
     */
    public Execute getExecute() {
        return execute;
    }

    /**
     * Legt den Wert der execute-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Execute }
     *     
     */
    public void setExecute(Execute value) {
        this.execute = value;
    }

    /**
     * Ruft den Wert der path-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Legt den Wert der path-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Ruft den Wert der runOnEntry-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRunOnEntry() {
        return runOnEntry;
    }

    /**
     * Legt den Wert der runOnEntry-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRunOnEntry(String value) {
        this.runOnEntry = value;
    }

    /**
     * Ruft den Wert der runOnExit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRunOnExit() {
        return runOnExit;
    }

    /**
     * Legt den Wert der runOnExit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRunOnExit(String value) {
        this.runOnExit = value;
    }

    /**
     * Ruft den Wert der runOnSuccess-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRunOnSuccess() {
        return runOnSuccess;
    }

    /**
     * Legt den Wert der runOnSuccess-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRunOnSuccess(String value) {
        this.runOnSuccess = value;
    }

    /**
     * Ruft den Wert der runOnFail-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRunOnFail() {
        return runOnFail;
    }

    /**
     * Legt den Wert der runOnFail-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRunOnFail(String value) {
        this.runOnFail = value;
    }

    /**
     * Ruft den Wert der runOnAbort-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRunOnAbort() {
        return runOnAbort;
    }

    /**
     * Legt den Wert der runOnAbort-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRunOnAbort(String value) {
        this.runOnAbort = value;
    }

    /**
     * Ruft den Wert der questions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuestions() {
        return questions;
    }

    /**
     * Legt den Wert der questions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuestions(String value) {
        this.questions = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Ruft den Wert der external-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isExternal() {
        if (external == null) {
            return false;
        } else {
            return external;
        }
    }

    /**
     * Legt den Wert der external-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExternal(Boolean value) {
        this.external = value;
    }

}
