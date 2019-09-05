package org.adhash.sdk.adhashask.constants

object ApiConstants {
    const val API_BASE_URL = "https://bidder.adhash.org"

    object Endpoint {
        const val GET_AD_BIDDER = "/protocol.php"
    }

    object Query{
        const val RTB = "rtb"
        const val VERSION = 1.0
    }

    object Param {
        const val ACTION = "action"
        const val VERSION = "version"
    }
}