//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.08.21 at 05:03:57 AM PDT 
//


package org.jobscheduler.dashboard.jobdefinition.xml;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="heart_beat_timeout" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="heart_beat_own_timeout">
 *         &lt;simpleType>
 *           &lt;union memberTypes=" {http://www.w3.org/2001/XMLSchema}positiveInteger">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *                 &lt;enumeration value="never"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/union>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="heart_beat_warn_timeout" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "cluster")
public class Cluster {

    @XmlAttribute(name = "heart_beat_timeout")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger heartBeatTimeout;
    @XmlAttribute(name = "heart_beat_own_timeout")
    protected String heartBeatOwnTimeout;
    @XmlAttribute(name = "heart_beat_warn_timeout")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger heartBeatWarnTimeout;

    /**
     * Gets the value of the heartBeatTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getHeartBeatTimeout() {
        return heartBeatTimeout;
    }

    /**
     * Sets the value of the heartBeatTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setHeartBeatTimeout(BigInteger value) {
        this.heartBeatTimeout = value;
    }

    /**
     * Gets the value of the heartBeatOwnTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeartBeatOwnTimeout() {
        return heartBeatOwnTimeout;
    }

    /**
     * Sets the value of the heartBeatOwnTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeartBeatOwnTimeout(String value) {
        this.heartBeatOwnTimeout = value;
    }

    /**
     * Gets the value of the heartBeatWarnTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getHeartBeatWarnTimeout() {
        return heartBeatWarnTimeout;
    }

    /**
     * Sets the value of the heartBeatWarnTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setHeartBeatWarnTimeout(BigInteger value) {
        this.heartBeatWarnTimeout = value;
    }

}
