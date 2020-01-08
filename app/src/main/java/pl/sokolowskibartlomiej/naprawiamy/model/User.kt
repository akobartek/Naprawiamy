package pl.sokolowskibartlomiej.naprawiamy.model

data class User(
    val id: Int?,
    val specialist: Boolean?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val phoneNr: String?,
    val passwordHash: String?,
    val address: String?,
    val token: String?
) {
    fun getUserAsString(): String =
        "$id~$specialist~${email ?: ""}~${firstName ?: ""}~${lastName ?: ""}" +
                "~${phoneNr ?: ""}~$passwordHash~${address ?: ""}~$token"

    companion object {
        fun createUserFromString(userString: String): User {
            val user = userString.split("~")
            return User(
                user[0].toInt(), user[1] == "true", user[2], user[3],
                user[4], user[5], user[6], user[7], user[8]
            )
        }
    }
}

data class UserWithVotes(
    val user: User,
    val votes: List<ListingVote>
)

data class AuthInfo(
    val Specialist: Boolean?,
    val Email: String?,
    val Password: String?
)