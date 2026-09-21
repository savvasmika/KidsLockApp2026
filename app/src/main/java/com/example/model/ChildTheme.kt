package com.example.model

import androidx.compose.ui.graphics.Color

enum class ChildTheme(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val mascotEmoji: String,
    val mascotName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color
) {
    SPACE(
        id = "space",
        displayName = "Space",
        iconEmoji = "🌌",
        mascotEmoji = "👨‍🚀",
        mascotName = "Astro",
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFF818CF8),
        accentColor = Color(0xFFFBBF24),
        backgroundColor = Color(0xFF0B0F19),
        surfaceColor = Color(0xFF1E293B)
    ),
    GAMING(
        id = "gaming",
        displayName = "Gaming",
        iconEmoji = "🎮",
        mascotEmoji = "🕹️",
        mascotName = "Pixel",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF06B6D4),
        accentColor = Color(0xFFA855F7),
        backgroundColor = Color(0xFF0F172A),
        surfaceColor = Color(0xFF1E293B)
    ),
    GALAXY(
        id = "galaxy",
        displayName = "Galaxy",
        iconEmoji = "🚀",
        mascotEmoji = "🛸",
        mascotName = "Cosmo",
        primaryColor = Color(0xFF8B5CF6),
        secondaryColor = Color(0xFFEC4899),
        accentColor = Color(0xFFFDE047),
        backgroundColor = Color(0xFF180D2B),
        surfaceColor = Color(0xFF2D1B4E)
    ),
    DINOSAURS(
        id = "dinosaurs",
        displayName = "Dinosaurs",
        iconEmoji = "🦖",
        mascotEmoji = "🦕",
        mascotName = "Rexy",
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFF84CC16),
        accentColor = Color(0xFFF59E0B),
        backgroundColor = Color(0xFF062817),
        surfaceColor = Color(0xFF0F3D24)
    ),
    ANIMALS(
        id = "animals",
        displayName = "Animals",
        iconEmoji = "🐼",
        mascotEmoji = "🦁",
        mascotName = "Panda & Leo",
        primaryColor = Color(0xFFF97316),
        secondaryColor = Color(0xFFFBBF24),
        accentColor = Color(0xFF10B981),
        backgroundColor = Color(0xFF2C1802),
        surfaceColor = Color(0xFF432504)
    ),
    RACING(
        id = "racing",
        displayName = "Racing",
        iconEmoji = "🏎",
        mascotEmoji = "🏁",
        mascotName = "Turbo",
        primaryColor = Color(0xFFEF4444),
        secondaryColor = Color(0xFFF97316),
        accentColor = Color(0xFFFACC15),
        backgroundColor = Color(0xFF1C0B0B),
        surfaceColor = Color(0xFF3B1515)
    ),
    SPORTS(
        id = "sports",
        displayName = "Sports",
        iconEmoji = "⚽",
        mascotEmoji = "🏆",
        mascotName = "Champ",
        primaryColor = Color(0xFF2563EB),
        secondaryColor = Color(0xFF38BDF8),
        accentColor = Color(0xFF22C55E),
        backgroundColor = Color(0xFF0A192F),
        surfaceColor = Color(0xFF172A45)
    ),
    FANTASY(
        id = "fantasy",
        displayName = "Fantasy",
        iconEmoji = "🧙",
        mascotEmoji = "🦄",
        mascotName = "Merlin & Spark",
        primaryColor = Color(0xFF9333EA),
        secondaryColor = Color(0xFFD946EF),
        accentColor = Color(0xFF38BDF8),
        backgroundColor = Color(0xFF1E0B36),
        surfaceColor = Color(0xFF35195C)
    ),
    OCEAN(
        id = "ocean",
        displayName = "Ocean",
        iconEmoji = "🌊",
        mascotEmoji = "🐬",
        mascotName = "Splash",
        primaryColor = Color(0xFF0284C7),
        secondaryColor = Color(0xFF06B6D4),
        accentColor = Color(0xFF34D399),
        backgroundColor = Color(0xFF041C2C),
        surfaceColor = Color(0xFF0C334D)
    ),
    ROBOTS(
        id = "robots",
        displayName = "Robots",
        iconEmoji = "🤖",
        mascotEmoji = "⚡",
        mascotName = "Gizmo",
        primaryColor = Color(0xFF64748B),
        secondaryColor = Color(0xFF38BDF8),
        accentColor = Color(0xFFF43F5E),
        backgroundColor = Color(0xFF0F172A),
        surfaceColor = Color(0xFF1E293B)
    ),
    COLORFUL(
        id = "colorful",
        displayName = "Colorful",
        iconEmoji = "🌈",
        mascotEmoji = "🎨",
        mascotName = "Rainbow",
        primaryColor = Color(0xFFEC4899),
        secondaryColor = Color(0xFFF59E0B),
        accentColor = Color(0xFF10B981),
        backgroundColor = Color(0xFF1F112B),
        surfaceColor = Color(0xFF371C4B)
    ),
    ADVENTURE(
        id = "adventure",
        displayName = "Adventure",
        iconEmoji = "🌳",
        mascotEmoji = "🧭",
        mascotName = "Scout",
        primaryColor = Color(0xFF15803D),
        secondaryColor = Color(0xFFB45309),
        accentColor = Color(0xFFEAB308),
        backgroundColor = Color(0xFF112211),
        surfaceColor = Color(0xFF1B381C)
    );

    companion object {
        fun fromId(id: String?): ChildTheme {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: SPACE
        }
    }
}
