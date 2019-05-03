package com.github.wakingrufus.filedb

import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.nield.kotlinstatistics.descriptiveStatistics
import org.nield.kotlinstatistics.simpleRegression
import kotlin.math.absoluteValue
import kotlin.test.fail

class PerformanceTests {
    companion object : KLogging()

    val db2 = fileDb(createTempDir(prefix = "tmp")) {
        entity<Player>("player")
        entity(jacksonEntity<Game>("game"))
    }

    fun warmUp() {
        val playerDb = db2.getAccessor<Player>()
        val playerId = playerDb.create(Player(name = "name", rating = 0)) ?: fail("no player created")
        playerDb.latest(playerId)
    }

    @Test
    fun `volume test create`() {
        logger.info { "*** volume test create ***" }
        val playerDb = db2.getAccessor<Player>()
        val results = listOf(10, 25, 50, 75, 100, 250, 500, 1000, 2000).map { n ->
            val time = time {
                (1..n).forEach {
                    val player = Player(name = "Player $it", rating = it)
                    playerDb.create(player) ?: fail("no player created")
                }
            }
            FileDbTest.logger.info("time to create $n files: $time")
            n to time.toMillis()
        }
        val regression = results.simpleRegression()
        logger.info("slope: ${regression.slope} r: ${regression.r}")
        assertThat(regression.r).`as`("create is linear").isGreaterThan(0.98)
        assertThat(results.map { it.second }.max()).isLessThan(1000)
    }

    @Test
    fun `test update`() {
        logger.info { "*** test update ***" }
        val playerDb = db2.getAccessor<Player>()
        val results = listOf(10, 25, 50, 75, 100, 250, 500, 1000, 2000).map { n ->
            val playerId = playerDb.create(Player(name = "name", rating = 0)) ?: fail("no player created")
            val time = time {
                (1..n).forEach {
                    val player = Player(name = "name", rating = it)
                    playerDb.update(playerId, player)
                }
            }
            logger.info("time to update $n files: $time")
            n to time.toMillis()
        }

        val regression = results.simpleRegression()
        val polynomialRegression = results.multipleLinearRegression()
        logger.info("slope: ${regression.slope} r: ${regression.r}")
        logger.info("polynomial r^2: ${polynomialRegression.calculateRSquared()}")
        assertThat(polynomialRegression.calculateRSquared()).`as`("update is polynomial").isGreaterThan(0.98)
        assertThat(regression.r).`as`("update is not linear").isLessThan(0.98)
    }

    @Test
    fun `test incremental update`() {
        logger.info { "*** test incremental update ***" }
        val playerDb = db2.getAccessor<Player>()
        val playerId = playerDb.create(Player(name = "name", rating = 0)) ?: fail("no player created")
        val results = (1..1000).map { n ->
            val player = Player(name = "name", rating = n)
            val time = time { playerDb.update(playerId, player) }
            n to time.toMillis()
        }

        val stats = results.map { it.second }.descriptiveStatistics
        val regression = results.simpleRegression()
        logger.info("slope: ${regression.slope} r: ${regression.r}")
        assertThat(regression.slope.absoluteValue).`as`("incremental update is constant").isLessThan(0.002)
        assertThat(stats.standardDeviation).`as`("incremental update is consistant").isLessThan(10.0)
        assertThat(stats.max).`as`("incremental update is consistant").isLessThan(10.0)
    }


    @Test
    fun `test allVersions`() {
        logger.info { "*** volume test allVersions ***" }
        warmUp()
        val playerDb = db2.getAccessor<Player>()

        val data = (1..1000).mapNotNull { n ->
            if (n % 10 == 0) {
                val playerId = playerDb.create(Player(name = "name", rating = 0)) ?: fail("no player created")
                (1..n).forEach {
                    val player = Player(name = "name", rating = it)
                    playerDb.update(playerId, player)
                }
                val time = time { playerDb.allVersions(playerId) }
                n to time.toMillis()
            } else {
                null
            }
        }

        logger.info { "read allVersion stats: $data" }
        val noOutliers = data.filter { it.second < 12 }
        val outlierCount = data.size - noOutliers.size
        logger.info { "$outlierCount outliers removed" }

        val regression = noOutliers.simpleRegression()

        logger.info("allVersions slope: ${regression.slope} r: ${regression.r}")
        assertThat(regression.slope).`as`("allVersions is fast").isLessThan(0.01)
        assertThat(outlierCount).`as`("outliers are rare").isLessThan(20)
    }

    @Test
    fun `volume test readLatest`() {
        logger.info { "*** volume test readLatest ***" }
        warmUp()
        val playerDb = db2.getAccessor<Player>()
        val results = listOf(10, 50, 100, 250, 500, 750, 1000).map { n ->
            val playerId = playerDb.create(Player(name = "name", rating = 0)) ?: fail("no player created")
            (1..n).forEach {
                val player = Player(name = "name", rating = it)
                playerDb.update(playerId, player)
            }
            val readLatestTime = time { playerDb.latest(playerId) }
            FileDbTest.logger.info("time to read latest of $n files: $readLatestTime")
            n to readLatestTime.toMillis()
        }

        val stats = results.map { it.second }.descriptiveStatistics
        FileDbTest.logger.info("median: ${stats.geometricMean} P90: ${stats.percentile(90.0)} std: ${stats.standardDeviation}")

        val regression = results.simpleRegression()
        FileDbTest.logger.info("regression slope: ${regression.slope} r: ${regression.r}")

        assertThat(regression.slope.absoluteValue).`as`("read latest is constant").isLessThan(0.002)
        assertThat(stats.standardDeviation).`as`("read latest is consistant").isLessThan(0.8)
    }

    @Test
    fun `volume test allLatest`() {
        logger.info { "*** read all latest ***" }
        val playerDb = db2.getAccessor<Player>()
        val stats = listOf(50, 100, 500, 1000, 2000).map { n ->
            (1..n).forEach {
                playerDb.create(Player(name = "name", rating = it)) ?: fail("no player created")
            }
            val duration = time { playerDb.allLatest() }
            FileDbTest.logger.info("time to read all latest of $n entities: $duration")
            n to duration.toMillis()
        }

        val syncRegression = stats.simpleRegression()
        FileDbTest.logger.info("sync slope: ${syncRegression.slope} r: ${syncRegression.r}")
        assertThat(syncRegression.r).`as`("read all latest sync is linear").isGreaterThan(0.98)
        assertThat(stats.map { it.second }.max()).isLessThan(200)
    }
}