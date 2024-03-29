//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.11.25 at 10:05:09 AM CET 
//


package de.foellix.aql.datastructure;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{}statementfull"/&gt;
 *         &lt;element ref="{}statementgeneric"/&gt;
 *         &lt;element ref="{}linenumber" minOccurs="0"/&gt;
 *         &lt;element ref="{}parameters" minOccurs="0"/&gt;
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
    "statementfull",
    "statementgeneric",
    "linenumber",
    "parameters"
})
@XmlRootElement(name = "statement")
public class Statement
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String statementfull;
    @XmlElement(required = true)
    protected String statementgeneric;
    protected Integer linenumber;
    protected Parameters parameters;

    /**
     * Gets the value of the statementfull property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatementfull() {
        return statementfull;
    }

    /**
     * Sets the value of the statementfull property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatementfull(String value) {
        this.statementfull = value;
    }

    /**
     * Gets the value of the statementgeneric property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatementgeneric() {
        return statementgeneric;
    }

    /**
     * Sets the value of the statementgeneric property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatementgeneric(String value) {
        this.statementgeneric = value;
    }

    /**
     * Gets the value of the linenumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLinenumber() {
        return linenumber;
    }

    /**
     * Sets the value of the linenumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLinenumber(Integer value) {
        this.linenumber = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link Parameters }
     *     
     */
    public Parameters getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Parameters }
     *     
     */
    public void setParameters(Parameters value) {
        this.parameters = value;
    }

}
