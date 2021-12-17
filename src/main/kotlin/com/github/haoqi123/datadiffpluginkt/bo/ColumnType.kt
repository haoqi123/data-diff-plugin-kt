package com.github.haoqi123.datadiffpluginkt.bo

data class ColumnType(
    var fieldName: String,
    var type: String,
    var isNotNull: Boolean,
    var key: String? = null,
    var default: String? = null,
    var tableName: String,
    var extra: String? = null,
    var comment: String? = null
)


data class TableColumn(
    var tableName: String,
    var column: List<ColumnType>
)
