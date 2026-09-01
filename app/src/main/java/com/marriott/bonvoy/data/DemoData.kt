package com.marriott.bonvoy.data

data class Member(
    val firstName: String,
    val lastName: String,
    val memberNumber: String,
    val tier: String,
    val points: Int,
    val nightsThisYear: Int,
    val nightsToNextTier: Int,
)

data class Stay(
    val hotel: String,
    val brand: String,
    val city: String,
    val dates: String,
    val confirmation: String,
    val upcoming: Boolean,
)

data class Hotel(
    val name: String,
    val brand: String,
    val city: String,
    val pointsPerNight: Int,
    val ratePerNight: Int,
    val rating: Double,
)

object DemoData {
    val member = Member(
        firstName = "Neil",
        lastName = "Kelly",
        memberNumber = "184 302 771",
        tier = "Titanium Elite",
        points = 148_250,
        nightsThisYear = 62,
        nightsToNextTier = 38,
    )

    val stays = listOf(
        Stay("The St. Regis New York", "St. Regis", "New York, NY", "Oct 14 – Oct 17", "87213345", true),
        Stay("W Barcelona", "W Hotels", "Barcelona, Spain", "Nov 2 – Nov 6", "91008217", true),
        Stay("JW Marriott Marquis Miami", "JW Marriott", "Miami, FL", "Aug 19 – Aug 22", "77320981", false),
        Stay("The Ritz-Carlton, Half Moon Bay", "The Ritz-Carlton", "Half Moon Bay, CA", "Jul 3 – Jul 6", "70211534", false),
        Stay("Moxy Chelsea", "Moxy", "New York, NY", "Jun 12 – Jun 13", "68821170", false),
    )

    val hotels = listOf(
        Hotel("The Ritz-Carlton, Kyoto", "The Ritz-Carlton", "Kyoto, Japan", 85_000, 1_290, 4.9),
        Hotel("Le Méridien Maldives Resort & Spa", "Le Méridien", "Lhaviyani Atoll, Maldives", 62_000, 640, 4.7),
        Hotel("EDITION Tokyo Toranomon", "EDITION", "Tokyo, Japan", 58_000, 520, 4.8),
        Hotel("Westin Grand Cayman Seven Mile Beach", "Westin", "Grand Cayman", 51_000, 480, 4.6),
        Hotel("Sheraton Grand London Park Lane", "Sheraton", "London, UK", 47_000, 410, 4.5),
        Hotel("Autograph Collection Hotel Emma", "Autograph Collection", "San Antonio, TX", 44_000, 395, 4.8),
        Hotel("Courtyard Reykjavik Keflavik Airport", "Courtyard", "Keflavik, Iceland", 22_000, 180, 4.3),
        Hotel("Aloft Austin Downtown", "Aloft", "Austin, TX", 18_000, 165, 4.2),
    )
}
