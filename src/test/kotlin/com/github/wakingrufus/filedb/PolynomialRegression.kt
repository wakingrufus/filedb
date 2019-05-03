package com.github.wakingrufus.filedb

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression


fun Iterable<Pair<Number, Number>>.multipleLinearRegression(): OLSMultipleLinearRegression {
    val regression = OLSMultipleLinearRegression()
    regression.isNoIntercept = false
    val y = mutableListOf<Double>()
    val x = mutableListOf<DoubleArray>()
    (listOf(0 to 0) + this).forEach {
        y.add(it.first.toDouble())
        x.add(arrayOf(it.second.toDouble(), Math.pow(it.second.toDouble(), 2.0)).toDoubleArray())
    }
    regression.newSampleData(y.toDoubleArray(), x.toTypedArray())
    return regression
}