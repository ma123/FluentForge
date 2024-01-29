package com.identic.fluentforge.ui.navigation

const val BOOK_ID_ARG_KEY = "bookId"

sealed class Screens(val route: String) {
    data object HomeScreen : Screens("home")
    data object LibraryScreen : Screens("library")

    data object BookDetailScreen : Screens("book_detail_screen/{$BOOK_ID_ARG_KEY}") {
        fun withBookId(id: String): String {
            return this.route.replace("{$BOOK_ID_ARG_KEY}", id)
        }
    }

    data object ReaderDetailScreen : Screens("reader_detail_screen/{$BOOK_ID_ARG_KEY}") {
        fun withBookId(id: String): String {
            return this.route.replace("{$BOOK_ID_ARG_KEY}", id)
        }
    }
}
