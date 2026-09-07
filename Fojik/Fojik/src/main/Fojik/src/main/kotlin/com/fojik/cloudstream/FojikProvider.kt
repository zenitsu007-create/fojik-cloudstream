package com.fojik.cloudstream

import com.lagradost.cloudstream3.*
import org.jsoup.nodes.Element
import java.net.URLEncoder

class FojikProvider : MainAPI() {

    override var mainUrl = "https://fojik.site"
    override var name = "Fojik"
    override var lang = "en"

    override val supportedTypes = setOf(TvType.Movie)
    override val hasMainPage = false

    override suspend fun search(query: String): List<SearchResponse> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val document = app.get("$mainUrl/?s=$encoded").document

        return document.select(
            "article, .result-item, .item, .search-item, .search-page .item"
        ).mapNotNull { it.toSearchResponse() }
            .distinctBy { it.url }
    }

    private fun Element.toSearchResponse(): SearchResponse? {
        val anchor = selectFirst("a[href*='/movie/']") ?: return null
        val href = anchor.attr("href").ifBlank { return null }

        val title = (
            selectFirst("h2, h3, .title, .data h1")?.text()
                ?: anchor.attr("title")
                ?: anchor.text()
        ).trim()

        if (title.isBlank()) return null

        val poster = selectFirst("img")?.let {
            it.attr("data-src").ifBlank { it.attr("src") }
        }

        return newMovieSearchResponse(title, href, TvType.Movie) {
            posterUrl = poster
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title = document
            .selectFirst(".sheader .data h1, .sheader h1, h1")
            ?.text()
            ?.trim()
            ?: return null

        val poster = document
            .selectFirst(".sheader .poster img, .poster img")
            ?.let { it.attr("data-src").ifBlank { it.attr("src") } }

        val dateText = document
            .selectFirst(".sheader .extra .date, .extra .date")
            ?.text()
            ?.trim()

        val year = Regex("(19|20)\\d{2}")
            .find(dateText.orEmpty())
            ?.value
            ?.toIntOrNull()

        val country = document
            .selectFirst(".sheader .extra .country, .extra .country")
            ?.text()
            ?.trim()

        val genres = document
            .select(".sgeneros a")
            .map { it.text().trim() }
            .filter { it.isNotBlank() }

        val plot = document
            .selectFirst(".sbox p, .wp-content p")
            ?.text()
            ?.trim()

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            posterUrl = poster
            this.year = year
            this.plot = plot
            this.tags = buildList {
                addAll(genres)
                if (!country.isNullOrBlank()) add(country)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        // The supplied Fojik page contains third-party download forms.
        // This extension deliberately does not resolve or automate those links.
        return false
    }
}
