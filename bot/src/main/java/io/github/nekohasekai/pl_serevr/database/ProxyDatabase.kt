package io.github.nekohasekai.pl_serevr.database

import io.github.nekohasekai.nekolib.core.utils.mkDatabase
import io.github.nekohasekai.nekolib.core.utils.mkTable

object ProxyDatabase {

    val database = mkDatabase("proxy")

    val table = database.mkTable<ProxyEntity>()

}