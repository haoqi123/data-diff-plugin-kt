package com.github.haoqi123.datadiffpluginkt.bo

import org.apache.commons.lang3.StringUtils

data class Field(
    var tableName: String,
    var fieldName: String,
    var type: String,
    var isNotNull: Boolean,
    var key: String?,
    var default: String?,
    var extra: String?,
    var comment: String?,
    val isNew: Boolean = false
) : Comparable<Field> {

    override fun compareTo(other: Field): Int {
        return if (compareFieldVal(other.fieldName, fieldName)
            && compareFieldVal(other.type, type)
            && compareFieldVal(other.isNotNull, isNotNull)
            && compareKeyFieldVal(other.key, key)
            && compareFieldVal(other.default, default)
            && compareFieldVal(other.extra, extra)
            && compareFieldVal(other.comment, comment)
        ) 1 else 0
    }


}

/**
 * 对比两张表是否相等
 */
private fun compareFieldVal(source: Any?, target: Any?): Boolean {
    return (source == null && target == null)
            || (source != null && target != null)
            && source == target
}

/**
 * 对比两张表的KEY是否相等
//如果Key是MUL,那么该列的值可以重复, 该列是一个非唯一索引的前导列(第一列)或者是一个唯一性索引的组成部分但是可以含有空值NULL。
//如果Key是UNI,  那么该列是一个唯一值索引的第一列(前导列),并别不能含有空值(NULL)；
//处理方式需要放在索引中，这里不做比较
if (fieldType == ColumnType.Key &&
(Objects.equals(sourceFieldVal, "MUL") || Objects.equals(sourceFieldVal, "UNI")
|| Objects.equals(targetFieldVal, "MUL") || Objects.equals(targetFieldVal, "UNI"))) {
continue;
}
 */
private fun compareKeyFieldVal(source: String?, target: String?): Boolean {

    return (!(StringUtils.equals(source, "MUL")
            || StringUtils.equals(target, "MUL")
            || StringUtils.equals(source, "UNI")
            || StringUtils.equals(target, "UNI")))
            && compareFieldVal(source, target)
}
