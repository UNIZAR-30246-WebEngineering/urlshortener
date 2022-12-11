package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

interface ClickSum {
    fun getHash(): String
    fun getSum(): Int
}

data class UrlSum(
    var id: String,
    var sum: Int
)

data class UserClicks(
    var name: String,
    var sum: Int
)

interface RankingUseCase {
    fun ranking(): List<UrlSum>
    /*fun userRanking(): List<UserClicks>*/
}

/**
 * Implementation of [RankingUseCase].
 */
class RankingUseCaseImpl(
    private val shortUrlRepositoryService: ShortUrlRepositoryService,
    private val clickRepositoryService: ClickRepositoryService
) : RankingUseCase {
    override fun ranking(): List<UrlSum> {
        val mylist  =
        clickRepositoryService.computeClickSum().map { case ->
            shortUrlRepositoryService.findByKey(case.getHash()).let { shortUrl ->
                if (shortUrl != null) {
                    UrlSum(shortUrl.hash, case.getSum())
                }
                else null
            }
        }.filterNotNull()

        System.out.println("RankingUseCaseImpl.ranking():")
        System.out.println(mylist)
        return mylist
    }

    /*override fun userRanking(): List<UserClicks> {
        val myuserlist  =
                shortUrlRepositoryService.computeUserClicks().map { case ->
                    shortUrlRepositoryService.findByKey(case.getHash()).let { shortUrl ->
                        if (shortUrl != null) {
                            UserClicks(shortUrl.hash, case.getSum())
                        }
                        else null
                    }
                }.filterNotNull()

        System.out.println("RankingUseCaseImpl.ranking():")
        System.out.println(myuserlist)
        return myuserlist
    }*/
}
