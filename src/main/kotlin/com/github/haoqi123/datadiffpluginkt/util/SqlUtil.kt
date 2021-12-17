package com.github.haoqi123.datadiffpluginkt.util

import com.github.haoqi123.datadiffpluginkt.bo.ColumnType
import com.github.haoqi123.datadiffpluginkt.bo.TableColumn
import com.intellij.database.dialects.mysqlbase.model.MysqlBaseTableColumn
import com.intellij.database.model.DasObject
import com.intellij.database.model.ObjectKind
import com.intellij.database.model.basic.BasicMixinTableOrView
import com.intellij.database.model.basic.BasicTableColumn
import com.intellij.database.psi.DbNamespaceImpl
import org.apache.commons.lang3.StringUtils

/**
 * @Created by haoqi
 */
object SqlUtil {
    const val SOURCE = "source"
    const val TARGET = "targets"

    fun getData(source: DbNamespaceImpl): Map<String, TableColumn> {
        val table = mutableMapOf<String, TableColumn>()
        val delegate: DasObject = source.getDelegate()
        for (dasChild in delegate.getDasChildren(ObjectKind.TABLE)) {
            val colNames: MutableList<String> = ArrayList()
            val primaryKey = (dasChild as BasicMixinTableOrView).primaryKey
            if (primaryKey != null) {
                colNames.addAll(primaryKey.colNames)
            }

            for (dasObject in dasChild.getDasChildren(ObjectKind.COLUMN)) {
                val basicTableColumn = dasObject as BasicTableColumn
                val columnTypeBo = ColumnType(
                    dasObject.getName(),
                    basicTableColumn.dataType.specification,
                    basicTableColumn.isNotNull,
                    if (colNames.contains(dasObject.getName())) "PRI" else null,
                    basicTableColumn.defaultExpression,
                    dasChild.name,
                    if ((basicTableColumn is MysqlBaseTableColumn) && (StringUtils.isNotBlank(basicTableColumn.onUpdate)))
                        basicTableColumn.onUpdate else null,
                    basicTableColumn.comment
                )

                table[dasChild.name].apply {
                    if (this == null) table[dasChild.name] =
                        TableColumn(dasChild.name, mutableListOf(columnTypeBo)) else this.column.plus(columnTypeBo)
                }
            }
        }
        return table
    }
}