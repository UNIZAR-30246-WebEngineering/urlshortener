package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

data class UrlSum(
    var id: String,
    var sum: Int
)

data class UserSum(
    var name: String,
    var sum: Int
)

interface RankingUseCase {
    fun ranking(): List<UrlSum>
    fun userRanking(): List<UserSum>
}

/**
 * Implementation of [RankingUseCase].
 */
class RankingUseCaseImpl(
    private val shortUrlRepositoryService: ShortUrlRepositoryService,
    private val clickRepositoryService: ClickRepositoryService
) : RankingUseCase {
    override fun ranking(): List<UrlSum> =
        clickRepositoryService.computeClickSum()

    override fun userRanking(): List<UserSum> =
                shortUrlRepositoryService.computeUserClicks()

}
