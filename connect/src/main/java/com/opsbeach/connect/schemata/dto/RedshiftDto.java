package com.opsbeach.connect.schemata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RedshiftDto {
    private Object tableCatalog;
    private Object tableSchema;
    private Object tableName;
    private Object columnName;
    private Object ordinalPosition;
    private Object columnDefault;
    private Object isNullable;
    private Object dataType;
    private Object characterMaximumLength;
    private Object characterOctetLength;
    private Object numericPrecision;
    private Object numericPrecisionRadix;
    private Object numericScale;
    private Object datetimePrecision;
    private Object intervalLype;
    private Object intervalPrecision;
    private Object characterSetCatalog;
    private Object characterSetSchema;
    private Object characterSetName; 
    private Object collationCatalog;
    private Object collationSchema;
    private Object collationName;
    private Object domainCatalog;
    private Object domainSchema;
    private Object domainName;
    private Object udtCatalog;
    private Object udtSchema;
    private Object udtName;
    private Object scopeCatalog;
    private Object scopeSchema;
    private Object scopeName;
    private Object maximumCardinality;
    private Object dtdIdentifier;
    private Object isSelfReferencing;
    private Object isIdentity;
    private Object identityGeneration;
    private Object identityStart;
    private Object identityIncrement;
    private Object identityMaximum;
    private Object identityMinimum;
    private Object identityCycle;
    private Object isGenerated;
    private Object generationExpression;
    private Object isUpdatable;

    public RedshiftDto toDto(Object[] a) {
        return RedshiftDto.builder().tableCatalog(a[0])
                                  .tableSchema(a[1])
                                  .tableName(a[2])
                                  .columnName(a[3])
                                  .ordinalPosition(a[4])
                                  .columnDefault(a[5])
                                  .isNullable(a[6])
                                  .dataType(a[7])
                                  .characterMaximumLength(a[8])
                                  .characterOctetLength(a[9])
                                  .numericPrecision(a[10])
                                  .numericPrecisionRadix(a[11])
                                  .numericScale(a[12])
                                  .datetimePrecision(a[13])
                                  .intervalLype(a[14])
                                  .intervalPrecision(a[15])
                                  .characterSetCatalog(a[16])
                                  .characterSetSchema(a[17])
                                  .characterSetName(a[18]) 
                                  .collationCatalog(a[19])
                                  .collationSchema(a[20])
                                  .collationName(a[21])
                                  .domainCatalog(a[22])
                                  .domainSchema(a[23])
                                  .domainName(a[24])
                                  .udtCatalog(a[25])
                                  .udtSchema(a[26])
                                  .udtName(a[27])
                                  .scopeCatalog(a[28])
                                  .scopeSchema(a[29])
                                  .scopeName(a[30])
                                  .maximumCardinality(a[31])
                                  .dtdIdentifier(a[32])
                                  .isSelfReferencing(a[33])
                                  .isIdentity(a[34])
                                  .identityGeneration(a[35])
                                  .identityStart(a[36])
                                  .identityIncrement(a[37])
                                  .identityMaximum(a[38])
                                  .identityMinimum(a[39])
                                  .identityCycle(a[40])
                                  .isGenerated(a[41])
                                  .generationExpression(a[42])
                                  .isUpdatable(a[43])
                                  .build();
    }
}
