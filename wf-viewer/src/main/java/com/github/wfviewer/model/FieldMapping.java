//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.4-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.07.27 at 10:06:37 PM EEST 
//


package com.github.wfviewer.model;

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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{}DB_FieldLength"/>
 *         &lt;element ref="{}DB_FieldType"/>
 *         &lt;element ref="{}DB_RelationChildFor"/>
 *         &lt;element ref="{}ES_DefaultValue"/>
 *         &lt;element ref="{}ES_FieldName"/>
 *         &lt;element ref="{}ES_Group"/>
 *         &lt;element ref="{}ES_Offset"/>
 *         &lt;element ref="{}ES_Size"/>
 *         &lt;element ref="{}ES_Type"/>
 *         &lt;element ref="{}ES_Usage"/>
 *         &lt;element ref="{}FieldName"/>
 *         &lt;element ref="{}IDL_FieldType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dbFieldLength",
    "dbFieldType",
    "dbRelationChildFor",
    "esDefaultValue",
    "esFieldName",
    "esGroup",
    "esOffset",
    "esSize",
    "esType",
    "esUsage",
    "fieldName",
    "idlFieldType"
})
@XmlRootElement(name = "FieldMapping")
public class FieldMapping {

    @XmlElement(name = "DB_FieldLength")
    protected DBFieldLength dbFieldLength;
    @XmlElement(name = "DB_FieldType")
    protected DBFieldType dbFieldType;
    @XmlElement(name = "DB_RelationChildFor")
    protected DBRelationChildFor dbRelationChildFor;
    @XmlElement(name = "ES_DefaultValue")
    protected ESDefaultValue esDefaultValue;
    @XmlElement(name = "ES_FieldName")
    protected ESFieldName esFieldName;
    @XmlElement(name = "ES_Group")
    protected ESGroup esGroup;
    @XmlElement(name = "ES_Offset")
    protected ESOffset esOffset;
    @XmlElement(name = "ES_Size")
    protected ESSize esSize;
    @XmlElement(name = "ES_Type")
    protected ESType esType;
    @XmlElement(name = "ES_Usage")
    protected ESUsage esUsage;
    @XmlElement(name = "FieldName")
    protected FieldName fieldName;
    @XmlElement(name = "IDL_FieldType")
    protected IDLFieldType idlFieldType;

    /**
     * Gets the value of the dbFieldLength property.
     * 
     * @return
     *     possible object is
     *     {@link DBFieldLength }
     *     
     */
    public DBFieldLength getDBFieldLength() {
        return dbFieldLength;
    }

    /**
     * Sets the value of the dbFieldLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link DBFieldLength }
     *     
     */
    public void setDBFieldLength(DBFieldLength value) {
        this.dbFieldLength = value;
    }

    /**
     * Gets the value of the dbFieldType property.
     * 
     * @return
     *     possible object is
     *     {@link DBFieldType }
     *     
     */
    public DBFieldType getDBFieldType() {
        return dbFieldType;
    }

    /**
     * Sets the value of the dbFieldType property.
     * 
     * @param value
     *     allowed object is
     *     {@link DBFieldType }
     *     
     */
    public void setDBFieldType(DBFieldType value) {
        this.dbFieldType = value;
    }

    /**
     * Gets the value of the dbRelationChildFor property.
     * 
     * @return
     *     possible object is
     *     {@link DBRelationChildFor }
     *     
     */
    public DBRelationChildFor getDBRelationChildFor() {
        return dbRelationChildFor;
    }

    /**
     * Sets the value of the dbRelationChildFor property.
     * 
     * @param value
     *     allowed object is
     *     {@link DBRelationChildFor }
     *     
     */
    public void setDBRelationChildFor(DBRelationChildFor value) {
        this.dbRelationChildFor = value;
    }

    /**
     * Gets the value of the esDefaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link ESDefaultValue }
     *     
     */
    public ESDefaultValue getESDefaultValue() {
        return esDefaultValue;
    }

    /**
     * Sets the value of the esDefaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESDefaultValue }
     *     
     */
    public void setESDefaultValue(ESDefaultValue value) {
        this.esDefaultValue = value;
    }

    /**
     * Gets the value of the esFieldName property.
     * 
     * @return
     *     possible object is
     *     {@link ESFieldName }
     *     
     */
    public ESFieldName getESFieldName() {
        return esFieldName;
    }

    /**
     * Sets the value of the esFieldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESFieldName }
     *     
     */
    public void setESFieldName(ESFieldName value) {
        this.esFieldName = value;
    }

    /**
     * Gets the value of the esGroup property.
     * 
     * @return
     *     possible object is
     *     {@link ESGroup }
     *     
     */
    public ESGroup getESGroup() {
        return esGroup;
    }

    /**
     * Sets the value of the esGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESGroup }
     *     
     */
    public void setESGroup(ESGroup value) {
        this.esGroup = value;
    }

    /**
     * Gets the value of the esOffset property.
     * 
     * @return
     *     possible object is
     *     {@link ESOffset }
     *     
     */
    public ESOffset getESOffset() {
        return esOffset;
    }

    /**
     * Sets the value of the esOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESOffset }
     *     
     */
    public void setESOffset(ESOffset value) {
        this.esOffset = value;
    }

    /**
     * Gets the value of the esSize property.
     * 
     * @return
     *     possible object is
     *     {@link ESSize }
     *     
     */
    public ESSize getESSize() {
        return esSize;
    }

    /**
     * Sets the value of the esSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESSize }
     *     
     */
    public void setESSize(ESSize value) {
        this.esSize = value;
    }

    /**
     * Gets the value of the esType property.
     * 
     * @return
     *     possible object is
     *     {@link ESType }
     *     
     */
    public ESType getESType() {
        return esType;
    }

    /**
     * Sets the value of the esType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESType }
     *     
     */
    public void setESType(ESType value) {
        this.esType = value;
    }

    /**
     * Gets the value of the esUsage property.
     * 
     * @return
     *     possible object is
     *     {@link ESUsage }
     *     
     */
    public ESUsage getESUsage() {
        return esUsage;
    }

    /**
     * Sets the value of the esUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESUsage }
     *     
     */
    public void setESUsage(ESUsage value) {
        this.esUsage = value;
    }

    /**
     * Gets the value of the fieldName property.
     * 
     * @return
     *     possible object is
     *     {@link FieldName }
     *     
     */
    public FieldName getFieldName() {
        return fieldName;
    }

    /**
     * Sets the value of the fieldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldName }
     *     
     */
    public void setFieldName(FieldName value) {
        this.fieldName = value;
    }

    /**
     * Gets the value of the idlFieldType property.
     * 
     * @return
     *     possible object is
     *     {@link IDLFieldType }
     *     
     */
    public IDLFieldType getIDLFieldType() {
        return idlFieldType;
    }

    /**
     * Sets the value of the idlFieldType property.
     * 
     * @param value
     *     allowed object is
     *     {@link IDLFieldType }
     *     
     */
    public void setIDLFieldType(IDLFieldType value) {
        this.idlFieldType = value;
    }

}