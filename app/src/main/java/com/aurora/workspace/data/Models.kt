package com.aurora.workspace.data

data class Member(
    val id: String,
    val name: String,
    val role: String,
    val color: Int,
    val online: Boolean = false
)

data class TaskItem(
    val id: String,
    var title: String,
    var done: Boolean,
    val assigneeId: String? = null,
    var priority: String = "NONE",
    var tag: String = ""
)

data class Project(
    val id: String,
    var name: String,
    var description: String,
    var emoji: String,
    var accent: Int,
    var dueLabel: String,
    var memberIds: MutableList<String>,
    var tasks: MutableList<TaskItem>
) {
    val progress: Int
        get() = if (tasks.isEmpty()) 0
        else Math.round(tasks.count { it.done } * 100f / tasks.size)

    val isDone: Boolean
        get() = tasks.isNotEmpty() && tasks.all { it.done }

    val doneCount: Int get() = tasks.count { it.done }
}

data class Message(
    val id: String,
    val authorId: String,
    val text: String,
    val time: String,
    val mine: Boolean
)

data class Conversation(
    val id: String,
    var title: String,
    var memberId: String?,
    var isGroup: Boolean,
    var preview: String,
    var time: String,
    var unread: Int,
    var messages: MutableList<Message>
)
