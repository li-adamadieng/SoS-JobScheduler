//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.08.21 at 05:03:57 AM PDT 
//


package org.jobscheduler.dashboard.jobdefinition.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{}param"/>
 *           &lt;element name="copy_params">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;attribute name="from" use="required">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *                         &lt;enumeration value="task"/>
 *                         &lt;enumeration value="order"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/attribute>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="include">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;attribute name="file" type="{}File" />
 *                   &lt;attribute name="live_file" type="{}File" />
 *                   &lt;attribute name="node" type="{}String" />
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "paramOrCopyParamsOrInclude"
})
@XmlRootElement(name = "params")
public class Params {

    @XmlElements({
        @XmlElement(name = "param", type = Param.class),
        @XmlElement(name = "copy_params", type = Params.CopyParams.class),
        @XmlElement(name = "include", type = Params.Include.class)
    })
    protected List<Object> paramOrCopyParamsOrInclude;

    /**
     * Gets the value of the paramOrCopyParamsOrInclude property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the paramOrCopyParamsOrInclude property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParamOrCopyParamsOrInclude().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Param }
     * {@link Params.CopyParams }
     * {@link Params.Include }
     * 
     * 
     */
    public List<Object> getParamOrCopyParamsOrInclude() {
        if (paramOrCopyParamsOrInclude == null) {
            paramOrCopyParamsOrInclude = new ArrayList<Object>();
        }
        return this.paramOrCopyParamsOrInclude;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="from" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
     *             &lt;enumeration value="task"/>
     *             &lt;enumeration value="order"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class CopyParams {

        @XmlAttribute(name = "from", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String from;

        /**
         * Gets the value of the from property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFrom() {
            return from;
        }

        /**
         * Sets the value of the from property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFrom(String value) {
            this.from = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="file" type="{}File" />
     *       &lt;attribute name="live_file" type="{}File" />
     *       &lt;attribute name="node" type="{}String" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Include {

        @XmlAttribute(name = "file")
        protected String file;
        @XmlAttribute(name = "live_file")
        protected String liveFile;
        @XmlAttribute(name = "node")
        protected String node;

        /**
         * Gets the value of the file property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFile() {
            return file;
        }

        /**
         * Sets the value of the file property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFile(String value) {
            this.file = value;
        }

        /**
         * Gets the value of the liveFile property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLiveFile() {
            return liveFile;
        }

        /**
         * Sets the value of the liveFile property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLiveFile(String value) {
            this.liveFile = value;
        }

        /**
         * Gets the value of the node property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNode() {
            return node;
        }

        /**
         * Sets the value of the node property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNode(String value) {
            this.node = value;
        }

    }

}
