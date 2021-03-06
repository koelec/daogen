<?xml version="1.0" encoding="UTF-8"?>
<entity-mappings xmlns="http://java.sun.com/xml/ns/persistence/orm"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="
                     http://java.sun.com/xml/ns/persistence/orm
                     http://java.sun.com/xml/ns/persistence/orm_1_0.xsd"
	version="1.0">
<!-- @Generated(value="Generated by ${generator}", date = "${currentTime}") -->

<#list types as type>
<#list type.methods as method>
<#if method.queryString?? && method.namedQuery>
	<named-query name="${type.baseName}.${method.name}">
		<query><![CDATA[
		 ${method.queryString}
		 ]]></query>
	</named-query>
</#if>
</#list>
</#list>
</entity-mappings>
