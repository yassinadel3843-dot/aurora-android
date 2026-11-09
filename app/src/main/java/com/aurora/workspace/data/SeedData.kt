package com.aurora.workspace.data

object SeedData {

    private val BLUE = 0xFF4DA3FF.toInt()
    private val INDIGO = 0xFF7C5CFF.toInt()
    private val TEAL = 0xFF35E0C8.toInt()
    private val GREEN = 0xFF37D67A.toInt()
    private val ORANGE = 0xFFFF8A5B.toInt()
    private val ROSE = 0xFFFF5E7E.toInt()

    fun members(): MutableList<Member> = mutableListOf(
        Member("me", "Layla", "Product designer", BLUE, true),
        Member("lina", "Lina Farah", "Design lead", INDIGO, true),
        Member("omar", "Omar Nabil", "Engineer", TEAL, true),
        Member("sara", "Sara Kamal", "Researcher", ORANGE, false),
        Member("yousef", "Yousef Adel", "Android dev", GREEN, true),
        Member("maya", "Maya Haddad", "Content", ROSE, false)
    )

    fun projects(): MutableList<Project> = mutableListOf(
        Project(
            id = "p1",
            name = "Aurora Launch",
            description = "Design and ship the 1.0 experience",
            emoji = "\uD83C\uDF0C",
            accent = BLUE,
            dueLabel = "Nov 28",
            memberIds = mutableListOf("me", "lina", "omar", "yousef"),
            tasks = mutableListOf(
                TaskItem("p1t1", "Finalize the visual language", true, "lina"),
                TaskItem("p1t2", "Polish onboarding flow", true, "me"),
                TaskItem("p1t3", "Motion pass on transitions", false, "omar"),
                TaskItem("p1t4", "Accessibility review", false, "sara"),
                TaskItem("p1t5", "Ship the beta", false, "yousef")
            )
        ),
        Project(
            id = "p2",
            name = "Brand Identity",
            description = "A calmer, warmer voice for the brand",
            emoji = "\uD83C\uDFA8",
            accent = INDIGO,
            dueLabel = "Dec 4",
            memberIds = mutableListOf("me", "sara", "maya"),
            tasks = mutableListOf(
                TaskItem("p2t1", "Moodboard and references", true, "me"),
                TaskItem("p2t2", "Logo explorations", false, "lina"),
                TaskItem("p2t3", "Typography pairing", false, "maya"),
                TaskItem("p2t4", "Brand guidelines draft", false, "sara")
            )
        ),
        Project(
            id = "p3",
            name = "Mobile Onboarding",
            description = "Make the first minute feel effortless",
            emoji = "\uD83D\uDCF1",
            accent = TEAL,
            dueLabel = "Nov 20",
            memberIds = mutableListOf("me", "omar", "yousef"),
            tasks = mutableListOf(
                TaskItem("p3t1", "Map the current funnel", true, "omar"),
                TaskItem("p3t2", "Rewrite welcome screens", true, "me"),
                TaskItem("p3t3", "Prototype the flow", true, "yousef"),
                TaskItem("p3t4", "Usability test round", false, "sara")
            )
        ),
        Project(
            id = "p4",
            name = "Q4 Campaign",
            description = "Stories that travel across channels",
            emoji = "\uD83D\uDE80",
            accent = ORANGE,
            dueLabel = "Dec 12",
            memberIds = mutableListOf("lina", "sara", "maya"),
            tasks = mutableListOf(
                TaskItem("p4t1", "Campaign narrative", false, "maya"),
                TaskItem("p4t2", "Visual keyframes", false, "lina"),
                TaskItem("p4t3", "Launch checklist", false, "sara")
            )
        )
    )

    fun conversations(): MutableList<Conversation> = mutableListOf(
        Conversation(
            id = "c1",
            title = "Design crew",
            memberId = null,
            isGroup = true,
            preview = "Lina: The new glass surfaces look unreal \u2728",
            time = "09:42",
            unread = 2,
            messages = mutableListOf(
                Message("c1m1", "lina", "Morning! The new glass surfaces look unreal \u2728", "09:31", false),
                Message("c1m2", "me", "Right? The night sky gradient finally feels calm.", "09:33", true),
                Message("c1m3", "omar", "Motion pass is in. Transitions are 220ms now.", "09:38", false),
                Message("c1m4", "lina", "Perfect. Let's review at 11.", "09:42", false)
            )
        ),
        Conversation(
            id = "c2",
            title = "Aurora Launch",
            memberId = null,
            isGroup = true,
            preview = "Yousef: Beta build is ready for QA",
            time = "Now",
            unread = 0,
            messages = mutableListOf(
                Message("c2m1", "yousef", "Beta build is ready for QA \uD83D\uDE80", "10:05", false),
                Message("c2m2", "me", "On it. I'll run the onboarding pass today.", "10:07", true),
                Message("c2m3", "sara", "I'll take accessibility after that.", "10:12", false)
            )
        ),
        Conversation(
            id = "c3",
            title = "Lina Farah",
            memberId = "lina",
            isGroup = false,
            preview = "Sent you the final frames",
            time = "08:15",
            unread = 1,
            messages = mutableListOf(
                Message("c3m1", "lina", "Sent you the final frames.", "08:12", false),
                Message("c3m2", "me", "Looks beautiful. The spacing is perfect.", "08:14", true),
                Message("c3m3", "lina", "Thank you \uD83E\uDD0D", "08:15", false)
            )
        ),
        Conversation(
            id = "c4",
            title = "Omar Nabil",
            memberId = "omar",
            isGroup = false,
            preview = "Deployed the nightly build",
            time = "Yesterday",
            unread = 0,
            messages = mutableListOf(
                Message("c4m1", "omar", "Deployed the nightly build.", "21:40", false),
                Message("c4m2", "me", "Any crashes?", "21:44", true),
                Message("c4m3", "omar", "None so far. Clean run.", "21:46", false)
            )
        )
    )
}
