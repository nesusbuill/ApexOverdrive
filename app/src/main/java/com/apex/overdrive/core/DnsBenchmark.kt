package com.apex.overdrive.core

import kotlinx.coroutines.*

object DnsBenchmark {
    data class DnsResult(val name: String, val ip: String, val ms: Int)

    private val SERVERS = listOf(
        "Google" to "8.8.8.8",
        "Cloudflare" to "1.1.1.1",
        "Quad9" to "9.9.9.9",
        "OpenDNS" to "208.67.222.222",
        "AdGuard" to "94.140.14.14",
        "NextDNS" to "45.90.28.0"
    )

    suspend fun benchmark(): List<DnsResult> = withContext(Dispatchers.IO) {
        coroutineScope {
            SERVERS.map { (name, ip) ->
                async {
                    val r = PingTester.test(ip, count = 3)
                    DnsResult(name, ip, if (r.avgMs > 0) r.avgMs else 9999)
                }
            }.awaitAll().sortedBy { it.ms }
        }
    }
}
