package com.github.wakingrufus.filedb

import javafx.collections.FXCollections
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import kotlin.test.Test

class ObservableMapExtTest {

    @Test
    fun bindTo() {
        runBlocking {
            val db2 = fileDb(createTempDir(prefix = "tmp")) {
                entity<Player>("player")
                entity(jacksonEntity<Game>("game"))
            }
            val map = FXCollections.observableHashMap<String, Player>()
            val playerDb = db2.getAccessor<Player>()
            map.bindTo(playerDb)
            val player = Player(name = "name", rating = 1200)
            val player2 = Player(name = "name2", rating = 1201)
            val created = playerDb.create(player).orEmpty()
            Assertions.assertThat(waitUntil(player) { map[created] }).isNotNull.isEqualTo(player)
            playerDb.update(created, player2)
            Assertions.assertThat(waitUntil(player2) { map[created] }).isNotNull.isEqualTo(player2)
        }
    }
}