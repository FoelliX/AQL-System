//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.01.10 um 12:57:59 PM CET 
//


package de.foellix.aql.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{}androidPlatforms"/&gt;
 *         &lt;element ref="{}maxMemory"/&gt;
 *         &lt;element ref="{}tools"/&gt;
 *         &lt;element ref="{}preprocessors"/&gt;
 *         &lt;element ref="{}operators"/&gt;
 *         &lt;element ref="{}converters"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "androidPlatforms",
    "maxMemory",
    "tools",
    "preprocessors",
    "operators",
    "converters"
})
@XmlRootElement(name = "config")
public class Config {

    @XmlElement(required = true)
    protected String androidPlatforms;
    protected int maxMemory;
    @XmlElement(required = true)
    protected Tools tools;
    @XmlElement(required = true)
    protected Preprocessors preprocessors;
    @XmlElement(required = true)
    protected Operators operators;
    @XmlElement(required = true)
    protected Converters converters;

    /**
     * Ruft den Wert der androidPlatforms-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAndroidPlatforms() {
        return androidPlatforms;
    }

    /**
     * Legt den Wert der androidPlatforms-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAndroidPlatforms(String value) {
        this.androidPlatforms = value;
    }

    /**
     * Ruft den Wert der maxMemory-Eigenschaft ab.
     * 
     */
    public int getMaxMemory() {
        return maxMemory;
    }

    /**
     * Legt den Wert der maxMemory-Eigenschaft fest.
     * 
     */
    public void setMaxMemory(int value) {
        this.maxMemory = value;
    }

    /**
     * Ruft den Wert der tools-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Tools }
     *     
     */
    public Tools getTools() {
        return tools;
    }

    /**
     * Legt den Wert der tools-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Tools }
     *     
     */
    public void setTools(Tools value) {
        this.tools = value;
    }

    /**
     * Ruft den Wert der preprocessors-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Preprocessors }
     *     
     */
    public Preprocessors getPreprocessors() {
        return preprocessors;
    }

    /**
     * Legt den Wert der preprocessors-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Preprocessors }
     *     
     */
    public void setPreprocessors(Preprocessors value) {
        this.preprocessors = value;
    }

    /**
     * Ruft den Wert der operators-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Operators }
     *     
     */
    public Operators getOperators() {
        return operators;
    }

    /**
     * Legt den Wert der operators-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Operators }
     *     
     */
    public void setOperators(Operators value) {
        this.operators = value;
    }

    /**
     * Ruft den Wert der converters-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Converters }
     *     
     */
    public Converters getConverters() {
        return converters;
    }

    /**
     * Legt den Wert der converters-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Converters }
     *     
     */
    public void setConverters(Converters value) {
        this.converters = value;
    }

}
