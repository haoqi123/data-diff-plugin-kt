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

                table[dasChild.name].apply {
                    if (this == null)
                        table[dasChild.name] = mutableMapOf(dasObject.getName() to field)
                    else dasObject.getName() to field
                }
            }
        }
        return table
    }

    fun getResultString(result: DiffResultBo, source: PsiElement): String {
        val sj = StringJoiner("\n")

        val dropField = result.dropField;
        dropColumns(dropField, sj)

        return sj.toString()
    }

    //alter table gc_levy_tax_detail_excel_template drop column file_path;
    private fun dropColumns(
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