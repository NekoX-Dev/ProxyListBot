package io.nekohasekai.pl_bot

import io.nekohasekai.ktlib.td.cli.TdCli

object Test : TdCli() {

    override val loginType = LoginType.USER

    override fun onLoad() {

        options databaseDirectory "data/fetcher"

        // getChatHistory 有缓存
        options useMessageDatabase false

        options apiId 971882
        options apiHash "1232533dd027dc2ec952ba91fc8e3f27"

    }

    @JvmStatic
    fun main(args: Array<String>) {

        launch(args)

        start()

    }

    override suspend fun onLogin() {


    }

}