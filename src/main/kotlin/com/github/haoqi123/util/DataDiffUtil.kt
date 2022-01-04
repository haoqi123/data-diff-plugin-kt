package com.github.haoqi123.util

import com.github.haoqi123.bo.Field

object DataDiffUtil {

    fun getDiffResult(
        source: MutableMap<String, MutableMap<String, Field>>,
        target: MutableMap<String, MutableMap<String, Field>>
    ): DiffResultBo {
        val addTable = mutableListOf<String>()
        val dropTable = mutableListOf<String>()
        val dropField = mutableMapOf<String, MutableMap<String, Field>>()
        val diffField = mutableMapOf<String, MutableMap<String, Field>>()
        val iterator = source.iterator()
        while (iterator.hasNext()) {
            val table = iterator.next()
            //是否是新表
            if (!target.containsKey(table.key)) {
                addTable.add(table.key);
                iterator.remove()
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
        dropFieldMap: MutableMap<String, MutableMap<String, Field>>
    ) {
        val iterator = source.values.iterator()
        while (iterator.hasNext()) {
            val field = iterator.next()
            //新字段
            if (!target.containsKey(field.fieldName)) {
                field.isNew = true
                val mutableMap = diffFieldMap.get(field.tableName)
                if (mutableMap == null) {
                    diffFieldMap[field.tableName] = mutableMapOf(field.fieldName to field)
                } else {
                    mutableMap.put(field.fieldName, field)
                }

                iterator.remove()
                continue
            }
            //不同的字段
            if (field.compareTo(target[field.fieldName]!!) < 1) {
                val mutableMap = diffFieldMap.get(field.tableName)
                if (mutableMap == null) {
                    diffFieldMap[field.tableName] = mutableMapOf(field.fieldName to field)
                } else {
                    mutableMap.put(field.fieldName, field)
                }
            }
        }

        for (field in target.values) {
            //删除的字段
            if (!source.containsKey(field.fieldName)) {
                val mutableMap = dropFieldMap.get(field.tableName)
                if (mutableMap == null) {
                    dropFieldMap[field.tableName] = mutableMapOf(field.fieldName to field)
                } else {
                    mutableMap.put(field.fieldName, field)
                }
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