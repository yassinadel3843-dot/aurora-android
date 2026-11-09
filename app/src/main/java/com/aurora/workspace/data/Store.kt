package com.aurora.workspace.data

import android.content.Context
import android.content.SharedPreferences
import com.aurora.workspace.R
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Single source of truth. Persisted as JSON inside SharedPreferences so the app
 * stays fully offline and works on Android 5.0+ without a database dependency.
 */
object Store {

    private const val PREFS = "aurora_store"

    // Task priority keys. Kept as plain strings so old persisted JSON that has
    // no priority field still parses (optString falls back to PRIORITY_NONE).
    const val PRIORITY_NONE = "NONE"
    const val PRIORITY_LOW = "LOW"
    const val PRIORITY_MEDIUM = "MEDIUM"
    const val PRIORITY_HIGH = "HIGH"
    private var prefs: SharedPreferences? = null
    private var loaded = false

    var members: MutableList<Member> = mutableListOf()
        private set
    var projects: MutableList<Project> = mutableListOf()
        private set
    var conversations: MutableList<Conversation> = mutableListOf()
        private set

    var signedIn = false
    var userName = "Layla"
    var userEmail = "layla@aurora.app"
    var userColor = 0xFF4DA3FF.toInt()
    var googleAccount: String? = null
    var notifications = true
    var haptics = true
    var streak = 5

    fun init(context: Context) {
        if (loaded) return
        val p = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p

        signedIn = p.getBoolean("signed_in", false)
        userName = p.getString("user_name", "Layla") ?: "Layla"
        userEmail = p.getString("user_email", "layla@aurora.app") ?: "layla@aurora.app"
        userColor = p.getInt("user_color", 0xFF4DA3FF.toInt())
        googleAccount = p.getString("google_account", null)
        notifications = p.getBoolean("notifications", true)
        haptics = p.getBoolean("haptics", true)
        streak = p.getInt("streak", 5)

        val membersJson = p.getString("members", null)
        if (membersJson.isNullOrBlank()) {
            members = SeedData.members()
            projects = SeedData.projects()
            conversations = SeedData.conversations()
        } else {
            val ok = runCatching {
                parseMembers(membersJson)
                parseProjects(p.getString("projects", "[]") ?: "[]")
                parseConversations(p.getString("conversations", "[]") ?: "[]")
            }.isSuccess
            if (!ok || members.isEmpty()) {
                members = SeedData.members()
                projects = SeedData.projects()
                conversations = SeedData.conversations()
            }
        }
        loaded = true
        syncMe()
        save(context)
    }

    // ---------------------------------------------------------------- auth

    fun signIn(context: Context, name: String, email: String, google: String? = null) {
        userName = name.ifBlank { email.substringBefore('@').replaceFirstChar { it.uppercase() } }
        userEmail = email
        googleAccount = google
        signedIn = true
        syncMe()
        save(context)
    }

    fun signOut(context: Context) {
        signedIn = false
        save(context)
    }

    fun linkGoogle(context: Context, account: String) {
        googleAccount = account
        save(context)
    }

    fun unlinkGoogle(context: Context) {
        googleAccount = null
        save(context)
    }

    fun setNotifications(context: Context, enabled: Boolean) {
        notifications = enabled
        save(context)
    }

    fun setHaptics(context: Context, enabled: Boolean) {
        haptics = enabled
        save(context)
    }

    // --------------------------------------------------------------- lookup

    fun member(id: String?): Member? = members.firstOrNull { it.id == id }
    fun project(id: String?): Project? = projects.firstOrNull { it.id == id }
    fun conversation(id: String?): Conversation? = conversations.firstOrNull { it.id == id }

    val activeProjects: List<Project> get() = projects.filter { !it.isDone }

    val totalTasks: Int get() = projects.sumOf { it.tasks.size }
    val doneTasks: Int get() = projects.sumOf { p -> p.tasks.count { it.done } }
    val overallProgress: Int
        get() = if (totalTasks == 0) 0 else Math.round(doneTasks * 100f / totalTasks)

    // -------------------------------------------------------------- mutations

    fun addProject(
        context: Context,
        name: String,
        description: String,
        emoji: String,
        accent: Int
    ): Project {
        val project = Project(
            id = "p" + UUID.randomUUID().toString().take(8),
            name = name,
            description = description,
            emoji = emoji,
            accent = accent,
            dueLabel = "Soon",
            memberIds = mutableListOf("me"),
            tasks = mutableListOf()
        )
        projects.add(0, project)
        save(context)
        return project
    }

    fun addTask(
        context: Context,
        projectId: String,
        title: String,
        priority: String = "NONE",
        tag: String = ""
    ) {
        val project = project(projectId) ?: return
        project.tasks.add(
            TaskItem(
                id = "t" + UUID.randomUUID().toString().take(8),
                title = title,
                done = false,
                assigneeId = "me",
                priority = priority,
                tag = tag
            )
        )
        save(context)
    }

    fun toggleTask(context: Context, projectId: String, taskId: String) {
        val project = project(projectId) ?: return
        project.tasks.firstOrNull { it.id == taskId }?.let { it.done = !it.done }
        save(context)
    }

    fun sendMessage(context: Context, conversationId: String, text: String) {
        val conversation = conversation(conversationId) ?: return
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        conversation.messages.add(
            Message("m" + UUID.randomUUID().toString().take(8), "me", text, time, true)
        )
        conversation.preview = context.getString(R.string.chat_you_prefix, text)
        conversation.time = time
        conversation.unread = 0
        save(context)
    }

    fun markRead(context: Context, conversationId: String) {
        val conversation = conversation(conversationId) ?: return
        if (conversation.unread != 0) {
            conversation.unread = 0
            save(context)
        }
    }

    // ------------------------------------------------------------- internals

    private fun syncMe() {
        val index = members.indexOfFirst { it.id == "me" }
        val me = Member(
            id = "me",
            name = userName.ifBlank { "Layla" },
            role = "Product designer",
            color = userColor,
            online = true
        )
        if (index >= 0) members[index] = me else members.add(0, me)
    }

    fun save(context: Context) {
        val p = prefs ?: context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE).also { prefs = it }

        p.edit()
            .putBoolean("signed_in", signedIn)
            .putString("user_name", userName)
            .putString("user_email", userEmail)
            .putInt("user_color", userColor)
            .putString("google_account", googleAccount)
            .putBoolean("notifications", notifications)
            .putBoolean("haptics", haptics)
            .putInt("streak", streak)
            .putString("members", membersToJson())
            .putString("projects", projectsToJson())
            .putString("conversations", conversationsToJson())
            .apply()
    }

    private fun membersToJson(): String {
        val arr = JSONArray()
        members.forEach { m ->
            arr.put(
                JSONObject()
                    .put("id", m.id)
                    .put("name", m.name)
                    .put("role", m.role)
                    .put("color", m.color)
                    .put("online", m.online)
            )
        }
        return arr.toString()
    }

    private fun projectsToJson(): String {
        val arr = JSONArray()
        projects.forEach { p ->
            val tasks = JSONArray()
            p.tasks.forEach { t ->
                tasks.put(
                    JSONObject()
                        .put("id", t.id)
                        .put("title", t.title)
                        .put("done", t.done)
                        .put("assignee", t.assigneeId ?: JSONObject.NULL)
                        .put("priority", t.priority)
                        .put("tag", t.tag)
                )
            }
            val ids = JSONArray()
            p.memberIds.forEach { ids.put(it) }
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("description", p.description)
                    .put("emoji", p.emoji)
                    .put("accent", p.accent)
                    .put("due", p.dueLabel)
                    .put("members", ids)
                    .put("tasks", tasks)
            )
        }
        return arr.toString()
    }

    private fun conversationsToJson(): String {
        val arr = JSONArray()
        conversations.forEach { c ->
            val messages = JSONArray()
            c.messages.forEach { m ->
                messages.put(
                    JSONObject()
                        .put("id", m.id)
                        .put("author", m.authorId)
                        .put("text", m.text)
                        .put("time", m.time)
                        .put("mine", m.mine)
                )
            }
            arr.put(
                JSONObject()
                    .put("id", c.id)
                    .put("title", c.title)
                    .put("member", c.memberId ?: JSONObject.NULL)
                    .put("group", c.isGroup)
                    .put("preview", c.preview)
                    .put("time", c.time)
                    .put("unread", c.unread)
                    .put("messages", messages)
            )
        }
        return arr.toString()
    }

    private fun parseMembers(json: String) {
        val arr = JSONArray(json)
        members = mutableListOf()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            members.add(
                Member(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    role = o.optString("role"),
                    color = o.optInt("color", 0xFF4DA3FF.toInt()),
                    online = o.optBoolean("online", false)
                )
            )
        }
    }

    private fun parseProjects(json: String) {
        val arr = JSONArray(json)
        projects = mutableListOf()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val idsArr = o.optJSONArray("members") ?: JSONArray()
            val ids = mutableListOf<String>()
            for (j in 0 until idsArr.length()) ids.add(idsArr.optString(j))

            val tasksArr = o.optJSONArray("tasks") ?: JSONArray()
            val tasks = mutableListOf<TaskItem>()
            for (j in 0 until tasksArr.length()) {
                val t = tasksArr.getJSONObject(j)
                val assignee = if (t.has("assignee") && !t.isNull("assignee")) t.optString("assignee") else null
                tasks.add(
                    TaskItem(
                        id = t.optString("id"),
                        title = t.optString("title"),
                        done = t.optBoolean("done", false),
                        assigneeId = assignee,
                        priority = t.optString("priority", PRIORITY_NONE),
                        tag = t.optString("tag", "")
                    )
                )
            }
            projects.add(
                Project(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    description = o.optString("description"),
                    emoji = o.optString("emoji", "\uD83C\uDF0C"),
                    accent = o.optInt("accent", 0xFF4DA3FF.toInt()),
                    dueLabel = o.optString("due", "Soon"),
                    memberIds = ids,
                    tasks = tasks
                )
            )
        }
    }

    private fun parseConversations(json: String) {
        val arr = JSONArray(json)
        conversations = mutableListOf()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val member = if (o.has("member") && !o.isNull("member")) o.optString("member") else null
            val msgsArr = o.optJSONArray("messages") ?: JSONArray()
            val messages = mutableListOf<Message>()
            for (j in 0 until msgsArr.length()) {
                val m = msgsArr.getJSONObject(j)
                messages.add(
                    Message(
                        id = m.optString("id"),
                        authorId = m.optString("author"),
                        text = m.optString("text"),
                        time = m.optString("time"),
                        mine = m.optBoolean("mine", false)
                    )
                )
            }
            conversations.add(
                Conversation(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    memberId = member,
                    isGroup = o.optBoolean("group", member == null),
                    preview = o.optString("preview"),
                    time = o.optString("time"),
                    unread = o.optInt("unread", 0),
                    messages = messages
                )
            )
        }
    }
}
