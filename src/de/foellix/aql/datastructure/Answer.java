//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.01.23 um 06:30:53 PM CET 
//


package de.foellix.aql.datastructure;

import java.io.Serializable;
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
 *         &lt;element ref="{}permissions"/&gt;
 *         &lt;element ref="{}intentsources"/&gt;
 *         &lt;element ref="{}intentsinks"/&gt;
 *         &lt;element ref="{}intents"/&gt;
 *         &lt;element ref="{}intentfilters"/&gt;
 *         &lt;element ref="{}flows"/&gt;
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
    "permissions",
    "intentsources",
    "intentsinks",
    "intents",
    "intentfilters",
    "flows"
})
@XmlRootElement(name = "answer")
public class Answer
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected Permissions permissions;
    @XmlElement(required = true)
    protected Intentsources intentsources;
    @XmlElement(required = true)
    protected Intentsinks intentsinks;
    @XmlElement(required = true)
    protected Intents intents;
    @XmlElement(required = true)
    protected Intentfilters intentfilters;
    @XmlElement(required = true)
    protected Flows flows;

    /**
     * Ruft den Wert der permissions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Permissions }
     *     
     */
    public Permissions getPermissions() {
        return permissions;
    }

    /**
     * Legt den Wert der permissions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Permissions }
     *     
     */
    public void setPermissions(Permissions value) {
        this.permissions = value;
    }

    /**
     * Ruft den Wert der intentsources-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Intentsources }
     *     
     */
    public Intentsources getIntentsources() {
        return intentsources;
    }

    /**
     * Legt den Wert der intentsources-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Intentsources }
     *     
     */
    public void setIntentsources(Intentsources value) {
        this.intentsources = value;
    }

    /**
     * Ruft den Wert der intentsinks-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Intentsinks }
     *     
     */
    public Intentsinks getIntentsinks() {
        return intentsinks;
    }

    /**
     * Legt den Wert der intentsinks-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Intentsinks }
     *     
     */
    public void setIntentsinks(Intentsinks value) {
        this.intentsinks = value;
    }

    /**
     * Ruft den Wert der intents-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Intents }
     *     
     */
    public Intents getIntents() {
        return intents;
    }

    /**
     * Legt den Wert der intents-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Intents }
     *     
     */
    public void setIntents(Intents value) {
        this.intents = value;
    }

    /**
     * Ruft den Wert der intentfilters-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Intentfilters }
     *     
     */
    public Intentfilters getIntentfilters() {
        return intentfilters;
    }

    /**
     * Legt den Wert der intentfilters-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Intentfilters }
     *     
     */
    public void setIntentfilters(Intentfilters value) {
        this.intentfilters = value;
    }

    /**
     * Ruft den Wert der flows-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Flows }
     *     
     */
    public Flows getFlows() {
        return flows;
    }

    /**
     * Legt den Wert der flows-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Flows }
     *     
     */
    public void setFlows(Flows value) {
        this.flows = value;
    }

}
