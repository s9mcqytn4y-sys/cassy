package id.azureenterprise.cassy.db

import app.cash.sqldelight.ColumnAdapter

val BooleanAdapter = object : ColumnAdapter<Boolean, Long> {
    override fun decode(databaseValue: Long): Boolean = databaseValue != 0L
    override fun encode(value: Boolean): Long = if (value) 1L else 0L
}
