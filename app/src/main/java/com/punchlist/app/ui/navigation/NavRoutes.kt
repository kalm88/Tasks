package com.punchlist.app.ui.navigation

object NavRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROJECT_LIST = "project_list"
    const val CREATE_PROJECT = "create_project"
    const val PUNCH_ITEM_FEED = "punch_item_feed/{projectId}/{projectName}"
    const val CREATE_PUNCH_ITEM = "create_punch_item/{projectId}"
    const val PUNCH_ITEM_DETAIL = "punch_item_detail/{projectId}/{itemId}/{projectName}"
    const val CAMERA = "camera"
    const val BARCODE_SCANNER = "barcode_scanner"

    fun punchItemFeed(projectId: String, projectName: String = "Project") =
        "punch_item_feed/$projectId/${projectName.ifBlank { "Project" }}"
    fun createPunchItem(projectId: String) = "create_punch_item/$projectId"
    fun punchItemDetail(projectId: String, itemId: String, projectName: String = "") =
        "punch_item_detail/$projectId/$itemId/${projectName.ifBlank { "Project" }}"
}
