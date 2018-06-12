//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.04.11 um 10:45:24 AM CEST 
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
 *         &lt;element ref="{}path"/&gt;
 *         &lt;element ref="{}run"/&gt;
 *         &lt;element ref="{}runOnExit" minOccurs="0"/&gt;
 *         &lt;element ref="{}runOnSuccess" minOccurs="0"/&gt;
 *         &lt;element ref="{}runOnFail" minOccurs="0"/&gt;
 *         &lt;element ref="{}runOnAbort" minOccurs="0"/&gt;
 *         &lt;element ref="{}result"/&gt;
 *         &lt;element ref="{}questions"/&gt;
 *         &lt;element ref="{}instances"/&gt;
 *         &lt;element ref="{}memoryPerInstance"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
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
    "path",
    "run",
    "runOnExit",
    "runOnSuccess",
    "runOnFail",
    "runOnAbort",
    "result",
    "questions",
    "instances",
    "memoryPerInstance"
})
@XmlRootElement(name = "tool")
public class Tool {

    protected List<Priority> priority;
    @XmlElement(required = true)
    protected String path;
    @XmlElement(required = true)
    protected String run;
    protected String runOnExit;
    protected String runOnSuccess;
    protected String runOnFail;
    protected String runOnAbort;
    @XmlElement(required = true)
    protected String result;
    @XmlElement(required = true)
    protected String questions;
    protected int instances;
    protected int memoryPerInstance;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "version")
    protected String version;

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
     * Ruft den Wert der run-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRun() {
        return run;
    }

    /**
     * Legt den Wert der run-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRun(String value) {
        this.run = value;
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
     * Ruft den Wert der result-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResult() {
        return result;
    }

    /**
     * Legt den Wert der result-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResult(String value) {
        this.result = value;
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
     * Ruft den Wert der instances-Eigenschaft ab.
     * 
     */
    public int getInstances() {
        return instances;
    }

    /**
     * Legt den Wert der instances-Eigenschaft fest.
     * 
     */
    public void setInstances(int value) {
        this.instances = value;
    }

    /**
     * Ruft den Wert der memoryPerInstance-Eigenschaft ab.
     * 
     */
    public int getMemoryPerInstance() {
        return memoryPerInstance;
    }

    /**
     * Legt den Wert der memoryPerInstance-Eigenschaft fest.
     * 
     */
    public void setMemoryPerInstance(int value) {
        this.memoryPerInstance = value;
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

}
