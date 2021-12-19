package com.github.haoqi123.datadiffpluginkt.util

import com.github.haoqi123.datadiffpluginkt.bo.Field

object DataDiffUtil {

    fun getDiffResult(
        source: MutableMap<String, MutableMap<String, Field>>,
        target: MutableMap<String, MutableMap<String, Field>>
    ): DiffResultBo {
        val addTable = mutableListOf<String>()
        val dropTable = mutableListOf<String>()
        val dropField = mutableMapOf<String, MutableMap<String, Field>>()
        val diffField = mutableMapOf<String, MutableMap<String, Field>>()
        for (table in source) {
            //是否是新表
            if (!target.containsKey(table.key)) {
                addTable.add(table.key);
                source.remove(table.key)
                continue
            }
            //获取不同的字段和删除的字段
            getDiffFields(table.value, target[table.key]!!, diffField, dropField)
        }

        //哪些表需要删除
        target.forEach { if (!source.containsKey(it.key)) dropTable.add(it.key) }
        return DiffResultBo(addTable, dropTable, dropField, diffField)
    }

    private fun getDiffFields(
        source: MutableMap<String, Field>,
        target: MutableMap<String, Field>,
        diffFieldMap: MutableMap<String, MutableMap<String, Field>>,
        dropField: MutableMap<String, MutableMap<String, Field>>
    ) {

        for (field in source.values) {
            //新字段
            if (!target.containsKey(field.fieldName)) {
                diffFieldMap.plus(field.tableName to field)
                source.remove(field.fieldName)
                continue
            }
            //不同的字段
            if (field.compareTo(target[field.fieldName]!!) < 1) {
                diffFieldMap.plus(field.tableName to field)
                source.remove(field.fieldName)
            }
        }

        for (field in target.values) {
            //删除字段
            if (!source.containsKey(field.fieldName)) {
                dropField.plus(field.tableName to field)
                continue
            }
        }

    }
}


data class DiffResultBo(
    var addTable: MutableList<String>,
    var dropTable: MutableList<String>,
    var dropField: MutableMap<String, MutableMap<String, Field>>,
    var diffField: MutableMap<String, MutableMap<String, Field>>
)