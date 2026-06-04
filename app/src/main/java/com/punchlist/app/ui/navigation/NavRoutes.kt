package com.punchlist.app.ui.navigation

object NavRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROJECT_LIST = "project_list"
    const val CREATE_PROJECT = "create_project"
    const val PUNCH_ITEM_FEED = "punch_item_feed/{projectId}"
    const val CREATE_PUNCH_ITEM = "create_punch_item/{projectId}"
    const val PUNCH_ITEM_DETAIL = "punch_item_detail/{projectId}/{itemId}"
    const val CAMERA = "camera"

    fun punchItemFeed(projectId: String) = "punch_item_feed/$projectId"
    fun createPunchItem(projectId: String) = "create_punch_item/$projectId"
    fun punchItemDetail(projectId: String, itemId: String) = "punch_item_detail/$projectId/$itemId"
}
