package com.github.wakingrufus.filedb

import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

data class Game(val gameTime: Instant)

data class Player(val name: String, val rating: Int)

class FileDbTest {
    companion object : KLogging()

    val db2 = fileDb(createTempDir(prefix = "tmp")) {
        entity<Player>("player")
        entity(jacksonEntity<Game>("game"))
    }

    @Test
    fun test() {
        val now = Instant.EPOCH

        val playerDb = db2.getAccessor<Player>()
        val player = Player(name = "name", rating = 1200)
        val playerId = playerDb.create(player) ?: fail("no player created")
        val actual: Player? = playerDb.latest(playerId)
        assertEquals(expected = player, actual = actual)

        val game = Game(gameTime = now)
        val gameDb = db2.getAccessor<Game>()
        val gameId = gameDb.create(game) ?: fail("no player created")
        val actualGame = gameDb.latest(gameId)
        assertEquals(expected = game, actual = actualGame)
    }

    @Test
    fun `test update`() {
        val player = Player(name = "name", rating = 1200)
        val playerDb = db2.getAccessor<Player>()
        val playerId = playerDb.create(player) ?: fail("no player created")
        val actualPlayer = playerDb.latest(playerId)
        assertEquals(expected = player, actual = actualPlayer)
        val player2 = player.copy(name = "new name")
        playerDb.update(playerId, player2)
        val actualPlayer2 = playerDb.latest(playerId)
        assertEquals(expected = player2, actual = actualPlayer2)
        assertThat(playerDb.allVersions(playerId))
                .`as`("allVersions returns both versions")
                .containsExactly(player, player2)
    }

    @Test
    fun `test multiple`() {
        val player = Player(name = "name", rating = 1200)
        val playerDb = db2.getAccessor<Player>()
        val playerId = playerDb.create(player) ?: fail("no player created")
        val actualPlayer = playerDb.latest(playerId)
        assertEquals(expected = player, actual = actualPlayer)
        val player2 = player.copy(name = "new name")
        val player2Id = playerDb.create(player2) ?: fail("no player created")
        val actualPlayer2 = playerDb.latest(player2Id)
        assertEquals(expected = player2, actual = actualPlayer2)
        assertThat(playerDb.allLatest())
                .containsExactlyInAnyOrder(player, player2)
    }
}