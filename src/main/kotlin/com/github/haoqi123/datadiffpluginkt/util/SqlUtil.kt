package com.github.haoqi123.datadiffpluginkt.util

import com.github.haoqi123.datadiffpluginkt.bo.Field
import com.intellij.database.dialects.mysqlbase.model.MysqlBaseTableColumn
import com.intellij.database.editor.DatabaseEditorHelper
import com.intellij.database.model.DasObject
import com.intellij.database.model.ObjectKind
import com.intellij.database.model.basic.BasicMixinTableOrView
import com.intellij.database.model.basic.BasicTableColumn
import com.intellij.database.psi.DbElement
import com.intellij.database.psi.DbNamespaceImpl
import com.intellij.psi.PsiElement
import com.intellij.util.ObjectUtils
import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * @Created by haoqi
 */
object SqlUtil {
    const val SOURCE = "source"
    const val TARGET = "targets"

    /**
     * 获取数据集
     */
    fun getData(source: DbNamespaceImpl): MutableMap<String, MutableMap<String, Field>> {
        val table = mutableMapOf<String, MutableMap<String, Field>>()
        val delegate: DasObject = source.getDelegate()
        for (dasChild in delegate.getDasChildren(ObjectKind.TABLE)) {
            val fieldNames: MutableList<String> = ArrayList()
            val primaryKey = (dasChild as BasicMixinTableOrView).primaryKey
            if (primaryKey != null) {
                fieldNames.addAll(primaryKey.colNames)
            }

            for (dasObject in dasChild.getDasChildren(ObjectKind.COLUMN)) {
                val basicTableColumn = dasObject as BasicTableColumn
                val field = Field(
                    dasChild.name,
                    dasObject.getName(),
                    basicTableColumn.dataType.specification,
                    basicTableColumn.isNotNull,
                    if (fieldNames.contains(dasObject.getName())) "PRI" else null,
                    basicTableColumn.defaultExpression,
                    if ((basicTableColumn is MysqlBaseTableColumn) && (StringUtils.isNotBlank(basicTableColumn.onUpdate)))
                        basicTableColumn.onUpdate else null,
                    basicTableColumn.comment
                )
//                if(table[dasChild.name]==null){
//                    table[dasChild.name]=mutableMapOf(dasObject.getName() to field)
//                }else{
//                    val mutableMap = table[dasChild.name]
//                    mutableMap.values
//                }

                table[dasChild.name].apply {
                    if (this == null)
                        table[dasChild.name] = mutableMapOf(dasObject.getName() to field)
                    else this[dasObject.getName()] = field
                }
            }
        }
        return table
    }

    fun getResultString(result: DiffResultBo, source: PsiElement): String {
        val sj = StringJoiner("\n")

        val dropField = result.dropField
        dropFields(dropField, sj)

        val dropTable = result.dropTable
        dropTable.forEach {
            sj.add("drop table $it;")
        }

        val diffField: MutableMap<String, MutableMap<String, Field>> = result.diffField
        modifyColumns(diffField, sj)

        val addTable = result.addTable
        getNewTables(source, addTable, sj)

        return sj.toString()
    }

    //alter table gc_levy_tax_detail_excel_template drop column file_path;
    private fun dropFields(
        table: MutableMap<String, MutableMap<String, Field>>,
        sj: StringJoiner
    ) {
        for (fields in table.values) {
            for (field in fields) {
                val builder = StringBuilder()
                builder.append("alter table ")
                    .append(field.value.tableName)
                    .append(" drop column ")
                builder.append(field.key)
                    .append(";")

                sj.add(builder)
            }
        }
    }

    private fun modifyColumns(table: MutableMap<String, MutableMap<String, Field>>, sj: StringJoiner) {

        for (fieldMap in table.values) {
            for (field in fieldMap.values) {
                val builder = StringBuilder()
                if (field.isNew) {
                    //存在字段差异
                    builder.append("alter table ")
                        .append(field.tableName)
                        .append(" modify ")
                } else {
                    builder.append("alter table ")
                        .append(field.tableName)
                        .append(" add ")
                }
                builder.append(field.fieldName)
                    .append(" ")
                    .append(field.type)
                if (StringUtils.equals(field.default, null)
                    && !field.isNotNull
                ) {
                } else {
                    if (StringUtils.contains(field.default, "'")
                        || StringUtils.equals(field.type, "datetime")
                        || StringUtils.equals(field.type, "timestamp")
                        || StringUtils.equals(field.default, null)
                    ) {
                        builder.append(" default ")
                        builder.append(field.default)
                    } else {
                        builder.append(" default ")
                        builder.append("'").append(field.default).append("'")
                    }
                }
                if (StringUtils.isNotEmpty(field.extra)) {
                    builder.append(" ").append(field.extra)
                }
                if (!field.isNotNull) {
                    builder.append(" NULL ")
                } else {
                    builder.append(" NOT NULL ")
                }
                if (StringUtils.isNotEmpty(field.comment)) {
                    builder.append("comment '").append(field.comment).append("'")
                }

                sj.add(builder.append(";").toString())
            }
        }
    }

    private fun getNewTables(psiElement: PsiElement?, newTables: MutableList<String>, sj: StringJoiner) {
        val dbElement = ObjectUtils.tryCast(psiElement, DbElement::class.java)
        val dasChildren = dbElement!!.getDasChildren(ObjectKind.TABLE)
        for (dasChild in dasChildren) {
            if (newTables.contains(dasChild.name)) {
                sj.add(DatabaseEditorHelper.generateDefinition(dasChild, java.lang.StringBuilder()))
            }
        }
    }

    fun getTableText(psiElement: PsiElement): String {
        val stringJoiner = StringJoiner("\n")
        val dbElement = ObjectUtils.tryCast(psiElement, DbElement::class.java)
        val dasChildren = dbElement!!.getDasChildren(ObjectKind.TABLE)
        for (dasChild in dasChildren) {
//            stringJoiner.add(DatabaseDefinitionHelper.generateDefinition(dasChild, StringBuilder()))
            stringJoiner.add(DatabaseEditorHelper.generateDefinition(dasChild, java.lang.StringBuilder()))
        }
        return stringJoiner.toString()
    }
}